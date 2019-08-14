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
package com.github.ggndnn.sofa.ark.utils;

import com.github.ggndnn.sofa.ark.execution.model.SofaArkId;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public abstract class SofaArkArchiveHelper {
    public static SofaArkId loadSofaArkIdFromFile(VirtualFile vf) {
        SofaArkId id = loadSofaArkIdFromManifest(vf);
        if (id == null) {
            id = loadSofaArkIdFromMavenFile(vf);
        }
        return id;
    }

    private static SofaArkId loadSofaArkIdFromManifest(VirtualFile vf) {
        VirtualFile manifestFile = VfsUtil.findRelativeFile("META-INF/MANIFEST.MF", vf);
        if (manifestFile == null)
            return null;
        try (InputStream in = manifestFile.getInputStream()) {
            Manifest manifest = new Manifest(in);
            Attributes attributes = manifest.getMainAttributes();
            SofaArkId id = new SofaArkId();
            id.setGroupId(attributes.getValue("groupId"));
            id.setArtifactId(attributes.getValue("artifactId"));
            id.setVersion(attributes.getValue("version"));
            if (id.getGroupId() != null
                    && id.getArtifactId() != null
                    && id.getVersion() != null) {
                return id;
            }
        } catch (IOException e) {
            // TODO log...
        }
        return null;
    }

    private static SofaArkId loadSofaArkIdFromMavenFile(VirtualFile vf) {
        VirtualFile mavenDir = VfsUtil.findRelativeFile("META-INF/maven", vf);
        if (mavenDir == null) {
            return null;
        }
        List<VirtualFile> mavenDirChildren = VfsUtil.collectChildrenRecursively(mavenDir);
        VirtualFile pomPropsFile = null;
        for (VirtualFile child : mavenDirChildren) {
            if (child.getName().equals("pom.properties")) {
                pomPropsFile = child;
                break;
            }
        }
        if (pomPropsFile == null)
            return null;
        try (InputStream in = pomPropsFile.getInputStream()) {
            Properties p = new Properties();
            p.load(in);
            if (p.containsKey("groupId") && p.containsKey("artifactId") && p.containsKey("version")) {
                SofaArkId id = new SofaArkId();
                id.setGroupId(p.getProperty("groupId"));
                id.setArtifactId(p.getProperty("artifactId"));
                id.setVersion(p.getProperty("version"));
                return id;
            }
        } catch (IOException e) {
            // TODO ...
        }
        return null;
    }
}
