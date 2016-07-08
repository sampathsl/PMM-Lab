package de.bund.bfr.knime.pmm.combinedfsk.archive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	private static enum Key {
		MODEL, PARAM, VIZ, WORKSPACE, METADATA
	};

	private final Map<Key, String> optionalFiles = new HashMap<>();

	private ArrayList<String> libraries = new ArrayList<>();

	// --- id ---
	public int getId() {
		return id;
	}

	public void setId(final int id) {
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
		return optionalFiles.get(Key.MODEL);
	}

	/**
	 * Sets the model script.
	 * 
	 * @param modelScript
	 *            null or empty strings are ignored
	 */
	public void setModelScript(final String script) {
		if (script != null && !script.isEmpty())
			optionalFiles.put(Key.MODEL, script);
	}

	/**
	 * Unsets the model script.
	 */
	public void unsetModelScript() {
		optionalFiles.remove(Key.MODEL);
	}

	/**
	 * Returns whether the model script is set.
	 * 
	 * @return whether the model script is set
	 */
	public boolean isSetModelScript() {
		return optionalFiles.containsKey(Key.MODEL);
	}

	// --- parameters script ---
	/**
	 * Returns the parameters script.
	 *
	 * @return the parameters script
	 * @throws NullPointerException
	 *             if the parameters script is not set
	 */
	public String getParametersScript() {
		return optionalFiles.get(Key.PARAM);
	}

	/**
	 * Sets the parameters script.
	 *
	 * @param paramScript
	 *            null or empty strings are ignored
	 */
	public void setParametersScript(final String script) {
		if (script != null && !script.isEmpty())
			optionalFiles.put(Key.PARAM, script);
	}

	/**
	 * Unsets the parameters script
	 */
	public void unsetParametersScript() {
		optionalFiles.remove(Key.PARAM);
	}

	/**
	 * Returns whether the parameters script is set.
	 *
	 * @return whether the parameters script is set
	 */
	public boolean isSetParametersScript() {
		return optionalFiles.containsKey(Key.PARAM);
	}

	// --- visualization script ---
	/**
	 * Returns the visualization script.
	 *
	 * @return the visualization script
	 * @throws NullPointerExceptionif
	 *             the visualization script is not set
	 */
	public String getVisualizationScript() {
		return optionalFiles.get(Key.VIZ);
	}

	/**
	 * Sets the visualization script.
	 *
	 * @param visualizationScript
	 *            null or empty strings are ignored
	 */
	public void setVisualizationScript(final String visualizationScript) {
		if (visualizationScript != null && !visualizationScript.isEmpty())
			optionalFiles.put(Key.VIZ, visualizationScript);
	}

	/**
	 * Unsets the visualization script.
	 */
	public void unsetsVisualizationScript() {
		optionalFiles.remove(Key.VIZ);
	}

	/**
	 * Returns whether the visualization script is set.
	 *
	 * @return whether the visualization script is set
	 */
	public boolean isSetVisualizationScript() {
		return optionalFiles.containsKey(Key.VIZ);
	}

	// --- workspace ---
	/**
	 * Returns the workspace.
	 *
	 * @return the workspace
	 * @throws NullPointerException
	 *             if the workspace is not set
	 */
	public String getWorkspace() {
		return optionalFiles.get(Key.WORKSPACE);
	}

	/**
	 * Sets the workspace.
	 *
	 * @param workspace
	 *            null or empty strings are ignored
	 */
	public void setWorkspace(final String workspace) {
		if (workspace != null && !workspace.isEmpty())
			optionalFiles.put(Key.WORKSPACE, workspace);
	}

	/**
	 * Unsets the workspace.
	 */
	public void unsetWorkspace() {
		optionalFiles.remove(Key.WORKSPACE);
	}

	/**
	 * Returns whether the workspace is set.
	 *
	 * @return whether the workspace is set
	 */
	public boolean isSetWorkspace() {
		return optionalFiles.containsKey(Key.WORKSPACE);
	}

	// --- meta data ---
	/**
	 * Returns the meta data.
	 *
	 * @return the meta data
	 * @throws NullPointerException
	 *             if the meta data is not set
	 */
	public String getMetaData() {
		return optionalFiles.get(Key.METADATA);
	}

	/**
	 * Sets the meta data.
	 *
	 * @param metadata
	 *            null or empty strings are ignored
	 */
	public void setMetaData(final String metaData) {
		if (metaData != null && !metaData.isEmpty())
			optionalFiles.put(Key.METADATA, metaData);
	}

	/**
	 * Unsets the meta data.
	 */
	public void unsetMetaData() {
		optionalFiles.remove(Key.METADATA);
	}

	/**
	 * Returns whether the meta data is set.
	 *
	 * @return whether the meta data is set
	 */
	public boolean isSetMetaData() {
		return optionalFiles.containsKey(Key.METADATA);
	}

	// --- libraries ---
	/**
	 * Returns the libraries. If not set returns empty list.
	 *
	 * @return the libraries
	 */
	public List<String> getLibraries() {
		return libraries;
	}

	/**
	 * Sets the libraries.
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
	 * Unsets the libraries.
	 */
	public void unsetLibraries() {
		libraries.clear();
	}

	/**
	 * Returns whether the libraries are set.
	 *
	 * @return whether the libraries are set
	 */
	public boolean isSetLibraries() {
		return !libraries.isEmpty();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((libraries == null) ? 0 : libraries.hashCode());
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
		if (libraries == null) {
			if (other.libraries != null)
				return false;
		} else if (!libraries.equals(other.libraries))
			return false;
		if (optionalFiles == null) {
			if (other.optionalFiles != null)
				return false;
		} else if (!optionalFiles.equals(other.optionalFiles))
			return false;
		return true;
	}
}
