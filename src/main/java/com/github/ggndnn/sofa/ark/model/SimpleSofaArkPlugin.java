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

import java.util.Set;

class SimpleSofaArkPlugin implements SofaArkPlugin {
    private SofaArkPluginModel metadata;

    private Set<String> importPackages;

    private Set<String> importPackageStems;

    private Set<String> importClasses;

    private Set<String> exportPackages;

    private Set<String> exportPackageStems;

    private Set<String> exportClasses;

    SimpleSofaArkPlugin(SofaArkPluginModel metadata) {
        this.metadata = metadata;
    }

    @Override
    public SofaArkPluginModel getMetadata() {
        return metadata;
    }

    @Override
    public Set<String> getImportPackages() {
        return importPackages;
    }

    @Override
    public Set<String> getImportPackageStems() {
        return importPackageStems;
    }

    @Override
    public Set<String> getImportClasses() {
        return importClasses;
    }

    @Override
    public Set<String> getExportPackages() {
        return exportPackages;
    }

    @Override
    public Set<String> getExportPackageStems() {
        return exportPackageStems;
    }

    @Override
    public Set<String> getExportClasses() {
        return exportClasses;
    }

    @Override
    public Integer getPriority() {
        if (metadata.getPriority() <= 0) {
            // TODO ...
            return 20000;
        }
        return metadata.getPriority();
    }

    void setImportPackages(SofaArkPluginModel metadata, Set<String> packages) {
        if (packages == null) {
            metadata.setImportPackages(null);
        } else {
            metadata.setImportPackages(String.join(",", packages));
        }
        importPackageStems = SofaArkBase.createPackageStem(packages);
        importPackages = packages;
    }

    void setImportClasses(SofaArkPluginModel metadata, Set<String> classes) {
        this.importClasses = classes;
        metadata.setImportClasses(classes == null ? null : String.join(",", classes));
    }

    void setExportPackages(SofaArkPluginModel metadata, Set<String> packages) {
        if (packages == null) {
            metadata.setExportPackages(null);
        } else {
            metadata.setExportPackages(String.join(",", packages));
        }
        exportPackageStems = SofaArkBase.createPackageStem(packages);
        exportPackages = packages;
    }

    void setExportClasses(SofaArkPluginModel metadata, Set<String> classes) {
        this.exportClasses = classes;
        metadata.setExportClasses(classes == null ? null : String.join(",", classes));
    }
}
