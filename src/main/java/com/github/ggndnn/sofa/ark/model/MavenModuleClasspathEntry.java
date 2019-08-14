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

import com.github.ggndnn.sofa.ark.execution.model.SofaArkClasspathEntryModel;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkConstants;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.vfs.VirtualFile;

import java.net.URI;

public class MavenModuleClasspathEntry implements SofaArkClasspathEntry {
    private SofaArkId id;

    private Module module;

    public MavenModuleClasspathEntry(SofaArkId id, Module module) {
        this.id = id;
        this.module = module;
    }

    @Override
    public SofaArkId getId() {
        return id;
    }

    @Override
    public SofaArkClasspathEntryModel createModel() {
        CompilerModuleExtension ext = CompilerModuleExtension.getInstance(module);
        if (ext != null && ext.getCompilerOutputPath() != null) {
            SofaArkClasspathEntryModel model = new VirtualFileClasspathEntry(id, ext.getCompilerOutputPath()).createModel();
            model.setType(SofaArkConstants.CLASS_PATH_ENTRY_TYPE_PROJECT);
            return model;
        } else if (ext != null && ext.getCompilerOutputUrl() != null) {
            SofaArkClasspathEntryModel model = new SofaArkClasspathEntryModel();
            model.setId(id);
            model.setType(SofaArkConstants.CLASS_PATH_ENTRY_TYPE_PROJECT);
            String url = ext.getCompilerOutputUrl();
            if (!url.endsWith("/")) {
                url += "/";
            }
            URI uri = URI.create(url);
            model.setPath(uri.toString());
            return model;
        } else {
            throw new IllegalStateException("missing output directory");
        }
    }

    @Override
    public boolean hasResource(String path) {
        // TODO 只支持在Idea中查找资源，即VirtualFile
        VirtualFile output = getOutput();
        if (output != null) {
            VirtualFileClasspathEntry delegate = new VirtualFileClasspathEntry(id, output);
            return delegate.hasResource(path);
        }
        return false;
    }

    private VirtualFile getOutput() {
        CompilerModuleExtension ext = CompilerModuleExtension.getInstance(module);
        if (ext != null) {
            return ext.getCompilerOutputPath();
        }
        return null;
    }
}
