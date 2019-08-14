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
package com.github.ggndnn.sofa.ark.model;

import com.github.ggndnn.sofa.ark.execution.model.SofaArkBizModel;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jdom.Element;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;

import java.util.Set;
import java.util.stream.Collectors;

public class MavenSofaArkBiz extends MavenSofaArkBase<SofaArkBizModel> implements SofaArkBiz {
    private final static String MAVEN_PLUGIN_ARK_GROUP_ID = "com.alipay.sofa";

    private final static String MAVEN_PLUGIN_ARK_ARTIFACT_ID = "sofa-ark-maven-plugin";

    private final static String MAVEN_PLUGIN_ARK_GOAL = "repackage";

    private SimpleSofaArkBiz delegate;

    public MavenSofaArkBiz(Module module, MavenProject maven) {
        super(module, maven);
    }

    @Override
    SofaArkBizModel createMetadata(MavenProject maven) {
        // TODO isBiz?
        return new SofaArkBizModel();
    }

    @Override
    void postProcessMetadata(SofaArkBizModel metadata) {
        MavenProject maven = getMaven();
        Element configurationElement = getConfiguration(maven);
        String bizName = null;
        if (configurationElement != null) {
            bizName = configurationElement.getChildText("bizName");
        }
        if (bizName == null || bizName.length() <= 0) {
            bizName = maven.getMavenId().getArtifactId();
        }
        metadata.setBizName(bizName);
        String bizVersion = null;
        if (configurationElement != null) {
            bizVersion = configurationElement.getChildText("bizVersion");
        }
        if (bizVersion == null || bizVersion.length() <= 0) {
            bizVersion = maven.getMavenId().getVersion();
        }
        metadata.setBizVersion(bizVersion);
        if (configurationElement != null) {
            metadata.setMainClass(configurationElement.getChildText("mainClass"));
        }
        if (metadata.getMainClass() == null) {
            metadata.setMainClass(findMainClass());
        }
        if (configurationElement != null) {
            metadata.setWebContextPath(configurationElement.getChildText("webContextPath"));
        }
        delegate = new SimpleSofaArkBiz(metadata);
        if (configurationElement == null)
            return;
        Element denyImportClassesElement = configurationElement.getChild("denyImportClasses");
        if (denyImportClassesElement != null) {
            delegate.setDenyImportClasses(metadata, denyImportClassesElement.getChildren("class")
                    .stream()
                    .map(Element::getText)
                    .collect(Collectors.toSet()));
        }
        Element denyImportPackagesElement = configurationElement.getChild("denyImportPackages");
        if (denyImportPackagesElement != null) {
            delegate.setDenyImportPackages(metadata, denyImportPackagesElement.getChildren("package")
                    .stream()
                    .map(Element::getText)
                    .collect(Collectors.toSet()));
        }
        Element denyImportResourcesElement = configurationElement.getChild("denyImportResources");
        if (denyImportResourcesElement != null) {
            metadata.setDenyImportResources(denyImportResourcesElement.getChildren("resource")
                    .stream()
                    .map(Element::getText)
                    .collect(Collectors.joining(",")));
        }
    }

    @Override
    Element getConfiguration(MavenProject maven) {
        return maven.getPluginGoalConfiguration(MAVEN_PLUGIN_ARK_GROUP_ID, MAVEN_PLUGIN_ARK_ARTIFACT_ID, MAVEN_PLUGIN_ARK_GOAL);
    }

    public static boolean isBiz(MavenProject maven) {
        MavenPlugin plugin = maven.findPlugin(MAVEN_PLUGIN_ARK_GROUP_ID, MAVEN_PLUGIN_ARK_ARTIFACT_ID, true);
        return plugin != null;
    }

    @Override
    public Set<String> getDenyImportPackages() {
        return delegate.getDenyImportPackages();
    }

    @Override
    public Set<String> getDenyImportClasses() {
        return delegate.getDenyImportClasses();
    }

    @Override
    public Set<String> getDenyImportPackageStems() {
        return delegate.getDenyImportPackageStems();
    }

    @Override
    public Integer getPriority() {
        return delegate.getPriority();
    }

    private String findMainClass() {
        Module module = super.getModule();
        GlobalSearchScope scope = module.getModuleScope(false);
        PsiClass mainClass = FilenameIndex.getAllFilesByExt(module.getProject(), "java", scope)
                .stream()
                .map(PsiManager.getInstance(module.getProject())::findFile)
                .flatMap(f -> PsiTreeUtil.findChildrenOfType(f, PsiMethod.class).stream())
                .filter(m -> m.getName().equals("main") && m.hasModifier(JvmModifier.STATIC) && m.getContainingClass() != null)
                .map(PsiMethod::getContainingClass).findFirst().orElse(null);
        if (mainClass == null)
            return null;
        return mainClass.getQualifiedName();
    }
}
