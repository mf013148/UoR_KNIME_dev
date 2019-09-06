package uk.ac.reading.cs.knime.saxvsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements text statistics and mining utilities.
 * @author psenin
 */
public final class TextProcessor {
	private static final Alphabet na = new NormalAlphabet();
	private static final TSProcessor tp = new TSProcessor();
	private static final SAXProcessor sp = new SAXProcessor();

	/**
	 * Constructor.
	 */
	public TextProcessor() {}

	/**
	 * Converts time series to a word bag.
	 * @param label the wordbag label.
	 * @param ts timeseries.
	 * @param params parameters for SAX transform.
	 * @return word bag.
	 * @throws SAXException if error occurs.
	 */
	public WordBag seriesToWordBag(String label, double[] ts, Params params) throws SAXException {
		WordBag resultBag = new WordBag(label);
		// scan across the time series extract sub sequences, and convert them to strings
		char[] previousString = null;

		for (int i=0; i<ts.length-(params.windowSize-1); i++) {
			// fix the current subsection
			double[] subSection = Arrays.copyOfRange(ts, i, i+params.windowSize);
			// Z normalize it
			subSection = tp.znorm(subSection, params.nThreshold);
			// perform PAA conversion if needed
			double[] paa = tp.paa(subSection, params.paaSize);
			// Convert the PAA to a string.
			char[] currentString = tp.ts2String(paa, na.getCuts(params.alphabetSize));

			if (null!=previousString) {
				if (NumerosityReductionStrategy.EXACT.equals(params.nrStrategy)
						&& Arrays.equals(previousString, currentString))
					continue; // NumerosityReduction
				else if (NumerosityReductionStrategy.MINDIST.equals(params.nrStrategy)
						&& sp.checkMinDistIsZero(previousString, currentString))
					continue;
			}
			previousString = currentString;
			resultBag.addWord(String.valueOf(currentString));
		}
		return resultBag;
	}

	public List<WordBag> labeledSeries2WordBags(Map<String, List<double[]>> data, Params params) throws SAXException {
		// make a map of resulting bags
		Map<String, WordBag> preRes = new HashMap<String, WordBag>();

		// process series one by one building word bags
		for (Entry<String, List<double[]>> e : data.entrySet()) {
			String classLabel = e.getKey();
			WordBag bag = new WordBag(classLabel);

			for (double[] series : e.getValue()) {
				WordBag cb = seriesToWordBag("tmp", series, params);
				bag.mergeWith(cb);
			}

			preRes.put(classLabel, bag);
		}
		List<WordBag> res = new ArrayList<WordBag>();
		res.addAll(preRes.values());
		return res;
	}

	/**
	 * Computes TF*IDF values.
	 * @param texts	The collection of text documents for which the statistics need to be computed.
	 * @return The map of source documents names to the word - tf*idf weight collections.
	 */
	public HashMap<String, HashMap<String, Double>> computeTFIDF(Collection<WordBag> texts) {
		// the number of docs
		int totalDocs = texts.size();

		// the result. map of document names to the pairs word - tfidf weight
		HashMap<String, HashMap<String, Double>> res = new HashMap<String, HashMap<String, Double>>();

		// build a collection of all observed words and their frequency in corpus
		HashMap<String, AtomicInteger> allWords = new HashMap<String, AtomicInteger>();
		for (WordBag bag : texts) {
			// here populate result map with empty entries
			res.put(bag.getLabel(), new HashMap<String, Double>());

			// and get those words
			for (Entry<String, AtomicInteger> e : bag.getInternalWords().entrySet()) {
				if (allWords.containsKey(e.getKey()))
					allWords.get(e.getKey()).incrementAndGet();
				else
					allWords.put(e.getKey(), new AtomicInteger(1));
			}
		}

		// outer loop - iterating over documents
		for (WordBag bag : texts) {
			// fix the doc name
			String bagName = bag.getLabel();
			HashMap<String, AtomicInteger> bagWords = bag.getInternalWords(); // these are words of documents

			// what we want to do for TF*IDF is to compute it for all WORDS ever seen in set
			for (Entry<String, AtomicInteger> word : allWords.entrySet()) {
				// by default it is zero
				double tfidf = 0;

				// if this document contains the word - here we go
				if (bagWords.containsKey(word.getKey()) & (totalDocs != word.getValue().intValue())) {
					int wordInBagFrequency = bagWords.get(word.getKey()).intValue();
					// compute TF: we take a log and correct for 0 by adding 1
					// OSULeaf: 0.08678
					double tfValue = 1.0D + Math.log(Integer.valueOf(wordInBagFrequency).doubleValue());
					// compute the IDF
					double idfLOGValue = Math.log10(Integer.valueOf(totalDocs).doubleValue() / word.getValue().doubleValue());
					// and the TF-IDF
					tfidf = tfValue * idfLOGValue;
				}
				res.get(bagName).put(word.getKey(), tfidf);
			}
		}
		return res;
	}

