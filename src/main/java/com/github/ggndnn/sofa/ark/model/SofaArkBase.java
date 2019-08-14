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

import com.alipay.sofa.ark.common.util.ClassUtils;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkClasspathEntryModel;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkModel;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.alipay.sofa.ark.spi.constant.Constants.PACKAGE_PREFIX_MARK;

public abstract class SofaArkBase<T extends SofaArkModel> {
    T metadata;

    private Set<SofaArkClasspathEntry> classpath;

    private VirtualFile location;

    public T getMetadata() {
        return metadata;
    }

    public VirtualFile getLocation() {
        return location;
    }

    void setLocation(VirtualFile location) {
        this.location = location;
        if (location != null) {
            VirtualFile vf = VfsUtil.getLocalFile(location);
            setLocationUrl(vf.getUrl());
        } else {
            setLocationUrl(null);
        }
    }

    void setLocationUrl(String url) {
        this.metadata.setLocation(url);
    }

    public void setClasspath(Set<SofaArkClasspathEntry> classpath) {
        if (classpath == null) {
            this.classpath = null;
            this.metadata.setClasspath(null);
        } else {
            this.classpath = classpath;
            Set<SofaArkClasspathEntryModel> metadataClasspath = new LinkedHashSet<>();
            for (SofaArkClasspathEntry entry : classpath) {
                SofaArkClasspathEntryModel model = entry.createModel();
                metadataClasspath.add(model);
            }
            this.metadata.setClasspath(metadataClasspath);
        }
    }

    public boolean hasResource(String path) {
        if (classpath != null) {
            for (SofaArkClasspathEntry entry : classpath) {
                if (entry.hasResource(path))
                    return true;
            }
        }
        return false;
    }

    // TODO ...
    static Set<String> createPackageStem(Set<String> packages) {
        if (packages == null)
            return null;
        Set<String> stems = new LinkedHashSet<>();
        for (String pkg : packages) {
            if (pkg.endsWith(PACKAGE_PREFIX_MARK)) {
                stems.add(ClassUtils.getPackageName(pkg));
            }
        }
        return stems;
    }
}
