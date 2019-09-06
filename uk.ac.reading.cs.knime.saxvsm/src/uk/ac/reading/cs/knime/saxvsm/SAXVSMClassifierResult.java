package uk.ac.reading.cs.knime.saxvsm;

public class SAXVSMClassifierResult {
	String actual, predicted;
	double[] series;

	public SAXVSMClassifierResult(String a, String p, double[] s){
		actual = a;
		predicted = p;
		series = s;
	}	
}
