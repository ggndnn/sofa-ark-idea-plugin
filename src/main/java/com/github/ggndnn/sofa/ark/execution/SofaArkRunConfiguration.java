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

import com.alipay.sofa.ark.spi.argument.CommandArgument;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.jar.JarApplicationConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.execution.util.ProgramParametersUtil;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.alipay.sofa.ark.spi.argument.CommandArgument.ARK_CONTAINER_ARGUMENTS_MARK;
import static com.alipay.sofa.ark.spi.argument.CommandArgument.CLASSPATH_ARGUMENT_KEY;

public class SofaArkRunConfiguration extends JarApplicationConfiguration {
    private final static String[] SOFA_AKR_CONTAINER_BASE_JAR_MARKS = {"aopalliance-1.0", "commons-io-2.5",
            "guava-16.0.1", "guice-4.0", "guice-multibindings-4.0", "javax.inject-1",
            "logback-core-1.1.11", "logback-classic-1.1.11", "slf4j-api-1.7"};

    private final static String[] SOFA_AKR_CONTAINER_JAR_MARKS = {"log-sofa-boot-starter", "sofa-common-tools",
            "sofa-ark-container", "sofa-ark-archive", "sofa-ark-spi",
            "sofa-ark-common", "sofa-ark-exception", "sofa-ark-api"};

    private final static String[] PLUGIN_SOFA_AKR_CONTAINER_JAR_MARKS = {"sofa-ark-idea-plugin"};

    private final static Pattern FILE_NAME_PATTERN = Pattern.compile("([a-zA-Z0-9\\-]+)-[0-9.]+\\.jar");

    private final static String ELEMENT_RUN_CONFIG = "sofaArkRunConfig";

    SofaArkRunConfigurationBean configBean = new SofaArkRunConfigurationBean();

    SofaArkRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        SettingsEditorGroup<SofaArkRunConfiguration> group = new SettingsEditorGroup<>();
        group.addEditor("PluginOrBiz", new SofaArkPluginOrBizListPanel(getProject()));
        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), new SofaArkRunSettingsEditor(getProject()));
        return group;
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        // TODO load from file
        return new SofaArkApplicationCommandLineState(this, environment);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        JavaParametersUtil.checkAlternativeJRE(this);
        ProgramParametersUtil.checkWorkingDirectoryExist(this, getProject(), null);
        JavaRunConfigurationExtensionManager.checkConfigurationIsValid(this);
    }

    @Override
    public SofaArkRunConfiguration clone() {
        SofaArkRunConfiguration clone = (SofaArkRunConfiguration) super.clone();
        clone.configBean = new SofaArkRunConfigurationBean();
        if (configBean.pluginOrBizModels != null) {
            clone.configBean.pluginOrBizModels = Stream.of(configBean.pluginOrBizModels)
                    .map(PluginOrBizModel::clone).toArray(PluginOrBizModel[]::new);
        }
        return clone;
    }

    @Override
    public void readExternal(@NotNull Element element) {
        super.readExternal(element);
        Element child = element.getChild(ELEMENT_RUN_CONFIG);
        if (child != null) {
            XmlSerializer.deserializeInto(configBean, child);
        }
    }

    @Override
    public void writeExternal(@NotNull Element element) {
        super.writeExternal(element);
        Element child = element.getChild(ELEMENT_RUN_CONFIG);
        if (child == null) {
            child = new Element(ELEMENT_RUN_CONFIG);
            element.addContent(child);
        }
        XmlSerializer.serializeInto(configBean, child);
    }

    @NotNull
    String getSofaArkContainerParameters() {
        // TODO filter out container and plugin or biz in workingDir
        String classpath = OrderEnumerator
                .orderEntries(getProject())
                .withoutSdk()
                .librariesOnly()
                .productionOnly()
                .classes()
                .getPathsList()
                .getVirtualFiles()
                .stream()
                .map(VirtualFile::getUrl).collect(Collectors.joining(CommandArgument.CLASSPATH_SPLIT));
        return String.format("%s%s=%s", ARK_CONTAINER_ARGUMENTS_MARK,
                CLASSPATH_ARGUMENT_KEY, classpath);
    }

    @NotNull
    List<String> getSofaArkLauncherClasspath() {
        Map<String, String> urls = new LinkedHashMap<>();
        ProjectRootManager.getInstance(getProject()).orderEntries().forEachLibrary(l -> {
            VirtualFile[] vfs = l.getFiles(OrderRootType.CLASSES);
            if (vfs.length <= 0) {
                return true;
            }
            VirtualFile vf = vfs[0];
            vf = VfsUtil.getLocalFile(vf);
            File f = VfsUtil.virtualToIoFile(vf);
            String fileName = f.getName();
            for (String mark : SOFA_AKR_CONTAINER_BASE_JAR_MARKS) {
                if (fileName.startsWith(mark)) {
                    urls.put(mark, f.getAbsolutePath());
                    return true;
                }
            }
            Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
            if (!matcher.matches()) {
                return true;
            }
            String prefix = matcher.group(1);
            for (String mark : SOFA_AKR_CONTAINER_JAR_MARKS) {
                if (mark.startsWith(prefix)) {
                    urls.put(mark, f.getAbsolutePath());
                }
            }
            return true;
        });
        // TODO check whether sofa ark container jars are all present
        // TODO deal with then compatibility issue between PLUGIN_SOFA_AKR_CONTAINER_JARs and SOFA_AKR_CONTAINER_JARs
        PluginClassLoader cl = (PluginClassLoader) SofaArkRunConfiguration.class.getClassLoader();
        cl.getUrls().forEach(u -> {
            String file = PathUtil.getFileName(u.getFile());
            for (String mark : SOFA_AKR_CONTAINER_BASE_JAR_MARKS) {
                if (urls.containsKey(mark)) {
                    continue;
                }
                if (file.startsWith(mark)) {
                    urls.put(mark, urlToClasspathEntry(u));
                    return;
                }
            }
            for (String mark : PLUGIN_SOFA_AKR_CONTAINER_JAR_MARKS) {
                if (urls.containsKey(mark)) {
                    continue;
                }
                if (file.startsWith(mark)) {
                    urls.put(mark, urlToClasspathEntry(u));
                }
            }
        });
        return new ArrayList<>(urls.values());
    }

    private String urlToClasspathEntry(URL url) {
        try {
            URI uri = url.toURI();
            return new File(uri).getAbsolutePath();
        } catch (URISyntaxException e) {
            // TODO log
        }
        return null;
    }
}
