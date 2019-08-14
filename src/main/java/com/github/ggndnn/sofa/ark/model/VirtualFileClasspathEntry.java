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
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

public class VirtualFileClasspathEntry implements SofaArkClasspathEntry {
    private SofaArkId id;

    private VirtualFile vf;

    public VirtualFileClasspathEntry(SofaArkId id, VirtualFile vf) {
        this.id = id;
        this.vf = vf;
    }

    @Override
    public SofaArkId getId() {
        return id;
    }

    @Override
    public SofaArkClasspathEntryModel createModel() {
        SofaArkClasspathEntryModel model = new SofaArkClasspathEntryModel();
        model.setId(id);
        model.setType(SofaArkConstants.CLASS_PATH_ENTRY_TYPE_FILE);
        VirtualFile localVf = VfsUtil.getLocalFile(vf);
        model.setPath(localVf.getCanonicalPath());
        return model;
    }

    @Override
    public boolean hasResource(String path) {
        VirtualFile child = vf.findFileByRelativePath(path);
        return child != null;
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
