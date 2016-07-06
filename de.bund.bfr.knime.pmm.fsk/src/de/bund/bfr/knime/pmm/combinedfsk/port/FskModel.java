package de.bund.bfr.knime.pmm.combinedfsk.port;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

import de.bund.bfr.openfsmr.FSMRTemplate;

/**
 * FSK model.
 */
public class FskModel implements Serializable {

	private String modelScript;
	private String parametersScript;
	private String visualizationScript;
	private FSMRTemplate template;
	private File workspace;
	private Set<File> libraries;

	public FskModel(final String modelScript, final String parametersScript,
		final String visualizationScript, final FSMRTemplate template,
		final File workspace, final Set<File> libraries) {

		this.modelScript = modelScript;
		this.parametersScript = parametersScript;
		this.visualizationScript = visualizationScript;
		this.template = template;
		this.workspace = workspace;
		this.libraries = libraries;
	}

	/**
	 * Return the model script. Null if not set.
	 *
	 * @return the model script
	 */
	public String getModelScript() {
		return modelScript;
	}

	/**
	 * Sets the model script.
	 */
	public void setModelScript(final String modelScript) {
		this.modelScript = modelScript;
	}

	/**
	 * Return the parameters script. Null if not set.
	 *
	 * @return the parameters script
	 */
	public String getParametersScript() {
		return parametersScript;
	}

	/**
	 * Sets the parameters script.
	 */
	public void setParametersScript(final String parametersScript) {
		this.parametersScript = parametersScript;
	}

	/**
	 * Returns the visualization script. Null if not set.
	 *
	 * @return the visualization script
	 */
	public String getVisualizationScript() {
		return visualizationScript;
	}

	/**
	 * Sets the visualization script.
	 */
	public void setVisualizationScript(final String visualizationScript) {
		this.visualizationScript = visualizationScript;
	}

	/**
	 * Returns the FSMR template with model meta data.
	 */
	public FSMRTemplate getTemplate() {
		return template;
	}

	/**
	 * Sets the FSMR template with model meta data.
	 */
	public void setTemplate(final FSMRTemplate template) {
		this.template = template;
	}

	/**
	 * Return the workspace.
	 */
	public File getWorkspace() {
		return workspace;
	}

	/**
	 * Sets the workspace
	 */
	public void setWorkspace(final File workspace) {
		this.workspace = workspace;
	}

	/**
	 * Return the R library files.
	 */
	public Set<File> getLibraries() {
		return libraries;
	}

	/**
	 * Sets the R library files.
	 */
	public void setLibraries(final Set<File> libraries) {
		this.libraries = libraries;
	}
}