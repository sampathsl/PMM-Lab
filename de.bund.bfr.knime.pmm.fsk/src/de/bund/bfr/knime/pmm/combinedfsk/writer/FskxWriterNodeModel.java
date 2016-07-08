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
package de.bund.bfr.knime.pmm.combinedfsk.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.jdom2.JDOMException;
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
import org.rosuda.REngine.REXPMismatchException;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.xml.XMLNode;
import org.sbml.jsbml.xml.XMLTriple;
import org.sbml.jsbml.xml.stax.SBMLWriter;

import de.bund.bfr.knime.pmm.combinedfsk.archive.FskxMetaDataNode;
import de.bund.bfr.knime.pmm.combinedfsk.archive.Link;
import de.bund.bfr.knime.pmm.combinedfsk.archive.ModelFiles;
import de.bund.bfr.knime.pmm.combinedfsk.port.CombinedFskPortObject;
import de.bund.bfr.knime.pmm.combinedfsk.port.FskModel;
import de.bund.bfr.knime.pmm.common.math.MathUtilities;
import de.bund.bfr.knime.pmm.common.writer.TableReader;
import de.bund.bfr.knime.pmm.common.writer.WriterUtils;
import de.bund.bfr.knime.pmm.fskx.RUri;
import de.bund.bfr.knime.pmm.fskx.ZipUri;
import de.bund.bfr.knime.pmm.fskx.controller.IRController.RException;
import de.bund.bfr.knime.pmm.fskx.controller.LibRegistry;
import de.bund.bfr.openfsmr.FSMRTemplate;
import de.bund.bfr.pmfml.ModelClass;
import de.bund.bfr.pmfml.PMFUtil;
import de.bund.bfr.pmfml.file.uri.UriFactory;
import de.bund.bfr.pmfml.sbml.LimitsConstraint;
import de.bund.bfr.pmfml.sbml.Metadata;
import de.bund.bfr.pmfml.sbml.MetadataAnnotation;
import de.bund.bfr.pmfml.sbml.MetadataImpl;
import de.bund.bfr.pmfml.sbml.PMFCompartment;
import de.bund.bfr.pmfml.sbml.PMFSpecies;
import de.bund.bfr.pmfml.sbml.PMFUnitDefinition;
import de.bund.bfr.pmfml.sbml.Reference;
import de.bund.bfr.pmfml.sbml.ReferenceSBMLNode;
import de.bund.bfr.pmfml.sbml.SBMLFactory;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;

/**
 */
class FskxWriterNodeModel extends NodeModel {

	// Configuration keys
	protected static final String CFG_FILE = "file";

	private final SettingsModelString filePath = new SettingsModelString(CFG_FILE, null);

	private static final PortType[] inPortTypes = { CombinedFskPortObject.TYPE };
	private static final PortType[] outPortTypes = {};

