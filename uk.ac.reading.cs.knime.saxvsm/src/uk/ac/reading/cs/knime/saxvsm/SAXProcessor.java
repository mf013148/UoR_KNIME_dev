package uk.ac.reading.cs.knime.saxvsm;

/**
 * Implements SAX algorithms.
 * @author Ryan Faulkner
 */
public final class SAXProcessor {
	/**
	 * Constructor.
	 */
	public SAXProcessor() {}

	/**
	 * Compute the distance between the two chars based on the ASCII symbol codes.
	 * @param a The first char.
	 * @param b The second char.
	 * @return The distance.
	 */
	public int charDistance(char a, char b) {
		return Math.abs(Character.getNumericValue(a) - Character.getNumericValue(b));
	}

	/**
	 * Check for trivial mindist case.
	 * @param a first string.
	 * @param b second string.
	 * @return true if mindist between strings is zero.
	 */
	public boolean checkMinDistIsZero(char[] a, char[] b) {
		for (int i = 0; i < a.length; i++)
			if (charDistance(a[i], b[i]) > 1)
				return false;
		return true;
	}
}