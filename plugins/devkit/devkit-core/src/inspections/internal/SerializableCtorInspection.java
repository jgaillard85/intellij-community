// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.idea.devkit.inspections.internal;

import com.intellij.codeInsight.daemon.impl.analysis.HighlightUtilBase;
import com.intellij.codeInsight.intention.AddAnnotationPsiFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.jvm.JvmParameter;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.devkit.inspections.DevKitInspectionBase;

public class SerializableCtorInspection extends DevKitInspectionBase {

  @Override
  protected PsiElementVisitor buildInternalVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JavaElementVisitor() {
      @Override
      public void visitClass(PsiClass aClass) {
        if (!InheritanceUtil.isInheritor(aClass, "java.io.Serializable"))
          return;
        if (aClass.findFieldByName(HighlightUtilBase.SERIAL_VERSION_UID_FIELD_NAME, false) == null)
          return;
        PsiMethod[] constructors = aClass.getConstructors();
        if (constructors.length != 1 || !constructors[0].hasParameters())
          return;
        PsiMethod constructor = constructors[0];
        String fqn = "com.intellij.serialization.PropertyMapping";
        if (constructor.getNameIdentifier() != null && constructor.getAnnotation(fqn) == null) {
          StringBuilder builder = new StringBuilder("@PropertyMapping({");
          JvmParameter[] parameters = constructor.getParameters();
          for (int i = 0; i < parameters.length; i++) {
            JvmParameter parameter = parameters[i];
            if (i > 0) builder.append(',');
            builder.append('"').append(parameter.getName()).append('"');
          }
          PsiAnnotation annotation = JavaPsiFacade.getElementFactory(aClass.getProject())
            .createAnnotationFromText(builder.append("})").toString(), aClass);
          holder.registerProblem(constructor.getNameIdentifier(), "Non-default ctor should be annotated with @PropertyMapping",
                                 new AddAnnotationPsiFix(fqn, constructor, annotation.getParameterList().getAttributes()));
        }
      }
    };
  }
}
