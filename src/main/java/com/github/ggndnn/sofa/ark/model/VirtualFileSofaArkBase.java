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

import com.github.ggndnn.sofa.ark.execution.model.SofaArkId;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkModel;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Manifest;

abstract class VirtualFileSofaArkBase<T extends SofaArkModel> extends SofaArkBase<T> {
    private final static String[] MANIFEST_PATH = {"META-INF", "MANIFEST.MF"};

    VirtualFileSofaArkBase(SofaArkId id, VirtualFile file) {
        init(id, file);
    }

    private void init(SofaArkId id, VirtualFile file) {
        metadata = createMetadata(file);
        metadata.setId(id);
        setLocation(file);
        postProcessMetadata(metadata);
    }

    @Override
    public void setClasspath(Set<SofaArkClasspathEntry> classpath) {
        if (classpath == null) {
            super.setClasspath(null);
            return;
        }
        // TODO exclude dependencies according to libs...
        VirtualFile location = getLocation();
        if (location == null)
            throw new IllegalStateException("missing location");
        Set<SofaArkClasspathEntry> newClasspath = new LinkedHashSet<>();
        for (SofaArkClasspathEntry entry : classpath) {
            SofaArkId id = entry.getId();
            StringBuilder jarPath = new StringBuilder();
            jarPath.append("lib/");
            jarPath.append(id.getArtifactId());
            jarPath.append("-");
            jarPath.append(id.getVersion());
            jarPath.append(".jar");
            if (VfsUtil.findRelativeFile(jarPath.toString(), location) != null) {
                newClasspath.add(entry);
            }
        }
        super.setClasspath(newClasspath);
    }

    abstract T createMetadata(VirtualFile file);

    abstract void postProcessMetadata(T metadata);

    Manifest getManifest() throws IOException {
        VirtualFile vf = VfsUtil.findRelativeFile(getLocation(), MANIFEST_PATH);
        if (vf == null || vf.isDirectory())
            return null;
        return new Manifest(vf.getInputStream());
    }
}
