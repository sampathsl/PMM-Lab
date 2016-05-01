/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   24.11.2011 (hofer): created
 */
package de.bund.bfr.knime.pmm.fskx.rsnippet;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ViewUtils;
import org.knime.core.node.workflow.FlowVariable;

import de.bund.bfr.knime.pmm.fskx.template.DefaultTemplateController;
import de.bund.bfr.knime.pmm.fskx.template.TemplatesPanel;

/**
 * The dialog of the R nodes.
 *
 * @author Heiko Hofer.
 * @author Jonathan Hale.
 */
class RSnippetNodeDialog extends DataAwareNodeDialogPane {

    private static final String SNIPPET_TAB = "R Snippet";

    private final RSnippetNodePanel m_panel;
    private DefaultTemplateController m_templatesController;

    private final Class<?> m_templateMetaCategory;
    private final RSnippetNodeConfig m_config;
    private int m_tableInPort;
    private int m_tableOutPort;

    private JCheckBox m_outNonNumbersAsMissing;

    /**
     * Create a new Dialog.
     *
     * @param templateMetaCategory the meta category used in the templates tab or to create templates.
     */
    RSnippetNodeDialog(final Class<?> templateMetaCategory, final RSnippetNodeConfig config) {
        m_templateMetaCategory = templateMetaCategory;
        m_config = config;

        // Count number of input ports that are tables (based on -1)
        m_tableInPort = (int) m_config.getInPortTypes().stream().filter(portType -> portType.equals(BufferedDataTable
                .TYPE)).count() - 1;

        // Count number of output ports that are tables (based on -1)
        m_tableOutPort = (int) m_config.getOutPortTypes().stream().filter(portType -> portType.equals
                (BufferedDataTable.TYPE)).count() - 1;

        m_panel = new RSnippetNodePanel(templateMetaCategory, m_config, false, true) {
            @Override
            public void applyTemplate(RSnippetTemplate template, DataTableSpec spec, Map<String, FlowVariable>
                    flowVariables) {
                super.applyTemplate(template, spec, flowVariables);
                setSelected(SNIPPET_TAB);
            }
        };

        addTab(SNIPPET_TAB, m_panel);
        // The preview does not have the templates tab
        addTab("Templates", createTemplatesPanel());

        // currently there is only one advanced option which applies only for R nodes with table output
        if (m_tableOutPort >= 0) {
            addTab("Advanced", createAdvancedPanel());
        }
        m_panel.setPreferredSize(new Dimension(800, 600));
    }

    /**
     * Create the templates tab.
     */
    private JPanel createTemplatesPanel() {
        final RSnippetNodePanel preview = new RSnippetNodePanel(m_templateMetaCategory, m_config, true, false);

        m_templatesController = new DefaultTemplateController(m_panel, preview);

        return new TemplatesPanel(Collections.singleton(m_templateMetaCategory), m_templatesController);
    }

    private JPanel createAdvancedPanel() {
        final GridBagConstraints gbc_nonNums = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE,
                GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0);
        final GridBagConstraints gbc_filler = new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.BASELINE,
                GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0);

        m_outNonNumbersAsMissing = new JCheckBox("Treat NaN, Inf and -Inf as missing values in the output table.");
        m_outNonNumbersAsMissing.setToolTipText("Check for backwards compatibility with pre 2.10 releases.");

        final JPanel p = new JPanel(new GridBagLayout());
        p.add(m_outNonNumbersAsMissing, gbc_nonNums);
        p.add(new JPanel(), gbc_filler); // Panel to fill up remaining space
        return p;
    }

    @Override
    public boolean closeOnESC() {
        // do not close on EST, since EST is used to close autocomlete popups in the snippets textarea
        return false;
    }

    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
            throws NotConfigurableException {
        final DataTableSpec spec = m_tableInPort >= 0 ? (DataTableSpec) specs[m_tableInPort] : null;
        m_panel.updateData(settings, specs, getAvailableFlowVariables().values());
        if (m_tableOutPort >= 0) {
            final RSnippetSettings s = new RSnippetSettings();
            s.loadSettingsForDialog(settings);
            m_outNonNumbersAsMissing.setSelected(s.getOutNonNumbersAsMissing());
        }

        m_templatesController.setDataTableSpec(spec);
        m_templatesController.setFlowVariables(getAvailableFlowVariables());
    }

    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObject[] input)
            throws NotConfigurableException {
        final DataTableSpec spec = m_tableInPort >= 0 ? ((BufferedDataTable) input[m_tableInPort]).getSpec() : null;
        m_panel.updateData(settings, input, getAvailableFlowVariables().values());
        if (m_tableOutPort >= 0) {
            final RSnippetSettings s = new RSnippetSettings();
            s.loadSettingsForDialog(settings);
            m_outNonNumbersAsMissing.setSelected(s.getOutNonNumbersAsMissing());
        }

        m_templatesController.setDataTableSpec(spec);
        m_templatesController.setFlowVariables(getAvailableFlowVariables());
    }

    @Override
    public void onOpen() {
        ViewUtils.invokeAndWaitInEDT(m_panel::onOpen);
    }

    @Override
    public void onClose() {
        ViewUtils.invokeAndWaitInEDT(m_panel::onClose);
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        if (m_tableOutPort >= 0) {
            m_panel.getRSnippet().getSettings().setOutNonNumbersAsMissing(m_outNonNumbersAsMissing.isSelected());
        }
        m_panel.saveSettingsTo(settings);
    }
}