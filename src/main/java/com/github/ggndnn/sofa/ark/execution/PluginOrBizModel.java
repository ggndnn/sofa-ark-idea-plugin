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

public class PluginOrBizModel implements Cloneable {
    public final static String TYPE_WORKSPACE = "workspace";

    public final static String TYPE_WORKSPACE_JAR = "workspace_jar";

    public final static String TYPE_JAR = "jar";

    public final static String CLASSIFIER_PLUGIN = "plugin";

    public final static String CLASSIFIER_BIZ = "biz";

    public final static String STATUS_ADDED = "added";

    public final static String STATUS_MISSING = "missing";

    public String groupId;

    public String artifactId;

    public String version;

    public String type;

    public String classifier;

    public String status;

    public boolean enabled;

    public String location;

    @Override
    public PluginOrBizModel clone() {
        try {
            PluginOrBizModel newModel = (PluginOrBizModel) super.clone();
            newModel.groupId = groupId;
            newModel.artifactId = artifactId;
            newModel.version = version;
            newModel.classifier = classifier;
            newModel.status = status;
            newModel.type = type;
            newModel.enabled = enabled;
            newModel.location = location;
            return newModel;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
