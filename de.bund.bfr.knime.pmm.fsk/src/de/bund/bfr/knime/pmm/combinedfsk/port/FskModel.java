package de.bund.bfr.knime.pmm.combinedfsk.port;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.bund.bfr.openfsmr.FSMRTemplate;

/**
 * FSK model.
 */
public class FskModel implements Serializable {

	private static final long serialVersionUID = 7918060170303915080L;
	
	private String modelScript = null;
	private String parametersScript = null;
	private String visualizationScript = null;
	private FSMRTemplate template = null;
	private File workspace = null;
	private List<String> libraries = new ArrayList<>();

	// --- model script ---
	/**
	 * Return the model script.
	 *
	 * @return the model script
	 * @throws RuntimeException if the model script is not set
	 */
	public String getModelScript() {
		if (modelScript == null)
			throw new RuntimeException("the model script is not set");
		return modelScript;
	}

	/**
	 * Sets the model script.
	 *
	 * @param modelScript
	 * 		Contents of the model script. Null or empty strings are ignored.
	 */
	public void setModelScript(final String modelScript) {
		if (modelScript != null && !modelScript.isEmpty())
			this.modelScript = modelScript;	
	}

	/**
	 * Unsets the model script.
	 */
	public void unsetModelScript() {
		this.modelScript = null;
	}

	/**
	 * Returns whether the model script is set.
	 */
	public boolean isSetModelScript() {
		return modelScript != null;
	}

	// --- parameters script ---
	/**
	 * Return the parameters script.
	 *
	 * @return the parameters script
	 * @throws RuntimeException if the parameters script is not set
	 */
	public String getParametersScript() {
		if (parametersScript == null)
			throw new RuntimeException("the parameters script is not set");
		return parametersScript;
	}

	/**
	 * Sets the parameters script.
	 *
	 * @param parametersScript
	 *		Contents of the parameters script. Null or empty strings are ignored.
	 */
	public void setParametersScript(final String parametersScript) {
		if (parametersScript != null && !parametersScript.isEmpty())
			this.parametersScript = parametersScript;
	}

	/**
	 * Unsets the parameters script.
	 */
	public void unsetParametersScript() {
		this.parametersScript = null;
	}

	/**
	 * Returns whether the parameters script is set.
	 */
	public boolean isSetParametersScript() {
		return parametersScript != null;
	}

	// --- visualization script ---
	/**
	 * Returns the visualization script.
	 *
	 * @return the visualization script
	 * @throws RuntimeException if the visualization script is not set
	 */
	public String getVisualizationScript() {
		if (visualizationScript == null)
			throw new RuntimeException("the visualization script is not set");
		return visualizationScript;
	}

	/**
	 * Sets the visualization script.
	 *
	 * @param visualizationScript
	 *		Contents of the visualization script. Null or empty strings are
	 *		ignored.
	 */
	public void setVisualizationScript(final String visualizationScript) {
		if (visualizationScript != null && !visualizationScript.isEmpty()) 
			this.visualizationScript = visualizationScript;
	}

	/**
	 * Unsets the visualization script.
	 */
	public void unsetVisualizationScript() {
		this.visualizationScript = null;
	}

	/**
	 * Returns whether the visualization script is set.
	 */
	public boolean isSetVisualizationScript() {
		return visualizationScript != null;
	}

	/**
	 * Returns the FSMR template with model meta data.
	 */
	public FSMRTemplate getTemplate() {
		return template;
	}

	/**
	 * Sets the FSMR template with model meta data.
	 *
	 * @param template null is ignored
	 */
	public void setTemplate(final FSMRTemplate template) {
		if (template != null)
			this.template = template;
	}

	/**
	 * Unsets the FSMR template.
	 */
	public void unsetTemplate() {
		template = null;
	}

	/**
	 * Returns whether the FSMR template is set.
	 */
	public boolean isSetTemplate() {
		return template != null;
	}

	// --- workspace file ---
	/**
	 * Return the workspace.
	 * 
	 * @return the workspace
	 * @throws RuntimeException if the workspace is not set
	 */
	public File getWorkspace() {
		if (workspace == null)
			throw new RuntimeException("the workspace is not set");
		return workspace;
	}

	/**
	 * Sets the workspace.
	 *
	 * @param workspace null is ignored
	 */
	public void setWorkspace(final File workspace) {
		if (workspace == null)
			this.workspace = workspace;
	}

	/**
	 * Unsets the workspace.
	 */
	public void unsetWorkspace() {
		workspace = null;
	}

	/**
	 * Returns whether the workspace is set.
	 */
	public boolean isSetWorkspace() {
		return workspace != null;
	}

	// --- libraries ---
	/**
	 * Returns the R library file names. If not set returns empty list.
	 * 
	 * @return the R library file names
	 */
	public List<String> getLibraries() {
		return libraries;
	}

	/**
	 * Sets the R library files.
	 * 
	 * @param libraries empty lists are ignored
	 */
	public void setLibraries(final List<String> libraries) {
		if (!libraries.isEmpty()) {
			this.libraries.clear();
			this.libraries.addAll(libraries);
		}
	}
	
	/**
	 * Unsets the R library files.
	 */
	public void unsetLibraries() {
		libraries.clear();
	}
	
	/**
	 * Return whether the libraries are set.
	 * 
	 * @return whether the libraries are set
	 */
	public boolean isSetLibraries() {
		return !libraries.isEmpty();
	}
}