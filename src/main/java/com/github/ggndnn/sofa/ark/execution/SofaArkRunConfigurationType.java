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

import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import org.jetbrains.annotations.NotNull;

public class SofaArkRunConfigurationType extends SimpleConfigurationType implements ConfigurationType {
    @NotNull
    public static SofaArkRunConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(SofaArkRunConfigurationType.class);
    }

    public SofaArkRunConfigurationType() {
        super("SofaArkApplication", "SofaArk Application",
                "Configuration to run  SofaArk application",
                NotNullLazyValue.createValue(() -> AllIcons.FileTypes.Archive));
    }

    @Override
    @NotNull
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new SofaArkRunConfiguration(project, this, "");
    }

    @Override
    public String getHelpTopic() {
        // TODO ...
        return super.getHelpTopic();
    }
}