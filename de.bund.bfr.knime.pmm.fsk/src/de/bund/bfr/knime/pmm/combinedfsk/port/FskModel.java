package de.bund.bfr.knime.pmm.combinedfsk.port;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.bund.bfr.openfsmr.FSMRTemplate;

/**
 * FSK model.
 */
public class FskModel implements Serializable {

	private static final long serialVersionUID = 7918060170303915080L;

	private enum Key {
		MODEL, PARAM, VIZ
	};

	private HashMap<Key, String> stringFields = new HashMap<>(3);

	private FSMRTemplate template = null;
	private File workspace = null;
	private ArrayList<String> libraries = new ArrayList<>();

	// --- model script ---
	/**
	 * Return the model script.
	 *
	 * @return the model script
	 * @throws RuntimeException
	 *             if the model script is not set
	 */
	public String getModelScript() {
		return stringFields.get(Key.MODEL);
	}

	/**
	 * Sets the model script.
	 *
	 * @param script
	 *            Contents of the model script. Null or empty strings are
	 *            ignored.
	 */
	public void setModelScript(final String script) {
		if (script != null && !script.isEmpty())
			stringFields.put(Key.MODEL, script);
	}

	/**
	 * Unsets the model script.
	 */
	public void unsetModelScript() {
		stringFields.remove(Key.MODEL);
	}

	/**
	 * Returns whether the model script is set.
	 */
	public boolean isSetModelScript() {
		return stringFields.containsKey(Key.MODEL);
	}

	// --- parameters script ---
	/**
	 * Return the parameters script.
	 *
	 * @return the parameters script
	 * @throws RuntimeException
	 *             if the parameters script is not set
	 */
	public String getParametersScript() {
		return stringFields.get(Key.PARAM);
	}

	/**
	 * Sets the parameters script.
	 *
	 * @param script
	 *            Contents of the parameters script. Null or empty strings are
	 *            ignored.
	 */
	public void setParametersScript(final String script) {
		if (script != null && !script.isEmpty())
			stringFields.put(Key.PARAM, script);
	}

	/**
	 * Unsets the parameters script.
	 */
	public void unsetParametersScript() {
		stringFields.remove(Key.PARAM);
	}

	/**
	 * Returns whether the parameters script is set.
	 */
	public boolean isSetParametersScript() {
		return stringFields.containsKey(Key.PARAM);
	}

	// --- visualization script ---
	/**
	 * Returns the visualization script.
	 *
	 * @return the visualization script
	 * @throws RuntimeException
	 *             if the visualization script is not set
	 */
	public String getVisualizationScript() {
		return stringFields.get(Key.VIZ);
	}

	/**
	 * Sets the visualization script.
	 *
	 * @param visualizationScript
	 *            Contents of the visualization script. Null or empty strings
	 *            are ignored.
	 */
	public void setVisualizationScript(final String script) {
		if (script != null && !script.isEmpty())
			stringFields.put(Key.VIZ, script);
	}

	/**
	 * Unsets the visualization script.
	 */
	public void unsetVisualizationScript() {
		stringFields.remove(Key.VIZ);
	}

	/**
	 * Returns whether the visualization script is set.
	 */
	public boolean isSetVisualizationScript() {
		return stringFields.containsKey(Key.VIZ);
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
	 * @param template
	 *            null is ignored
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
	 * @throws RuntimeException
	 *             if the workspace is not set
	 */
	public File getWorkspace() {
		if (workspace == null)
			throw new RuntimeException("the workspace is not set");
		return workspace;
	}

	/**
	 * Sets the workspace.
	 *
	 * @param workspace
	 *            null is ignored
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
	 * @param libraries
	 *            empty lists are ignored
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