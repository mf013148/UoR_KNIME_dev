package uk.ac.reading.cs.knime.hotsax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

/**
 * Implements HOTSAX discord discovery algorithm.
 * @author Ryan Faulkner
 */
public class HotSAXProcessor {
	/*Time indexes*/
	String ts_d[];
	/*Timeseries data*/
	double ts[];
	/*Collection of Discord records*/
	DiscordRecords dr;

	private static TSProcessor tp = new TSProcessor();
	//private static SAXProcessor sp = new SAXProcessor();
	//private static NormalAlphabet na = new NormalAlphabet();

	
	/**
	 * Hash-table backed implementation. Time series is converted into a:
	 * 1) SAXRecords data structure first, 
	 * 2) Hash-table backed magic array second, 
	 * and HOTSAX is applied thirdly. 
	 * Nearest neighbours are searched only among the subsequences which were 
	 * produced by SAX with specified numerosity reduction. Thus, if the 
	 * strategy is EXACT or MINDIST, discords do not match those produced by 
	 * BruteForce or NONE.
	 * @param series				The time series.
	 * @param discordsNumToReport	The number of discords to report.
	 * @param windowSize			SAX sliding window size.
	 * @param paaSize				SAX PAA value.
	 * @param alphabetSize			SAX alphabet size.
	 * @param strategy				the numerosity reduction strategy.
	 * @param nThreshold			the normalisation threshold value.
	 * @return The set of discords found within the time series, it may return less than asked for -- in this case, there are no more discords.
	 * @throws Exception if error occurs.
	 */
	public BufferedDataTable series2Discords(BufferedDataTable[] inData, SAXParameters params, ExecutionContext exec) throws Exception {
		// Define results collection object 
		DataTableSpec outSpec = SAXTable.createDataTableSpec();
		BufferedDataContainer container = exec.createDataContainer(outSpec);
		
		// get the SAX transform done
		ts_d = TSProcessor.readDateColumn(inData, params.DATECOL);
		ts = TSProcessor.readColumn(inData, params.COLNAME);
		SAXRecords sax = SAXProcessor.readSAXColumn(inData, "SAX String");//sp.ts2saxViaWindow(HotSAXNodeModel.ts, params.SAX_WINDOW_SIZE,	params.SAX_PAA_SIZE, na.getCuts(params.SAX_ALPHABET_SIZE), params.SAX_NR_STRATEGY, params.SAX_NORM_THRESHOLD);

		// fill the array for the outer loop
		ArrayList<MagicArrayEntry> magicArray = new ArrayList<MagicArrayEntry>(sax.getRecords().size());
		for (SAXRecord sr : sax.getRecords())
			magicArray.add(new MagicArrayEntry(String.valueOf(sr.getPayload()), sr.getIndexes().size()));
		System.err.println("Magic array filled");

		DiscordRecords discords = getDiscordsWithMagic(ts, sax, params.SAX_WINDOW_SIZE, magicArray, params.DISCORDS);
		dr = discords;
		System.err.println(discords.getSize() + " discords found");

		//Add records to outport table
		RowKey key = null;
		DataCell[] cells = null;
		DataRow row = null;
		int row_count = 0;
		for (DiscordRecord idx : discords) {
			key = new RowKey("Row"+row_count);
			cells = new DataCell[5];
			cells[0] = new StringCell(idx.getPayload());
			cells[1] = new IntCell(idx.getPosition());
			cells[2] = new IntCell(idx.getLength());
			cells[3] = new DoubleCell(idx.getNNDistance());
			cells[4] = new IntCell(idx.getRuleId());
			row = new DefaultRow(key,cells);
			container.addRowToTable(row);
			row_count++;
		}
		container.close();
		return container.getTable();
	}

	private DiscordRecords getDiscordsWithMagic(double[] series, SAXRecords sax, int windowSize,
			ArrayList<MagicArrayEntry> magicArray, int discordCollectionSize) throws Exception {
		// sort the candidates
		Collections.sort(magicArray);

		// resulting discords collection
		DiscordRecords discords = new DiscordRecords();
		
		// visit registry
		HashSet<Integer> visitRegistry = new HashSet<Integer>(windowSize * discordCollectionSize);

		// we conduct the search until the number of discords is less than desired
		while (discords.getSize() < discordCollectionSize) {
			System.err.println("Currently known discords: " + discords.getSize() + " out of " + discordCollectionSize);

			DiscordRecord bestDiscord = findBestDiscordWithMagic(series, windowSize, sax, magicArray, visitRegistry);

			// if the discord is null we getting out of the search
			if (bestDiscord.getNNDistance() == 0.0D || bestDiscord.getPosition() == -1) {
				System.err.println("Search end: " + discords.getSize() + " discords found, Last seen:" + bestDiscord);
				break;
			}

			bestDiscord.setInfo("position " + bestDiscord.getPosition() + ", NN distance " + bestDiscord.getNNDistance() + bestDiscord.getInfo());
			System.err.println(bestDiscord.getInfo());

			bestDiscord.setLength(windowSize);
			
			// collect the result
			discords.add(bestDiscord);

			// and maintain data structures
			int markStart = bestDiscord.getPosition() - windowSize;
			int markEnd = bestDiscord.getPosition() + windowSize;
			for (int i = markStart; i < markEnd; i++)
				visitRegistry.add(i);
		}
		return discords;
	}

