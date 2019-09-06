package uk.ac.reading.cs.knime.sax;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "SAX" Node.
 * Read and discretise time series data
 *
 * @author Ryan Faulkner
 */
public class SAXNodeFactory extends NodeFactory<SAXNodeModel> {
    /**
     * {@inheritDoc}
     */
    @Override
    public SAXNodeModel createNodeModel() {
        return new SAXNodeModel();
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
    public NodeView<SAXNodeModel> createNodeView(final int viewIndex, final SAXNodeModel nodeModel) {
      	SAXNodeModel m = (SAXNodeModel) nodeModel;
     	return new SAXNodeView(m);	
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
        return new SAXNodeDialog();
    }
}