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

public class SofaArkBizModel extends SofaArkModel {
    private String bizName;

    private String bizVersion;

    private String webContextPath;

    private String mainClass;

    private String denyImportClasses;

    private String denyImportPackages;

    private String denyImportResources;

    public String getBizName() {
        return bizName;
    }

    public String getBizVersion() {
        return bizVersion;
    }

    public String getWebContextPath() {
        return webContextPath;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getDenyImportClasses() {
        return denyImportClasses;
    }

    public String getDenyImportPackages() {
        return denyImportPackages;
    }

    public String getDenyImportResources() {
        return denyImportResources;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public void setBizVersion(String bizVersion) {
        this.bizVersion = bizVersion;
    }

    public void setWebContextPath(String webContextPath) {
        this.webContextPath = webContextPath;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public void setDenyImportClasses(String denyImportClasses) {
        this.denyImportClasses = denyImportClasses;
    }

    public void setDenyImportPackages(String denyImportPackages) {
        this.denyImportPackages = denyImportPackages;
    }

    public void setDenyImportResources(String denyImportResources) {
        this.denyImportResources = denyImportResources;
    }

    @Override
    public void read(InputStream input) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(input)) {
            try {
                SofaArkBizModel model = (SofaArkBizModel) ois.readObject();
                setPriority(model.getPriority());
                setMainClass(model.getMainClass());
                setBizName(model.getBizName());
                setBizVersion(model.getBizVersion());
                setLocation(model.getLocation());
                setWebContextPath(model.getWebContextPath());
                setDenyImportResources(model.getDenyImportResources());
                setDenyImportPackages(model.getDenyImportPackages());
                setDenyImportClasses(model.getDenyImportClasses());
                setClasspath(model.getClasspath());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
