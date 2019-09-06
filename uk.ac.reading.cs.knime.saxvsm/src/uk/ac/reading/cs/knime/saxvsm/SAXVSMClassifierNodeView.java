package uk.ac.reading.cs.knime.saxvsm;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "SAXVSMClassifier" Node.
 * Time series classification using the SAX Discretisation transform via a Sliding window in order to construct a Bag of Words characterising a class using TFIDF to form class-characteristic vectors for identification and classification.
 *
 * @author Ryan Faulkner
 */
public class SAXVSMClassifierNodeView extends NodeView<SAXVSMClassifierNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link SAXVSMClassifierNodeModel})
     */
    protected SAXVSMClassifierNodeView(final SAXVSMClassifierNodeModel nodeModel) {
        super(nodeModel);

        // TODO instantiate the components of the view here.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        SAXVSMClassifierNodeModel nodeModel = 
            (SAXVSMClassifierNodeModel)getNodeModel();
        assert nodeModel != null;
        
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
    
        // TODO things to do when closing the view
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {

        // TODO things to do when opening the view
    }

}

