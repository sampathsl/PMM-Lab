package de.bund.bfr.knime.pmm.combinedfsk.archive;

import org.jdom2.Element;

/**
 * Node that keeps the name of a library used by a {@link ModelFilesNode}.
 * <p>
 * E.g: {@code <library>triangle</library>}
 * 
 * @author Miguel Alba, BfR, Berlin.
 */
public class LibraryNode {

	private final Element element;

	public LibraryNode(final String library) {
		element = new Element("library");
		element.setText(library);
	}

	public LibraryNode(final Element element) {
		this.element = element;
	}

	public String getLibrary() {
		return element.getText();
	}

	public Element getElement() {
		return element;
	}
}