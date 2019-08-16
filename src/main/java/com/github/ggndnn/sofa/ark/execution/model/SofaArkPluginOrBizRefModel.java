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

import java.io.*;

public class SofaArkPluginOrBizRefModel implements SofaArkSerializable {
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public void read(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        location = reader.readLine();
    }

    @Override
    public void write(OutputStream output) throws IOException {
        if (location != null) {
            OutputStreamWriter writer = new OutputStreamWriter(output);
            writer.write(location);
            writer.write("\n");
            writer.flush();
        }
    }
}
