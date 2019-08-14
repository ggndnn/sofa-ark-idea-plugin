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

import com.github.ggndnn.sofa.ark.execution.model.SofaArkBizModel;

import java.util.Set;

import static com.github.ggndnn.sofa.ark.model.SofaArkBase.createPackageStem;

public class SimpleSofaArkBiz implements SofaArkBiz {
    private SofaArkBizModel metadata;

    private Set<String> denyImportPackages;

    private Set<String> denyImportPackageStems;

    private Set<String> denyImportClasses;

    SimpleSofaArkBiz(SofaArkBizModel metadata) {
        this.metadata = metadata;
    }

    @Override
    public SofaArkBizModel getMetadata() {
        return metadata;
    }

    @Override
    public Set<String> getDenyImportPackages() {
        return denyImportPackages;
    }

    @Override
    public Set<String> getDenyImportClasses() {
        return denyImportClasses;
    }

    @Override
    public Set<String> getDenyImportPackageStems() {
        return denyImportPackageStems;
    }

    @Override
    public Integer getPriority() {
        if (metadata.getPriority() <= 0) {
            // TODO ...
            return 20000;
        }
        return metadata.getPriority();
    }

    void setDenyImportPackages(SofaArkBizModel metadata, Set<String> packages) {
        denyImportPackages = packages;
        metadata.setDenyImportPackages(packages == null ? null : String.join(",", packages));
        denyImportPackageStems = createPackageStem(packages);
    }

    void setDenyImportClasses(SofaArkBizModel metadata, Set<String> classes) {
        denyImportClasses = classes;
        metadata.setDenyImportClasses(classes == null ? null : String.join(",", classes));
    }
}
