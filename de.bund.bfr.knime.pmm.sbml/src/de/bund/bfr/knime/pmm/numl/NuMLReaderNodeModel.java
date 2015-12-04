package de.bund.bfr.knime.pmm.numl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import de.bund.bfr.knime.pmm.common.AgentXml;
import de.bund.bfr.knime.pmm.common.LiteratureItem;
import de.bund.bfr.knime.pmm.common.MatrixXml;
import de.bund.bfr.knime.pmm.common.MdInfoXml;
import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.TimeSeriesXml;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeSchema;
import de.bund.bfr.knime.pmm.extendedtable.TimeSeriesMetadata;
import de.bund.bfr.knime.pmm.extendedtable.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.extendedtable.pmmtablemodel.SchemaFactory;
import de.bund.bfr.knime.pmm.common.units.Categories;
import de.bund.bfr.knime.pmm.common.units.UnitsFromDB;
import de.bund.bfr.knime.pmm.dbutil.DBUnits;
import de.bund.bfr.knime.pmm.extendedtable.items.MDAgentXml;
import de.bund.bfr.knime.pmm.extendedtable.items.MDLiteratureItem;
import de.bund.bfr.knime.pmm.extendedtable.items.MDMatrixXml;
import de.bund.bfr.knime.pmm.extendedtable.pmmtablemodel.TimeSeriesSchema;
import de.bund.bfr.pmf.numl.NuMLDocument;
import de.bund.bfr.pmf.numl.NuMLReader;
import de.bund.bfr.pmf.numl.Tuple;
import de.bund.bfr.pmf.sbml.ModelVariable;
import de.bund.bfr.pmf.sbml.PMFCompartment;
import de.bund.bfr.pmf.sbml.PMFSpecies;
import de.bund.bfr.pmf.sbml.Reference;
import de.bund.bfr.pmf.sbml.ReferenceType;

public class NuMLReaderNodeModel extends NodeModel {

	// configuration keys
	public static final String CFGKEY_FILE = "filename";

	// defaults for persistent state
	private static final String DEFAULT_FILE = "c:/temp/foo.numl";

	// persistent state
	private SettingsModelString filename = new SettingsModelString(CFGKEY_FILE, DEFAULT_FILE);

	/**
	 * Constructor for the node model
	 */
	protected NuMLReaderNodeModel() {
		// 0 input ports and 1 input port
		super(0, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		File file = new File(filename.getStringValue());
		NuMLDocument doc = NuMLReader.read(file);
		KnimeTuple tuple = new DataTuple(doc).tuple;

		DataTableSpec dataSpec = SchemaFactory.createDataSchema().createSpec();
		BufferedDataContainer dataContainer = exec.createDataContainer(dataSpec);
		dataContainer.addRowToTable(tuple);
		dataContainer.close();

		BufferedDataTable[] table = { dataContainer.getTable() };
		return table;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		return new DataTableSpec[] { null };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		filename.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		filename.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		filename.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}
}

class DataTuple {

	KnimeTuple tuple;
	// time series schema
	// private static final KnimeSchema schema =
	// SchemaFactory.createDataSchema();

