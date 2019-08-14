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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.function.Consumer;

abstract class NewPluginOrBizAction extends DumbAwareAction {
    private JComponent parentComponent;

    private LibraryEditor editor;

    private Project project;

    private Consumer<LibraryEditor> callback;

    NewPluginOrBizAction(String title, Icon icon, JComponent parentComponent, LibraryEditor editor, Project project, Consumer<LibraryEditor> callback) {
        super(title, null, icon);
        this.parentComponent = parentComponent;
        this.editor = editor;
        this.project = project;
        this.callback = callback;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        NewLibraryConfiguration configuration = createConfiguration(project, parentComponent);
        if (configuration != null) {
            configuration.addRoots(editor);
            if (callback != null) {
                callback.accept(editor);
            }
        }
    }

    abstract NewLibraryConfiguration createConfiguration(Project project, JComponent parentComponent);
}
