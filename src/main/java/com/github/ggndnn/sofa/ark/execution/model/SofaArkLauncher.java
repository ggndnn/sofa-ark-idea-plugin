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

import com.alipay.sofa.ark.container.ArkContainer;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.argument.LaunchCommand;

import java.io.File;

public class SofaArkLauncher {
    public static void main(String[] args) throws Exception {
        File userDir = new File(System.getProperty("user.dir"));
        File workingDir = new File(userDir, ".sofa-ark");
        if (!workingDir.exists()) {
            workingDir = userDir;
        }
        LaunchCommand launchCommand = LaunchCommand.parse(args);
        ExecutableArchive archive = new SofaArckClassPathArchive(workingDir, launchCommand.getClasspath());
        new ArkContainer(archive, launchCommand).start();
    }
}
