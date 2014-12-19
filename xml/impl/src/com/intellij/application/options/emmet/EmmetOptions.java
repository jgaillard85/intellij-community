/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.application.options.emmet;

import com.intellij.codeInsight.template.emmet.filters.BemEmmetFilter;
import com.intellij.codeInsight.template.emmet.filters.ZenCodingFilter;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.openapi.components.*;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

@State(
  name = "EmmetOptions",
  storages = @Storage(file = StoragePathMacros.APP_CONFIG + "/emmet.xml")
)
public class EmmetOptions implements PersistentStateComponent<EmmetOptions> {
  /**
   * @deprecated delete after IDEA 14
   */
  private boolean myBemFilterEnabledByDefault = false;
  private boolean myEmmetEnabled = true;
  private int myEmmetExpandShortcut = TemplateSettings.TAB_CHAR;
  private boolean myFuzzySearchEnabled = true;
  private boolean myAutoInsertCssPrefixedEnabled = true;
  private boolean myPreviewEnabled = false;
  private boolean myCompactBooleanAllowed = true;
  private Set<String> myBooleanAttributes = ContainerUtil.newHashSet("contenteditable", "seamless");
  private Set<String> myFiltersEnabledByDefault = ContainerUtil.newHashSet();
  @NotNull
  private Map<String, Integer> prefixes = ContainerUtil.newHashMap();

  /**
   * @deprecated delete after IDEA 14
   */
  public boolean isBemFilterEnabledByDefault() {
    return myBemFilterEnabledByDefault;
  }

  /**
   * @deprecated delete after IDEA 14
   */
  public void setBemFilterEnabledByDefault(boolean enableBemFilterByDefault) {
    myBemFilterEnabledByDefault = enableBemFilterByDefault;
  }

  @NotNull
  public Set<String> getFiltersEnabledByDefault() {
    return myFiltersEnabledByDefault;
  }

  public void setFiltersEnabledByDefault(@NotNull Set<String> filtersEnabledByDefault) {
    myFiltersEnabledByDefault = filtersEnabledByDefault;
  }

  public boolean isFilterEnabledByDefault(@NotNull ZenCodingFilter filter) {
    return myFiltersEnabledByDefault.contains(filter.getSuffix());
  } 

  public void setEmmetExpandShortcut(int emmetExpandShortcut) {
    myEmmetExpandShortcut = emmetExpandShortcut;
  }

  public int getEmmetExpandShortcut() {
    return myEmmetExpandShortcut;
  }

  public boolean isPreviewEnabled() {
    return myPreviewEnabled;
  }

  public void setPreviewEnabled(boolean previewEnabled) {
    myPreviewEnabled = previewEnabled;
  }
  
  public boolean isCompactBooleanAllowed() {
    return myCompactBooleanAllowed;
  }

  public void setCompactBooleanAllowed(boolean compactBooleanAllowed) {
    myCompactBooleanAllowed = compactBooleanAllowed;
  }

  public Set<String> getBooleanAttributes() {
    return myBooleanAttributes;
  }

  public void setBooleanAttributes(@NotNull Set<String> booleanAttributes) {
    myBooleanAttributes = booleanAttributes;
  }
  
  public boolean isEmmetEnabled() {
    return myEmmetEnabled;
  }

  public void setEmmetEnabled(boolean emmetEnabled) {
    myEmmetEnabled = emmetEnabled;
  }

  @Deprecated
  //use {@link CssEmmetOptions}
  public boolean isAutoInsertCssPrefixedEnabled() {
    return myAutoInsertCssPrefixedEnabled;
  }

  @Deprecated
  //use {@link CssEmmetOptions}
  public void setAutoInsertCssPrefixedEnabled(boolean autoInsertCssPrefixedEnabled) {
    myAutoInsertCssPrefixedEnabled = autoInsertCssPrefixedEnabled;
  }

  @Deprecated
  //use {@link CssEmmetOptions}
  public void setFuzzySearchEnabled(boolean fuzzySearchEnabled) {
    myFuzzySearchEnabled = fuzzySearchEnabled;
  }

  @Deprecated
  //use {@link CssEmmetOptions}
  public boolean isFuzzySearchEnabled() {
    return myFuzzySearchEnabled;
  }

  @Nullable
  @Override
  public EmmetOptions getState() {
    return this;
  }

  @Override
  public void loadState(final EmmetOptions state) {
    XmlSerializerUtil.copyBean(state, this);
    
    // todo delete after IDEA 14
    if (myFiltersEnabledByDefault.isEmpty() && myBemFilterEnabledByDefault) {
      myFiltersEnabledByDefault.add(BemEmmetFilter.SUFFIX);
    }
  }

  public static EmmetOptions getInstance() {
    return ServiceManager.getService(EmmetOptions.class);
  }

  @NotNull
  @Deprecated
  //use {@link CssEmmetOptions}
  public Map<String, Integer> getPrefixes() {
    return prefixes;
  }

  @SuppressWarnings("UnusedDeclaration")
  @Deprecated
  //use {@link CssEmmetOptions}
  public void setPrefixes(@NotNull Map<String, Integer> prefixes) {
    this.prefixes = prefixes;
  }
}
