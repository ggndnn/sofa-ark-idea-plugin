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

import java.io.Serializable;

public class SofaArkId implements Serializable {
    private String groupId;

    private String artifactId;

    private String version;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public static SofaArkId parseSofaArkId(String text) {
        SofaArkId result = new SofaArkId();
        if (text == null) {
            result.groupId = result.artifactId = result.version = null;
        } else {
            String[] parts = text.split(":");
            result.groupId = parts.length > 0 ? parts[0] : null;
            result.artifactId = parts.length > 1 ? parts[1] : null;
            result.version = parts.length > 2 ? parts[2] : null;
        }
        return result;
    }

    public static SofaArkId parseSofaArkIdIgnoreVersion(String text) {
        SofaArkId id = parseSofaArkId(text);
        id.setVersion(null);
        return id;
    }

    private static void append(StringBuilder builder, String part) {
        if (builder.length() != 0) {
            builder.append(':');
        }
        builder.append(part == null ? "<unknown>" : part);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        append(builder, groupId);
        append(builder, artifactId);
        append(builder, version);
        return builder.toString();
    }

    public SofaArkId toIdIgnoreVersion() {
        SofaArkId id = new SofaArkId();
        id.setGroupId(groupId);
        id.setArtifactId(artifactId);
        return id;
    }

    public SofaArkId toIdIgnoreGroup() {
        SofaArkId id = new SofaArkId();
        id.setArtifactId(artifactId);
        id.setVersion(version);
        return id;
    }

    public SofaArkId toIdIgnoreGroupAndVersion() {
        SofaArkId id = new SofaArkId();
        id.setArtifactId(artifactId);
        return id;
    }

    public boolean isSameIgnoreVersion(SofaArkId that) {
        if (this.artifactId != null) {
            if (!this.artifactId.equals(that.artifactId)) {
                return false;
            }
        } else if (that.artifactId != null) {
            return false;
        }
        if (this.groupId != null) {
            if (!this.groupId.equals(that.groupId)) {
                return false;
            }
        } else if (that.groupId != null) {
            return false;
        }
        return true;
    }

    private boolean isSame(SofaArkId that) {
        if (!this.isSameIgnoreVersion(that)) {
            return false;
        } else {
            if (this.version != null) {
                if (!this.version.equals(that.version)) {
                    return false;
                }
            } else if (that.version != null) {
                return false;
            }
            return true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            SofaArkId that = (SofaArkId) o;
            return this.isSame(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = this.groupId != null ? this.groupId.hashCode() : 0;
        result = 31 * result + (this.artifactId != null ? this.artifactId.hashCode() : 0);
        result = 31 * result + (this.version != null ? this.version.hashCode() : 0);
        return result;
    }
}
