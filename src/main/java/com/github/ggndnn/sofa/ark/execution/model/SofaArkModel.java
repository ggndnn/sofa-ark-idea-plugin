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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Set;

public abstract class SofaArkModel implements SofaArkSerializable {
    private SofaArkId id;

    private int type;

    private String location;

    private int priority;

    private Set<SofaArkClasspathEntryModel> classpath;

    public SofaArkId getId() {
        return id;
    }

    public void setId(SofaArkId id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Set<SofaArkClasspathEntryModel> getClasspath() {
        return classpath;
    }

    public void setClasspath(Set<SofaArkClasspathEntryModel> classpath) {
        this.classpath = classpath;
    }

    @Override
    public void write(OutputStream output) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(output)) {
            oos.writeObject(this);
        }
    }
}
