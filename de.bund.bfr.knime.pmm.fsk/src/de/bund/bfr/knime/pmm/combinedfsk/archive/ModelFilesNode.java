package de.bund.bfr.knime.pmm.combinedfsk.archive;

import java.util.List;
import java.util.stream.Collectors;

import org.jdom2.Element;

/**
 * Nodes envolving a ModelFiles.
 * 
 * @author Miguel de Alba, BfR, Berlin.
 */
public class ModelFilesNode {

	// Attribute names
	private static final String ID = "id";
	private static final String MODEL = "model-script";
	private static final String PARAM = "parameters-script";
	private static final String VIZ = "visualization-script";
	private static final String WORKSPACE = "workspace";
	private static final String METADATA = "metadata";

	private final Element element;

	public ModelFilesNode(final ModelFiles model) {
		element = new Element("model");
		element.setAttribute(ID, Integer.toString(model.getId()));

		if (model.isSetModelScript())
			element.setAttribute(MODEL, model.getModelScript());

		if (model.isSetParametersScript())
			element.setAttribute(PARAM, model.getParametersScript());

		if (model.isSetVisualizationScript())
			element.setAttribute(VIZ, model.getVisualizationScript());

		if (model.isSetWorkspace())
			element.setAttribute(WORKSPACE, model.getWorkspace());

		if (model.isSetMetaData())
			element.setAttribute(METADATA, model.getMetaData());

		if (model.isSetLibraries()) {
			model.getLibraries().stream().map(LibraryNode::new).map(LibraryNode::getElement)
					.forEach(element::addContent);
		}
	}

	public ModelFilesNode(final Element element) {
		this.element = element;
	}

	public ModelFiles getModel() {
		ModelFiles modelFiles = new ModelFiles();
		modelFiles.setId(Integer.parseInt(element.getAttributeValue(ID)));
		modelFiles.setModelScript(element.getAttributeValue(MODEL));
		modelFiles.setParametersScript(element.getAttributeValue(PARAM));
		modelFiles.setVisualizationScript(element.getAttributeValue(VIZ));
		modelFiles.setMetaData(element.getAttributeValue(METADATA));
		modelFiles.setWorkspace(element.getAttributeValue(WORKSPACE));

		List<Element> libElements = element.getChildren("library");
		if (!libElements.isEmpty()) {
			List<String> libs = libElements.stream().map(LibraryNode::new).map(LibraryNode::getLibrary)
					.collect(Collectors.toList());
			modelFiles.setLibraries(libs);
		}

		return modelFiles;
	}

	// --- element ---
	public Element getElement() {
		return element;
	}
}
