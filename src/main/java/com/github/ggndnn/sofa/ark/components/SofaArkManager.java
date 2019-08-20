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
package com.github.ggndnn.sofa.ark.components;

import com.alipay.sofa.ark.common.util.ClassUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkId;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkModel;
import com.github.ggndnn.sofa.ark.model.*;
import com.github.ggndnn.sofa.ark.utils.SofaArkArchiveHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.intellij.ide.highlighter.ArchiveFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.io.ZipUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SofaArkManager {
    private Project project;

    private Map<Module, SofaArkManagerRefreshContext> contextsOfModule;

    public SofaArkManager(@NotNull Project project) {
        this.project = project;
    }

    public Collection<SofaArkPlugin> getAllPlugins() {
        List<SofaArkPlugin> result = new ArrayList<>();
        if (contextsOfModule != null) {
            Map<SofaArkId, SofaArkPlugin> pluginMap = Stream.of(ModuleManager.getInstance(project).getSortedModules())
                    .map(contextsOfModule::get)
                    .filter(Objects::nonNull)
                    .flatMap(c -> c.sortedPluginSet.stream())
                    .collect(Collectors.toMap(m -> m.getMetadata().getId(),
                            m -> m,
                            (e1, e2) -> e1,
                            LinkedHashMap::new));
            result.addAll(pluginMap.values());
        }
        return result;
    }

    public Collection<SofaArkBiz> getAllBiz() {
        List<SofaArkBiz> result = new ArrayList<>();
        if (contextsOfModule != null) {
            Map<SofaArkId, SofaArkBiz> bizMap = Stream.of(ModuleManager.getInstance(project).getSortedModules())
                    .map(contextsOfModule::get)
                    .filter(Objects::nonNull)
                    .flatMap(c -> c.sortedBizSet.stream())
                    .collect(Collectors.toMap(m -> m.getMetadata().getId(),
                            m -> m,
                            (e1, e2) -> e1,
                            LinkedHashMap::new));
            result.addAll(bizMap.values());
        }
        return result;
    }

    public boolean isBizModule(Module module) {
        SofaArkId id = moduleToSofaArkId(module);
        if (contextsOfModule != null && id != null) {
            for (SofaArkManagerRefreshContext c : contextsOfModule.values()) {
                if (c.bizCache.containsKey(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPluginModule(Module module) {
        SofaArkId id = moduleToSofaArkId(module);
        if (contextsOfModule != null && id != null) {
            for (SofaArkManagerRefreshContext c : contextsOfModule.values()) {
                if (c.pluginCache.containsKey(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public SofaArkBiz getBizByModule(Module module) {
        SofaArkId id = moduleToSofaArkId(module);
        if (id == null || contextsOfModule == null) {
            return null;
        }
        SofaArkManagerRefreshContext c = contextsOfModule.get(module);
        if (c == null) {
            return null;
        }
        return c.bizCache.get(id);
    }

    public SofaArkPlugin getPluginByModule(Module module) {
        SofaArkId id = moduleToSofaArkId(module);
        if (id == null || contextsOfModule == null) {
            return null;
        }
        SofaArkManagerRefreshContext c = contextsOfModule.get(module);
        if (c == null) {
            return null;
        }
        return c.pluginCache.get(id);
    }

    public SofaArkBiz getBizById(SofaArkId id) {
        if (contextsOfModule != null) {
            for (SofaArkManagerRefreshContext c : contextsOfModule.values()) {
                SofaArkBiz biz = c.bizCache.get(id);
                if (biz != null) {
                    return biz;
                }
            }
        }
        return null;
    }

    public SofaArkPlugin getPluginById(SofaArkId id) {
        if (contextsOfModule != null) {
            for (SofaArkManagerRefreshContext c : contextsOfModule.values()) {
                SofaArkPlugin plugin = c.pluginCache.get(id);
                if (plugin != null) {
                    return plugin;
                }
            }
        }
        return null;
    }

    private SofaArkId moduleToSofaArkId(Module module) {
        MavenProject maven = MavenProjectsManager.getInstance(project).findProject(module);
        if (maven != null) {
            return SofaArkId.parseSofaArkId(maven.getMavenId().toString());
        }
        return null;
    }

    public SofaArkPlugin findExportPlugin(String pkg, String className) {
        if (contextsOfModule != null) {
            for (SofaArkManagerRefreshContext c : contextsOfModule.values()) {
                SofaArkPlugin plugin = findExportPlugin(pkg, className, c);
                if (plugin != null) {
                    return plugin;
                }
            }
        }
        return null;
    }

    public SofaArkPlugin findExportPlugin(String pkg, String className, SofaArkManagerRefreshContext context) {
        SofaArkPlugin plugin = null;
        if (className != null && className.length() > 0) {
            plugin = context.exportClass2PluginMap.get(className);
            pkg = ClassUtils.getPackageName(className);
            if (plugin == null) {
                plugin = context.exportPackage2PluginMap.get(pkg);
            }
        }
        while (!Constants.DEFAULT_PACKAGE.equals(pkg) && plugin == null) {
            plugin = context.exportStem2PluginMap.get(pkg);
            pkg = ClassUtils.getPackageName(pkg);
        }
        return plugin;
    }

    public void onMavenProjectUpdate(MavenProject maven) {
        Module module = MavenProjectsManager.getInstance(project).findModule(maven);
        if (module == null) {
            // TODO ...
            onMavenProjectDelete(maven);
        } else {
            Map<Module, SofaArkManagerRefreshContext> newContextsOfModule = new HashMap<>();
            if (contextsOfModule != null) {
                newContextsOfModule.putAll(contextsOfModule);
            }
            SofaArkManagerRefreshContext oldContext = newContextsOfModule.remove(module);
            SofaArkManagerRefreshContext contextToUpdate = new SofaArkManagerRefreshContext(project);
            refreshModule(module, contextToUpdate);
            if (contextToUpdate.sortedBizSet.isEmpty() && contextToUpdate.sortedPluginSet.isEmpty()) {
                return;
            }
            postProcessContext(contextToUpdate);
            newContextsOfModule.put(module, contextToUpdate);
            contextsOfModule = newContextsOfModule;
        }
    }

    public void onMavenProjectDelete(MavenProject maven) {
        if (contextsOfModule == null) {
            return;
        }
        Module module = MavenProjectsManager.getInstance(project).findModule(maven);
        if (module == null) {
            return;
        }
        Map<Module, SofaArkManagerRefreshContext> newContextsOfModule = new HashMap<>(contextsOfModule);
        newContextsOfModule.remove(module);
        contextsOfModule = newContextsOfModule;
    }

    private void deletePluginOrBiz(SofaArkManagerRefreshContext context, Set<SofaArkId> pluginOrBizToDelete) {
        for (SofaArkId pluginOrBizId : pluginOrBizToDelete) {
            context.bizCache.remove(pluginOrBizId);
            context.pluginCache.remove(pluginOrBizId);
        }
        if (!pluginOrBizToDelete.isEmpty()) {
            context.sortedBizSet.clear();
            context.sortedBizSet.addAll(context.bizCache.values());
            context.sortedPluginSet.clear();
            context.sortedPluginSet.addAll(context.pluginCache.values());
        }
    }

    private void postProcessContext(SofaArkManagerRefreshContext context) {
        filterOutErrorPluginOrBiz(context);
        refreshContextExports(context);
        context.pluginOrBizAndLibraries.clear();
    }

    private void filterOutErrorPluginOrBiz(SofaArkManagerRefreshContext context) {
        deletePluginOrBiz(context, context.pluginOrBizErrors.keySet().stream()
                .map(SofaArkBase::getMetadata)
                .map(SofaArkModel::getId)
                .collect(Collectors.toSet()));
    }

    private void refreshContextExports(SofaArkManagerRefreshContext context) {
        // sort ...
        context.exportPackage2PluginMap.clear();
        context.exportStem2PluginMap.clear();
        context.exportClass2PluginMap.clear();
        for (SofaArkPlugin plugin : context.sortedPluginSet) {
            Set<String> exportPackages = plugin.getExportPackages();
            if (exportPackages != null) {
                for (String key : exportPackages) {
                    context.exportPackage2PluginMap.putIfAbsent(key, plugin);
                }
            }
            Set<String> exportPackageStems = plugin.getExportPackageStems();
            if (exportPackageStems != null) {
                for (String key : exportPackageStems) {
                    context.exportStem2PluginMap.putIfAbsent(key, plugin);
                }
            }
            Set<String> exportClasses = plugin.getExportClasses();
            if (exportClasses != null) {
                for (String key : exportClasses) {
                    context.exportClass2PluginMap.putIfAbsent(key, plugin);
                }
            }
        }
    }

    private void refreshModule(Module module, SofaArkManagerRefreshContext context) {
        MavenProject maven = MavenProjectsManager.getInstance(project).findProject(module);
        if (maven == null) {
            return;
        }
        if (maven.hasReadingProblems()
                || maven.hasUnresolvedArtifacts()
                || maven.hasUnresolvedPlugins()) {
            return;
        }
        List<SofaArkBase<?>> pluginOrBizList = new ArrayList<>();
        SofaArkBase<?> base = createModelFromModule(module, context);
        if (base == null) {
            return;
        }
        pluginOrBizList.add(base);
        Map<SofaArkId, LibraryOrderEntry> candidates = findLibraryCandidatesOfModule(module);
        for (SofaArkId id : candidates.keySet()) {
            LibraryOrderEntry libraryEntry = candidates.get(id);
            SofaArkBase<?> pluginOrBiz = createModelFromLibraryEntry(module, id, libraryEntry, context);
            if (pluginOrBiz == null) {
                continue;
            }
            pluginOrBizList.add(pluginOrBiz);
        }
        for (SofaArkBase<?> pluginOrBiz : pluginOrBizList) {
            Set<SofaArkClasspathEntry> classpath = new LinkedHashSet<>();
            Set<SofaArkId> artifacts = findLibrariesForPluginOrBiz(pluginOrBiz, maven, context);
            if (pluginOrBiz instanceof MavenSofaArkBase) {
                classpath.add(new MavenModuleClasspathEntry(pluginOrBiz.getMetadata().getId(), ((MavenSofaArkBase<?>) pluginOrBiz).getModule()));
                classpath.addAll(collectionLibrariesForProjectPluginOrBiz(pluginOrBiz, artifacts, candidates, context));
            } else {
                classpath.add(new VirtualFileClasspathEntry(pluginOrBiz.getMetadata().getId(), pluginOrBiz.getLocation()));
                classpath.addAll(collectionLibrariesForFilePluginOrBiz(pluginOrBiz, candidates, context));
            }
            pluginOrBiz.setClasspath(classpath);
        }
    }

    private Collection<SofaArkClasspathEntry> collectionLibrariesForProjectPluginOrBiz(SofaArkBase<?> pluginOrBiz,
                                                                                       Set<SofaArkId> artifacts,
                                                                                       Map<SofaArkId, LibraryOrderEntry> candidates,
                                                                                       SofaArkManagerRefreshContext context) {
        List<SofaArkClasspathEntry> result = new ArrayList<>();
        for (SofaArkId libraryId : artifacts) {
            LibraryOrderEntry libraryEntry = candidates.get(libraryId);
            if (libraryEntry == null) {
                context.pluginOrBizErrors.put(pluginOrBiz, String.format("Can not find library %s of %s", libraryId.toString(), pluginOrBiz.getMetadata().getId().toString()));
                continue;
            }
            Library library = libraryEntry.getLibrary();
            if (library == null) {
                throw new IllegalStateException();
            }
            VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
            if (files.length <= 0) {
                throw new IllegalStateException();
            }
            VirtualFile classpathFile = files[0];
            result.add(new VirtualFileClasspathEntry(libraryId, classpathFile));
        }
        return result;
    }

    private Collection<SofaArkClasspathEntry> collectionLibrariesForFilePluginOrBiz(SofaArkBase<?> pluginOrBiz,
                                                                                    Map<SofaArkId, LibraryOrderEntry> candidates,
                                                                                    SofaArkManagerRefreshContext context) {
        VirtualFile location = pluginOrBiz.getLocation();
        File outDir = null;
        Map<SofaArkId, SofaArkId> libraryCandidates = new HashMap<>();
        try {
            // TODO use virtual file system
            VirtualFile vf = VfsUtil.getLocalFile(location);
            File file = VfsUtil.virtualToIoFile(vf);
            outDir = FileUtil.createTempDirectory("", "libs");
            ZipUtil.extract(file, outDir, new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return dir.getName().startsWith("lib") && name.endsWith(".jar");
                }
            });
            File libDir = new File(outDir, "lib");
            if (libDir.exists()) {
                File[] files = libDir.listFiles();
                if (files != null) {
                    List<SofaArkId> candidateList = Stream.of(files)
                            .map(f -> VfsUtil.findFileByIoFile(f, true))
                            .filter(Objects::nonNull)
                            .map(f -> JarFileSystem.getInstance().getRootByLocal(f))
                            .filter(Objects::nonNull)
                            .map(f -> {
                                SofaArkId id = SofaArkArchiveHelper.loadSofaArkIdFromFile(f);
                                if (id == null) {
                                    String fileName = f.getNameWithoutExtension();
                                    int idx = fileName.lastIndexOf('-');
                                    if (idx <= 0) {
                                        return null;
                                    }
                                    if (idx + 1 >= fileName.length()) {
                                        return null;
                                    }
                                    String artifactPart = fileName.substring(0, idx);
                                    String versionPart = fileName.substring(idx + 1);
                                    if ("SNAPSHORT".equals(versionPart)) {
                                        idx = artifactPart.lastIndexOf('-');
                                        if (idx <= 0) {
                                            return null;
                                        }
                                        if (idx + 1 >= artifactPart.length()) {
                                            return null;
                                        }
                                        artifactPart = artifactPart.substring(0, idx);
                                        versionPart = artifactPart.substring(idx + 1) + "-" + versionPart;
                                    }
                                    id = new SofaArkId();
                                    id.setArtifactId(artifactPart);
                                    id.setVersion(versionPart);
                                }
                                return id;
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    candidateList.forEach(c -> {
                        libraryCandidates.put(c, c);
                        libraryCandidates.put(c.toIdIgnoreVersion(), c);
                        libraryCandidates.put(c.toIdIgnoreGroup(), c);
                        libraryCandidates.put(c.toIdIgnoreGroupAndVersion(), c);
                    });
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (outDir != null) {
                FileUtil.delete(outDir);
            }
        }
        if (libraryCandidates.isEmpty()) {
            return new ArrayList<>();
        }
        File localRepo = MavenProjectsManager.getInstance(project).getLocalRepository();
        return candidates.keySet().stream()
                .map(id -> {
                    SofaArkId newId = libraryCandidates.get(id);
                    if (newId == null) {
                        newId = libraryCandidates.get(id.toIdIgnoreVersion());
                    }
                    if (newId == null) {
                        newId = libraryCandidates.get(id.toIdIgnoreGroup());
                    }
                    if (newId == null) {
                        newId = libraryCandidates.get(id.toIdIgnoreGroupAndVersion());
                    }
                    if (newId != null && newId.getGroupId() == null) {
                        newId.setGroupId(id.getGroupId());
                    }
                    return newId;
                })
                .filter(Objects::nonNull)
                .map(id -> {
                    File f = MavenArtifactUtil.getArtifactFile(localRepo, id.getGroupId(), id.getArtifactId(), id.getVersion(), "jar");
                    if (!f.exists()) {
                        return null;
                    }
                    VirtualFile vf = VfsUtil.findFileByIoFile(f, false);
                    if (vf == null) {
                        return null;
                    }
                    vf = JarFileSystem.getInstance().getRootByLocal(vf);
                    return new VirtualFileClasspathEntry(id, vf);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Set<SofaArkId> findLibrariesForPluginOrBiz(SofaArkBase<?> pluginOrBiz, MavenProject maven, SofaArkManagerRefreshContext context) {
        SofaArkId id = pluginOrBiz.getMetadata().getId();
        Set<SofaArkId> classpath = context.pluginOrBizAndLibraries.get(id);
        if (classpath != null) {
            return classpath;
        }
        ArtifactNode root = new MavenProjectNode(maven);
        ArtifactNode node = findArtifactNode(id, root, context);
        if (node == null) {
            throw new IllegalStateException();
        }
        classpath = findLibrariesForArtifactNode(node, root, context);
        context.pluginOrBizAndLibraries.put(id, classpath);
        return classpath;
    }

    private ArtifactNode findArtifactNode(SofaArkId id, ArtifactNode root, SofaArkManagerRefreshContext context) {
        Stack<ArtifactNode> stack = new Stack<>();
        stack.push(root);
        while (!stack.empty()) {
            ArtifactNode node = stack.pop();
            if (!node.isValid()) {
                continue;
            }
            if (node.isDuplicate()) {
                continue;
            }
            if (id.equals(node.getId())) {
                return node;
            }
            Set<ArtifactNode> children = node.getDependencies();
            if (children != null && children.size() > 0) {
                for (ArtifactNode child : children) {
                    stack.push(child);
                }
            }
        }
        return null;
    }

    private Set<SofaArkId> findLibrariesForArtifactNode(ArtifactNode artifactNode, ArtifactNode root, SofaArkManagerRefreshContext context) {
        Set<SofaArkId> result = new LinkedHashSet<>();
        Stack<ArtifactNode> stack = new Stack<>();
        stack.push(artifactNode);
        while (!stack.isEmpty()) {
            ArtifactNode node = stack.pop();
            if (node == null) {
                continue;
            }
            Set<ArtifactNode> children = node.getDependencies();
            if (children != null && children.size() > 0) {
                for (ArtifactNode child : children) {
                    if (!child.isInProductionScope()) {
                        continue;
                    }
                    if (child.isDuplicate()) {
                        ArtifactNode relatedChild = findArtifactNode(child.getId(), root, context);
                        if (relatedChild == null || relatedChild == child) {
                            continue;
                        }
                        child = relatedChild;
                    }
                    if (!child.isValid()) {
                        continue;
                    }
                    SofaArkId childNodeId = child.getId();
                    if (childNodeId == null) {
                        throw new IllegalStateException();
                    }
                    MavenProject mavenProject = context.getMavenProjectByNode(child);
                    if (mavenProject != null) {
                        continue;
                    }
                    // TODO JarFileSystem
                    VirtualFile vf = child.getFile();
                    if (vf == null) {
                        throw new IllegalStateException("Can not find file of " + child.getId().toString());
                    }
                    // 忽略biz
                    if (VirtualFileSofaArkBiz.isBiz(vf)) {
                        continue;
                    }
                    // 忽略plugin
                    if (VirtualFileSofaArkPlugin.isPlugin(vf)) {
                        continue;
                    }
                    result.add(childNodeId);
                    stack.push(child);
                }
            }
        }
        return result;
    }

    private SofaArkBase<?> createModelFromLibraryEntry(Module module, SofaArkId artifact, LibraryOrderEntry libraryEntry, SofaArkManagerRefreshContext context) {
        VirtualFile file = getLibraryFile(libraryEntry);
        if (file == null) {
            return null;
        }
        if (context.pluginCache.containsKey(artifact) || context.bizCache.containsKey(artifact)) {
            return null;
        }
        if (VirtualFileSofaArkPlugin.isPlugin(file)) {
            VirtualFileSofaArkPlugin plugin = VirtualFileSofaArkPlugin.createFromIdAndFile(artifact, file);
            context.sortedPluginSet.add(plugin);
            context.pluginCache.put(artifact, plugin);
            return plugin;
        } else if (VirtualFileSofaArkBiz.isBiz(file)) {
            VirtualFileSofaArkBiz biz = VirtualFileSofaArkBiz.createFromIdAndFile(artifact, file);
            context.sortedBizSet.add(biz);
            context.bizCache.put(artifact, biz);
            return biz;
        }
        return null;
    }

    private SofaArkBase<?> createModelFromModule(Module module, SofaArkManagerRefreshContext context) {
        MavenProject maven = MavenProjectsManager.getInstance(project).findProject(module);
        if (maven == null) {
            return null;
        }
        if (MavenSofaArkPlugin.isPlugin(maven)) {
            MavenSofaArkPlugin plugin = new MavenSofaArkPlugin(module, maven);
            SofaArkId id = plugin.getMetadata().getId();
            if (context.pluginCache.containsKey(id)) {
                return null;
            }
            context.sortedPluginSet.add(plugin);
            context.pluginCache.put(id, plugin);
            return plugin;
        } else if (MavenSofaArkBiz.isBiz(maven)) {
            MavenSofaArkBiz biz = new MavenSofaArkBiz(module, maven);
            SofaArkId id = biz.getMetadata().getId();
            if (context.bizCache.containsKey(id)) {
                return null;
            }
            context.sortedBizSet.add(biz);
            context.bizCache.put(id, biz);
            return biz;
        }
        return null;
    }

    private Map<SofaArkId, LibraryOrderEntry> findLibraryCandidatesOfModule(Module module) {
        Map<SofaArkId, LibraryOrderEntry> result = new LinkedHashMap<>();
        for (OrderEntry entry : ModuleRootManager.getInstance(module).getOrderEntries()) {
            if (entry instanceof LibraryOrderEntry) {
                LibraryOrderEntry libraryEntry = (LibraryOrderEntry) entry;
                String name = libraryEntry.getPresentableName();
                if (!StringUtil.isNotEmpty(name)) {
                    continue;
                }
                if (!DependencyScope.COMPILE.equals(libraryEntry.getScope())
                        && !DependencyScope.RUNTIME.equals(libraryEntry.getScope())
                        && !DependencyScope.PROVIDED.equals(libraryEntry.getScope())) {
                    continue;
                }
                if (!StringUtil.startsWith(name, MavenArtifact.MAVEN_LIB_PREFIX)) {
                    continue;
                }
                name = StringUtil.trimStart(name, MavenArtifact.MAVEN_LIB_PREFIX);
                SofaArkId artifact = SofaArkId.parseSofaArkId(name);
                result.put(artifact, libraryEntry);
            }
        }
        return result;
    }

    private VirtualFile getLibraryFile(@NotNull LibraryOrderEntry libraryEntry) {
        Library library = libraryEntry.getLibrary();
        if (library == null) {
            return null;
        }
        VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
        if (files.length <= 0) {
            return null;
        }
        for (VirtualFile file : files) {
            if (file == null
                    || !file.isValid()
                    || !(file.getFileType() instanceof ArchiveFileType)) {
                continue;
            }
            // TODO 支持多个文件的情况
            // return JarFileSystem.getInstance().getJarRootForLocalFile(file);
            return file;
        }
        return null;
    }

    private final static class SofaArkManagerRefreshContext {
        private Project project;

        Set<SofaArkPlugin> sortedPluginSet = new TreeSet<>(new SofaArkPluginComparator());

        Set<SofaArkBiz> sortedBizSet = new TreeSet<>(new SofaArkBizComparator());

        Map<SofaArkId, SofaArkPlugin> pluginCache = new HashMap<>();

        Map<SofaArkId, SofaArkBiz> bizCache = new HashMap<>();

        Multimap<SofaArkBase<?>, String> pluginOrBizErrors = HashMultimap.create();

        Map<String, SofaArkPlugin> exportClass2PluginMap = new HashMap<>();

        Map<String, SofaArkPlugin> exportPackage2PluginMap = new HashMap<>();

        Map<String, SofaArkPlugin> exportStem2PluginMap = new HashMap<>();

        Map<SofaArkId, Set<SofaArkId>> pluginOrBizAndLibraries = new LinkedHashMap<>();

        SofaArkManagerRefreshContext(Project project) {
            this(project, null);
        }

        SofaArkManagerRefreshContext(Project project, SofaArkManagerRefreshContext context) {
            this.project = project;
            if (context == null) {
                return;
            }
            this.sortedPluginSet.addAll(context.sortedPluginSet);
            this.pluginCache.putAll(context.pluginCache);
            this.sortedBizSet.addAll(context.sortedBizSet);
            this.bizCache.putAll(context.bizCache);
            this.exportClass2PluginMap.putAll(context.exportClass2PluginMap);
            this.exportPackage2PluginMap.putAll(context.exportPackage2PluginMap);
            this.exportStem2PluginMap.putAll(context.exportStem2PluginMap);
            this.pluginOrBizAndLibraries.putAll(context.pluginOrBizAndLibraries);
            this.pluginOrBizErrors.putAll(context.pluginOrBizErrors);
        }

        MavenProject getMavenProjectByNode(ArtifactNode node) {
            if (node instanceof NormalArtifactNode) {
                MavenId mavenId = ((NormalArtifactNode) node).node.getArtifact().getMavenId();
                return MavenProjectsManager.getInstance(project).findProject(mavenId);
            } else if (node instanceof MavenProjectNode) {
                return ((MavenProjectNode) node).maven;
            }
            return null;
        }
    }

    private static class SofaArkPluginComparator implements Comparator<SofaArkPlugin> {
        @Override
        public int compare(SofaArkPlugin p1, SofaArkPlugin p2) {
            if (p1 == null || p2 == null) {
                throw new IllegalStateException();
            }
            int r = p1.getPriority() - p2.getPriority();
            if (r == 0) {
                String id1 = p1.getMetadata().getId().toString();
                String id2 = p2.getMetadata().getId().toString();
                return id1.compareTo(id2);
            }
            return r;
        }
    }

    private static class SofaArkBizComparator implements Comparator<SofaArkBiz> {
        @Override
        public int compare(SofaArkBiz b1, SofaArkBiz b2) {
            if (b1 == null || b2 == null) {
                throw new IllegalStateException();
            }
            int r = b1.getPriority() - b2.getPriority();
            if (r == 0) {
                String id1 = b1.getMetadata().getId().toString();
                String id2 = b2.getMetadata().getId().toString();
                return id1.compareTo(id2);
            }
            return r;
        }
    }

}
