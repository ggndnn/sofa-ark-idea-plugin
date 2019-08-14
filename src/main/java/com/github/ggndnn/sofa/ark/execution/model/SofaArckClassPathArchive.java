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
package com.github.ggndnn.sofa.ark.execution.model;

import com.alipay.sofa.ark.bootstrap.ClasspathLauncher;
import com.alipay.sofa.ark.loader.JarBizArchive;
import com.alipay.sofa.ark.loader.JarPluginArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.archive.PluginArchive;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SofaArckClassPathArchive extends ClasspathLauncher.ClassPathArchive {
    private File workingDir;

    public SofaArckClassPathArchive(File workingDir, URL[] urls) throws IOException {
        super("test", null, urls);
        this.workingDir = workingDir;
    }

    @Override
    public List<BizArchive> getBizArchives() throws Exception {
        File[] files = workingDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String fileName = pathname.getName();
                return fileName.endsWith(".biz") || fileName.endsWith(".biz.ref");
            }
        });
        List<BizArchive> archives = null;
        if (files != null) {
            archives = Stream.of(files).map(file -> {
                try (InputStream input = new FileInputStream(file)) {
                    if (file.getName().endsWith(".biz")) {
                        SofaArkBizModel bizModel = new SofaArkBizModel();
                        bizModel.read(input);
                        return new SofaArkBizArchive(bizModel);
                    } else if (file.getName().endsWith(".biz.ref")) {
                        SofaArkPluginOrBizRefModel bizModel = new SofaArkPluginOrBizRefModel();
                        bizModel.read(input);
                        return new JarBizArchive(new JarFileArchive(new File(bizModel.getLocation())));
                    }
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return archives;
    }

    @Override
    public List<PluginArchive> getPluginArchives() throws Exception {
        File[] files = workingDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String fileName = pathname.getName();
                return fileName.endsWith(".plugin") || fileName.endsWith(".plugin.ref");
            }
        });
        List<PluginArchive> archives = null;
        if (files != null) {
            archives = Stream.of(files).map(file -> {
                try (InputStream input = new FileInputStream(file)) {
                    if (file.getName().endsWith(".plugin")) {
                        SofaArkPluginModel pluginModel = new SofaArkPluginModel();
                        pluginModel.read(input);
                        return new SofaArkPluginArchive(pluginModel);
                    } else if (file.getName().endsWith(".plugin.ref")) {
                        SofaArkPluginOrBizRefModel pluginModel = new SofaArkPluginOrBizRefModel();
                        pluginModel.read(input);
                        return new JarPluginArchive(new JarFileArchive(new File(pluginModel.getLocation())));
                    }
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return archives;
    }

    @Override
    public List<URL> getConfClasspath() throws IOException {
        List<URL> urls = new ArrayList<>();
        File confDir = new File(workingDir, "conf");
        if (confDir.exists()) {
            urls.add(confDir.toURI().toURL());
        }
        return urls;
    }
}
