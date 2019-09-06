package uk.ac.reading.cs.knime.saxvsm;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
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
 * This is the model implementation of SAXVSMClassifier.
 * Time series classification using the SAX Discretisation transform via a Sliding window in order to construct a Bag of Words characterising a class using TFIDF to form class-characteristic vectors for identification and classification.
 * @author Ryan Faulkner
 */
public class SAXVSMClassifierNodeModel extends NodeModel {
	private SAXVSMClassifier classifier;
	
	/** the settings key which is used to retrieve and store the settings (from the dialog or from a settings file)
	 * (package visibility to be usable from the dialog). */
	protected static final String WINDOW = "sax.window.name";
	protected static final String PAA_SIZE = "sax.paa_size.name";
	protected static final String ALPHABET_SIZE = "sax.alphabet_size.name";
	protected static final String NUMEROSITY = "sax.numerosity.name";
	protected static final String THRESH = "sax.thresh.name";

    // example value: the models count variable filled from the dialog 
    // and used in the models execution method. The default components of the
    // dialog work with "SettingsModels".
	private final SettingsModelIntegerBounded window_sz = new SettingsModelIntegerBounded(WINDOW, 30, 0, 100);
	private final SettingsModelInteger paa_sz = new SettingsModelInteger(PAA_SIZE, 4);
	private final SettingsModelInteger alpha_sz = new SettingsModelInteger(ALPHABET_SIZE, 3);
	private final SettingsModelString numerosity = new SettingsModelString(NUMEROSITY, "NONE");
	private final SettingsModelDouble threshold = new SettingsModelDouble(THRESH, 0.01);

    /**
     * Constructor for the node model.
     */
    protected SAXVSMClassifierNodeModel() {
        // Two incoming ports - 0 = training data, 1 = test data
    	// One outgoing port  - confusion matrix 
        super(2, 2);
        // Initialise classifier
        classifier = new SAXVSMClassifier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {
    	classifier.params.windowSize = window_sz.getIntValue();
		classifier.params.paaSize = paa_sz.getIntValue();
		classifier.params.alphabetSize = alpha_sz.getIntValue();
		classifier.params.nrStrategy = NumerosityReductionStrategy.fromString(numerosity.getStringValue());
		classifier.params.nThreshold = threshold.getDoubleValue();

        classifier.run(inData);
        BufferedDataTable[] out = new BufferedDataTable[2];
        
        DataColumnSpec act = new DataColumnSpecCreator("class_actual",    DataType.getType(StringCell.class)).createSpec();
        DataColumnSpec pre = new DataColumnSpecCreator("class_predicted", DataType.getType(StringCell.class)).createSpec();
        DataColumnSpec ts  = new DataColumnSpecCreator("timeseries", DataType.getType(StringCell.class)).createSpec();
        DataTableSpec outSpec = new DataTableSpec(act, pre, ts);
        BufferedDataContainer buf = exec.createDataContainer(outSpec);
        //Add records to outport table
  		RowKey key = null;
  		DataCell[] cells = null;
  		DataRow row = null;
  		int row_count = 0;
  		for (SAXVSMClassifierResult res : classifier.results) {
  			key = new RowKey("Row"+row_count);
  			cells = new DataCell[3];
  			cells[0] = new StringCell(res.actual);
  			String pred = res.predicted;
  			if(pred=="")
  				pred="unknown";
  			cells[1] = new StringCell(pred);
  			StringBuffer sb = new StringBuffer();
  			for(double d: res.series)
  				sb.append(d + " ");
  			sb.deleteCharAt(sb.length()-1);
  			cells[2] = new StringCell(sb.toString());
  			row = new DefaultRow(key,cells);
  			buf.addRowToTable(row);
  			row_count++;
  		}
        buf.close();
        out[0]=buf.getTable(); 
        
        
        // Fill output table for port 2 confusion matrix
        DataColumnSpec[] allColSpecs = new DataColumnSpec[classifier.classes.size()];
        for(int i=0; i<classifier.classes.size(); i++)
        	allColSpecs[i] = new DataColumnSpecCreator(classifier.classes.get(i)/*+"_actual"*/, IntCell.TYPE).createSpec();
        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
        BufferedDataContainer container = exec.createDataContainer(outputSpec);
        for(int i=0; i<classifier.classes.size(); i++){
        	key = new RowKey(classifier.classes.get(i)/*+"_predicted"*/);
            cells = new DataCell[classifier.classes.size()];
            for(int j=0; j<classifier.classes.size(); j++)
            	cells[j] = new IntCell(classifier.confusion[i][j]); 
            row = new DefaultRow(key, cells);
            container.addRowToTable(row);
        }
        container.close();
        out[1] = container.getTable();
        return out;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Code executed on reset.
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
        return new DataTableSpec[]{null,null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
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
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
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
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	// Check if the settings could be applied to our model
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