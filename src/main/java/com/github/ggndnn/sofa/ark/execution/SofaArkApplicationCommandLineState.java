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
package com.github.ggndnn.sofa.ark.execution;

import com.github.ggndnn.sofa.ark.components.SofaArkManager;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkId;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkLauncher;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkPluginOrBizRefModel;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkSerializable;
import com.github.ggndnn.sofa.ark.model.SofaArkBase;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.application.BaseJavaApplicationCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static com.intellij.execution.configurations.JavaParameters.JDK_ONLY;

class SofaArkApplicationCommandLineState extends BaseJavaApplicationCommandLineState<SofaArkRunConfiguration> {
    SofaArkApplicationCommandLineState(@NotNull SofaArkRunConfiguration configuration, ExecutionEnvironment environment) {
        super(environment, configuration);
    }

    @Override
    protected JavaParameters createJavaParameters() throws ExecutionException {
        SofaArkRunConfiguration configuration = getConfiguration();
        final JavaParameters params = new JavaParameters();
        final String jreHome = myConfiguration.isAlternativeJrePathEnabled() ? myConfiguration.getAlternativeJrePath() : null;
        Sdk sdk = JavaParametersUtil.createProjectJdk(myConfiguration.getProject(), jreHome);
        params.configureByProject(configuration.getProject(), JDK_ONLY, sdk);
        try {
            final File workingDir = new File(configuration.getWorkingDirectory(), ".sofa-ark");
            if (workingDir.exists()) {
                FileUtil.delete(workingDir);
            }
            if (!workingDir.mkdirs()) {
                throw new IllegalStateException(String.format("Create workingDir %s error", workingDir.getAbsolutePath()));
            }
            // TODO ...
            List<PluginOrBizModel> resolvePluginOrBizModles = SofaArkPluginOrBizListPanel.resolvePluginOrBizModels(configuration);
            SofaArkManager sofaArkManager = getConfiguration().getProject().getComponent(SofaArkManager.class);
            resolvePluginOrBizModles.stream()
                    .filter(m -> m.enabled)
                    .forEach(m -> {
                        SofaArkId id = new SofaArkId();
                        id.setGroupId(m.groupId);
                        id.setArtifactId(m.artifactId);
                        id.setVersion(m.version);
                        SofaArkSerializable pluginOrBizWriter = null;
                        if (PluginOrBizModel.TYPE_WORKSPACE.equals(m.type)
                                || PluginOrBizModel.TYPE_WORKSPACE_JAR.equals(m.type)) {
                            SofaArkBase<?> pluginOrBiz = (SofaArkBase<?>) sofaArkManager.getBizById(id);
                            if (pluginOrBiz == null) {
                                pluginOrBiz = (SofaArkBase<?>) sofaArkManager.getPluginById(id);
                            }
                            if (pluginOrBiz != null) {
                                pluginOrBizWriter = pluginOrBiz.getMetadata();
                            }
                        } else if (PluginOrBizModel.TYPE_JAR.equals(m.type)) {
                            SofaArkPluginOrBizRefModel pluginOrBizRef = new SofaArkPluginOrBizRefModel();
                            pluginOrBizRef.setLocation(m.location);
                            if (pluginOrBizRef.getLocation() != null) {
                                pluginOrBizWriter = pluginOrBizRef;
                            }
                        }
                        if (pluginOrBizWriter == null) {
                            return;
                        }
                        String fileName = m.artifactId;
                        if (PluginOrBizModel.CLASSIFIER_BIZ.equals(m.classifier)) {
                            fileName += ".biz";
                        } else if (PluginOrBizModel.CLASSIFIER_PLUGIN.equals(m.classifier)) {
                            fileName += ".plugin";
                        }
                        if (PluginOrBizModel.TYPE_JAR.equals(m.type)) {
                            fileName += ".ref";
                        }
                        try (OutputStream output = new FileOutputStream(new File(workingDir, fileName))) {
                            pluginOrBizWriter.write(output);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            params.getClassPath().addAll(configuration.getSofaArkLauncherClasspath());
        } catch (Throwable e) {
            throw new ExecutionException(e);
        }
        params.setMainClass(SofaArkLauncher.class.getName());
        params.getProgramParametersList().add(configuration.getSofaArkContainerParameters());
        setupJavaParameters(params);
        return params;
    }
}
