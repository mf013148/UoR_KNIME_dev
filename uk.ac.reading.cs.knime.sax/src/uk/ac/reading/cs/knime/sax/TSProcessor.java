package uk.ac.reading.cs.knime.sax;

import java.util.Arrays;
import java.util.ArrayList;

import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;

/**
 * Implements algorithms for low-level data manipulation.
 * 
 * @author Ryan Faulkner
 */
public class TSProcessor {
	ArrayList<PAARecord> paa_idx;
	
	/** The Latin alphabet, lower case letters a-z. */
	public static final char[] ALPHABET = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 
											'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
	/**
	 * Constructor.
	 */
	public TSProcessor() {
		paa_idx = new ArrayList<PAARecord>();
	}
	
	/**
	 * Reads time series from a input node. 
	 * Assumes that input has a single double value on every line.
	 * Assigned timestamps are the line numbers.
	 * @param filename	The file to read from.
	 * @param columnIdx The column index.
	 * @param sizeLimit The number of lines to read, 0 == all.
	 * @return Timeseries data.
	 */
	public static double[] readColumn(BufferedDataTable[] inData, String colname) {
		ArrayList<Double> ts_a = new ArrayList<Double>();
		// Create iterator
		CloseableRowIterator tableIterator = inData[0].iterator();
		// Access rows using iterator
		int index = inData[0].getDataTableSpec().findColumnIndex(colname);
		// Using the index execute the converter
		while(tableIterator.hasNext()){
			String cell = tableIterator.next().getCell(index).toString();
			ts_a.add(Double.parseDouble(cell));
		}
		return ts_a.stream().mapToDouble(d -> d).toArray();
	}
	
	public static String[] readTSColumn(BufferedDataTable[] inData, String colname) {
		ArrayList<String> ts_a = new ArrayList<String>();
		// Create iterator
		CloseableRowIterator tableIterator = inData[0].iterator();
		// Access rows using iterator
		int index = inData[0].getDataTableSpec().findColumnIndex(colname);
		// Using the index execute the converter
		while(tableIterator.hasNext()){
			String cell = tableIterator.next().getCell(index).toString();
			ts_a.add(cell);
		}
		String[] ts_d = new String[ts_a.size()];
		ts_d = ts_a.toArray(ts_d);
		return ts_d;
	}

	/**
	 * Computes the mean value of timeseries.
	 * @param series The timeseries.
	 * @return The mean value.
	 */
	public static double mean(double[] series) {
		double res = 0D;
		int count = 0;
		for (double tp : series) {
			res += tp;
			count += 1;
		}
		if (count > 0)
			return res / ((Integer) count).doubleValue();
		return Double.NaN;
	}

	/**
	 * Computes the standard deviation of timeseries.
	 * @param series The time series.
	 * @return the Standard Deviation.
	 */
	public static double stDev(double[] series) {
		double num0 = 0D;
		double sum = 0D;
		int count = 0;
		for (double tp : series) {
			num0 = num0 + tp * tp;
			sum = sum + tp;
			count += 1;
		}
		double len = ((Integer) count).doubleValue();
		return Math.sqrt((len * num0 - sum * sum) / (len * (len - 1)));
	}

	/**
	 * Speed-optimised Z-Normalise routine
	 * @param series					The time series.
	 * @param normalizationThreshold	The zNormalization threshold value.
	 * @return Z-normalised time-series.
	 */
	public static double[] znorm(double[] series, double normalizationThreshold) {
		double[] res = new double[series.length];
		double mean = mean(series);
		double sd = stDev(series);
		if (sd < normalizationThreshold)
			return series.clone();
		for (int i = 0; i < res.length; i++)
			res[i] = (series[i] - mean) / sd;
		return res;
	}

	/**
	 * Approximate the timeseries using PAA. 
	 * If the timeseries has some NaN's they are handled as follows: 
	 * 1) if all values of the piece are NaNs - the piece is approximated as NaN, 
	 * 2) if there are some (more or equal one) values happened to be in the piece 
	 *    - algorithm will handle it as usual - getting the mean.
	 * @param ts		The timeseries to approximate.
	 * @param paaSize	The desired length of approximated timeseries.
	 * @return PAA-approximated timeseries.
	 * @throws SAXException if error occurs.
	 */
	public double[] paa(double[] ts, int paaSize, int offset) throws SAXException {
		// fix the length
		int len = ts.length;
		if (len < paaSize)
			throw new SAXException("PAA size can't be greater than the timeseries size.");
		// check for the trivial case
		if (len == paaSize)
			return Arrays.copyOf(ts, ts.length);
		else {
			double[] paa = new double[paaSize];
			double pointsPerSegment = (double) len / (double) paaSize;
			double[] breaks = new double[paaSize + 1];
			for (int i = 0; i < paaSize + 1; i++)
				breaks[i] = i * pointsPerSegment;

			for (int i = 0; i < paaSize; i++) {
				double segStart = breaks[i];
				double segEnd = breaks[i + 1];

				double fractionStart = Math.ceil(segStart) - segStart;
				double fractionEnd = segEnd - Math.floor(segEnd);

				int fullStart = Double.valueOf(Math.floor(segStart)).intValue();
				int fullEnd = Double.valueOf(Math.ceil(segEnd)).intValue();

				double[] segment = Arrays.copyOfRange(ts, fullStart, fullEnd);

				if (fractionStart > 0)
					segment[0] = segment[0] * fractionStart;
        
				if (fractionEnd > 0)
					segment[segment.length - 1] = segment[segment.length - 1] * fractionEnd;

				double elementsSum = 0.0;
				for (double e : segment)
					elementsSum = elementsSum + e;

				paa[i] = elementsSum / pointsPerSegment;
				
				paa_idx.add(new PAARecord(segStart + offset, segEnd + offset, paa[i]));
			}
			return paa;
		}
	}

	/**
	 * Converts the timeseries into string using given cuts intervals. 
	 * Useful for not-normal distribution cuts.
	 * @param vals The timeseries.
	 * @param cuts The cut intervals.
	 * @return The timeseries SAX representation.
	 */
	public static char[] ts2String(double[] vals, double[] cuts) {
		char[] res = new char[vals.length];
		for (int i = 0; i < vals.length; i++)
			res[i] = num2char(vals[i], cuts);
		return res;
	}

	/**
	 * Get mapping of a number to char.
	 * @param value the value to map.
	 * @param cuts the array of intervals.
	 * @return character corresponding to numeric value.
	 */
	public static char num2char(double value, double[] cuts) {
		int count = 0;
		while ((count < cuts.length) && (cuts[count] <= value))
			count++;
		return ALPHABET[count];
	}	
}