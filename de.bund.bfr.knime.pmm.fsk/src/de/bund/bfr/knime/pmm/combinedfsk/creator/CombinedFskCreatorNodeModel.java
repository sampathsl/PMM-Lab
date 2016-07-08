package de.bund.bfr.knime.pmm.combinedfsk.creator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.util.FileUtil;
import org.rosuda.REngine.REXPMismatchException;

import com.google.common.base.Strings;

import de.bund.bfr.knime.pmm.FSMRUtils;
import de.bund.bfr.knime.pmm.combinedfsk.port.CombinedFskPortObject;
import de.bund.bfr.knime.pmm.combinedfsk.port.CombinedFskPortObjectSpec;
import de.bund.bfr.knime.pmm.combinedfsk.port.FskModel;
import de.bund.bfr.knime.pmm.combinedfsk.port.VariableLink;
import de.bund.bfr.knime.pmm.common.KnimeUtils;
import de.bund.bfr.knime.pmm.fskx.RScript;
import de.bund.bfr.knime.pmm.fskx.controller.IRController.RException;
import de.bund.bfr.knime.pmm.fskx.controller.LibRegistry;
import de.bund.bfr.openfsmr.FSMRTemplate;

public class CombinedFskCreatorNodeModel extends NodeModel {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(CombinedFskCreatorNodeModel.class);

	// configuration keys
	static final String LIB_DIR = "libraries directory";
	static final String LIBRARIES = "libraries";
	static final String MODEL_SCRIPT = "model script";
	static final String PARAM_SCRIPT = "parameters script";
	static final String VIZ_SCRIPT = "visualization script";
	static final String METADATA = "metadata";

	private final static PortType[] inPortTypes = new PortType[] {};
	private final static PortType[] outPortTypes = new PortType[] { CombinedFskPortObject.TYPE };

	// settings models
	private final SettingsModelString modelScript = new SettingsModelString(MODEL_SCRIPT, "");
	private final SettingsModelString paramScript = new SettingsModelString(PARAM_SCRIPT, "");
	private final SettingsModelString vizScript = new SettingsModelString(VIZ_SCRIPT, "");
	private final SettingsModelString metadata = new SettingsModelString(METADATA, "");
	private final SettingsModelString libDirectory = new SettingsModelString(LIB_DIR, "");
	private final SettingsModelStringArray libraries = new SettingsModelStringArray(LIBRARIES, new String[0]);

	public CombinedFskCreatorNodeModel() {
		super(inPortTypes, outPortTypes);
	}

	// --- NodeModel ---
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// nothing
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// nothing
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		modelScript.saveSettingsTo(settings);
		paramScript.saveSettingsTo(settings);
		vizScript.saveSettingsTo(settings);
		metadata.saveSettingsTo(settings);
		libDirectory.saveSettingsTo(settings);
		libraries.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		modelScript.validateSettings(settings);
		paramScript.validateSettings(settings);
		vizScript.validateSettings(settings);
		metadata.validateSettings(settings);
		libDirectory.validateSettings(settings);
		libraries.validateSettings(settings);
	}

	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		modelScript.loadSettingsFrom(settings);
		paramScript.loadSettingsFrom(settings);
		vizScript.loadSettingsFrom(settings);
		metadata.loadSettingsFrom(settings);
		libDirectory.loadSettingsFrom(settings);
		libraries.loadSettingsFrom(settings);
	};

	@Override
	protected void reset() {
		// nothing nothing
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {

		FskModel fskModel = new FskModel();

		// Reads model script
		try {
			String model = readScript(modelScript.getStringValue()).getScript();
			fskModel.setModelScript(model);
		} catch (IOException error) {
			error.printStackTrace();
			LOGGER.warn(error.getMessage());
		}

		// Reads parameters script
		try {
			String param = readScript(paramScript.getStringValue()).getScript();
			fskModel.setParametersScript(param);
		} catch (IOException error) {
			error.printStackTrace();
			LOGGER.warn(error.getMessage());
		}

		// Reads visualization script
		try {
			String viz = readScript(vizScript.getStringValue()).getScript();
			fskModel.setVisualizationScript(viz);
		} catch (IOException error) {
			error.printStackTrace();
			LOGGER.warn(error.getMessage());
		}

		// Reads model meta data
		try (InputStream is = FileUtil.openInputStream(metadata.getStringValue())) {
			// Finds the workbook instance for XLSX file
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			FSMRTemplate template = FSMRUtils.processSpreadsheet(workbook);
			fskModel.setTemplate(template);
		}

		// Reads R libraries
		if (libraries.getStringArrayValue() != null && libraries.getStringArrayValue().length > 0) {
			try {
				List<String> libNames = Arrays.stream(libraries.getStringArrayValue())
						.map(fullName -> fullName.split("\\.")[0]).collect(Collectors.toList());

				// Only install missing libraries
				LibRegistry libRegistry = LibRegistry.instance();
				List<String> missingLibs = libNames.stream().filter(lib -> !libRegistry.isInstalled(lib))
						.collect(Collectors.toList());
				if (!missingLibs.isEmpty()) {
					libRegistry.installLibs(missingLibs);
				}

				// Sets libraries in the FSK model
				fskModel.setLibraries(libNames);
			} catch (RException | REXPMismatchException e) {
				LOGGER.error(e.getMessage());
			}
		}

		// Return port object
		CombinedFskPortObject portObject = new CombinedFskPortObject(Arrays.asList(fskModel),
				new ArrayList<VariableLink>());
		return new PortObject[] { portObject };
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] { CombinedFskPortObjectSpec.INSTANCE };
	}

	// --- utility methods ---
	/**
	 * Reads R script.
	 * 
	 * @param path
	 *            File path to R model script
	 * @throws InvalidSettingsException
	 *             if {@link path} is null or whitespace
	 * @throws IOException
	 *             if the file cannot be read
	 */
	private static RScript readScript(final String path) throws InvalidSettingsException, IOException {

		// throws InvalidSettingsException if path is null
		if (path == null) {
			throw new InvalidSettingsException("Unespecified script");
		}

		// throws InvalidSettingsException if path is whitespace
		String trimmedPath = Strings.emptyToNull(path.trim());
		if (trimmedPath == null) {
			throw new InvalidSettingsException("Unespecified model script");
		}

		// path is not null or whitespace, thus try to read it

		try {
			// may throw IOException
			return new RScript(KnimeUtils.getFile(trimmedPath));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(trimmedPath + ": cannot be read");
		}
	}
}