	/**
	 * Compute document frequency, DF, metrics.
	 * 
	 * @param bags The word bags collection.
	 * @param string The string term.
	 * @return The DF value.
	 */
	public int df(HashMap<String, WordBag> bags, String string) {
		int res = 0;
		for (WordBag b : bags.values())
			if (b.contains(string))
				res += 1;
		return res;
	}

	/**
	 * Compute idf (inverse document frequency) metrics.
	 * @param bags The bags of words collection.
	 * @param string The string (term).
	 * @return The idf value.
	 */
	public double idf(HashMap<String, WordBag> bags, String string) {
		return Integer.valueOf(bags.size()).doubleValue()
				/ Integer.valueOf(df(bags, string)).doubleValue();
	}

	public double cosineSimilarity(WordBag testSample, HashMap<String, Double> weightVector) {
		double res = 0;
		for (Entry<String, Integer> entry : testSample.getWords().entrySet()) {
			if (weightVector.containsKey(entry.getKey())) {
				res = res + entry.getValue().doubleValue() * weightVector.get(entry.getKey()).doubleValue();
			}
		}
		double m1 = magnitude(testSample.getWordsAsDoubles().values());
		double m2 = magnitude(weightVector.values());
		return res / (m1 * m2);
	}

	private double magnitude(Collection<Double> values) {
		Double res = 0.0D;
		for (Double v : values)
			res = res + v * v;
		return Math.sqrt(res.doubleValue());
	}

	public /*int*/String classify(SAXVSMClassifier cl, String classKey, double[] series, HashMap<String, HashMap<String, Double>> tfidf, Params params) throws SAXException {
		WordBag test = seriesToWordBag("test", series, params);
		return classify(cl, classKey, test, tfidf, params);
	}

	public /*int*/String classify(SAXVSMClassifier cl, String trueClassLabel, WordBag test, HashMap<String, HashMap<String, Double>> tfidf, Params params) {
		// Cosine similarity, which ranges from 0.0 for the angle of 90 to 1.0 for the angle of 0
		// i.e. LARGES value is a SMALLEST distance
		double minDist = Double.MIN_VALUE;
		String className = "";
		double[] cosines = new double[tfidf.entrySet().size()];

		int index = 0;
		for (Entry<String, HashMap<String, Double>> e : tfidf.entrySet()) {
			double dist = cosineSimilarity(test, e.getValue());
			cosines[index] = dist;
			index++;
			if (dist>minDist) {
				className = e.getKey();
				minDist = dist;
			}
		}
		
		// Update Confusion Matrix
		if(cl.classes.contains(className))
			cl.confusion[cl.classes.indexOf(className)][cl.classes.indexOf(trueClassLabel)]++;
		
		return className;
		
//		// sometimes, due to the VECTORs specific layout, all values are the same, NEED to take care
//		boolean allEqual = true;
//		double cosine = cosines[0];
//		for (int i = 1; i < cosines.length; i++)
//			if (!(cosines[i] == cosine))
//				allEqual = false;
//		// report our findings
//		if (!(allEqual) && className.equalsIgnoreCase(trueClassLabel))
//			return 1;
//		else
//			return 0;
	}
}