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
import com.github.ggndnn.sofa.ark.model.MavenSofaArkBase;
import com.github.ggndnn.sofa.ark.model.SofaArkBase;
import com.github.ggndnn.sofa.ark.model.SofaArkBiz;
import com.github.ggndnn.sofa.ark.model.SofaArkPlugin;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import java.util.Set;

class SofaArkImportImpl implements SofaArkImport {
    private SofaArkBase<?> srcModel;

    private PsiFile container;

    private String pkg;

    private String className;

    private String warningText;

    private String errorText;

    SofaArkImportImpl(SofaArkBase<?> srcModel, PsiFile container, String pkg, String className) {
        this.srcModel = srcModel;
        this.container = container;
        this.pkg = pkg;
        this.className = className;
    }

    public boolean isOk() {
        boolean result = isLoadFromSdkOk();
        if (!result) {
            if (srcModel instanceof SofaArkBiz) {
                result = isBizImportOk((SofaArkBiz) srcModel) && isPluginExportOk();
            } else if (srcModel instanceof SofaArkPlugin) {
                result = isPluginImportOk((SofaArkPlugin) srcModel) && isPluginExportOk();
            }
            if (!result) {
                result = isLoadOk(srcModel);
            }
            if (!result) {
                errorText = "error";
            }
        }
        return result;
    }

    @Override
    public String getError() {
        return errorText;
    }

    private boolean isBizImportOk(SofaArkBiz src) {
        Set<String> denyImportClasses = src.getDenyImportClasses();
        if (denyImportClasses != null) {
            for (String pattern : denyImportClasses) {
                if (pattern.equals(className)) {
                    return false;
                }
            }
        }
        Set<String> denyImportPackages = src.getDenyImportPackages();
        if (denyImportPackages != null) {
            for (String pattern : denyImportPackages) {
                if (pkg.equals(pattern)) {
                    return false;
                }
            }
        }
        Set<String> denyImportPackageStems = src.getDenyImportPackageStems();
        if (denyImportPackageStems != null) {
            for (String pattern : denyImportPackageStems) {
                if (pkg.startsWith(pattern)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isPluginImportOk(SofaArkPlugin plugin) {
        if (className != null) {
            Set<String> importClasses = plugin.getImportClasses();
            if (importClasses != null) {
                for (String importClass : importClasses) {
                    if (className.equals(importClass)) {
                        return true;
                    }
                }
            }
        }
        Set<String> importPackages = plugin.getImportPackages();
        if (importPackages != null) {
            for (String pattern : importPackages) {
                if (pkg.equals(pattern)) {
                    return true;
                }
            }
        }
        Set<String> importPackageStems = plugin.getImportPackageStems();
        if (importPackageStems != null) {
            for (String pattern : importPackageStems) {
                if (pkg.startsWith(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPluginExportOk() {
        SofaArkManager sofaArkManager = container.getProject().getComponent(SofaArkManager.class);
        SofaArkPlugin exportPlugin = sofaArkManager.findExportPlugin(pkg, className);
        if (exportPlugin != null) {
            SofaArkBase<?> base = (SofaArkBase<?>) exportPlugin;
            return isLoadOk(base);
        }
        return false;
    }

    private boolean isLoadOk(SofaArkBase<?> pluginOrBiz) {
        // TODO 考虑不同操作系统路径间隔符是否相同
        if (className != null) {
            String basePath = className.replace(".", "/");
            String javaFilePath = basePath + ".java";
            String classFilePath = basePath + ".class";
            return hasResource(pluginOrBiz, classFilePath)
                    || hasSourceResource(pluginOrBiz, classFilePath)
                    || hasSourceResource(pluginOrBiz, javaFilePath);
        } else if (pkg != null) {
            String resPath = pkg.replace(",", "/");
            return hasResource(pluginOrBiz, resPath) || hasSourceResource(pluginOrBiz, resPath);
        }
        return false;
    }

    private boolean hasResource(SofaArkBase<?> pluginOrBiz, String path) {
        return pluginOrBiz.hasResource(path);
    }

    private boolean hasSourceResource(SofaArkBase<?> pluginOrBiz, String path) {
        // TODO 使用FileIndex和Psi模型
        if (pluginOrBiz instanceof MavenSofaArkBase) {
            Module containingModule = ModuleUtilCore.findModuleForFile(this.container);
            if (containingModule == null) {
                // TODO error ...
                return false;
            }
            boolean includingTest = ModuleRootManager.getInstance(containingModule)
                    .getFileIndex()
                    .isInTestSourceContent(this.container.getVirtualFile());
            return ((MavenSofaArkBase<?>) pluginOrBiz).hasResource(path, includingTest)
                    || ((MavenSofaArkBase<?>) pluginOrBiz).hasSourceResource(path, includingTest);
        }
        return false;
    }

    private boolean isLoadFromSdkOk() {
        // TODO cache...
        Sdk sdk = ProjectRootManager.getInstance(container.getProject()).getProjectSdk();
        if (sdk == null)
            return false;
        VirtualFile[] files = sdk.getRootProvider().getFiles(OrderRootType.CLASSES);
        for (VirtualFile file : files) {
            VirtualFile res = null;
            if (className != null) {
                res = file.findFileByRelativePath(className.replace(".", "/") + ".class");
            } else if (pkg != null) {
                res = file.findFileByRelativePath(pkg.replace(".", "/"));
            }
            if (res != null)
                return true;
        }
        return false;
    }
}
