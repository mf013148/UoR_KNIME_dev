package uk.ac.reading.cs.knime.saxvsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;

/**
 * This implements variety utils for UCR-formatted data.
 * @author psenin
 */
public class TableUtils {
	/**
	 * Reads time series from input table row. 
	 * Assumes that the timeseries has a double value in each column.
	 * Assigned time stamps are the line numbers.
	 * @param inData Data table containing training (in port = 0) or testing (1) Time Series one record per row
	 * @param inPort Training (0) or Testing (1) datasets
	 * @return data  Labelled time series arrays
	 * @throws IOException if error occurs.
	 * @throws SAXException if error occurs.
	 */
	public static Map<String, List<double[]>> readTSRow(BufferedDataTable[] inData, int inPort) {
		Map<String, List<double[]>> res = new HashMap<String, List<double[]>>();
		// Create iterator
		CloseableRowIterator tableIterator = inData[inPort].iterator();
		// Access rows using iterator
		int c_idx = inData[inPort].getDataTableSpec().findColumnIndex("class");
		int t_idx = inData[inPort].getDataTableSpec().findColumnIndex("timeseries");
		
		// Using the index execute the converter
		while(tableIterator.hasNext()) {
			DataRow dr = tableIterator.next();
			
			String label = dr.getCell(c_idx).toString();
			String series[] = dr.getCell(t_idx).toString().split(" ");
			
			double[] ts = new double[series.length];
			for(int i=0; i<ts.length; i++)
				ts[i] = Double.valueOf(series[i]);
				
			if (!res.containsKey(label))
				res.put(label, new ArrayList<double[]>());
			res.get(label).add(ts);
		}
		return res;
	}
}