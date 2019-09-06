package uk.ac.reading.cs.knime.hotsax;

//import java.util.Arrays;

//import org.joda.time.Duration;
//import org.joda.time.format.PeriodFormatter;
//import org.joda.time.format.PeriodFormatterBuilder;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;

/**
 * Implements SAX algorithms.
 * 
 * @author Ryan Faulkner
 * 
 */
public final class SAXProcessor {
//	private final TSProcessor tsProcessor;

	/**
	 * Constructor.
	 */
	public SAXProcessor() {
//		this.tsProcessor = new TSProcessor();
	}

//	/**
//	 * Converts the input time series into a SAX data structure via sliding window and Z
//	 * normalisation.
//	 * 
//	 * @param ts the input data.
//	 * @param windowSize the sliding window size.
//	 * @param paaSize the PAA size.
//	 * @param cuts the Alphabet cuts.
//	 * @param nThreshold the normalisation threshold value.
//	 * @param strategy the NR strategy.
//	 * 
//	 * @return SAX representation of the time series.
//	 * @throws SAXException if error occurs.
//	 */
//	public SAXRecords ts2saxViaWindow(double[] ts, int windowSize, int paaSize, double[] cuts,
//			NumerosityReductionStrategy strategy, double nThreshold) throws SAXException {
//		// the resulting data structure initialisation
//		SAXRecords saxFrequencyData = new SAXRecords();
//
//		// scan across the time series extract sub sequences, and convert them to strings
//		char[] previousString = null;
//
//		for (int i = 0; i <= ts.length - windowSize; i++) {
//			// fix the current subsection
//			double[] subSection = Arrays.copyOfRange(ts, i, i + windowSize);
//
//			// Z normalise it
//			subSection = tsProcessor.znorm(subSection, nThreshold);
//
//			// perform PAA conversion if needed
//			double[] paa = tsProcessor.paa(subSection, paaSize);
//
//			// Convert the PAA to a string.
//			char[] currentString = tsProcessor.ts2String(paa, cuts);
//
//			if (null != previousString) {
//				if (NumerosityReductionStrategy.EXACT.equals(strategy) && Arrays.equals(previousString, currentString))
//					continue;
//				else if (NumerosityReductionStrategy.MINDIST.equals(strategy) && checkMinDistIsZero(previousString, currentString))
//					continue;
//			}
//			previousString = currentString;
//			saxFrequencyData.add(currentString, i);
//		}
//		return saxFrequencyData;
//	}
//
//	/**
//	 * Compute the distance between the two chars based on the ASCII symbol codes.
//	 * 
//	 * @param a The first char.
//	 * @param b The second char.
//	 * @return The distance.
//	 */
//	public int charDistance(char a, char b) {
//		return Math.abs(Character.getNumericValue(a) - Character.getNumericValue(b));
//	}
//
//	/**
//	 * Check for trivial mindist case.
//	 * 
//	 * @param a first string.
//	 * @param b second string.
//	 * @return true if mindist between strings is zero.
//	 */
//	public boolean checkMinDistIsZero(char[] a, char[] b) {
//		for (int i = 0; i < a.length; i++)
//			if (charDistance(a[i], b[i]) > 1)
//				return false;
//		return true;
//	}
//
//	/**
//	 * Generic method to convert the milliseconds into the elapsed time string.
//	 * 
//	 * @param start Start time stamp.
//	 * @param finish End time stamp.
//	 * @return String representation of the elapsed time.
//	 */
//	
//	public static String timeToString(long start, long finish) {
//		Duration duration = new Duration(finish - start); // in milliseconds
//		PeriodFormatter formatter = new PeriodFormatterBuilder().appendDays().appendSuffix("d")
//		.appendHours().appendSuffix("h").appendMinutes().appendSuffix("m").appendSeconds()
//		.appendSuffix("s").appendMillis().appendSuffix("ms").toFormatter();
//
//		return formatter.print(duration.toPeriod());
//	}
	
	public static SAXRecords readSAXColumn(BufferedDataTable[] inData, String colname) {
		SAXRecords saxFrequencyData = new SAXRecords();
		// Create iterator
		CloseableRowIterator tableIterator = inData[1].iterator();
		// Access rows using iterator
		int index = inData[1].getDataTableSpec().findColumnIndex(colname);
		// Using the index execute the converter
		int i = 0;
		while(tableIterator.hasNext()){
			saxFrequencyData.add(tableIterator.next().getCell(index).toString().toCharArray(), i);
			i++;
		}
		return saxFrequencyData;
	}
}