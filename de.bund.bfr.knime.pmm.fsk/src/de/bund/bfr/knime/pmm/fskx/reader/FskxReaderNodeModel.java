/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors: Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.pmm.fskx.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.jdom2.JDOMException;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.util.FileUtil;
import org.knime.ext.r.node.local.port.RPortObject;
import org.knime.ext.r.node.local.port.RPortObjectSpec;
import org.rosuda.REngine.REXPMismatchException;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.xml.stax.SBMLReader;

import de.bund.bfr.knime.pmm.FSMRUtils;
import de.bund.bfr.knime.pmm.fskx.FskMetaDataTuple;
import de.bund.bfr.knime.pmm.fskx.MissingValueError;
import de.bund.bfr.knime.pmm.fskx.RMetaDataNode;
import de.bund.bfr.knime.pmm.fskx.ZipUri;
import de.bund.bfr.knime.pmm.fskx.controller.IRController.RException;
import de.bund.bfr.knime.pmm.fskx.controller.LibRegistry;
import de.bund.bfr.knime.pmm.fskx.port.FskPortObject;
import de.bund.bfr.knime.pmm.fskx.port.FskPortObjectSpec;
import de.bund.bfr.openfsmr.FSMRTemplate;
import de.bund.bfr.openfsmr.FSMRTemplateImpl;
import de.bund.bfr.pmfml.file.uri.UriFactory;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;

class FskxReaderNodeModel extends NodeModel {

	// configuration keys
	static final String CFGKEY_FILE = "filename";

	// defaults for persistent state
	private static final String DEFAULT_FILE = "c:/temp/foo.numl";

	// defaults for persistent state
	private final SettingsModelString filename = new SettingsModelString(CFGKEY_FILE, DEFAULT_FILE);

	private static final PortType[] inPortTypes = {};
	private static final PortType[] outPortTypes = { FskPortObject.TYPE, RPortObject.TYPE, BufferedDataTable.TYPE };

	// Specs
	private static final FskPortObjectSpec fskSpec = FskPortObjectSpec.INSTANCE;
	private static final RPortObjectSpec rSpec = RPortObjectSpec.INSTANCE;
	private static final DataTableSpec metadataSpec = FskMetaDataTuple.createMetaDataTableSpec();

