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
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.maven.project.MavenProject;

import java.util.Set;
import java.util.stream.Collectors;

class MavenProjectNode implements ArtifactNode {
    SofaArkId id;

    MavenProject maven;

    Set<ArtifactNode> dependencies;

    MavenProjectNode(MavenProject maven) {
        this.maven = maven;
        this.id = SofaArkId.parseSofaArkId(maven.getMavenId().toString());
        this.dependencies = maven.getDependencyTree()
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
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isInTestScope() {
        return false;
    }

    @Override
    public boolean isInProductionScope() {
        return true;
    }

    @Override
    public Set<ArtifactNode> getDependencies() {
        return dependencies;
    }

    @Override
    public VirtualFile getFile() {
        return null;
    }
}
