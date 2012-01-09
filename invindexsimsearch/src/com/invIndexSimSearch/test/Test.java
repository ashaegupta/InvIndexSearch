package com.invIndexSimSearch.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.invIndexSimSearch.DAAT;
import com.invIndexSimSearch.Dictionary;
import com.invIndexSimSearch.FE;
import com.invIndexSimSearch.Query;
import com.invIndexSimSearch.TAAT;

public class Test {

	public static final int HEAP_SIZE = 5;
	public static final int NUM_QUERIES = 200;

	private static final boolean COMPRESSED = true;
	private static final boolean SORT_QUERIES = false;
	private static final boolean REVERSE_SORT_QUERIES = false;

	private static final String QUERY_FILENAME = "data/queryData.txt";

	public static final String DICTIONARY_FILENAME = "data/dictionary.ser";
	public static final String POSTING_LIST_FILENAME = "data/posting.ser";
	private static final String TAAT_RESULTS_FILENAME = "results/TAAT_k5_results_FTT.txt";
	private static final String DAAT_RESULTS_FILENAME = "results/DAAT_k5_results_m1000.txt";
	private static final String FE_RESULTS_FILENAME = "results/FE_k5_results.txt";

	public static final String COMPRESSED_DICTIONARY_FILENAME = "data/compressedDictionary.ser";
	public static final String COMPRESSED_POSTING_LIST_FILENAME = "data/compressedPosting.ser";
	private static final String COMPRESSED_TAAT_RESULTS_FILENAME = "results/compressed_TAAT_k" + String.valueOf(HEAP_SIZE) + "_results_FTT.txt";
	private static final String COMPRESSED_DAAT_RESULTS_FILENAME = "results/compressed_DAAT_k" + String.valueOf(HEAP_SIZE) + "_results_m10000.txt";
	private static final String COMPRESSED_FE_RESULTS_FILENAME = "results/compressed_FE_k5_results.txt";

	private static final int[] queryIndices = { 7142, 8238, 7906, 6598, 7472, 7468, 1496, 5628, 11707, 5510, 1837,
			2410, 2516, 10554, 6683, 6495, 3039, 11837, 2587, 4778, 4893, 275, 4589, 10639, 4145, 9439, 7962, 2731, 68,
			7399, 10236, 8375, 6811, 6707, 8309, 10228, 272, 7904, 7547, 10118, 9058, 2481, 10547, 11006, 8431, 6881,
			10767, 8475, 8198, 2207, 4443, 2217, 8821, 9635, 3153, 8160, 847, 7490, 1789, 7837, 11820, 4884, 2425,
			3659, 9736, 3803, 11811, 9134, 11260, 7071, 9765, 5478, 11005, 1315, 9725, 4427, 7438, 6254, 9048, 5281,
			5954, 12281, 12241, 8215, 1827, 4604, 569, 7843, 1426, 6678, 1720, 3346, 6926, 7783, 6839, 4752, 8404,
			8337, 2515, 8264, 3990, 11849, 555, 8687, 9466, 4917, 4728, 11125, 9857, 2695, 5945, 8771, 10013, 4298,
			1401, 6947, 11775, 8557, 8162, 1867, 6883, 1420, 7709, 6915, 12125, 11013, 5970, 1534, 10843, 7342, 7564,
			8236, 9238, 2337, 3283, 4109, 3043, 10154, 9933, 5912, 8261, 6905, 1386, 8571, 10273, 4310, 5004, 5772,
			10474, 6242, 5905, 12242, 3368, 10081, 394, 3594, 238, 2459, 445, 7544, 12234, 9713, 5575, 8810, 7745,
			9215, 3620, 2101, 9342, 6797, 10982, 12158, 5716, 483, 2165, 1003, 9196, 6214, 10845, 9160, 3995, 5883,
			974, 5056, 6862, 9946, 545, 7366, 5638, 9159, 9009, 2445, 2708, 10628, 11234, 6501, 8841, 8520, 10587, 9790 };

	public ArrayList<Query> queries;
	public Iterator<Query> itr;
	public Query query;

	public Dictionary dictionary;
	public DAAT daat;
	public TAAT taat;
	public FE fe;

	public Test() {
		System.out.println("Starting a new test... ");
		if (COMPRESSED) {
			dictionary = new Dictionary(COMPRESSED_DICTIONARY_FILENAME, COMPRESSED_POSTING_LIST_FILENAME, COMPRESSED);
		} else {
			dictionary = new Dictionary(DICTIONARY_FILENAME, POSTING_LIST_FILENAME, COMPRESSED);
		}
		queries = getQueries(QUERY_FILENAME);
		// taat = new TAAT(dictionary, HEAP_SIZE);
		daat = new DAAT(dictionary, HEAP_SIZE);
		// fe = new FE(dictionary, HEAP_SIZE);
	}

