/*******************************************************************************
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * J�rgen Brandt (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Christian Th�ns (BfR)
 * Annemarie K�sbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * PMM-Lab is a project under development. Contributions are welcome.
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.bund.bfr.knime.pmm.modelanddatajoiner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import de.bund.bfr.knime.pmm.common.CellIO;
import de.bund.bfr.knime.pmm.common.DepXml;
import de.bund.bfr.knime.pmm.common.IndepXml;
import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeRelationReader;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeSchema;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.math.MathUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

public class PrimaryJoiner implements Joiner {

	private KnimeSchema modelSchema;
	private KnimeSchema dataSchema;
	private KnimeSchema peiSchema;

	private BufferedDataTable modelTable;
	private BufferedDataTable dataTable;
	private boolean joinSameConditions;

	private boolean isEstimated;

	private JCheckBox joinBox;
	private List<JComboBox<String>> variableBoxes;

	private List<String> variables;
	private List<String> parameters;

	private List<KnimeTuple> modelTuples;
	private List<KnimeTuple> conditionTuples;

	public PrimaryJoiner(BufferedDataTable modelTable,
			BufferedDataTable dataTable, boolean joinSameConditions)
			throws PmmException {
		this.modelTable = modelTable;
		this.dataTable = dataTable;
		this.joinSameConditions = joinSameConditions;

		modelSchema = new Model1Schema();
		dataSchema = new TimeSeriesSchema();
		peiSchema = new KnimeSchema(new Model1Schema(), new TimeSeriesSchema());
		readModelTable();
		readDataTable();
	}

	@Override
	public JComponent createPanel(List<String> assignments) {
		JPanel panel = new JPanel();
		JPanel parameterPanel = new JPanel();
		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();
		Map<String, String> replacements = getAssignmentsMap(assignments);

		panel.setLayout(new BorderLayout());
		parameterPanel.setLayout(new BorderLayout());
		leftPanel.setLayout(new GridLayout(variables.size(), 1, 5, 5));
		leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		rightPanel.setLayout(new GridLayout(variables.size(), 1, 5, 5));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		variableBoxes = new ArrayList<JComboBox<String>>(variables.size());

		for (String var : variables) {
			JComboBox<String> box = new JComboBox<String>(
					parameters.toArray(new String[0]));

			if (replacements.containsKey(var)) {
				box.setSelectedItem(replacements.get(var));
			} else {
				box.setSelectedItem(null);
			}

			box.setPreferredSize(new Dimension(150,
					box.getPreferredSize().height));
			variableBoxes.add(box);
			leftPanel.add(new JLabel(var + ":"));
			rightPanel.add(box);
		}

		parameterPanel.add(leftPanel, BorderLayout.WEST);
		parameterPanel.add(rightPanel, BorderLayout.EAST);

		if (isEstimated) {
			joinBox = new JCheckBox(
					"Only join when model was estimated under same conditions");
			joinBox.setSelected(joinSameConditions);
			parameterPanel.add(joinBox, BorderLayout.SOUTH);
		}

		panel.add(parameterPanel, BorderLayout.NORTH);

		return panel;
	}

	@Override
	public List<String> getAssignments() {
		Map<String, String> replacements = getReplacementsFromFrame();
		List<String> assignments = new ArrayList<String>();

		for (String var : replacements.keySet()) {
			assignments.add(var + "=" + replacements.get(var));
		}

		return assignments;
	}

	@Override
	public BufferedDataTable getOutputTable(List<String> assignments,
			ExecutionContext exec) throws InvalidSettingsException,
			CanceledExecutionException, PmmException, InterruptedException {
		BufferedDataContainer container = exec.createDataContainer(peiSchema
				.createSpec());
		Map<String, String> replacements = getAssignmentsMap(assignments);
		int rowCount = modelTuples.size() * dataTable.getRowCount();
		int index = 0;

		for (int i = 0; i < modelTuples.size(); i++) {
			KnimeTuple modelTuple = modelTuples.get(i);
			Double condTemp = null;
			Double condPH = null;
			Double condAW = null;

			if (isEstimated) {
				KnimeTuple condTuple = conditionTuples.get(i);

				condTemp = condTuple
						.getDouble(TimeSeriesSchema.ATT_TEMPERATURE);
				condPH = condTuple.getDouble(TimeSeriesSchema.ATT_PH);
				condAW = condTuple
						.getDouble(TimeSeriesSchema.ATT_WATERACTIVITY);
			}

			String formula = modelTuple.getString(Model1Schema.ATT_FORMULA);
			PmmXmlDoc depVar = modelTuple.getPmmXml(Model1Schema.ATT_DEPENDENT);
			String depVarName = ((DepXml) depVar.get(0)).getName();
			PmmXmlDoc indepVar = modelTuple
					.getPmmXml(Model1Schema.ATT_INDEPENDENT);
			PmmXmlDoc newIndepVar = new PmmXmlDoc();
			boolean allVarsReplaced = true;

			if (replacements.containsKey(depVarName)) {
				depVarName = replacements.get(depVarName);
				((DepXml) depVar.get(0)).setName(depVarName);
			} else {
				allVarsReplaced = false;
			}

			for (String var : replacements.keySet()) {
				String newVar = replacements.get(var);

				formula = MathUtilities.replaceVariable(formula, var, newVar);
			}

			for (PmmXmlElementConvertable el : indepVar.getElementSet()) {
				IndepXml iv = (IndepXml) el;

				if (replacements.containsKey(iv.getName())) {
					iv.setName(replacements.get(iv.getName()));
					newIndepVar.add(iv);
				} else {
					allVarsReplaced = false;
					break;
				}
			}

			if (!allVarsReplaced) {
				continue;
			}

			modelTuple.setValue(Model1Schema.ATT_FORMULA, formula);
			modelTuple.setValue(Model1Schema.ATT_DEPENDENT, depVar);
			modelTuple.setValue(Model1Schema.ATT_INDEPENDENT, newIndepVar);
			modelTuple.setValue(Model1Schema.ATT_DATABASEWRITABLE,
					Model1Schema.NOTWRITABLE);

			KnimeRelationReader reader = new KnimeRelationReader(dataSchema,
					dataTable);

			while (reader.hasMoreElements()) {
				KnimeTuple dataTuple = reader.nextElement();
				boolean addRow = true;

				if (isEstimated && joinSameConditions) {
					Double temp = dataTuple
							.getDouble(TimeSeriesSchema.ATT_TEMPERATURE);
					Double ph = dataTuple.getDouble(TimeSeriesSchema.ATT_PH);
					Double aw = dataTuple
							.getDouble(TimeSeriesSchema.ATT_WATERACTIVITY);

					if (temp != null && !temp.equals(condTemp)) {
						addRow = false;
					}

					if (ph != null && !ph.equals(condPH)) {
						addRow = false;
					}

					if (aw != null && !aw.equals(condAW)) {
						addRow = false;
					}
				}

				if (addRow) {
					KnimeTuple tuple = new KnimeTuple(peiSchema, modelTuple,
							dataTuple);

					container.addRowToTable(tuple);
				}

				exec.checkCanceled();
				exec.setProgress((double) index / (double) rowCount, "");
				index++;
			}
		}

		container.close();

		return container.getTable();
	}

	@Override
	public boolean isValid() {
		for (JComboBox<String> box : variableBoxes) {
			if (box.getSelectedItem() == null) {
				return false;
			}
		}

		return true;
	}

	public boolean isJoinSameConditions() {
		if (joinBox != null) {
			return joinBox.isSelected();
		} else {
			return joinSameConditions;
		}
	}

	private void readModelTable() throws PmmException {
		Set<Integer> ids = new LinkedHashSet<Integer>();
		Set<Integer> estIDs = new LinkedHashSet<Integer>();

		modelTuples = new ArrayList<KnimeTuple>();
		conditionTuples = new ArrayList<KnimeTuple>();

		if (peiSchema.conforms(modelTable)) {
			KnimeRelationReader reader = new KnimeRelationReader(peiSchema,
					modelTable);

			while (reader.hasMoreElements()) {
				KnimeTuple tuple = reader.nextElement();
				KnimeTuple modelTuple = new KnimeTuple(modelSchema,
						peiSchema.createSpec(), tuple);
				KnimeTuple condTuple = new KnimeTuple(dataSchema,
						peiSchema.createSpec(), tuple);
				Integer id = modelTuple.getInt(Model1Schema.ATT_MODELID);
				Integer estID = modelTuple.getInt(Model1Schema.ATT_ESTMODELID);

				if (estID != null) {
					if (estIDs.add(estID)) {
						modelTuples.add(tuple);
						conditionTuples.add(condTuple);
					}
				} else {
					if (ids.add(id)) {
						modelTuples.add(tuple);
						conditionTuples.add(condTuple);
					}
				}
			}

			isEstimated = true;
		} else {
			KnimeRelationReader reader = new KnimeRelationReader(modelSchema,
					modelTable);

			while (reader.hasMoreElements()) {
				KnimeTuple modelRow = reader.nextElement();
				Integer id = modelRow.getInt(Model1Schema.ATT_MODELID);
				Integer estID = modelRow.getInt(Model1Schema.ATT_ESTMODELID);

				if (estID != null) {
					if (estIDs.add(estID)) {
						modelTuples.add(modelRow);
					}
				} else {
					if (ids.add(id)) {
						modelTuples.add(modelRow);
					}
				}
			}

			isEstimated = false;
		}

		Set<String> variableSet = new LinkedHashSet<String>();

		for (KnimeTuple tuple : modelTuples) {
			variableSet.add(((DepXml) tuple.getPmmXml(
					Model1Schema.ATT_DEPENDENT).get(0)).getName());
			variableSet.addAll(CellIO.getNameList(tuple
					.getPmmXml(Model1Schema.ATT_INDEPENDENT)));
		}

		variables = new ArrayList<String>(variableSet);
	}

	private void readDataTable() throws PmmException {
		Set<String> parameterSet = new LinkedHashSet<String>();

		parameterSet.add(TimeSeriesSchema.TIME);
		parameterSet.add(TimeSeriesSchema.LOGC);
		parameterSet.add(TimeSeriesSchema.ATT_TEMPERATURE);
		parameterSet.add(TimeSeriesSchema.ATT_PH);
		parameterSet.add(TimeSeriesSchema.ATT_WATERACTIVITY);

		KnimeRelationReader reader = new KnimeRelationReader(dataSchema,
				dataTable);

		while (reader.hasMoreElements()) {
			KnimeTuple tuple = reader.nextElement();

			PmmXmlDoc misc = tuple.getPmmXml(TimeSeriesSchema.ATT_MISC);

			for (PmmXmlElementConvertable el : misc.getElementSet()) {
				MiscXml element = (MiscXml) el;

				parameterSet.add(element.getName());
			}
		}

		parameters = new ArrayList<String>(parameterSet);
	}

	private Map<String, String> getReplacementsFromFrame() {
		Map<String, String> replacements = new LinkedHashMap<String, String>();

		for (int i = 0; i < variables.size(); i++) {
			String replacement = (String) variableBoxes.get(i)
					.getSelectedItem();

			if (!replacement.equals("")) {
				replacements.put(variables.get(i), replacement);
			}
		}

		return replacements;
	}

	private Map<String, String> getAssignmentsMap(List<String> assignments) {
		Map<String, String> replacements = new LinkedHashMap<String, String>();
		Map<String, String> assignmentsMap = new LinkedHashMap<String, String>();

		for (String s : assignments) {
			String[] elements = s.split("=");

			if (elements.length == 2) {
				String variable = elements[0].trim();
				String parameter = elements[1].trim();

				assignmentsMap.put(variable, parameter);
			}
		}

		for (String var : variables) {
			if (assignmentsMap.containsKey(var)) {
				replacements.put(var, assignmentsMap.get(var));
			}
		}

		return replacements;
	}

}