	protected FskxWriterNodeModel() {
		super(inPortTypes, outPortTypes);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws FileCreationException
	 *             If a critical file could not be created. E.g. model script.
	 * @throws IOException
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec)
			throws CombineArchiveException {

		CombinedFskPortObject portObject = (CombinedFskPortObject) inData[0];

		try {
			Files.deleteIfExists(Paths.get(filePath.getStringValue()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// try to create CombineArchive
		try (CombineArchive archive = new CombineArchive(new File(filePath.getStringValue()))) {

			URI rUri = RUri.createURI();
			List<ModelFiles> models = new LinkedList<>();
			List<Link> links = new LinkedList<>();

			Set<String> sharedLibraries = new HashSet<>();

			for (FskModel fskModel : portObject.getModels()) {
				ModelFiles modelFiles = new ModelFiles();

				// TODO: dummy value for id. Need to give it a real value
				modelFiles.setId(models.size());

				// Adds model script
				if (fskModel.isSetModelScript()) {
					File file = createScriptFile(fskModel.getModelScript());
					String targetName = modelFiles.getId() + File.separator + "model.r";
					archive.addEntry(file, targetName, rUri);

					modelFiles.setModelScript("model.r");
				}

				// Adds parameters script
				if (fskModel.isSetParametersScript()) {
					File file = createScriptFile(fskModel.getParametersScript());
					String targetName = modelFiles.getId() + File.separator + "param.r";
					archive.addEntry(file, targetName, rUri);

					modelFiles.setParametersScript("param.r");
				}

				// Adds visualization script
				if (fskModel.isSetVisualizationScript()) {
					File file = createScriptFile(fskModel.getVisualizationScript());
					String targetName = modelFiles.getId() + File.separator + "viz.r";
					archive.addEntry(file, targetName, rUri);

					modelFiles.setVisualizationScript("viz.r");
				}

				// Adds R workspace file
				if (fskModel.isSetWorkspace()) {
					String targetName = modelFiles.getId() + File.separator + "workspace.r";
					archive.addEntry(fskModel.getWorkspace(), targetName, rUri);

					modelFiles.setWorkspace("workspace.r");
				}

				// Adds model meta data
				if (fskModel.isSetTemplate()) {
					SBMLDocument doc = createSbmlDocument(fskModel.getTemplate());

					File f = FileUtil.createTempFile("metaData", ".pmf");
					try {
						new SBMLWriter().write(doc, f);
						String targetName = modelFiles.getId() + File.separator + "metadata.pmf";
						archive.addEntry(f, targetName, UriFactory.createPMFURI());

						modelFiles.setMetaData("metadata.pmf");
					} catch (SBMLException | XMLStreamException e) {
						e.printStackTrace();
					}
				}

				// Adds libraries
				if (!fskModel.getLibraries().isEmpty()) {
					modelFiles.setLibraries(fskModel.getLibraries());

					sharedLibraries.addAll(fskModel.getLibraries());
				}

				models.add(modelFiles);
			}

			ModelFiles[] modelArray = models.toArray(new ModelFiles[models.size()]);
			Link[] linkArray = links.toArray(new Link[links.size()]);
			FskxMetaDataNode metaDataNode = new FskxMetaDataNode(modelArray, linkArray);

			archive.addDescription(new DefaultMetaDataObject(metaDataNode.getElement()));

			// Add libraries
			if (!sharedLibraries.isEmpty()) {
				URI zipUri = ZipUri.createURI();
				List<File> files = LibRegistry.instance().getPaths(new LinkedList<>(sharedLibraries)).stream()
						.map(Path::toFile).collect(Collectors.toList());
				for (File f : files) {
					archive.addEntry(f, f.getName(), zipUri);
				}
			}

			archive.pack();

		} catch (IOException | JDOMException | ParseException | TransformerException | RException
				| REXPMismatchException e1) {
			e1.printStackTrace();
		}

		return new PortObject[] {};
	}

	private File createScriptFile(String script) throws IOException {
		File f = FileUtil.createTempFile("script", ".r");
		try (FileWriter fw = new FileWriter(f)) {
			fw.write(script);
		}

		return f;
	}

	/** {@inheritDoc} */
	@Override
	protected void reset() {
		// does nothing
	}

	/** {@inheritDoc} */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] {};
	}

	/** {@inheritDoc} */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		filePath.saveSettingsTo(settings);
	}