	// Method to parse the queries from a file
	public ArrayList<Query> getQueries(String queryFileName) {
		ArrayList<Query> queryList = new ArrayList<Query>();
		Query query = new Query();
		FileReader fileReader;

		try {
			fileReader = new FileReader(queryFileName);
			BufferedReader reader = new BufferedReader(fileReader);
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					long featureId = Long.parseLong(tokenizer.nextToken());
					int weight = Integer.parseInt(tokenizer.nextToken());
					if (!(featureId == 0 && weight == 0)) {
						if (dictionary.map.containsKey(featureId))
							query.addTerm(featureId, weight);
					} else {
						queryList.add(query);
						query = new Query();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		ArrayList<Query> returnList;
		if (SORT_QUERIES) {
			// Send back the shortest queries
			Collections.sort(queryList);
			returnList = new ArrayList<Query>(queryList.subList(0, NUM_QUERIES));
		} else if (REVERSE_SORT_QUERIES) {
			// Send back the longest queries
			Collections.sort(queryList, Collections.reverseOrder());
			returnList = new ArrayList<Query>(queryList.subList(0, NUM_QUERIES));
		} else {
			// Send back random queries
			returnList = new ArrayList<Query>();
			for (int i = 0; i < queryIndices.length; i++) {
				returnList.add(queryList.get(queryIndices[i]));
			}
		}

		return returnList;
	}

	public void testTAAT() {
		System.out.println("**************************** TESTING TAAT *************************");
		BufferedWriter bufferedWriter;
		long runTime = 0;

		try {
			if (COMPRESSED) {
				bufferedWriter = new BufferedWriter(new FileWriter(COMPRESSED_TAAT_RESULTS_FILENAME));
			} else {
				bufferedWriter = new BufferedWriter(new FileWriter(TAAT_RESULTS_FILENAME));
			}
			for (int i = 0; i < queries.size(); i++) {
				System.out.println("Query id: " + queryIndices[i]);
				query = queries.get(i);
				long before = Calendar.getInstance().getTimeInMillis();
				taat.evaluateQuery(query);
				long after = Calendar.getInstance().getTimeInMillis();
				runTime += after - before;
				bufferedWriter.write(taat.heap.toString());
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
			bufferedWriter.write("Runtime: " + runTime);
			bufferedWriter.newLine();
			bufferedWriter.write("% EarlyTerminated: " + ((float) TAAT.numETQueries / (float) TAAT.numQueries));
			bufferedWriter.newLine();
			bufferedWriter.write("% Pruned: " + ((float) TAAT.numPRQueries / (float) TAAT.numQueries));
			bufferedWriter.newLine();
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void testDAAT() {
		System.out.println("**************************** TESTING DAAT *************************");
		BufferedWriter bufferedWriter;
		long runTime = 0;
		try {
			if (COMPRESSED) {
				bufferedWriter = new BufferedWriter(new FileWriter(COMPRESSED_DAAT_RESULTS_FILENAME));
			} else {
				bufferedWriter = new BufferedWriter(new FileWriter(DAAT_RESULTS_FILENAME));
			}
			for (int i = 0; i < queries.size(); i++) {
				query = queries.get(i);
				long before = Calendar.getInstance().getTimeInMillis();
				daat.evaluateQuery(query);
				long after = Calendar.getInstance().getTimeInMillis();
				runTime += after - before;
				bufferedWriter.write(daat.heap.toString());
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
			bufferedWriter.write("Runtime: " + runTime);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void testFE() {
		System.out.println("**************************** TESTING FE *************************");
		BufferedWriter bufferedWriter;
		long runTime = 0;
		try {
			if (COMPRESSED) {
				bufferedWriter = new BufferedWriter(new FileWriter(COMPRESSED_FE_RESULTS_FILENAME));
			} else {
				bufferedWriter = new BufferedWriter(new FileWriter(FE_RESULTS_FILENAME));
			}
			for (int i = 0; i < queries.size(); i++) {
				query = queries.get(queryIndices[i]);
				long before = Calendar.getInstance().getTimeInMillis();
				fe.evaluateQuery(query);
				long after = Calendar.getInstance().getTimeInMillis();
				runTime += after - before;
				bufferedWriter.write(fe.heap.toString());
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
			bufferedWriter.write("Runtime: " + runTime);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Test t = new Test();
		// t.testTAAT();
		t.testDAAT();
		// t.testFE();
	}
}
