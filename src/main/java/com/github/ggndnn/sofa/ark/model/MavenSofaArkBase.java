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

import com.github.ggndnn.sofa.ark.execution.model.SofaArkId;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.idea.maven.project.MavenProject;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public abstract class MavenSofaArkBase<T extends SofaArkModel> extends SofaArkBase<T> {
    private Module module;

    private MavenProject maven;

    private Set<VirtualFile> testScopeClasspath;

    MavenSofaArkBase(Module module, MavenProject maven) {
        init(module, maven);
    }

    private void init(Module module, MavenProject maven) {
        this.module = module;
        this.maven = maven;
        this.metadata = this.createMetadata(maven);
        this.postProcessMetadata(metadata);
        this.metadata.setId(SofaArkId.parseSofaArkId(maven.getMavenId().toString()));
        CompilerModuleExtension ext = CompilerModuleExtension.getInstance(module);
        if (ext != null && ext.getCompilerOutputPath() != null) {
            this.setLocation(ext.getCompilerOutputPath());
        } else if (ext != null && ext.getCompilerOutputUrl() != null) {
            // TODO ...
            String url = ext.getCompilerOutputUrl();
            if (!url.endsWith("/")) {
                url += "/";
            }
            URI uri = URI.create(url);
            this.setLocationUrl(uri.toString());
        } else {
            // TODO maven.getOutputDirectory()
            // this.setLocation(null);
            throw new IllegalStateException("missing output directory");
        }
        // TODO ...
        testScopeClasspath = new HashSet<>();
        for (OrderEntry entry : ModuleRootManager.getInstance(module).getOrderEntries()) {
            if (entry instanceof LibraryOrderEntry) {
                LibraryOrderEntry libraryEntry = (LibraryOrderEntry) entry;
                if (DependencyScope.TEST.equals(libraryEntry.getScope())) {
                    VirtualFile[] files = libraryEntry.getFiles(OrderRootType.CLASSES);
                    if (files.length <= 0)
                        continue;
                    testScopeClasspath.add(files[0]);
                }
            }
        }
    }

    public Module getModule() {
        return module;
    }

    public MavenProject getMaven() {
        return maven;
    }

    public boolean hasSourceResource(String path, boolean includingTest) {
        for (VirtualFile root : ModuleRootManager.getInstance(getModule()).getSourceRoots(includingTest)) {
            VirtualFile vf = VfsUtil.findRelativeFile(path, root);
            if (vf != null)
                return true;
        }
        return false;
    }

    public boolean hasResource(String path, boolean includingTest) {
        boolean result = super.hasResource(path);
        if (!result && includingTest) {
            for (VirtualFile root : testScopeClasspath) {
                VirtualFile vf = VfsUtil.findRelativeFile(path, root);
                if (vf != null) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void setClasspath(Set<SofaArkClasspathEntry> classpath) {
        if (maven == null || metadata == null)
            throw new IllegalStateException();
        Element configurationElement = getConfiguration(maven);
        if (configurationElement == null) {
            super.setClasspath(classpath);
        } else {
            if (classpath != null && classpath.size() > 0) {
                Set<String> excludes = null;
                Set<String> excludeGroupIds = null;
                Set<String> excludeArtifactIds = null;
                Element excludesElement = configurationElement.getChild("excludes");
                if (excludesElement != null) {
                    excludes = excludesElement.getChildren("exclude").stream().map(Element::getText).collect(Collectors.toSet());
                }
                Element excludeGroupIdsElement = configurationElement.getChild("excludeGroupIds");
                if (excludeGroupIdsElement != null) {
                    excludeGroupIds = excludeGroupIdsElement.getChildren("excludeGroupId").stream().map(Element::getText).collect(Collectors.toSet());
                }
                Element excludeArtifactIdsElement = configurationElement.getChild("excludeArtifactIds");
                if (excludeArtifactIdsElement != null) {
                    excludeArtifactIds = excludeArtifactIdsElement.getChildren("excludeArtifactId").stream().map(Element::getText).collect(Collectors.toSet());
                }
                List<SofaArkId> excludeList = new ArrayList<>();
                if (excludes != null) {
                    for (String exclude : excludes) {
                        SofaArkId id = SofaArkId.parseSofaArkIdIgnoreVersion(exclude);
                        excludeList.add(id);
                    }
                }
                // TODO 去重
                Set<SofaArkClasspathEntry> included = new LinkedHashSet<>();
                for (SofaArkClasspathEntry entry : classpath) {
                    SofaArkId id = entry.getId();
                    boolean isExclude = false;
                    for (SofaArkId exclude : excludeList) {
                        if (exclude.isSameIgnoreVersion(id)) {
                            isExclude = true;
                            break;
                        }
                    }
                    if (excludeGroupIds != null && excludeGroupIds.contains(id.getGroupId())) {
                        isExclude = true;
                    }
                    if (excludeArtifactIds != null && excludeArtifactIds.contains(id.getArtifactId())) {
                        isExclude = true;
                    }
                    if (!isExclude) {
                        included.add(entry);
                    }
                }
                classpath = included;
            }
            super.setClasspath(classpath);
        }
    }

    abstract T createMetadata(MavenProject maven);

    abstract void postProcessMetadata(T metadata);

    abstract Element getConfiguration(MavenProject maven);
}