	/** {@inheritDoc} */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		filePath.loadSettingsFrom(settings);
	}

	/** {@inheritDoc} */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		filePath.validateSettings(settings);
	}

	/** {@inheritDoc} */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// nothing
	}

	/** {@inheritDoc} */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// nothing
	}

	/** Creates SBMLDocument out of a OpenFSMR template. */
	private static SBMLDocument createSbmlDocument(final FSMRTemplate template) {

		// Creates SBMLDocument for the primary model
		final SBMLDocument sbmlDocument = new SBMLDocument(TableReader.LEVEL, TableReader.VERSION);

		// Adds namespaces to the sbmlDocument
		TableReader.addNamespaces(sbmlDocument);

		// Adds document annotation
		Metadata metaData = new MetadataImpl();
		if (template.isSetCreator()) {
			metaData.setGivenName(template.getCreator());
		}
		if (template.isSetFamilyName()) {
			metaData.setFamilyName(template.getFamilyName());
		}
		if (template.isSetContact()) {
			metaData.setContact(template.getContact());
		}
		if (template.isSetCreatedDate()) {
			metaData.setCreatedDate(template.getCreatedDate().toString());
		}
		if (template.isSetModifiedDate()) {
			metaData.setModifiedDate(template.getModifiedDate().toString());
		}
		if (template.isSetCreatedDate()) {
			metaData.setType(template.getModelType());
		}
		if (template.isSetRights()) {
			metaData.setRights(template.getRights());
		}
		if (template.isSetReferenceDescriptionLink()) {
			metaData.setReferenceLink(template.getReferenceDescriptionLink().toString());
		}

		sbmlDocument.setAnnotation(new MetadataAnnotation(metaData).getAnnotation());

		// Creates model and names it
		Model model = sbmlDocument.createModel(PMFUtil.createId(template.getModelId()));
		if (template.isSetModelName()) {
			model.setName(template.getModelName());
		}

		// Sets model notes
		if (template.isSetNotes()) {
			try {
				model.setNotes(template.getNotes());
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}

		// Creates and adds compartment to the model
		PMFCompartment compartment = SBMLFactory.createPMFCompartment(PMFUtil.createId(template.getMatrixName()),
				template.getMatrixName());
		compartment.setDetail(template.getMatrixDetails());
		model.addCompartment(compartment.getCompartment());

		// Creates and adds species to the model
		String speciesId = PMFUtil.createId(template.getOrganismName());
		String speciesName = template.getOrganismName();
		String speciesUnit = PMFUtil.createId(template.getDependentVariableUnit());
		PMFSpecies species = SBMLFactory.createPMFSpecies(compartment.getId(), speciesId, speciesName, speciesUnit);
		model.addSpecies(species.getSpecies());

		// Add unit definitions here (before parameters)
		Set<String> unitsSet = new LinkedHashSet<>();
		unitsSet.add(template.getDependentVariableUnit().trim());
		for (String unit : template.getIndependentVariablesUnits()) {
			unitsSet.add(unit.trim());
		}
		for (String unit : unitsSet) {
			try {
				PMFUnitDefinition unitDef = WriterUtils.createUnitFromDB(unit);

				// unitDef is not in PmmLab DB
				if (unitDef == null) {
					UnitDefinition ud = model.createUnitDefinition(PMFUtil.createId(unit));
					ud.setName(unit);
				} else {
					model.addUnitDefinition(unitDef.getUnitDefinition());
				}
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}

		// Adds dep parameter
		Parameter depParam = new Parameter(PMFUtil.createId(template.getDependentVariable()));
		depParam.setName(template.getDependentVariable());
		depParam.setUnits(PMFUtil.createId(template.getDependentVariableUnit()));
		model.addParameter(depParam);

		// Adds dep constraint
		if (template.isSetDependentVariableMin() || template.isSetDependentVariableMax()) {
			LimitsConstraint lc = new LimitsConstraint(template.getDependentVariable(),
					template.getDependentVariableMin(), template.getDependentVariableMax());
			if (lc.getConstraint() != null) {
				model.addConstraint(lc.getConstraint());
			}
		}

		// Adds independent parameters
		for (int i = 0; i < template.getIndependentVariables().length; i++) {
			String var = template.getIndependentVariables()[i];
			Parameter param = model.createParameter(PMFUtil.createId(var));
			param.setName(var);

			try {
				param.setUnits(PMFUtil.createId(template.getIndependentVariablesUnits()[i]));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}

			Double min = template.isSetIndependentVariablesMins() ? template.getIndependentVariablesMins()[i] : null;
			Double max = template.isSetIndependentVariablesMaxs() ? template.getIndependentVariablesMaxs()[i] : null;
			LimitsConstraint lc = new LimitsConstraint(param.getId(), min, max);
			if (lc.getConstraint() != null) {
				model.addConstraint(lc.getConstraint());
			}
		}

		// Add rule
		String formulaName = "Missing formula name";
		ModelClass modelClass = template.getModelSubject();
		int modelId = MathUtilities.getRandomNegativeInt();
		Reference[] references = new Reference[0];

		AssignmentRule rule = new AssignmentRule(3, 1);
		rule.setVariable(depParam.getId());
		rule.setAnnotation(new ModelRuleAnnotation(formulaName, modelClass, modelId, references).annotation);
		model.addRule(rule);

		return sbmlDocument;
	}

	private static class ModelRuleAnnotation {

		private Annotation annotation;

		private static final String FORMULA_TAG = "formulaName";
		private static final String SUBJECT_TAG = "subject";
		private static final String PMMLAB_ID = "pmmlabID";

		private ModelRuleAnnotation(String formulaName, ModelClass modelClass, int pmmlabID, Reference[] references) {
			// Builds metadata node
			XMLNode metadataNode = new XMLNode(new XMLTriple("metadata", null, "pmf"));
			this.annotation = new Annotation();
			this.annotation.setNonRDFAnnotation(metadataNode);

			// Creates annotation for formula name
			XMLNode nameNode = new XMLNode(new XMLTriple(FORMULA_TAG, null, "pmmlab"));
			nameNode.addChild(new XMLNode(formulaName));
			metadataNode.addChild(nameNode);

			// Creates annotation for modelClass
			XMLNode modelClassNode = new XMLNode(new XMLTriple(SUBJECT_TAG, null, "pmmlab"));
			modelClassNode.addChild(new XMLNode(modelClass.fullName()));
			metadataNode.addChild(modelClassNode);

			// Create annotation for pmmlabID
			XMLNode idNode = new XMLNode(new XMLTriple(PMMLAB_ID, null, "pmmlab"));
			idNode.addChild(new XMLNode(new Integer(pmmlabID).toString()));
			metadataNode.addChild(idNode);

			// Builds reference nodes
			for (Reference ref : references) {
				metadataNode.addChild(new ReferenceSBMLNode(ref).getNode());
			}
		}
	}
}
