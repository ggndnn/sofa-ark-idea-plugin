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

import com.github.ggndnn.sofa.ark.execution.model.SofaArkConstants;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkId;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkPluginModel;
import com.github.ggndnn.sofa.ark.utils.SofaArkArchiveHelper;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Manifest;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

public class VirtualFileSofaArkPlugin extends VirtualFileSofaArkBase<SofaArkPluginModel> implements SofaArkPlugin {
    private SimpleSofaArkPlugin delegate;

    VirtualFileSofaArkPlugin(SofaArkId id, VirtualFile file) {
        super(id, file);
    }

    @Override
    SofaArkPluginModel createMetadata(VirtualFile file) {
        return new SofaArkPluginModel();
    }

    @Override
    void postProcessMetadata(SofaArkPluginModel metadata) {
        try {
            Manifest manifest = super.getManifest();
            if (manifest == null)
                throw new IllegalStateException();
            String priorityValue = manifest.getMainAttributes().getValue(PRIORITY_ATTRIBUTE);
            if (priorityValue != null) {
                metadata.setPriority(Integer.parseInt(priorityValue));
            }
            String pluginName = manifest.getMainAttributes().getValue(PLUGIN_NAME_ATTRIBUTE);
            metadata.setPluginName(pluginName);
            String pluginVersion = manifest.getMainAttributes().getValue(PLUGIN_VERSION_ATTRIBUTE);
            metadata.setPluginVersion(pluginVersion);
            String activator = manifest.getMainAttributes().getValue(ACTIVATOR_ATTRIBUTE);
            metadata.setActivator(activator);
            delegate = new SimpleSofaArkPlugin(metadata);
            delegate.setImportClasses(metadata, attributeToSet(manifest, IMPORT_CLASSES_ATTRIBUTE));
            delegate.setImportPackages(metadata, attributeToSet(manifest, IMPORT_PACKAGES_ATTRIBUTE));
            metadata.setImportResources(manifest.getMainAttributes().getValue(IMPORT_RESOURCES_ATTRIBUTE));
            delegate.setExportClasses(metadata, attributeToSet(manifest, EXPORT_CLASSES_ATTRIBUTE));
            delegate.setExportPackages(metadata, attributeToSet(manifest, EXPORT_PACKAGES_ATTRIBUTE));
            metadata.setExportResources(manifest.getMainAttributes().getValue(EXPORT_RESOURCES_ATTRIBUTE));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> attributeToSet(Manifest manifest, String attributeName) {
        String attributeValue = manifest.getMainAttributes().getValue(attributeName);
        if (attributeValue == null)
            return null;
        return new LinkedHashSet<>(Arrays.asList(attributeValue.split(",")));
    }

    public static boolean isPlugin(VirtualFile file) {
        return VfsUtil.findRelativeFile(file, SofaArkConstants.SOFA_ARK_PLUGIN_MARK.split("/")) != null;
    }

    public static VirtualFileSofaArkPlugin createFromIdAndFile(SofaArkId id, VirtualFile vf) {
        if (id == null) {
            return createFromFile(vf);
        }
        if (vf != null) {
            return new VirtualFileSofaArkPlugin(id, vf);
        }
        return null;
    }

    public static VirtualFileSofaArkPlugin createFromFile(VirtualFile vf) {
        SofaArkId id = SofaArkArchiveHelper.loadSofaArkIdFromFile(vf);
        if (id == null)
            return null;
        return createFromIdAndFile(id, vf);
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
