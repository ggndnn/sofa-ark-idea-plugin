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
package com.github.ggndnn.sofa.ark.execution;

import com.intellij.jarRepository.JarRepositoryManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import icons.MavenIcons;

import javax.swing.*;
import java.util.function.Consumer;

class NewPluginOrBizFromMavenAction extends NewPluginOrBizAction {
    NewPluginOrBizFromMavenAction(JComponent parentComponent, LibraryEditor editor, Project project, Consumer<LibraryEditor> callback) {
        super("Maven", MavenIcons.MavenProject, parentComponent, editor, project, callback);
    }

    @Override
    NewLibraryConfiguration createConfiguration(Project project, JComponent parentComponent) {
        return JarRepositoryManager.chooseLibraryAndDownload(project, null, parentComponent);
    }
}