	/**
	 * This method reports the best found discord. Note, that this discord is
	 * approximately the best. Due to the fuzzy-logic search with randomisation
	 * and aggressive labelling of the magic array locations.
	 *
	 * @param series 			The series we are looking for discord in.
	 * @param windowSize 		The sliding window size.
	 * @param sax 				The SAX data structure for the reference.
	 * @param allWords 			The magic heuristics array.
	 * @param discordRegistry	The global visit array.
	 * @return The best discord instance.
	 * @throws Exception If error occurs.
	 */
	private static DiscordRecord findBestDiscordWithMagic(double[] series, int windowSize, SAXRecords sax,
			ArrayList<MagicArrayEntry> allWords, HashSet<Integer> discordRegistry) throws Exception {
		// prepare the visits array, note that there can't be more points to
		// visit that in a SAX index
		int[] visitArray = new int[series.length];

		// init tracking variables
		int bestSoFarPosition = -1;
		double bestSoFarDistance = 0.0D;
		String bestSoFarWord = "";

		System.err.println("Iterating over " + allWords.size() + " entries");

		for (MagicArrayEntry currentEntry : allWords) {
			// look into each entry
			String currentWord = currentEntry.getStr();
			Set<Integer> occurrences = sax.getByWord(currentWord).getIndexes();

			// iterate over these candidate positions first
			for (int currentPos : occurrences) {
				// make sure it is not a previously found discord passed through the parameters array note, that the
				// discordRegistry contains the whole span of previously found discord, not just it's position...
				if (discordRegistry.contains(currentPos))
					continue;

				System.err.println("Conducting search for " + currentWord + " at " + currentPos);

				int markStart = currentPos - windowSize;
				int markEnd = currentPos + windowSize;

				// all the candidates we are not going to try
				HashSet<Integer> alreadyVisited = new HashSet<Integer>(occurrences.size() + (markEnd - markStart));

				for (int i = markStart; i < markEnd; i++)
					alreadyVisited.add(i);

				// fix the current subsequence trace
				double[] currentCandidateSeq = tp.subseriesByCopy(series, currentPos, currentPos + windowSize);

				// Begin the search...
				double nearestNeighborDist = Double.MAX_VALUE;
				boolean doRandomSearch = true;

				for (Integer nextOccurrence : occurrences) {
					// just in case there is an overlap
					if (alreadyVisited.contains(nextOccurrence))
						continue;
					else
						alreadyVisited.add(nextOccurrence);

					// get the subsequence and the distance
					double dist = distance(currentCandidateSeq, series, nextOccurrence, nextOccurrence + windowSize);

					// keep track of best so far distance
					if (dist < nearestNeighborDist) {
						nearestNeighborDist = dist;
						System.err.println("** current NN at " + nextOccurrence + ", distance: " + nearestNeighborDist
								+ ", Pos" + currentPos);
					}
					if (dist < bestSoFarDistance) {
						doRandomSearch = false;
						System.err.println("** abandoning the occurrences loop, distance " + dist
								+ " is less than the best so far " + bestSoFarDistance);
						break;
					}
				}

				// check if we must continue with random neighbors
				if (doRandomSearch) {
					System.err.println("Starting random search");
					// init the visit array
					int visitCounter = 0;
					int cIndex = 0;
					for (int i = 0; i < series.length - windowSize; i++)
						if (!(alreadyVisited.contains(i))) {
							visitArray[cIndex] = i;
							cIndex++;
						}
					cIndex--;

					// shuffle the visit array
					Random rnd = new Random();
					for (int i = cIndex; i > 0; i--) {
						int index = rnd.nextInt(i + 1);
						int a = visitArray[index];
						visitArray[index] = visitArray[i];
						visitArray[i] = a;
					}

					// while there are unvisited locations
					while (cIndex >= 0) {
						int randomPos = visitArray[cIndex];
						cIndex--;

						double dist = distance(currentCandidateSeq, series, randomPos, randomPos + windowSize);

						// keep track
						if (dist < nearestNeighborDist) {
							System.err.println("** current NN at " + +randomPos + ", distance: " + dist);
							nearestNeighborDist = dist;
						}

						// early abandoning of the search:
						// the current word is not discord, we have seen better
						if (dist < bestSoFarDistance) {
							nearestNeighborDist = dist;
							System.err.println(" ** abandoning random visits loop, seen distance " + nearestNeighborDist
									+ " at iteration " + visitCounter);
							break;
						}
						visitCounter = visitCounter + 1;
					} // while inner loop
				} // end of random search loop

				if (nearestNeighborDist > bestSoFarDistance && nearestNeighborDist < Double.MAX_VALUE) {
					System.err.println("discord updated: pos " + currentPos + ", dist " + bestSoFarDistance);
					bestSoFarDistance = nearestNeighborDist;
					bestSoFarPosition = currentPos;
					bestSoFarWord = currentWord;
				}
				System.err.println("Best distance: " + bestSoFarDistance + " for a string " + bestSoFarWord + " at "
						+ bestSoFarPosition);
			} // outer loop inner part
		} // outer loop
		return new DiscordRecord(bestSoFarPosition, bestSoFarDistance, bestSoFarWord);
	}

	/**
	 * Calculates the Euclidean distance between two points. Don't use this
	 * unless you need that.
	 * 
	 * @param subseries	The first point.
	 * @param series 	The second point.
	 * @param from 		the initial index of the range to be copied, inclusive
	 * @param to 		the final index of the range to be copied, exclusive. (This index may lie outside the array.)
	 * @return The Euclidean distance.
	 */
	private static double distance(double[] subseries, double[] series, int from, int to) throws Exception {
		Double sum = 0D;
		for (int i = from; i < to; i++) {
			double tmp = subseries[i - from] - series[i];
			sum = sum + tmp * tmp;
		}
		return Math.sqrt(sum);
	}
}
