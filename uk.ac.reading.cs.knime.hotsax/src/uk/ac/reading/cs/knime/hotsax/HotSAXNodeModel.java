package uk.ac.reading.cs.knime.hotsax;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
//import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This is the model implementation of HotSAXProcessor.
 * Finds time series discords; maximally different subsequences within a time series.
 * @author Ryan Faulkner
 */
public class HotSAXNodeModel extends NodeModel {
	SAXParameters params;
	HotSAXProcessor hsp;
	
//	/*Array containing timeseries timestamps*/
//	static String ts_d[];
//	/*Timeseries data*/
//	static double ts[];
//	/*Collection of Discord records*/
//	static DiscordRecords dr;
	
    /** the settings key which is used to retrieve and 
        store the settings (from the dialog or from a settings file)    
       (package visibility to be usable from the dialog). */
	static final String TS_DATE = "ts.dat.name";
	static final String TS_DATA = "ts.col.name";
	static final String WINDOW = "sax.window.name";
//	static final String PAA_SIZE = "sax.paa_size.name";
//	static final String ALPHABET_SIZE = "sax.alphabet_size.name";
//	static final String NUMEROSITY = "sax.numerosity.name";
//	static final String THRESH = "sax.thresh.name";
	static final String DISCORDS = "hotsax.discords.name";

    // example value: the models count variable filled from the dialog 
    // and used in the models execution method. The default components of the
    // dialog work with "SettingsModels".
	private final SettingsModelString dateCol = new SettingsModelString(TS_DATE, null);
	private final SettingsModelString colname = new SettingsModelString(TS_DATA, null);
	private final SettingsModelIntegerBounded window_sz = new SettingsModelIntegerBounded(WINDOW, 30, 0, Integer.MAX_VALUE);
//	private final SettingsModelInteger paa_sz = new SettingsModelInteger(PAA_SIZE, 4);
//	private final SettingsModelInteger alpha_sz = new SettingsModelInteger(ALPHABET_SIZE, 3);
//	private final SettingsModelString numerosity = new SettingsModelString(NUMEROSITY, "NONE");
//	private final SettingsModelDouble threshold = new SettingsModelDouble(THRESH, 0.01);
	private final SettingsModelInteger discords = new SettingsModelInteger(DISCORDS, 1);

    /**
     * Constructor for the node model.
     */
    protected HotSAXNodeModel() {
        super(2, 1);
    }

    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {
    	hsp = new HotSAXProcessor();
    	params = new SAXParameters();
    	params.DATECOL = dateCol.getStringValue();
		params.COLNAME = colname.getStringValue();
		params.SAX_WINDOW_SIZE = window_sz.getIntValue();
//		params.SAX_PAA_SIZE = paa_sz.getIntValue();
//		params.SAX_ALPHABET_SIZE = alpha_sz.getIntValue();
//		params.SAX_NR_STRATEGY = NumerosityReductionStrategy.fromString(numerosity.getStringValue());
//		params.SAX_NORM_THRESHOLD = threshold.getDoubleValue();
		params.DISCORDS = discords.getIntValue();
		
		return new BufferedDataTable[]{hsp.series2Discords(inData, params, exec)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Models build during execute are cleared here.
        // Also data handled in load/saveInternals will be erased here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {        
        // TODO: check if user settings are available, fit to the incoming
        // table structure, and the incoming types are feasible for the node
        // to execute. If the node can execute in its current state return
        // the spec of its output data table(s) (if you can, otherwise an array
        // with null elements), or throw an exception with a useful user message

        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	this.dateCol.saveSettingsTo(settings);
    	this.colname.saveSettingsTo(settings);
		this.window_sz.saveSettingsTo(settings);
//		this.paa_sz.saveSettingsTo(settings);
//		this.alpha_sz.saveSettingsTo(settings);
//		this.numerosity.saveSettingsTo(settings);
//		this.threshold.saveSettingsTo(settings);
    	this.discords.saveSettingsTo(settings);
	}

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	this.dateCol.loadSettingsFrom(settings);
    	this.colname.loadSettingsFrom(settings);
		this.window_sz.loadSettingsFrom(settings);
//		this.paa_sz.loadSettingsFrom(settings);
//		this.alpha_sz.loadSettingsFrom(settings);
//		this.numerosity.loadSettingsFrom(settings);
//		this.threshold.loadSettingsFrom(settings);
    	this.discords.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException { 
        // Check if the settings could be applied to our model
    	this.dateCol.validateSettings(settings);
    	this.colname.validateSettings(settings);
		this.window_sz.validateSettings(settings);
//		this.paa_sz.validateSettings(settings);
//		this.alpha_sz.validateSettings(settings);
//		this.numerosity.validateSettings(settings);
//		this.threshold.validateSettings(settings);
    	this.discords.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO load internal data. 
        // Everything handed to output ports is loaded automatically (data
        // returned by the execute method, models loaded in loadModelContent,
        // and user settings set through loadSettingsFrom - is all taken care 
        // of). Load here only the other internals that need to be restored
        // (e.g. data used by the views).

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO save internal models. 
        // Everything written to output ports is saved automatically (data
        // returned by the execute method, models saved in the saveModelContent,
        // and user settings saved through saveSettingsTo - is all taken care 
        // of). Save here only the other internals that need to be preserved
        // (e.g. data used by the views).
    }
}