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
import com.github.ggndnn.sofa.ark.execution.model.SofaArkConstants;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkId;
import com.github.ggndnn.sofa.ark.utils.SofaArkArchiveHelper;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Manifest;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

public class VirtualFileSofaArkBiz extends VirtualFileSofaArkBase<SofaArkBizModel> implements SofaArkBiz {
    private SimpleSofaArkBiz delegate;

    VirtualFileSofaArkBiz(SofaArkId id, VirtualFile file) {
        super(id, file);
    }

    @Override
    SofaArkBizModel createMetadata(VirtualFile file) {
        return new SofaArkBizModel();
    }

    @Override
    void postProcessMetadata(SofaArkBizModel metadata) {
        try {
            Manifest manifest = super.getManifest();
            if (manifest == null)
                throw new IllegalStateException();
            String priorityValue = manifest.getMainAttributes().getValue(PRIORITY_ATTRIBUTE);
            if (priorityValue != null) {
                metadata.setPriority(Integer.parseInt(priorityValue));
            }
            String mainClass = manifest.getMainAttributes().getValue(MAIN_CLASS_ATTRIBUTE);
            if (mainClass != null) {
                metadata.setMainClass(mainClass);
            }
            metadata.setBizName(manifest.getMainAttributes().getValue(ARK_BIZ_NAME));
            metadata.setBizVersion(manifest.getMainAttributes().getValue(ARK_BIZ_VERSION));
            metadata.setWebContextPath(manifest.getMainAttributes().getValue(WEB_CONTEXT_PATH));

            delegate = new SimpleSofaArkBiz(metadata);
            delegate.setDenyImportPackages(metadata, attributeToSet(manifest, DENY_IMPORT_PACKAGES));
            delegate.setDenyImportClasses(metadata, attributeToSet(manifest, DENY_IMPORT_CLASSES));
            metadata.setDenyImportResources(manifest.getMainAttributes().getValue(DENY_IMPORT_RESOURCES));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> attributeToSet(Manifest manifest, String attributeName) {
        String attributeValue = manifest.getMainAttributes().getValue(attributeName);
        if (attributeValue == null)
            return null;
        return new LinkedHashSet<>(Arrays.asList(attributeValue.split(",")));
    }

    public static boolean isBiz(VirtualFile file) {
        return VfsUtil.findRelativeFile(file, SofaArkConstants.SOFA_ARK_BIZ_MARK.split("/")) != null;
    }

    public static VirtualFileSofaArkBiz createFromIdAndFile(SofaArkId id, VirtualFile vf) {
        if (id == null) {
            return createFromFile(vf);
        }
        if (vf != null) {
            return new VirtualFileSofaArkBiz(id, vf);
        }
        return null;
    }

    public static VirtualFileSofaArkBiz createFromFile(VirtualFile vf) {
        SofaArkId id = SofaArkArchiveHelper.loadSofaArkIdFromFile(vf);
        if (id == null)
            return null;
        return createFromIdAndFile(id, vf);
    }

    @Override
    public Set<String> getDenyImportPackages() {
        return delegate.getDenyImportPackages();
    }

    @Override
    public Set<String> getDenyImportClasses() {
        return delegate.getDenyImportClasses();
    }

    @Override
    public Set<String> getDenyImportPackageStems() {
        return delegate.getDenyImportPackageStems();
    }

    @Override
    public Integer getPriority() {
        return delegate.getPriority();
    }
}
