package uk.ac.reading.cs.knime.hotsax;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "HotSAXProcessor" Node.
 * Finds time series discords; maximally different subsequences within a time series.
 *
 * @author Ryan Faulkner
 */
public class HotSAXNodeFactory extends NodeFactory<HotSAXNodeModel> {
    /**
     * {@inheritDoc}
     */
    @Override
    public HotSAXNodeModel createNodeModel() {
        return new HotSAXNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<HotSAXNodeModel> createNodeView(final int viewIndex,
            final HotSAXNodeModel nodeModel) {
    	return new HotSAXNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new HotSAXNodeDialog();
    }
}