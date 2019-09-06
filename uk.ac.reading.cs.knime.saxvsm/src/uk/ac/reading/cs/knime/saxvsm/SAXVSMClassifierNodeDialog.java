package uk.ac.reading.cs.knime.saxvsm;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "SAXVSMClassifier" Node.
 * Time series classification using the SAX Discretisation transform via a Sliding window in order to construct a Bag of Words characterising a class using TFIDF to form class-characteristic vectors for identification and classification.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Ryan Faulkner
 */
public class SAXVSMClassifierNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring SAXVSMClassifier node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
	protected SAXVSMClassifierNodeDialog() {
        super();
		createNewGroup("SAX Discretisation");
		// SAX_WINDOW_SIZE
		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(SAXVSMClassifierNodeModel.WINDOW, 30),
				"SAX Sliding Window Size:", 1));

		// SAX_PAA_SIZE
		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(SAXVSMClassifierNodeModel.PAA_SIZE, 4),
				"SAX PAA Word Size:", 1));

		// SAX_ALPHABET_SIZE
		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(SAXVSMClassifierNodeModel.ALPHABET_SIZE, 3),
				"SAX Alphabet Size:", 1));

		// SAX_NR_STRATEGY
		addDialogComponent(
				new DialogComponentStringSelection(new SettingsModelString(SAXVSMClassifierNodeModel.NUMEROSITY, "EXACT"),
						"Numerosity Reduction Strategy:", "NONE", "EXACT", "MINDIST"));

		// SAX_NORM_THRESHOLD
		addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(SAXVSMClassifierNodeModel.THRESH, 0.01),
				"SAX Normalisation Threshold:", 0.01));
    }
}

