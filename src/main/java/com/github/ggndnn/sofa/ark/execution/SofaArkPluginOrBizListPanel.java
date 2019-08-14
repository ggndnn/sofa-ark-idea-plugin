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

import com.github.ggndnn.sofa.ark.components.SofaArkManager;
import com.github.ggndnn.sofa.ark.execution.model.SofaArkId;
import com.github.ggndnn.sofa.ark.model.SofaArkBase;
import com.github.ggndnn.sofa.ark.model.VirtualFileSofaArkBiz;
import com.github.ggndnn.sofa.ark.model.VirtualFileSofaArkPlugin;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ui.componentsList.components.ScrollablePanel;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.NewLibraryEditor;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.table.TableView;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SofaArkPluginOrBizListPanel extends SettingsEditor<SofaArkRunConfiguration> {
    private final TableView<PluginOrBizModel> pluginOrBizTable;

    private final ListTableModel<PluginOrBizModel> pluginOrBizTableModel;

    private final JPanel mainPanel;

    public SofaArkPluginOrBizListPanel(final Project project) {
        PluginOrBizColumnInfo pluginOrBizColumn = new PluginOrBizColumnInfo();
        PluginOrBizEnabledColumnInfo pluginOrBizActiveColumn = new PluginOrBizEnabledColumnInfo();
        pluginOrBizTableModel = new ListTableModel<>(pluginOrBizColumn, pluginOrBizActiveColumn);
        pluginOrBizTable = new TableView<>(pluginOrBizTableModel);
        pluginOrBizTable.getEmptyText().setText("no plugin or biz");
        pluginOrBizTable.setColumnSelectionAllowed(false);
        pluginOrBizTable.setShowGrid(false);
        pluginOrBizTable.setDragEnabled(false);
        pluginOrBizTable.setShowHorizontalLines(false);
        pluginOrBizTable.setShowVerticalLines(false);
        pluginOrBizTable.setIntercellSpacing(new Dimension(0, 0));

        final JTableHeader tableHeader = pluginOrBizTable.getTableHeader();
        final FontMetrics fontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());
        int preferredWidth = fontMetrics.stringWidth(pluginOrBizActiveColumn.getName()) + 20;
        TableColumn enabledColumn = tableHeader.getColumnModel().getColumn(1);
        enabledColumn.setWidth(preferredWidth);
        enabledColumn.setPreferredWidth(preferredWidth);
        enabledColumn.setMinWidth(preferredWidth);
        enabledColumn.setMaxWidth(preferredWidth);

        Component decorator = ToolbarDecorator.createDecorator(pluginOrBizTable)
                .setAddAction(b -> {
                    Consumer<LibraryEditor> callback = (editor) -> {
                        VirtualFile[] vfs = editor.getFiles(OrderRootType.CLASSES);
                        List<PluginOrBizModel> items = new ArrayList<>(pluginOrBizTableModel.getItems());
                        items.addAll(Stream.of(vfs)
                                .map(vf -> {
                                    PluginOrBizModel m = null;
                                    if (VirtualFileSofaArkBiz.isBiz(vf)) {
                                        VirtualFileSofaArkBiz biz = VirtualFileSofaArkBiz.createFromFile(vf);
                                        if (biz != null) {
                                            m = convertPluginOrBiz(biz);
                                            m.classifier = PluginOrBizModel.CLASSIFIER_BIZ;
                                        }
                                    } else if (VirtualFileSofaArkPlugin.isPlugin(vf)) {
                                        VirtualFileSofaArkPlugin plugin = VirtualFileSofaArkPlugin.createFromFile(vf);
                                        if (plugin != null) {
                                            m = convertPluginOrBiz(plugin);
                                            m.classifier = PluginOrBizModel.CLASSIFIER_PLUGIN;
                                        }
                                    }
                                    if (m != null) {
                                        m.type = PluginOrBizModel.TYPE_JAR;
                                        m.status = PluginOrBizModel.STATUS_ADDED;
                                        VirtualFile lvf = VfsUtil.getLocalFile(vf);
                                        m.location = lvf.getCanonicalPath();
                                    }
                                    return m;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()));
                        pluginOrBizTableModel.setItems(items);
                        // fireEditorStateChanged();
                    };
                    NewLibraryEditor editor = new NewLibraryEditor();
                    DefaultActionGroup addActionGroup = new DefaultActionGroup();
                    addActionGroup.add(new NewPluginOrBizDefaultAction(pluginOrBizTable, editor, project, callback));
                    addActionGroup.add(new NewPluginOrBizFromMavenAction(pluginOrBizTable, editor, project, callback));
                    ListPopup popup = JBPopupFactory.getInstance()
                            .createActionGroupPopup("", addActionGroup, b.getDataContext(), JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING, false, null, 10);
                    RelativePoint point = b.getPreferredPopupPoint();
                    if (point != null) {
                        popup.show(point);
                    } else {
                        popup.show(b.getContextComponent());
                    }
                    popup.addListener(new JBPopupListener() {
                        @Override
                        public void onClosed(@NotNull LightweightWindowEvent event) {
                        }
                    });
                })
                .disableUpDownActions()
                .createPanel();
        JPanel tableScrollPanel = new ScrollablePanel(new BorderLayout());
        tableScrollPanel.add(decorator, BorderLayout.CENTER);
        mainPanel = new JPanel(new GridLayout());
        mainPanel.add(tableScrollPanel, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 7, new Dimension(-1, 150), new Dimension(-1, 300), null));
    }

    @Override
    protected void resetEditorFrom(@NotNull SofaArkRunConfiguration configuration) {
        // TODO ...
        configuration = configuration.clone();
        List<PluginOrBizModel> resolvedPluginOrModels = resolvePluginOrBizModels(configuration);
        pluginOrBizTableModel.setItems(resolvedPluginOrModels);
    }

    @Override
    protected void applyEditorTo(@NotNull SofaArkRunConfiguration configuration) throws ConfigurationException {
        pluginOrBizTable.stopEditing();
        List<PluginOrBizModel> models = pluginOrBizTableModel.getItems();
        configuration.configBean.pluginOrBizModels = models.toArray(new PluginOrBizModel[0]);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return mainPanel;
    }

    static List<PluginOrBizModel> resolvePluginOrBizModels(@NotNull SofaArkRunConfiguration configuration) {
        Project project = configuration.getProject();
        PluginOrBizModel[] pluginOrBizModels = configuration.configBean.pluginOrBizModels;
        SofaArkManager mgr = project.getComponent(SofaArkManager.class);
        List<PluginOrBizModel> resolvedPluginOrModels = new ArrayList<>();
        Map<SofaArkId, PluginOrBizModel> pluginOrBizInWorkspace = new LinkedHashMap<>();
        mgr.getAllBiz().forEach(biz -> {
            PluginOrBizModel m = convertPluginOrBiz((SofaArkBase<?>) biz);
            m.type = PluginOrBizModel.TYPE_WORKSPACE;
            m.classifier = PluginOrBizModel.CLASSIFIER_BIZ;
            m.enabled = true;
            m.status = PluginOrBizModel.STATUS_ADDED;

            SofaArkId id = new SofaArkId();
            id.setGroupId(m.groupId);
            id.setArtifactId(m.artifactId);
            id.setVersion(m.version);
            pluginOrBizInWorkspace.put(id, m);

            resolvedPluginOrModels.add(m);
        });
        mgr.getAllPlugins().forEach(plugin -> {
            PluginOrBizModel m = convertPluginOrBiz((SofaArkBase<?>) plugin);
            m.type = PluginOrBizModel.TYPE_WORKSPACE;
            m.classifier = PluginOrBizModel.CLASSIFIER_PLUGIN;
            m.enabled = true;
            m.status = PluginOrBizModel.STATUS_ADDED;

            SofaArkId id = new SofaArkId();
            id.setGroupId(m.groupId);
            id.setArtifactId(m.artifactId);
            id.setVersion(m.version);
            pluginOrBizInWorkspace.put(id, m);

            resolvedPluginOrModels.add(m);
        });
        if (pluginOrBizModels != null) {
            List<PluginOrBizModel> unresolvedPluginOrBizModels = Stream.of(pluginOrBizModels)
                    .filter(m -> {
                        SofaArkId id = new SofaArkId();
                        id.setGroupId(m.groupId);
                        id.setArtifactId(m.artifactId);
                        id.setVersion(m.version);

                        PluginOrBizModel newModel = pluginOrBizInWorkspace.get(id);
                        if (newModel != null) {
                            newModel.enabled = m.enabled;
                            return false;
                        } else {
                            return true;
                        }
                    })
                    .collect(Collectors.toList());
            File localRepo = MavenProjectsManager.getInstance(project).getLocalRepository();
            unresolvedPluginOrBizModels = unresolvedPluginOrBizModels
                    .stream()
                    .filter(m -> {
                        MavenId mavenId = new MavenId(m.groupId, m.artifactId, m.version);
                        if (MavenArtifactUtil.hasArtifactFile(localRepo, mavenId, "jar")) {
                            File f = MavenArtifactUtil.getArtifactFile(localRepo, m.groupId, m.artifactId, m.version, "jar");
                            VirtualFile vf = VfsUtil.findFileByIoFile(f, false);
                            if (vf != null) {
                                if (VirtualFileSofaArkBiz.isBiz(vf)) {
                                    m.classifier = PluginOrBizModel.CLASSIFIER_BIZ;
                                } else if (VirtualFileSofaArkPlugin.isPlugin(vf)) {
                                    m.classifier = PluginOrBizModel.CLASSIFIER_PLUGIN;
                                }
                                m.type = PluginOrBizModel.TYPE_JAR;
                                m.status = PluginOrBizModel.STATUS_ADDED;
                                resolvedPluginOrModels.add(m);
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.toList());
            unresolvedPluginOrBizModels.forEach(m -> {
                m.type = PluginOrBizModel.TYPE_JAR;
                m.status = PluginOrBizModel.STATUS_MISSING;
                m.enabled = false;
                resolvedPluginOrModels.add(m);
            });
        }
        return resolvedPluginOrModels;
    }

    private static PluginOrBizModel convertPluginOrBiz(SofaArkBase<?> pluginOrBiz) {
        PluginOrBizModel result = new PluginOrBizModel();
        SofaArkId id = pluginOrBiz.getMetadata().getId();
        result.groupId = id.getGroupId();
        result.artifactId = id.getArtifactId();
        result.version = id.getVersion();
        return result;
    }

    private static class PluginOrBizColumnInfo extends ColumnInfo<PluginOrBizModel, String> {
        PluginOrBizColumnInfo() {
            super("Name");
        }

        @Override
        public String valueOf(final PluginOrBizModel pluginOrBiz) {
            String value = pluginOrBiz.artifactId + "-" + pluginOrBiz.version;
            if (pluginOrBiz.classifier != null) {
                value += " (" + pluginOrBiz.classifier + ")";
            }
            if(PluginOrBizModel.STATUS_MISSING.equals(pluginOrBiz.status)) {
                value += " (missing)";
            }
            return value;
        }
    }

    private static class PluginOrBizEnabledColumnInfo extends ColumnInfo<PluginOrBizModel, Boolean> {
        PluginOrBizEnabledColumnInfo() {
            super("Enabled");
        }

        @Override
        public Class getColumnClass() {
            return Boolean.class;
        }

        @Override
        public boolean isCellEditable(PluginOrBizModel item) {
            return true;
        }

        @Nullable
        @Override
        public TableCellRenderer getRenderer(PluginOrBizModel item) {
            return new BooleanTableCellRenderer();
        }

        @Nullable
        @Override
        public Boolean valueOf(PluginOrBizModel sofaArkBase) {
            return sofaArkBase.enabled;
        }

        @Override
        public void setValue(PluginOrBizModel element, Boolean enabled) {
            element.enabled = enabled;
        }
    }
}
