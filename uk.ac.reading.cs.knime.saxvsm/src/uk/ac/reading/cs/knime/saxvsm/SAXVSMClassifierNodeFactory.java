package uk.ac.reading.cs.knime.saxvsm;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "SAXVSMClassifier" Node.
 * Time series classification using the SAX Discretisation transform via a Sliding window in order to construct a Bag of Words characterising a class using TFIDF to form class-characteristic vectors for identification and classification.
 *
 * @author Ryan Faulkner
 */
public class SAXVSMClassifierNodeFactory 
        extends NodeFactory<SAXVSMClassifierNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public SAXVSMClassifierNodeModel createNodeModel() {
        return new SAXVSMClassifierNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<SAXVSMClassifierNodeModel> createNodeView(final int viewIndex,
            final SAXVSMClassifierNodeModel nodeModel) {
        throw new IllegalStateException();
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
        return new SAXVSMClassifierNodeDialog();
    }

}