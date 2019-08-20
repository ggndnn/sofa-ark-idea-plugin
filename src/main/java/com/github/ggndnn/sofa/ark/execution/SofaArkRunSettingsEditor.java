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

import com.intellij.application.options.ModulesComboBox;
import com.intellij.execution.ui.CommonJavaParametersPanel;
import com.intellij.execution.ui.DefaultJreSelector;
import com.intellij.execution.ui.JrePathEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.*;

/**
 * SofaArkRunSettingsEditor
 *
 * @author ggndnn
 */
public class SofaArkRunSettingsEditor extends SettingsEditor<SofaArkRunConfiguration> implements PanelWithAnchor {
    private final CommonJavaParametersPanel commonJavaParametersPanel;

    private final LabeledComponent<ModulesComboBox> modulesComponent;

    private final JrePathEditor jrePathEditor;

    private JComponent editorAnchor;

    private final JPanel mainPanel;

    public SofaArkRunSettingsEditor(Project project) {
        commonJavaParametersPanel = new CommonJavaParametersPanel();
        modulesComponent = new LabeledComponent<>();
        modulesComponent.setLabelLocation(BorderLayout.WEST);
        modulesComponent.setComponent(new ModulesComboBox());
        modulesComponent.setText("Search sources using module's classpath");
        ModulesComboBox modulesComboBox = modulesComponent.getComponent();
        modulesComboBox.allowEmptySelection("<whole project>");
        modulesComboBox.fillModules(project);
        jrePathEditor = new JrePathEditor(DefaultJreSelector.projectSdk(project));
        editorAnchor = UIUtil.mergeComponentsWithAnchor(commonJavaParametersPanel, jrePathEditor);

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(commonJavaParametersPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, NORTHWEST, BOTH, JBInsets.create(12, 0), 0, 0));
        mainPanel.add(modulesComponent, new GridBagConstraints(RELATIVE, 1, 1, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL, JBUI.emptyInsets(), 0, 0));
        mainPanel.add(jrePathEditor, new GridBagConstraints(RELATIVE, 2, 1, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL, JBUI.insetsTop(12), 0, 0));
    }

    @Override
    protected void resetEditorFrom(@NotNull SofaArkRunConfiguration configuration) {
        commonJavaParametersPanel.reset(configuration);
        jrePathEditor.setPathOrName(configuration.getAlternativeJrePath(), configuration.isAlternativeJrePathEnabled());
        modulesComponent.getComponent().setSelectedModule(configuration.getModule());
    }

    @Override
    protected void applyEditorTo(@NotNull SofaArkRunConfiguration configuration) throws ConfigurationException {
        commonJavaParametersPanel.applyTo(configuration);
        configuration.setAlternativeJrePath(jrePathEditor.getJrePathOrName());
        configuration.setAlternativeJrePathEnabled(jrePathEditor.isAlternativeJreSelected());
        configuration.setModule(modulesComponent.getComponent().getSelectedModule());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return mainPanel;
    }

    @Override
    public JComponent getAnchor() {
        return editorAnchor;
    }

    @Override
    public void setAnchor(@Nullable JComponent anchor) {
        this.editorAnchor = anchor;
        this.commonJavaParametersPanel.setAnchor(anchor);
        this.jrePathEditor.setAnchor(anchor);
        this.modulesComponent.setAnchor(anchor);
    }
}
