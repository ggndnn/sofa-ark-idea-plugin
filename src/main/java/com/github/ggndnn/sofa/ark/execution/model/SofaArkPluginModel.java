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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class SofaArkPluginModel extends SofaArkModel {
    private String pluginName;

    private String pluginVersion;

    private String activator;

    private String importClasses;

    private String importPackages;

    private String importResources;

    private String exportClasses;

    private String exportPackages;

    private String exportResources;

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public String getActivator() {
        return activator;
    }

    public void setActivator(String activator) {
        this.activator = activator;
    }

    public String getImportClasses() {
        return importClasses;
    }

    public void setImportClasses(String importClasses) {
        this.importClasses = importClasses;
    }

    public String getImportPackages() {
        return importPackages;
    }

    public void setImportPackages(String importPackages) {
        this.importPackages = importPackages;
    }

    public String getImportResources() {
        return importResources;
    }

    public void setImportResources(String importResources) {
        this.importResources = importResources;
    }

    public String getExportClasses() {
        return exportClasses;
    }

    public void setExportClasses(String exportClasses) {
        this.exportClasses = exportClasses;
    }

    public String getExportPackages() {
        return exportPackages;
    }

    public void setExportPackages(String exportPackages) {
        this.exportPackages = exportPackages;
    }

    public String getExportResources() {
        return exportResources;
    }

    public void setExportResources(String exportResources) {
        this.exportResources = exportResources;
    }

    @Override
    public void read(InputStream input) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(input)) {
            try {
                SofaArkPluginModel model = (SofaArkPluginModel) ois.readObject();
                setPriority(model.getPriority());
                setPluginName(model.getPluginName());
                setPluginVersion(model.getPluginVersion());
                setActivator(model.getActivator());
                setId(model.getId());
                setType(model.getType());
                setLocation(model.getLocation());
                setClasspath(model.getClasspath());
                setExportClasses(model.getExportClasses());
                setExportPackages(model.getExportPackages());
                setExportResources(model.getExportResources());
                setImportClasses(model.getImportClasses());
                setImportPackages(model.getImportPackages());
                setImportResources(model.getImportResources());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
