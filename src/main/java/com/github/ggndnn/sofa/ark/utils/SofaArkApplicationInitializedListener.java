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
package com.github.ggndnn.sofa.ark.utils;

import com.github.ggndnn.sofa.ark.components.SofaArkManager;
import com.intellij.ProjectTopics;
import com.intellij.ide.ApplicationInitializedListener;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.*;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectChanges;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.project.MavenProjectsTree;
import org.jetbrains.idea.maven.server.NativeMavenProjectHolder;

import java.util.List;

/**
 * SofaArkApplicationInitializedListener
 *
 * @author ggndnn
 */
public class SofaArkApplicationInitializedListener implements ApplicationInitializedListener {
    @Override
    public void componentsInitialized() {
        Application app = ApplicationManager.getApplication();
        if (app.isUnitTestMode()) {
            return;
        }
        app.getMessageBus().connect().subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
            @Override
            public void projectOpened(@NotNull Project project) {
//                project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
//                    @Override
//                    public void rootsChanged(@NotNull ModuleRootEvent event) {
//                        DumbService.getInstance(project).runWhenSmart(() -> {
//                            SofaArkManager sofaArkManager = project.getComponent(SofaArkManager.class);
//                            sofaArkManager.refresh();
//                        });
//                    }
//                });
                project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
                    @Override
                    public void moduleAdded(@NotNull Project project, @NotNull Module module) {
                        DumbService.getInstance(project).runWhenSmart(() -> {
                            MavenProject maven = MavenProjectsManager.getInstance(project).findProject(module);
                            if (maven != null) {
                                SofaArkManager sofaArkManager = project.getComponent(SofaArkManager.class);
                                sofaArkManager.onMavenProjectUpdate(maven);
                            }
                        });
                    }

                    @Override
                    public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {
                        DumbService.getInstance(project).runWhenSmart(() -> {
                            MavenProject maven = MavenProjectsManager.getInstance(project).findProject(module);
                            if (maven != null) {
                                SofaArkManager sofaArkManager = project.getComponent(SofaArkManager.class);
                                sofaArkManager.onMavenProjectDelete(maven);
                            }
                        });
                    }

                });
                MavenProjectsManager.getInstance(project).addProjectsTreeListener(new MavenProjectsTree.Listener() {
                    @Override
                    public void projectResolved(@NotNull Pair<MavenProject, MavenProjectChanges> projectWithChanges, @Nullable NativeMavenProjectHolder nativeMavenProject) {
                        DumbService.getInstance(project).runWhenSmart(new Runnable() {
                            @Override
                            public void run() {
                                SofaArkManager sofaArkManager = project.getComponent(SofaArkManager.class);
                                sofaArkManager.onMavenProjectUpdate(projectWithChanges.first);
                            }
                        });
                    }

                    @Override
                    public void projectsUpdated(@NotNull List<Pair<MavenProject, MavenProjectChanges>> updated, @NotNull List<MavenProject> deleted) {
                        DumbService.getInstance(project).runWhenSmart(new Runnable() {
                            @Override
                            public void run() {
                                SofaArkManager sofaArkManager = project.getComponent(SofaArkManager.class);
                                deleted.forEach(sofaArkManager::onMavenProjectDelete);
                            }
                        });
                    }
                });
            }
        });
    }
}
