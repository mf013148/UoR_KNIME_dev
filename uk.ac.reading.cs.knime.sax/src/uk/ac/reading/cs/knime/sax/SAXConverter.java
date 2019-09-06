package uk.ac.reading.cs.knime.sax;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

/**
 * This implements a simple tool for ad-hoc SAX discretisation.
 * @author Ryan Faulkner
 */
public final class SAXConverter {
	String[] ts_d;
	double[] ts;
	double[] ts_z;
	ArrayList<PAARecord> paa;
	SAXRecords res;
	
	/**
	 * Constructor.
	 */
	public SAXConverter() {}

	/**
	 * The main runnable implementing the SAX logic.
	 * @param  SAXParameters		the parameters parsed from the node dialog box
	 * @return BufferedDataTable	the discretised representation of the time series input
	 */
	public BufferedDataTable run(SAXParameters params, BufferedDataTable[] inData, ExecutionContext exec) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		DateFormat tf = new SimpleDateFormat("HH:mm:ss.SSS");

		// Define results collection object 
		DataTableSpec outSpec = SAXTable.createDataTableSpec();
		BufferedDataContainer container = exec.createDataContainer(outSpec);
		
		try {
			// Read column data
			ts_d = TSProcessor.readTSColumn(inData, params.DATECOL);
			ts = TSProcessor.readColumn(inData, params.COLNAME);

			NormalAlphabet na = new NormalAlphabet();
			SAXProcessor sp = new SAXProcessor();
			
			if(params.SAX_WINDOW_SIZE == 0)
				res = sp.ts2saxByChunking(ts, params.SAX_PAA_SIZE, na.getCuts(params.SAX_ALPHABET_SIZE), params.SAX_NORM_THRESHOLD);
			else
				res = sp.ts2saxViaWindow(ts, params.SAX_WINDOW_SIZE, params.SAX_PAA_SIZE, na.getCuts(params.SAX_ALPHABET_SIZE), params.SAX_NR_STRATEGY, params.SAX_NORM_THRESHOLD);
			
			ts_z = TSProcessor.znorm(ts, params.SAX_NORM_THRESHOLD);
			paa = sp.tsp.paa_idx;
			
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			indexes.addAll(res.getIndexes());
			Collections.sort(indexes);
        
			RowKey key = null;
			DataCell[] cells = null;
			DataRow row = null;
			for (Integer idx : indexes) {
				key = new RowKey("Row"+idx);
				cells = new DataCell[2];
				//Timestamp: Handle dates
				Calendar c = Calendar.getInstance();
				if(ts_d[idx].length()<=12) {
					//Time only
					c.setTime(tf.parse(ts_d[idx]));
					cells[0] = new DateAndTimeCell(c.get(Calendar.HOUR), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), c.get(Calendar.MILLISECOND));
				} else {
					//Date and Time
					c.setTime(df.parse(ts_d[idx]));
					cells[0] = new DateAndTimeCell(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
				}
				//SAX String
				cells[1] = new StringCell(String.valueOf(res.getByIndex(idx).getPayload()));
				//Add cells to row
				row = new DefaultRow(key,cells);
				//Add row to table
				container.addRowToTable(row);
			}
			container.close();
		
		} catch (SAXException | ParseException e) {
			System.err.println("error occured while parsing parameters." + e.getStackTrace());
			System.exit(-1);
		}
		return container.getTable();
	}
}