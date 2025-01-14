package uk.ac.reading.cs.knime.sax;

/**
 * SAX custom exception.
 * @author Ryan Faulkner
 */
public class SAXException extends Exception {
	/** The default serial version UID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Thrown when some problem occurs.
	 * @param description The problem description.
	 * @param error The previous error.
	 */
	public SAXException(String description, Throwable error) {
		super(description, error);
	}

	/**
	 * Thrown when some problem occurs.
	 * @param description The problem description.
	 */
	public SAXException(String description) {
		super(description);
	}
}