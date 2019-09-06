package uk.ac.reading.cs.knime.sax;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
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
 * This is the model implementation of SAX.
 * Read and discretise time series data
 *
 * @author Ryan Faulkner
 */
public class SAXNodeModel extends NodeModel {
	SAXConverter converter;
	SAXParameters params;
	
	/**
	 * The settings key which is used to retrieve and 
	 * store the settings (from the dialog or from a settings file)
	 * (package visibility to be usable from the dialog). 
	 */
	static final String TS_DATE = "ts.dat.name";
	static final String TS_DATA = "ts.col.name";
	static final String WINDOW = "sax.window.name";
	static final String PAA_SIZE = "sax.paa_size.name";
	static final String ALPHABET_SIZE = "sax.alphabet_size.name";
	static final String NUMEROSITY = "sax.numerosity.name";
	static final String THRESH = "sax.thresh.name";

	// example value: the models count variable filled from the dialog 
	// and used in the models execution method. The default components of the
	// dialog work with "SettingsModels".
	private final SettingsModelString  dateCol    = new SettingsModelString(TS_DATE, null);
	private final SettingsModelString  colname    = new SettingsModelString(TS_DATA, null);
	private final SettingsModelIntegerBounded window_sz  = new SettingsModelIntegerBounded(WINDOW, 30, 0, 100);
	private final SettingsModelInteger paa_sz     = new SettingsModelInteger(PAA_SIZE, 4);
	private final SettingsModelInteger alpha_sz   = new SettingsModelInteger(ALPHABET_SIZE, 3);
	private final SettingsModelString  numerosity = new SettingsModelString(NUMEROSITY, "NONE");
	private final SettingsModelDouble  threshold  = new SettingsModelDouble(THRESH, 0.01);

	/**
	 * Constructor for the node model.
	 */
	protected SAXNodeModel() {
		// Input port:	Time series data
		// Output port:	SAX String
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {
		converter = new SAXConverter();
		params = new SAXParameters();
		params.DATECOL = dateCol.getStringValue();
		params.COLNAME = colname.getStringValue();
		params.SAX_WINDOW_SIZE = window_sz.getIntValue();
		params.SAX_PAA_SIZE = paa_sz.getIntValue();
		params.SAX_ALPHABET_SIZE = alpha_sz.getIntValue();
		params.SAX_NR_STRATEGY = NumerosityReductionStrategy.fromString(numerosity.getStringValue());
		params.SAX_NORM_THRESHOLD = threshold.getDoubleValue();
		
		return new BufferedDataTable[]{converter.run(params, inData, exec)};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		System.err.println("SAXModel: reset called");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		System.err.println("SAXModel: configure called");
		return new DataTableSpec[]{SAXTable.createDataTableSpec()};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		System.err.println("SAXModel: saveSettingsTo called "+ this.colname);
		this.dateCol.saveSettingsTo(settings);
		this.colname.saveSettingsTo(settings);
		this.window_sz.saveSettingsTo(settings);
		this.paa_sz.saveSettingsTo(settings);
		this.alpha_sz.saveSettingsTo(settings);
		this.numerosity.saveSettingsTo(settings);
		this.threshold.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		System.err.println("SAXModel: loadValidatedSettingsFrom called input="+this.colname);
		this.dateCol.loadSettingsFrom(settings);
		this.colname.loadSettingsFrom(settings);
		this.window_sz.loadSettingsFrom(settings);
		this.paa_sz.loadSettingsFrom(settings);
		this.alpha_sz.loadSettingsFrom(settings);
		this.numerosity.loadSettingsFrom(settings);
		this.threshold.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		SettingsModelInteger window = window_sz.createCloneWithValidatedValue(settings);
		if(window.getIntValue() < 0 || window.getIntValue() > 100) 
			throw new InvalidSettingsException("Invalid Window size");
		
		SettingsModelInteger paa = paa_sz.createCloneWithValidatedValue(settings);
		if(paa.getIntValue() < 0 || paa.getIntValue() > 20) 
			throw new InvalidSettingsException("Invalid PAA Word size");
		
		SettingsModelInteger alpha = alpha_sz.createCloneWithValidatedValue(settings);
		if(alpha.getIntValue() < 0 || alpha.getIntValue() > 20) 
			throw new InvalidSettingsException("Invalid Alphabet size");

		SettingsModelDouble thresh = threshold.createCloneWithValidatedValue(settings);
		if(thresh.getDoubleValue() < 0.0 || thresh.getDoubleValue() > 1.0) 
			throw new InvalidSettingsException("Invalid Threshold value");
		
		this.dateCol.validateSettings(settings);
		this.colname.validateSettings(settings);
		this.window_sz.validateSettings(settings);
		this.paa_sz.validateSettings(settings);
		this.alpha_sz.validateSettings(settings);
		this.numerosity.validateSettings(settings);
		this.threshold.validateSettings(settings);
	}
    
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException, CanceledExecutionException {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException, CanceledExecutionException {}
}
