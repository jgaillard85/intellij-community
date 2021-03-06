package com.intellij.jps.cache.loader;

import com.intellij.jps.cache.client.JpsServerClient;
import com.intellij.jps.cache.hashing.PersistentCachingModuleHashingService;
import com.intellij.jps.cache.ui.SegmentedProgressIndicatorManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

abstract class JpsCompilationOutputLoader implements JpsOutputLoader {
  private static final Logger LOG = Logger.getInstance("com.intellij.jps.loader.JpsCompilationOutputLoader");
  protected final PersistentCachingModuleHashingService myHashingService;
  protected final JpsServerClient myClient;
  protected final Project myProject;
  protected Map<File, String> myTmpFolderToModuleName;

  JpsCompilationOutputLoader(JpsServerClient client, Project project, PersistentCachingModuleHashingService hashingService) {
    myClient = client;
    myProject = project;
    myHashingService = hashingService;
  }

  @Override
  public LoaderStatus load(@NotNull String commitId, @NotNull SegmentedProgressIndicatorManager progressIndicatorManager) {
    myTmpFolderToModuleName = null;
    ProgressIndicator indicator = progressIndicatorManager.getProgressIndicator();
    CompilerProjectExtension projectExtension = CompilerProjectExtension.getInstance(myProject);
    if (projectExtension == null || projectExtension.getCompilerOutputUrl() == null) {
      LOG.warn("Compiler output setting not specified for the project ");
      return LoaderStatus.FAILED;
    }
    File compilerOutputDir = new File(VfsUtilCore.urlToPath(projectExtension.getCompilerOutputUrl()));
    return load(compilerOutputDir, progressIndicatorManager);
  }

  abstract LoaderStatus load(@NotNull File compilerOutputDir, @NotNull SegmentedProgressIndicatorManager progressIndicatorManager);

  @Override
  public void rollback() {
    if (myTmpFolderToModuleName == null) return;
    myTmpFolderToModuleName.forEach((tmpFolder, __) -> {if (tmpFolder.isDirectory() && tmpFolder.exists()) FileUtil.delete(tmpFolder);});
    LOG.debug("JPS cache loader rolled back");
  }

  @Override
  public void apply() {
    if (myTmpFolderToModuleName == null) {
      LOG.debug("Nothing to apply, download results are empty");
      return;
    }

    myTmpFolderToModuleName.forEach((tmpModuleFolder, moduleName) -> {
      File currentModuleBuildDir = new File(tmpModuleFolder.getParentFile(), moduleName);
      FileUtil.delete(currentModuleBuildDir);
      try {
        FileUtil.rename(tmpModuleFolder, currentModuleBuildDir);
        LOG.debug("Module: " + moduleName + " was replaced successfully");
      }
      catch (IOException e) {
        LOG.warn("Couldn't replace compilation output for module: " + moduleName, e);
      }
    });
  }

  protected static Map<String, String> getAffectedModules(@NotNull File outDir, @NotNull Supplier<Map<String, String>> affectedModules,
                                                        @NotNull Supplier<Map<String, String>> allModules) {
    long start = System.currentTimeMillis();
    Map<String, String> allModulesMap = allModules.get();
    if (outDir.exists()) {
      File[] listFiles = outDir.listFiles();
      if (listFiles == null) return allModulesMap;
      // Create map for currently exists module compilation outputs
      Map<String, File> currentModulesFolderMap = Arrays.stream(listFiles).filter(File::isDirectory)
                                                                   .collect(Collectors.toMap(folder -> folder.getName(), Function.identity()));

      // Detect modules which compilation outputs were not found but should be
      Set<String> modulesWithRemovedOutDir = new HashSet<>(allModulesMap.keySet());
      modulesWithRemovedOutDir.removeAll(currentModulesFolderMap.keySet());

      // Delete compilation outputs for currently not existing modules
      Set<String> oldModulesOutDir = new HashSet<>(currentModulesFolderMap.keySet());
      oldModulesOutDir.removeAll(allModulesMap.keySet());
      oldModulesOutDir.stream().map(currentModulesFolderMap::get).forEach(FileUtil::delete);

      Map<String, String> affectedModulesMap = affectedModules.get();
      modulesWithRemovedOutDir.forEach(moduleName -> {
        affectedModulesMap.put(moduleName, allModulesMap.get(moduleName));
      });
      LOG.debug("Compilation output affected for the " + affectedModulesMap.size() + " modules. Computation took " + (System.currentTimeMillis() - start) + "ms");
      return affectedModulesMap;
    }
    LOG.warn("Compilation output doesn't exist, force to download " + allModulesMap.size() +" modules. Computation took " +  (System.currentTimeMillis() - start) + "ms");
    return allModulesMap;
  }

  protected static void displaySkippedStepOnProgressBar(@NotNull SegmentedProgressIndicatorManager progressIndicatorManager) {
    progressIndicatorManager.setTasksCount(1);
    progressIndicatorManager.updateFraction(1.0);
  }
}