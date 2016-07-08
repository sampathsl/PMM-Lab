package de.bund.bfr.knime.pmm.combinedfsk.archive;

import java.util.List;
import java.util.stream.Collectors;

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

	public List<ModelFiles> getModels() {
		return element.getChildren("model").stream().map(ModelFilesNode::new).map(ModelFilesNode::getModel)
				.collect(Collectors.toList());
	}

	public List<Link> getLinks() {
		return element.getChildren("link").stream().map(LinkNode::new).map(LinkNode::getLink)
				.collect(Collectors.toList());
	}

	public Element getElement() {
		return element;
	}
}