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

import com.alipay.sofa.ark.loader.JarBizArchive;
import com.alipay.sofa.ark.spi.archive.Archive;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

abstract class SofaArkProjectArchive implements Archive {
    private SofaArkModel model;

    private URL[] classpath;

    private final Manifest manifest = new Manifest();

    protected SofaArkProjectArchive(SofaArkModel model) {
        this.model = model;
        this.classpath = createClasspath(model);

    }

    @Override
    public URL getUrl() throws MalformedURLException {
        return URI.create(model.getLocation()).toURL();
    }

    @Override
    public List<Archive> getNestedArchives(EntryFilter filter) throws IOException {
        throw new UnsupportedOperationException("unreachable");
    }

    @Override
    public Archive getNestedArchive(Entry entry) throws IOException {
        // TODO ...
        if (Constants.ARK_BIZ_MARK_ENTRY.equals(entry.getName())) {
            return new NullBizArchive();
        }
        throw new UnsupportedOperationException("unreachable");
    }

    @Override
    public InputStream getInputStream(ZipEntry zipEntry) throws IOException {
        throw new UnsupportedOperationException("unreachable");
    }

    @Override
    public Iterator<Entry> iterator() {
        throw new UnsupportedOperationException("unreachable");
    }

    @Override
    public Manifest getManifest() {
        return manifest;
    }

    URL[] getClasspath() {
        return classpath;
    }

    private URL[] createClasspath(SofaArkModel model) {
        return model.getClasspath()
                .stream()
                .map(cp -> {
                    URI uri;
                    try {
                        uri = URI.create(cp.getPath());
                        if (!uri.isAbsolute()) {
                            uri = null;
                        }
                    } catch (IllegalArgumentException e) {
                        uri = null;
                    }
                    if (uri == null) {
                        uri = new File(cp.getPath()).toURI();
                    }
                    try {
                        return uri.toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(URL[]::new);
    }

    class NullBizArchive extends JarBizArchive {
        public NullBizArchive() {
            super(null);
        }
    }
}