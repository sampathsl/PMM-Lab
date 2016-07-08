package de.bund.bfr.knime.pmm.combinedfsk.archive;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps the names of the files used in an FskModel. Data:
 * <ul>
 * <li>model identifier (mandatory)
 * <li>model script
 * <li>parameters script
 * <li>visualization script
 * <li>workspace
 * <li>meta data
 * </ul>
 * 
 * @author Miguel de Alba, BfR, Berlin.
 */
public class ModelFiles {

	private int id;

	private static final String MODEL_KEY = "model-script";
	private static final String PARAM_KEY = "parameters-script";
	private static final String VIZ_KEY = "visualization-script";
	private static final String WORKSPACE_KEY = "workspace";
	private static final String METADATA_KEY = "metadata";
	private final Map<String, String> optionalFiles = new HashMap<>();

	private String[] libraries;

	// --- id ---
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	// --- model script ---
	/**
	 * Returns the model script.
	 * 
	 * @return the model script.
	 * @throws NullPointerException
	 *             if the model script is not set
	 */
	public String getModelScript() {
		return optionalFiles.get(MODEL_KEY);
	}

	/**
	 * Sets the model script.
	 * 
	 * @param modelScript
	 *            null or empty strings are ignored
	 */
	public void setModelScript(final String script) {
		if (script != null && !script.isEmpty())
			optionalFiles.put(MODEL_KEY, script);
	}
	
	/**
	 * Unsets the model script.
	 */
	public void unsetModelScript() {
		optionalFiles.remove(MODEL_KEY);
	}
	
	/**
	 * Returns whether the model script is set.
	 * 
	 * @return whether the model script is set
	 */
	public boolean isSetModelScript() {
		return optionalFiles.containsKey(MODEL_KEY);
	}
	
	// --- parameters script ---
	/**
	 * Returns the parameters script.
	 *
	 * @return the parameters script
	 * @throws NullPointerException if the parameters script is not set
	 */
	public String getParametersScript() {
		return optionalFiles.get(PARAM_KEY);
	}

	/**
	 * Sets the parameters script.
	 *
	 * @param paramScript
	 *		null or empty strings are ignored
	 */
	public void setParametersScript(final String script) {
		if (script != null && !script.isEmpty())
			optionalFiles.put(PARAM_KEY, script);
	}

	/**
	 * Unsets the parameters script
	 */
	public void unsetParametersScript() {
		optionalFiles.remove(PARAM_KEY);
	}

	/**
	 * Returns whether the parameters script is set.
	 *
	 * @return whether the parameters script is set
	 */
	public boolean isSetParametersScript() {
		return optionalFiles.containsKey(PARAM_KEY);
	}

	// --- visualization script ---
	/**
	 * Returns the visualization script.
	 *
	 * @return the visualization script
	 * @throws NullPointerExceptionif the visualization script is not set
	 */
	public String getVisualizationScript() {
		return optionalFiles.get(VIZ_KEY);
	}

	/**
	 * Sets the visualization script.
	 *
	 * @param visualizationScript null or empty strings are ignored
	 */
	public void setVisualizationScript(final String visualizationScript) {
		if (visualizationScript != null && !visualizationScript.isEmpty())
			optionalFiles.put(VIZ_KEY, visualizationScript);
	}

	/**
	 * Unsets the visualization script.
	 */
	public void unsetsVisualizationScript() {
		optionalFiles.remove(VIZ_KEY);
	}

	/**
	 * Returns whether the visualization script is set.
	 *
	 * @return whether the visualization script is set
	 */
	public boolean isSetVisualizationScript() {
		return optionalFiles.containsKey(VIZ_KEY);
	}

	// --- workspace ---
	/**
	 * Returns the workspace.
	 *
	 * @return the workspace
	 * @throws NullPointerException if the workspace is not set
	 */
	public String getWorkspace() {
		return optionalFiles.get(WORKSPACE_KEY);
	}

	/**
	 * Sets the workspace.
	 *
	 * @param workspace null or empty strings are ignored
	 */
	public void setWorkspace(final String workspace) {
		if (workspace != null && !workspace.isEmpty())
			optionalFiles.put(WORKSPACE_KEY, workspace);
	}

	/**
	 * Unsets the workspace.
	 */
	public void unsetWorkspace() {
		optionalFiles.remove(WORKSPACE_KEY);
	}

	/**
	 * Returns whether the workspace is set.
	 *
	 * @return whether the workspace is set
	 */
	public boolean isSetWorkspace() {
		return optionalFiles.containsKey(WORKSPACE_KEY);
	}

	// --- meta data ---
	/**
	 * Returns the meta data.
	 *
	 * @return the meta data
	 * @throws NullPointerException if the meta data is not set
	 */
	public String getMetaData() {
		return optionalFiles.get(METADATA_KEY);
	}

	/**
	 * Sets the meta data.
	 *
	 * @param metadata null or empty strings are ignored
	 */
	public void setMetaData(final String metaData) {
		if (metaData != null && !metaData.isEmpty())
			optionalFiles.put(METADATA_KEY, metaData);
	}

	/**
	 * Unsets the meta data.
	 */
	public void unsetMetaData() {
		optionalFiles.remove(METADATA_KEY);
	}

	/**
	 * Returns whether the meta data is set.
	 *
	 * @return whether the meta data is set
	 */
	public boolean isSetMetaData() {
		return optionalFiles.containsKey(METADATA_KEY);
	}
	
	// --- libraries ---
	/**
	 * Returns the libraries.
	 *
	 * @return the libraries
	 * @throw NullPointerException if the libraries are not set.
	 */
	public String[] getLibraries() {
		if (libraries == null)
			throw new RuntimeException("libraries not set");
		return libraries;
	}

	/**
	 * Sets the libraries.
	 *
	 * @param libraries null or empty arrays are ignored
	 */
	public void setLibraries(String[] libraries) {
		if (libraries != null && libraries.length > 0)
			this.libraries = libraries;
	}

	/**
	 * Unsets the libraries.
	 */
	public void unsetLibraries() {
		libraries = null;
	}

	/**
	 * Returns whether the libraries are set.
	 *
	 * @return whether the libraries are set
	 */
	public boolean isSetLibraries() {
		return libraries != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + Arrays.hashCode(libraries);
		result = prime * result + ((optionalFiles == null) ? 0 : optionalFiles.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelFiles other = (ModelFiles) obj;
		if (id != other.id)
			return false;
		if (!Arrays.equals(libraries, other.libraries))
			return false;
		if (optionalFiles == null) {
			if (other.optionalFiles != null)
				return false;
		} else if (!optionalFiles.equals(other.optionalFiles))
			return false;
		return true;
	}
}
