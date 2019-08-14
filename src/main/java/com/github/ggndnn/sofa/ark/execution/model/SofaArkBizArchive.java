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

import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Manifest;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

public class SofaArkBizArchive extends SofaArkProjectArchive implements BizArchive {
    public SofaArkBizArchive(SofaArkBizModel bizModel) {
        super(bizModel);
        Manifest manifest = getManifest();
        manifest.getMainAttributes().putValue(MAIN_CLASS_ATTRIBUTE, bizModel.getMainClass());
        manifest.getMainAttributes().putValue(PRIORITY_ATTRIBUTE,
                String.valueOf(bizModel.getPriority()));
        manifest.getMainAttributes().putValue(ARK_BIZ_NAME, bizModel.getBizName());
        manifest.getMainAttributes().putValue(ARK_BIZ_VERSION, bizModel.getBizVersion());
        manifest.getMainAttributes().putValue(WEB_CONTEXT_PATH, bizModel.getWebContextPath());
        manifest.getMainAttributes().putValue(DENY_IMPORT_CLASSES, bizModel.getDenyImportClasses());
        manifest.getMainAttributes().putValue(DENY_IMPORT_PACKAGES, bizModel.getDenyImportPackages());
        manifest.getMainAttributes().putValue(DENY_IMPORT_RESOURCES, bizModel.getDenyImportResources());
    }

    @Override
    public URL[] getUrls() throws IOException {
        return getClasspath();
    }

    @Override
    public boolean isEntryExist(EntryFilter filter) {
        return filter.matches(new Entry() {
            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public String getName() {
                return Constants.ARK_BIZ_MARK_ENTRY;
            }
        });
    }
}
