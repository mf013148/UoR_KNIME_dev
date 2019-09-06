package uk.ac.reading.cs.knime.saxvsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.knime.core.node.BufferedDataTable;

/**
 * This implements a vector space model classifier.
 * @author Ryan Faulkner (modified from: psenin)
 */
public class SAXVSMClassifier {
	protected Params params;
	private TextProcessor tp;

	private Map<String, List<double[]>> trainData;
	private Map<String, List<double[]>> testData;
	
	protected SortedArrayList<String> classes;
	protected ArrayList<String> predictions;
	protected int[][] confusion;
	private double accuracy;
	
	protected ArrayList<SAXVSMClassifierResult> results;
	
	
	public SAXVSMClassifier() {
		params = new Params();
		tp = new TextProcessor();
		classes = new SortedArrayList<String>();
		predictions = new ArrayList<String>();
		results = new ArrayList<SAXVSMClassifierResult>();
	}
	
	public void run(BufferedDataTable[] inData) throws SAXException {
		try {
			trainData = TableUtils.readTSRow(inData, 0);
			testData = TableUtils.readTSRow(inData, 1);
			for(String l: trainData.keySet())
				classes.insertSortedNoDuplicates(l);
			for(String l: testData.keySet())
				classes.insertSortedNoDuplicates(l);
			confusion = new int[classes.size()][classes.size()];
			classify();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void classify() throws SAXException {
		// making training bags collection
		List<WordBag> bags = tp.labeledSeries2WordBags(trainData, params);
		// getting TFIDF done
		HashMap<String, HashMap<String, Double>> tfidf = tp.computeTFIDF(bags);
		// classifying
		int testSampleSize = 0;
		int positiveTestCounter = 0;
		for (String label : tfidf.keySet()) {
			List<double[]> testD = testData.get(label);
			for (double[] series : testD) {
				String prediction = tp.classify(this, label, series, tfidf, params);
				if(prediction==label)
					positiveTestCounter++;
				//positiveTestCounter = positiveTestCounter + tp.classify(this, label, series, tfidf, params);
				testSampleSize++;
				results.add(new SAXVSMClassifierResult(label,prediction,series));
			}
		}
		// accuracy and error
		accuracy = (double) positiveTestCounter / (double) testSampleSize;
		System.out.println(params.toString() + "," + accuracy + "," + getError());
	}
	// Report Statistics
	public double getAccuracy() {
		return accuracy;
	}
	public double getError() {
		return 1.0d - accuracy;
	}
}