	public DataTuple(NuMLDocument doc) {

		int condID = doc.getResultComponent().getCondID();
		String combaseID = (doc.getResultComponent().isSetCombaseID()) ? doc.getResultComponent().getCombaseID() : "?";

		String timeUnit = doc.getTimeOntologyTerm().getUnitDefinition().getName();
		String concUnit = doc.getConcentrationOntologyTerm().getUnitDefinition().getName();

		// Gets concentration unit object type from DB
		UnitsFromDB ufdb = DBUnits.getDBUnits().get(concUnit);
		String concUnitObjectType = ufdb.getObject_type();

		PMFSpecies species = doc.getConcentrationOntologyTerm().getSpecies();
		AgentXml originalAgentXml = new AgentXml();
		originalAgentXml.setName(species.getName());
		MDAgentXml agentXml = new MDAgentXml();
		agentXml.setName(species.getName());
		if (species.isSetDetail()) {
			originalAgentXml.setDetail(species.getDetail());
			agentXml.setDetail(species.getDetail());
		}

		PMFCompartment compartment = doc.getConcentrationOntologyTerm().getCompartment();
		MatrixXml originalMatrixXml = new MatrixXml();
		MDMatrixXml matrixXml = new MDMatrixXml();
		originalMatrixXml.setName(compartment.getName());
		matrixXml.setName(compartment.getName());
		if (compartment.isSetDetail()) {
			originalMatrixXml.setDetail(compartment.getDetail());
			matrixXml.setDetail(compartment.getDetail());
		}

		// Gets time series
		Tuple[] dimensions = doc.getResultComponent().getDimensions();
		double[][] data = new double[dimensions.length][2];
		for (int i = 0; i < dimensions.length; i++) {
			Tuple tuple = dimensions[i];
			data[i] = new double[] { tuple.getConcValue().getValue(), tuple.getTimeValue().getValue() };
		}
		PmmXmlDoc mdData = Util.createTimeSeries(timeUnit, concUnit, concUnitObjectType, data);

		// Gets model variables
		ModelVariable[] modelVariables = compartment.getModelVariables();
		Map<String, Double> miscs = new HashMap<>(modelVariables.length);
		for (ModelVariable modelVariable : modelVariables) {
			miscs.put(modelVariable.getName(), modelVariable.getValue());
		}
		PmmXmlDoc miscDoc = Util.parseMiscs(miscs);

		// Creates empty model info
		MdInfoXml mdInfo = new MdInfoXml(null, null, null, null, null);

		TimeSeriesMetadata metadata = new TimeSeriesMetadata();
		metadata.setAgentXml(agentXml);
		metadata.setMatrixXml(matrixXml);

		// Gets literature items
		PmmXmlDoc litDoc = new PmmXmlDoc();
		for (Reference reference : doc.getResultComponent().getReferences()) {
			String author = (reference.isSetAuthor()) ? reference.getAuthor() : null;
			Integer year = (reference.isSetYear()) ? reference.getYear() : null;
			String title = (reference.isSetTitle()) ? reference.getTitle() : null;
			String abstractText = (reference.isSetAbstractText()) ? reference.getAbstractText() : null;
			String journal = (reference.isSetJournal()) ? reference.getJournal() : null;
			String volume = (reference.isSetVolume()) ? reference.getVolume() : null;
			String issue = (reference.isSetIssue()) ? reference.getIssue() : null;
			Integer page = (reference.isSetPage()) ? reference.getPage() : null;
			Integer approvalMode = (reference.isSetApprovalMode()) ? reference.getApprovalMode() : null;
			String website = (reference.isSetWebsite()) ? reference.getWebsite() : null;
			ReferenceType type = (reference.isSetType()) ? reference.getType() : null;
			String comment = (reference.isSetComment()) ? reference.getComment() : null;

			LiteratureItem lit = new LiteratureItem(author, year, title, abstractText, journal, volume, issue, page,
					approvalMode, website, type.value(), comment);
			litDoc.add(lit);

			MDLiteratureItem mdLit = new MDLiteratureItem(author, year, title, abstractText, journal, volume, issue,
					page, approvalMode, website, type.value(), comment);
			metadata.addLiteratureItem(mdLit);
		}

		// Creates and fills tuple
		KnimeSchema schema = SchemaFactory.createDataSchema();
		tuple = new KnimeTuple(schema);
		tuple.setValue(TimeSeriesSchema.ATT_CONDID, condID);
		tuple.setValue(TimeSeriesSchema.ATT_COMBASEID, combaseID);
		tuple.setValue(TimeSeriesSchema.ATT_AGENT, new PmmXmlDoc(originalAgentXml));
		tuple.setValue(TimeSeriesSchema.ATT_MATRIX, new PmmXmlDoc(originalMatrixXml));
		tuple.setValue(TimeSeriesSchema.ATT_TIMESERIES, mdData);
		tuple.setValue(TimeSeriesSchema.ATT_MISC, miscDoc);
		tuple.setValue(TimeSeriesSchema.ATT_MDINFO, new PmmXmlDoc(mdInfo));
		tuple.setValue(TimeSeriesSchema.ATT_LITMD, litDoc);
		tuple.setValue(TimeSeriesSchema.ATT_DBUUID, "?");
		tuple.setValue(TimeSeriesSchema.ATT_METADATA, metadata);
	}
}

class Util {
	private Util() {
	}

	/**
	 * Parses misc items.
	 * 
	 * @param miscs
	 *            . Dictionary that maps miscs names and their values.
	 * @return
	 */
	public static PmmXmlDoc parseMiscs(Map<String, Double> miscs) {
		PmmXmlDoc cell = new PmmXmlDoc();

		if (miscs != null) {
			// First misc item has id -1 and the rest of items have negative
			// ints
			int counter = -1;
			for (Entry<String, Double> entry : miscs.entrySet()) {
				String name = entry.getKey();
				Double value = entry.getValue();

				List<String> categories;
				String description, unit;

				switch (name) {
				case "Temperature":
					categories = Arrays.asList(Categories.getTempCategory().getName());
					description = name;
					unit = Categories.getTempCategory().getStandardUnit();

					cell.add(new MiscXml(counter, name, description, value, categories, unit));

					counter -= 1;
					break;

				case "pH":
					categories = Arrays.asList(Categories.getPhCategory().getName());
					description = name;
					unit = Categories.getPhUnit();

					cell.add(new MiscXml(counter, name, description, value, categories, unit));

					counter -= 1;
					break;
				}
			}
		}
		return cell;
	}
	
	/**
	 * Creates time series
	 */
	public static PmmXmlDoc createTimeSeries(String timeUnit, String concUnit, String concUnitObjectType,
			double[][] data) {

		PmmXmlDoc mdData = new PmmXmlDoc();

		Double concStdDev = null;
		Integer numberOfMeasurements = null;

		for (double[] point : data) {
			double conc = point[0];
			double time = point[1];
			String name = "t" + mdData.size();

			TimeSeriesXml t = new TimeSeriesXml(name, time, timeUnit, conc, concUnit, concStdDev, numberOfMeasurements);
			t.setConcentrationUnitObjectType(concUnitObjectType);
			mdData.add(t);
		}

		return mdData;
	}
}