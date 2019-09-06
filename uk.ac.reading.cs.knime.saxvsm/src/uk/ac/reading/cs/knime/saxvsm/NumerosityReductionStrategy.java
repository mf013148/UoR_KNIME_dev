package uk.ac.reading.cs.knime.saxvsm;

/**
 * The SAX Collection strategy.
 * @author Ryan Faulkner
 */
public enum NumerosityReductionStrategy {
	/** No reduction at all - all the words going make it into collection. */
	NONE(0),

	/** Exact - the strategy based on the exact string match. */
	EXACT(1),

	/** Classic - the Lin's and Keogh's MINDIST based strategy. */
	MINDIST(2);

	/**
	 * Constructor.
	 * @param index The strategy index.
	 */
	NumerosityReductionStrategy(int index) {}
	
	/**
	 * Parse the string value into an instance.
	 * @param value the string value.
	 * @return new instance.
	 */
	public static NumerosityReductionStrategy fromString(String value) {
		if ("none".equalsIgnoreCase(value))
			return NumerosityReductionStrategy.NONE;
		else if ("exact".equalsIgnoreCase(value))
			return NumerosityReductionStrategy.EXACT;
		else if ("mindist".equalsIgnoreCase(value))
			return NumerosityReductionStrategy.MINDIST;
		else
			throw new RuntimeException("Unknown index:" + value);
	}
}
