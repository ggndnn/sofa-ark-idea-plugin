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

import com.alipay.sofa.ark.spi.archive.PluginArchive;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Manifest;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

public class SofaArkPluginArchive extends SofaArkProjectArchive implements PluginArchive {
    protected SofaArkPluginArchive(SofaArkPluginModel pluginModel) {
        super(pluginModel);
        Manifest manifest = getManifest();
        manifest.getMainAttributes().putValue(PRIORITY_ATTRIBUTE, String.valueOf(pluginModel.getPriority()));
        manifest.getMainAttributes().putValue(PLUGIN_NAME_ATTRIBUTE, pluginModel.getPluginName());
        manifest.getMainAttributes().putValue(PLUGIN_VERSION_ATTRIBUTE, pluginModel.getPluginVersion());
        manifest.getMainAttributes().putValue(ACTIVATOR_ATTRIBUTE, pluginModel.getActivator());
        manifest.getMainAttributes().putValue(IMPORT_PACKAGES_ATTRIBUTE, pluginModel.getImportPackages());
        manifest.getMainAttributes().putValue(IMPORT_CLASSES_ATTRIBUTE, pluginModel.getImportClasses());
        manifest.getMainAttributes().putValue(IMPORT_RESOURCES_ATTRIBUTE, pluginModel.getImportResources());
        manifest.getMainAttributes().putValue(EXPORT_PACKAGES_ATTRIBUTE, pluginModel.getExportPackages());
        manifest.getMainAttributes().putValue(EXPORT_CLASSES_ATTRIBUTE, pluginModel.getExportClasses());
        manifest.getMainAttributes().putValue(EXPORT_RESOURCES_ATTRIBUTE, pluginModel.getExportResources());
    }

    @Override
    public URL[] getUrls() throws IOException {
        return super.getClasspath();
    }

    @Override
    public boolean isEntryExist(EntryFilter filter) {
        return filter.matches(new Entry() {
            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public String getName() {
                return Constants.ARK_PLUGIN_MARK_ENTRY;
            }
        });
    }
}
