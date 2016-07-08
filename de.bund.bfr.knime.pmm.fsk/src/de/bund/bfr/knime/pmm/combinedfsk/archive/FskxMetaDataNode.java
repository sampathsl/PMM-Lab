package de.bund.bfr.knime.pmm.combinedfsk.archive;

import org.jdom2.Element;

/**
 * Meta data node for Combine FSKX archives.
 *
 * @author Miguel Alba, BfR, Berlin.
 */
public class FskxMetaDataNode {

	private final Element element;

	public FskxMetaDataNode(final ModelFiles[] models, final Link[] links) {
		element = new Element("metaParent");

		for (final ModelFiles model : models) {
			element.addContent(new ModelFilesNode(model).getElement());
		}

		for (final Link link : links) {
			element.addContent(new LinkNode(link).getElement());
		}
	}

	public ModelFiles[] getModels() {
		return element.getChildren("model").stream().map(ModelFilesNode::new).map(ModelFilesNode::getModel)
				.toArray(size -> new ModelFiles[size]);
	}

	public Link[] getLinks() {
		return element.getChildren("link").stream().map(LinkNode::new).map(LinkNode::getLink)
				.toArray(size -> new Link[size]);
	}
	
	public Element getElement() {
		return element;
	}
}