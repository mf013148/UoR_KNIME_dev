package uk.ac.reading.cs.knime.hotsax;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.date.DateAndTimeValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
//import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
//import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "HotSAXProcessor" Node. Finds time series
 * discords; maximally different subsequences within a time series.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Ryan Faulkner
 */
public class HotSAXNodeDialog extends DefaultNodeSettingsPane {
	/**
	 * New pane for configuring HotSAXProcessor node dialog. This is just a
	 * suggestion to demonstrate possible default dialog components.
	 */
	@SuppressWarnings("unchecked")
	protected HotSAXNodeDialog() {
		super();
		createNewGroup("Time Indexes");
		addDialogComponent(new DialogComponentColumnNameSelection(new SettingsModelString(HotSAXNodeModel.TS_DATE, ""),
				"Select a column", 0, true, DateAndTimeValue.class));
		
		createNewGroup("Time Series");
		addDialogComponent(new DialogComponentColumnNameSelection(new SettingsModelString(HotSAXNodeModel.TS_DATA, ""),
				"Select a column", 0, true, DoubleValue.class));

		createNewGroup("SAX Discretisation");
		// SAX_WINDOW_SIZE
		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(HotSAXNodeModel.WINDOW, 30),
				"SAX Sliding Window Size:", 1));

//		// SAX_PAA_SIZE
//		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(HotSAXNodeModel.PAA_SIZE, 4),
//				"SAX PAA Word Size:", 1));
//
//		// SAX_ALPHABET_SIZE
//		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(HotSAXNodeModel.ALPHABET_SIZE, 3),
//				"SAX Alphabet Size:", 1));
//
//		// SAX_NR_STRATEGY
//		addDialogComponent(
//				new DialogComponentStringSelection(new SettingsModelString(HotSAXNodeModel.NUMEROSITY, "EXACT"),
//						"Numerosity Reduction Strategy:", "NONE", "EXACT", "MINDIST"));
//
//		// SAX_NORM_THRESHOLD
//		addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(HotSAXNodeModel.THRESH, 0.01),
//				"SAX Normalisation Threshold:", 0.01));
		
		createNewGroup("HOTSAX Discords");
		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(HotSAXNodeModel.DISCORDS, 1),
				"No. of Discords:", 1));


	}
}
