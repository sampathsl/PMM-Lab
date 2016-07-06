package de.bund.bfr.knime.pmm.combinedfsk.creator;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class CombinedFskCreatorNodeFactory extends NodeFactory<CombinedFskCreatorNodeModel> {

	@Override
	public CombinedFskCreatorNodeModel createNodeModel() {
		return new CombinedFskCreatorNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<CombinedFskCreatorNodeModel> createNodeView(int viewIndex, CombinedFskCreatorNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new CombinedFskCreatorNodeDialog();
	}

}
