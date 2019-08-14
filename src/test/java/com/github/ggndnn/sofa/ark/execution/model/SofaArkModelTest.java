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

import com.intellij.openapi.util.io.FileUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class SofaArkModelTest {
    @Test
    public void testBizModelReadAndWrite() throws IOException {
        File baseDir = FileUtil.createTempDirectory("sofark", "sofark");
        try {
            SofaArkBizModel bizModel = new SofaArkBizModel();
            bizModel.setBizName("testbiz");
            bizModel.setBizVersion("1.0");
            bizModel.setMainClass("com.x.x.T");
            bizModel.setId(SofaArkId.parseSofaArkId("test:test:1.2"));
            File location = new File(baseDir, bizModel.getBizName());
            bizModel.setLocation(location.getAbsolutePath());
            bizModel.setPriority(100);
            bizModel.setType(10);
            bizModel.setDenyImportPackages("org.x,org.y,com.a.*");
            try (OutputStream output = new FileOutputStream(new File(baseDir, bizModel.getBizName() + ".biz"))) {
                bizModel.write(output);
            }
            try (InputStream input = new FileInputStream(new File(baseDir, bizModel.getBizName() + ".biz"))) {
                SofaArkBizModel newBizModel = new SofaArkBizModel();
                newBizModel.read(input);
                Assert.assertEquals(bizModel.getBizName(), newBizModel.getBizName());
                Assert.assertEquals(bizModel.getDenyImportClasses(), newBizModel.getDenyImportClasses());
                Assert.assertEquals(bizModel.getPriority(), newBizModel.getPriority());
            }
        } finally {
            baseDir.delete();
        }
    }
}
