package de.bund.bfr.knime.pmm.combinedfsk.archive;

import org.jdom2.Element;

/**
 * Node envolving a Link.
 * 
 * @author Miguel de Alba, Bfr, Berlin.
 */
public class LinkNode {

	// Attribute names
	private static final String ORIG_MODEL = "origin-model";
	private static final String ORIG_VAR = "origin-var";
	private static final String DEST_MODEL = "dest-model";
	private static final String DEST_VAR = "dest-var";

	private final Element element;

	public LinkNode(final Link link) {
		element = new Element("link");
		element.setAttribute(ORIG_MODEL, Integer.toString(link.originModelId));
		element.setAttribute(ORIG_VAR, link.originVar);
		element.setAttribute(DEST_MODEL, Integer.toString(link.destModelId));
		element.setAttribute(DEST_VAR, link.destVar);
	}

	public LinkNode(final Element element) {
		this.element = element;
	}

	public Link getLink() {
		int originModel = Integer.parseInt(element.getAttributeValue(ORIG_MODEL));
		String originVar = element.getAttributeValue(ORIG_VAR);
		int destModel = Integer.parseInt(element.getAttributeValue(DEST_MODEL));
		String destVar = element.getAttributeValue(DEST_VAR);

		return new Link(originModel, originVar, destModel, destVar);
	}

	// --- element ---
	public Element getElement() {
		return element;
	}
}
