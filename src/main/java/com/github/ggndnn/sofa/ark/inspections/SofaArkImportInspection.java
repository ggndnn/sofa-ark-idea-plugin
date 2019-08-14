/*
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
package com.github.ggndnn.sofa.ark.inspections;

import com.github.ggndnn.sofa.ark.components.SofaArkManager;
import com.github.ggndnn.sofa.ark.model.SofaArkBase;
import com.github.ggndnn.sofa.ark.model.SofaArkBiz;
import com.github.ggndnn.sofa.ark.model.SofaArkPlugin;
import com.intellij.codeInspection.CleanupLocalInspectionTool;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.fixes.DeleteImportFix;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class SofaArkImportInspection extends BaseInspection implements CleanupLocalInspectionTool {
    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "sofa ark import";
    }

    @NotNull
    @Override
    protected String buildErrorString(Object... objects) {
        return "sofa ark import error";
    }

    @Override
    public InspectionGadgetsFix buildFix(Object... infos) {
        return new DeleteImportFix();
    }

    @Override
    public boolean shouldInspect(PsiFile file) {
        if (!(file instanceof PsiJavaFile)) {
            return false;
        }
        Module module = ModuleUtilCore.findModuleForFile(file);
        if (module == null)
            return false;
        SofaArkManager sofaArkManager = file.getProject().getComponent(SofaArkManager.class);
        return sofaArkManager.isBizModule(module) || sofaArkManager.isPluginModule(module);
    }

    @Override
    public BaseInspectionVisitor buildVisitor() {
        return new SofaArkImportVisitor(this);
    }

    private static class SofaArkImportVisitor extends BaseInspectionVisitor {
        SofaArkImportInspection inspection;

        SofaArkImportVisitor(SofaArkImportInspection inspection) {
            this.inspection = inspection;
        }

        @Override
        public void visitImportList(PsiImportList importList) {
            final PsiJavaFile javaFile = (PsiJavaFile) importList.getParent();
            SofaArkBase<?> srcModel = findContainerOfFile(javaFile);
            if (srcModel == null)
                return;
            final PsiImportStatement[] importStatements = importList.getImportStatements();
            for (final PsiImportStatement importStatement : importStatements) {
                SofaArkImport i = createImport(srcModel, importStatement);
                if (i != null && !i.isOk()) {
                    registerError(importStatement);
                }
            }
        }

        private SofaArkImport createImport(SofaArkBase<?> srcModel, PsiImportStatement importStatement) {
            final PsiJavaCodeReferenceElement reference = importStatement.getImportReference();
            if (reference == null)
                return null;
            final String text = importStatement.getQualifiedName();
            if (text == null)
                return null;
            PsiFile file = reference.getContainingFile();
            if (file == null)
                return null;
            String pkg = ".";
            if (!importStatement.isOnDemand()) {
                final int idx = text.lastIndexOf((int) '.');
                if (idx > 0) {
                    pkg = text.substring(0, idx);
                }
                return new SofaArkImportImpl(srcModel, file, pkg, text);
            } else {
                pkg = text;
                return new SofaArkImportImpl(srcModel, file, pkg, null);
            }
        }

        private SofaArkBase<?> findContainerOfFile(PsiFile file) {
            Module module = ModuleUtilCore.findModuleForFile(file);
            if (module == null)
                return null;
            Project project = file.getProject();
            SofaArkManager sofaArkManager = project.getComponent(SofaArkManager.class);
            SofaArkBiz biz = sofaArkManager.getBizByModule(module);
            if (biz != null) {
                return (SofaArkBase<?>) biz;
            }
            SofaArkPlugin plugin = sofaArkManager.getPluginByModule(module);
            if (plugin != null) {
                return (SofaArkBase<?>) plugin;
            }
//            if (!onlyModule) {
//                VirtualFile vf = file.getVirtualFile();
//                if (vf == null)
//                    return null;
//                VirtualFile jarvf = JarFileSystem.getInstance().getLocalVirtualFileFor(vf);
//                biz = sofaArkManager.getBizByVirtualFile(jarvf);
//                if (biz != null) {
//                    return (SofaArkBase<?>) biz;
//                }
//                plugin = sofaArkManager.getPluginByVirtualFile(jarvf);
//                if (plugin != null) {
//                    return (SofaArkBase<?>) plugin;
//                }
//            }
            return null;
        }
    }
}
