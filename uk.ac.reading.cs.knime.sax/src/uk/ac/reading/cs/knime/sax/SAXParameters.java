package uk.ac.reading.cs.knime.sax;

/**
 * Parameters accepted by the bitmap printer and their default values.
 * @author Ryan Faulkner
 */
public class SAXParameters {
	// General parameters for discretisation
	//
	public SAXParameters() {}

	// Data set
	//
	/**
	 * Time Step column id
	 */
	public String DATECOL;
	/**
	 * Time Series column id
	 */
	public String COLNAME;
	
	// Discretisation parameters
	//
	/**
	 * SAX Sliding window size
	 */
	public int SAX_WINDOW_SIZE = 30;
	
	/**
	 * SAX PAA word size
	 */
	public int SAX_PAA_SIZE = 4;
	
	/**
	 * SAX Alphabet size
	 */
	public int SAX_ALPHABET_SIZE = 3;
	
	/**
	 * SAX Numerosity reduction strategy
	 */
	public NumerosityReductionStrategy SAX_NR_STRATEGY = NumerosityReductionStrategy.EXACT;
	
	/**
	 * SAX Normalisation threshold
	 */
	public double SAX_NORM_THRESHOLD = 0.01;

}