	protected FskxReaderNodeModel() {
		super(inPortTypes, outPortTypes);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws MissingValueError
	 * @throws RException
	 * @throws REXPMismatchException
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec)
			throws CombineArchiveException, FileAccessException, MissingValueError, REXPMismatchException, RException {

		String model = "";
		String param = "";
		String viz = "";
		FSMRTemplate template = new FSMRTemplateImpl();
		File workspaceFile = null;
		Set<File> libs = new HashSet<>();

		File archiveFile = new File(filename.getStringValue());
		try (CombineArchive archive = new CombineArchive(archiveFile)) {
			// Gets annotation
			RMetaDataNode node = new RMetaDataNode(archive.getDescriptions().get(0).getXmlDescription());

			// Gets model script
			if (node.getMainScript() != null) {
				ArchiveEntry entry = archive.getEntry(node.getMainScript());
				model = loadScriptFromEntry(entry);
			}

			// Gets parameters script
			if (node.getParametersScript() != null) {
				ArchiveEntry entry = archive.getEntry(node.getParametersScript());
				param = loadScriptFromEntry(entry);
			}

			// Gets visualization script
			if (node.getVisualizationScript() != null) {
				ArchiveEntry entry = archive.getEntry(node.getVisualizationScript());
				viz = loadScriptFromEntry(entry);
			}

			// Gets workspace file
			if (node.getWorkspaceFile() != null) {
				ArchiveEntry entry = archive.getEntry(node.getWorkspaceFile());
				workspaceFile = FileUtil.createTempFile("workspace", ".r");
				entry.extractFile(workspaceFile);
			}

			// Gets model meta data
			URI pmfUri = UriFactory.createPMFURI();
			if (archive.getNumEntriesWithFormat(pmfUri) == 1) {
				ArchiveEntry entry = archive.getEntriesWithFormat(pmfUri).get(0);
				File f = FileUtil.createTempFile("metaData", ".pmf");
				entry.extractFile(f);

				SBMLDocument doc = new SBMLReader().readSBML(f);
				template = FSMRUtils.processPrevalenceModel(doc);
			}

			// Gets R libraries
			URI zipUri = ZipUri.createURI();

			// Gets library names from the zip entries in the CombineArchive
			List<String> libNames = archive.getEntriesWithFormat(zipUri).stream()
					.map(entry -> entry.getFileName().split("\\_")[0]).collect(Collectors.toList());

			if (!libNames.isEmpty()) {

				LibRegistry libRegistry = LibRegistry.instance();

				// Filters and installs missing libraries
				List<String> missingLibs = libNames.stream().filter(lib -> !libRegistry.isInstalled(lib))
						.collect(Collectors.toList());
				if (!missingLibs.isEmpty()) {
					libRegistry.installLibs(missingLibs);
				}

				// Converts and return set of Paths returned from plugin to set
				libs = libRegistry.getPaths(libNames).stream().map(Path::toFile).collect(Collectors.toSet());
			}

		} catch (IOException | JDOMException | ParseException | XMLStreamException e) {
			e.printStackTrace();
		}

		// Meta data port
		BufferedDataContainer fsmrContainer = exec.createDataContainer(metadataSpec);
		if (template != null) {
			FskMetaDataTuple metadataTuple = new FskMetaDataTuple();
			if (template.isSetModelName())
				metadataTuple.setModelName(template.getModelName());
			if (template.isSetModelId())
				metadataTuple.setModelId(template.getModelId());
			if (template.isSetModelLink())
				metadataTuple.setModelLink(template.getModelLink());
			if (template.isSetOrganismName())
				metadataTuple.setOrganismName(template.getOrganismName());
			if (template.isSetOrganismDetails())
				metadataTuple.setOrganismDetails(template.getOrganismDetails());
			if (template.isSetMatrixName())
				metadataTuple.setMatrixName(template.getMatrixName());
			if (template.isSetMatrixDetails())
				metadataTuple.setMatrixDetails(template.getMatrixDetails());
			if (template.isSetCreator())
				metadataTuple.setCreator(template.getCreator());
			if (template.isSetFamilyName())
				metadataTuple.setFamilyName(template.getFamilyName());
			if (template.isSetContact())
				metadataTuple.setContact(template.getContact());
			if (template.isSetReferenceDescription())
				metadataTuple.setReferenceDescription(template.getReferenceDescription());
			if (template.isSetReferenceDescriptionLink())
				metadataTuple.setReferenceDescriptionLink(template.getReferenceDescriptionLink());
			if (template.isSetCreatedDate())
				metadataTuple.setCreatedDate(template.getCreatedDate().toString());
			if (template.isSetModifiedDate())
				metadataTuple.setModifiedDate(template.getModifiedDate().toString());
			if (template.isSetRights())
				metadataTuple.setRights(template.getRights());
			if (template.isSetNotes())
				metadataTuple.setNotes(template.getNotes());
			if (template.isSetCurationStatus())
				metadataTuple.setCurationStatus(template.getCurationStatus());
			if (template.isSetModelType())
				metadataTuple.setModelType(template.getModelType().toString());
			if (template.isSetModelSubject())
				metadataTuple.setModelSubject(template.getModelSubject().toString());
			if (template.isSetFoodProcess())
				metadataTuple.setFoodProcess(template.getFoodProcess());
			if (template.isSetDependentVariable())
				metadataTuple.setDependentVariable(template.getDependentVariable());
			if (template.isSetDependentVariableUnit())
				metadataTuple.setDependentVariableUnit(template.getDependentVariableUnit());
			if (template.isSetDependentVariableMin())
				metadataTuple.setDependentVariableMin(template.getDependentVariableMin());
			if (template.isSetDependentVariableMax())
				metadataTuple.setDependentVariableMax(template.getDependentVariableMax());
			if (template.isSetIndependentVariables()) {
				String[] vars = template.getIndependentVariables();
				metadataTuple.setIndependentVariables(Arrays.asList(vars));
			}
			if (template.isSetIndependentVariablesUnits()) {
				String[] units = template.getIndependentVariablesUnits();
				metadataTuple.setIndependentVariablesUnits(Arrays.asList(units));
			}
			if (template.isSetIndependentVariablesMins()) {
				double[] mins = template.getIndependentVariablesMins();
				List<Double> objList = Arrays.stream(mins).boxed().collect(Collectors.toList());
				metadataTuple.setIndependentVariablesMins(objList);
			}
			if (template.isSetIndependentVariablesMaxs()) {
				double[] maxs = template.getIndependentVariablesMaxs();
				List<Double> objList = Arrays.stream(maxs).boxed().collect(Collectors.toList());
				metadataTuple.setIndependentVariablesMaxs(objList);
			}
			if (template.isSetHasData())
				metadataTuple.setHasData(Boolean.toString(template.getHasData()));

			Map<String, String> indepValues = new HashMap<>();
			for (String line : param.split("\\r?\\n")) {
				if (line.indexOf("<-") != -1) {
					String[] tokens = line.split("<-");
					String variableName = tokens[0].trim();
					String variableValue = tokens[1].trim();
					indepValues.put(variableName, variableValue);
				}
			}
			String values = Arrays.stream(template.getIndependentVariables()).map(indepValues::get)
					.collect(Collectors.joining("||"));
			metadataTuple.setIndependentVariablesValues(values);

			fsmrContainer.addRowToTable(metadataTuple);
		}
		fsmrContainer.close();

		FskPortObject fskObj = new FskPortObject(model, param, viz, template, workspaceFile, libs);
		RPortObject rObj = new RPortObject(fskObj.getWorkspaceFile());

		return new PortObject[] { fskObj, rObj, fsmrContainer.getTable() };
	}

	private String loadScriptFromEntry(final ArchiveEntry entry) throws IOException {
		// Create temporary file with a random name. The name does not matter,
		// since the file will be
		// deleted by KNIME itself.
		File f = FileUtil.createTempFile("script", ".r");
		entry.extractFile(f);

		// Read script from f and return script
		FileInputStream fis = new FileInputStream(f);
		String script = IOUtils.toString(fis, "UTF-8");
		fis.close();

		return script;
	}

	/** {@inheritDoc} */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] { fskSpec, rSpec, metadataSpec };
	}

	/** {@inheritDoc} */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		filename.saveSettingsTo(settings);
	}

	/** {@inheritDoc} */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		filename.loadSettingsFrom(settings);
	}

	/** {@inheritDoc} */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		filename.validateSettings(settings);
	}

	/** {@inheritDoc} */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// nothing
	}

	/** {@inheritDoc} */
	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// nothing
	}

	/** {@inheritDoc} */
	@Override
	protected void reset() {
		// does nothing
	}

	// --- utility ---

	private class FileAccessException extends Exception {

		private static final long serialVersionUID = 1L;
	}
}
