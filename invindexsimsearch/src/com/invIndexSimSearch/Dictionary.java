package com.invIndexSimSearch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

// Class that stores features and a pointer to their respective posting list in a serialized file
public class Dictionary implements Serializable {

	private static final long serialVersionUID = -7071787345891097485L;

	public HashMap<Long, DictionaryItem> map;
	private String postingListFileName;
	private boolean compressed;

	public Dictionary() {
		map = new HashMap<Long, DictionaryItem>();
	}

	public Dictionary(String fileName, String postingListFileName, boolean compressed) {
		try {
			FileReader dictionaryReader = new FileReader(fileName);
			BufferedReader dictionaryFileReader = new BufferedReader(dictionaryReader);
			String line;
			map = new HashMap<Long, DictionaryItem>();
			this.compressed = compressed;
			int i = 0;
			while ((line = dictionaryFileReader.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line);
				long featureId = Long.parseLong(tokenizer.nextToken());
				long offset = Long.parseLong(tokenizer.nextToken());
				int maxWeight = Integer.parseInt(tokenizer.nextToken());
				this.add(featureId, maxWeight, offset);
				if (i % 100000 == 0) {
					System.out.println(i);
				}
				i++;
			}
			this.postingListFileName = postingListFileName;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		return map.toString();
	}

	// Method to add a featureId, maxWeight to an Dictionary
	public void add(long featureId, int maxWeight, long offset) {
		map.put(featureId, new DictionaryItem(offset, maxWeight));
	}

	PostingList getPostingList(long f) {
		if (compressed) {
			return new CompressedPostingList(map.get(f).offset, postingListFileName);
		} else {
			return new BasicPostingList(map.get(f).offset, postingListFileName);
		}
	}

	int getMaxWeight(long f) {
		// long = FID
		return map.get(f).maxWeight;
	}

	void writeToFile(String fileName) {
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName));
			for (Map.Entry<Long, DictionaryItem> entry : map.entrySet()) {
				bufferedWriter.write(entry.getKey() + " " + entry.getValue().offset + " " + entry.getValue().maxWeight);
				bufferedWriter.newLine();
			}
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class DictionaryItem implements Serializable {
		private static final long serialVersionUID = -5339805204007231515L;

		public long offset;
		public int maxWeight;

		public DictionaryItem(long offset, int maxWeight) {
			this.offset = offset;
			this.maxWeight = maxWeight;
		}

		public String toString() {
			return "Offset: " + offset + " Max Weight: " + maxWeight;
		}
	}
}
