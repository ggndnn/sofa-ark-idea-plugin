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
package com.github.ggndnn.sofa.ark.components;

import com.github.ggndnn.sofa.ark.execution.model.SofaArkId;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.model.MavenArtifactState;
import org.jetbrains.idea.maven.model.MavenConstants;

import java.util.Set;
import java.util.stream.Collectors;

class NormalArtifactNode implements ArtifactNode {
    SofaArkId id;

    MavenArtifactNode node;

    Set<ArtifactNode> dependencies;

    NormalArtifactNode(MavenArtifactNode node) {
        this.node = node;
        MavenArtifact artifact = this.node.getArtifact();
        // TODO ...
        this.id = new SofaArkId();
        this.id.setArtifactId(artifact.getArtifactId());
        this.id.setGroupId(artifact.getGroupId());
        this.id.setVersion(artifact.getBaseVersion());
        this.dependencies = node.getDependencies()
                .stream()
                .map(NormalArtifactNode::new)
                .collect(Collectors.toSet());
    }

    @Override
    public SofaArkId getId() {
        return id;
    }

    @Override
    public boolean isDuplicate() {
        return MavenArtifactState.DUPLICATE.equals(node.getState());
    }

    @Override
    public boolean isValid() {
        return MavenArtifactState.ADDED.equals(node.getState())
                || isDuplicate();
    }

    @Override
    public boolean isInTestScope() {
        return MavenConstants.SCOPE_TEST.equals(node.getOriginalScope());
    }

    @Override
    public boolean isInProductionScope() {
        return MavenConstants.SCOPE_COMPILE.equals(node.getOriginalScope())
                || MavenConstants.SCOPE_RUNTIME.equals(node.getOriginalScope());
    }

    @Override
    public Set<ArtifactNode> getDependencies() {
        return dependencies;
    }

    @Override
    public VirtualFile getFile() {
        VirtualFile vf = VfsUtil.findFileByIoFile(node.getArtifact().getFile(), false);
        if (vf != null) {
            return JarFileSystem.getInstance().getRootByLocal(vf);
        }
        return null;
    }
}
