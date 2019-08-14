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

import com.github.ggndnn.sofa.ark.execution.model.SofaArkPluginModel;
import com.intellij.openapi.module.Module;
import org.jdom.Element;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;

import java.util.Set;
import java.util.stream.Collectors;

public class MavenSofaArkPlugin extends MavenSofaArkBase<SofaArkPluginModel> implements SofaArkPlugin {
    private final static String MAVEN_PLUGIN_ARK_PLUGIN_GROUP_ID = "com.alipay.sofa";

    private final static String MAVEN_PLUGIN_ARK_PLUGIN_ARTIFACT_ID = "sofa-ark-plugin-maven-plugin";

    private final static String MAVEN_PLUGIN_ARK_PLUGIN_GOAL = "ark-plugin";

    private SimpleSofaArkPlugin delegate;

    public MavenSofaArkPlugin(Module module, MavenProject maven) {
        super(module, maven);
    }

    @Override
    SofaArkPluginModel createMetadata(MavenProject maven) {
        // TODO isPlugin?
        return new SofaArkPluginModel();
    }

    @Override
    void postProcessMetadata(SofaArkPluginModel metadata) {
        MavenProject maven = getMaven();
        Element configurationElement = getConfiguration(maven);
        // TODO 处理变量的情况
        String pluginName = null;
        if (configurationElement != null) {
            pluginName = configurationElement.getChildTextTrim("pluginName");
        }
        if (pluginName == null) {
            pluginName = maven.getMavenId().getArtifactId();
        }
        metadata.setPluginName(pluginName);
        metadata.setPluginVersion(maven.getMavenId().getVersion());
        if (configurationElement != null) {
            String activator = configurationElement.getChildTextTrim("activator");
            metadata.setActivator(activator);
            String priorityValue = configurationElement.getChildTextTrim("priority");
            try {
                metadata.setPriority(Integer.parseInt(priorityValue));
            } catch (NumberFormatException e) {
            }
        }
        delegate = new SimpleSofaArkPlugin(metadata);
        if (configurationElement == null)
            return;
        Element importedElement = configurationElement.getChild("imported");
        if (importedElement != null) {
            Element importedPackagesElement = importedElement.getChild("packages");
            if (importedPackagesElement != null) {
                delegate.setImportPackages(metadata, importedPackagesElement.getChildren("package")
                        .stream().map(Element::getTextTrim).collect(Collectors.toSet()));
            }

            Element importedClassesElement = importedElement.getChild("classes");
            if (importedClassesElement != null) {
                delegate.setImportClasses(metadata, importedClassesElement.getChildren("class")
                        .stream().map(Element::getTextTrim).collect(Collectors.toSet()));
            }

            Element importedResourcesElement = importedElement.getChild("resources");
            if (importedResourcesElement != null) {
                metadata.setImportResources(importedResourcesElement.getChildren("resource")
                        .stream().map(Element::getTextTrim).collect(Collectors.joining(",")));
            }
        }

        Element exportedElement = configurationElement.getChild("exported");
        if (exportedElement != null) {
            Element exportedPackagesElement = exportedElement.getChild("packages");
            if (exportedPackagesElement != null) {
                delegate.setExportPackages(metadata, exportedPackagesElement.getChildren("package")
                        .stream().map(Element::getTextTrim).collect(Collectors.toSet()));
            }

            Element exportedClassesElement = exportedElement.getChild("classes");
            if (exportedClassesElement != null) {
                delegate.setExportClasses(metadata, exportedClassesElement.getChildren("class")
                        .stream().map(Element::getTextTrim).collect(Collectors.toSet()));
            }

            Element exportedResourcesElement = exportedElement.getChild("resources");
            if (exportedResourcesElement != null) {
                metadata.setExportResources(exportedResourcesElement.getChildren("resource")
                        .stream().map(Element::getTextTrim).collect(Collectors.joining(",")));
            }
        }
    }

    @Override
    Element getConfiguration(MavenProject maven) {
        return maven.getPluginGoalConfiguration(MAVEN_PLUGIN_ARK_PLUGIN_GROUP_ID, MAVEN_PLUGIN_ARK_PLUGIN_ARTIFACT_ID, MAVEN_PLUGIN_ARK_PLUGIN_GOAL);
    }

    public static boolean isPlugin(MavenProject maven) {
        MavenPlugin plugin = maven.findPlugin(MAVEN_PLUGIN_ARK_PLUGIN_GROUP_ID, MAVEN_PLUGIN_ARK_PLUGIN_ARTIFACT_ID, true);
        return plugin != null;
    }

    @Override
    public Set<String> getImportPackages() {
        return delegate.getImportPackages();
    }

    @Override
    public Set<String> getImportPackageStems() {
        return delegate.getImportPackageStems();
    }

    @Override
    public Set<String> getImportClasses() {
        return delegate.getImportClasses();
    }

    @Override
    public Set<String> getExportPackages() {
        return delegate.getExportPackages();
    }

    @Override
    public Set<String> getExportPackageStems() {
        return delegate.getExportPackageStems();
    }

    @Override
    public Set<String> getExportClasses() {
        return delegate.getExportClasses();
    }

    @Override
    public Integer getPriority() {
        return delegate.getPriority();
    }
}
