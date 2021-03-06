/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.intellij.execution.junit;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit2.info.MethodLocation;
import com.intellij.ide.util.PsiClassListCellRenderer;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBList;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: anna
 * Date: 1/17/12
 */
public class InheritorChooser {

  protected void runForClasses(final List<PsiClass> classes, final PsiMethod method, final ConfigurationContext context, final Runnable performRunnable) {
    performRunnable.run();
  }

  protected void runForClass(final PsiClass aClass, final PsiMethod psiMethod, final ConfigurationContext context, final Runnable performRunnable) {
    performRunnable.run();
  }

  public boolean runMethodInAbstractClass(final ConfigurationContext context,
                                          final Runnable performRunnable,
                                          final PsiMethod psiMethod,
                                          final PsiClass containingClass) {
    if (containingClass != null && containingClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
      final Location location = context.getLocation();
      if (location instanceof MethodLocation) {
        final PsiClass aClass = ((MethodLocation)location).getContainingClass();
        if (aClass != null && !aClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
          return false;
        }
      }
      final List<PsiClass> classes = new ArrayList<PsiClass>();
      if (!ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
        @Override
        public void run() {
          ClassInheritorsSearch.search(containingClass).forEach(new Processor<PsiClass>() {
            @Override
            public boolean process(PsiClass aClass) {
              if (!aClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
                classes.add(aClass);
              }
              return true;
            }
          });
        }
      }, "Search for " + containingClass.getQualifiedName() + " inheritors", true, containingClass.getProject())) {
        return true;
      }

      if (classes.size() == 1) {
        runForClass(classes.get(0), psiMethod, context, performRunnable);
        return true;
      }
      if (classes.isEmpty()) return false;
      final PsiClassListCellRenderer renderer = new PsiClassListCellRenderer() {
        @Override
        protected boolean customizeNonPsiElementLeftRenderer(ColoredListCellRenderer renderer,
                                                             JList list,
                                                             Object value,
                                                             int index,
                                                             boolean selected,
                                                             boolean hasFocus) {
          if (value == null) {
            renderer.append("All");
            return true;
          }
          return super.customizeNonPsiElementLeftRenderer(renderer, list, value, index, selected, hasFocus);
        }
      };
      Collections.sort(classes, renderer.getComparator());

      //suggest to run all inherited tests 
      classes.add(0, null);
      final JBList list = new JBList(classes);
      list.setCellRenderer(renderer);
      JBPopupFactory.getInstance().createListPopupBuilder(list)
        .setTitle("Choose executable classes to run " + (psiMethod != null ? psiMethod.getName() : containingClass.getName()))
        .setMovable(false)
        .setResizable(false)
        .setRequestFocus(true)
        .setItemChoosenCallback(new Runnable() {
          public void run() {
            final Object[] values = list.getSelectedValues();
            if (values == null) return;
            chooseAndPerform(values, psiMethod, context, performRunnable, classes);
          }
        }).createPopup().showInBestPositionFor(context.getDataContext());
      return true;
    }
    return false;
  }

  private void chooseAndPerform(Object[] values,
                                PsiMethod psiMethod,
                                ConfigurationContext context,
                                Runnable performRunnable,
                                List<PsiClass> classes) {
    classes.remove(null);
    if (values.length == 1) {
      final Object value = values[0];
      if (value instanceof PsiClass) {
        runForClass((PsiClass)value, psiMethod, context, performRunnable);
      }
      else {
        runForClasses(classes, psiMethod, context, performRunnable);
      }
      return;
    }
    if (ArrayUtil.contains(null, values)) {
      runForClasses(classes, psiMethod, context, performRunnable);
    }
    else {
      final List<PsiClass> selectedClasses = new ArrayList<PsiClass>();
      for (Object value : values) {
        if (value instanceof PsiClass) {
          selectedClasses.add((PsiClass)value);
        }
      }
      runForClasses(selectedClasses, psiMethod, context, performRunnable);
    }
  }
}
