package com.invIndexSimSearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class Serialization {

	public static void main(String[] args) {
		try {
			Dictionary dictionary = new Dictionary();
			Dictionary compressedDictionary = new Dictionary();
			FileReader postingReader = new FileReader("data/postingData.txt");
			BufferedReader postingFileReader = new BufferedReader(postingReader);
			String line;

			int i = 0;
			while ((line = postingFileReader.readLine()) != null) {
				// System.out.println(line);
				StringTokenizer tokenizer = new StringTokenizer(line);
				long featureId = Long.parseLong(tokenizer.nextToken());
				// System.out.println(featureId);
				File postingFile = new File("data/posting.ser");
				File compressedPostingFile = new File("data/compressedPosting.ser");
				long postingOffset = postingFile.length();
				long compressedPostingOffset = compressedPostingFile.length();
				int maxWeight = createPostingList(featureId, tokenizer, postingFile, compressedPostingFile);
				dictionary.add(featureId, maxWeight, postingOffset);
				compressedDictionary.add(featureId, maxWeight, compressedPostingOffset);
				i++;
				if (i % 100000 == 0) {
					System.out.println(i);
				}
			}

			// System.out.println(dictionary);
			dictionary.writeToFile("data/dictionary.ser");
			compressedDictionary.writeToFile("data/compressedDictionary.ser");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// Method that creates a posting list and returns the max weight
	private static Integer createPostingList(long featureId, StringTokenizer tokenizer, File postingFile,
			File compressedPostingFile) {

		TreeMap<Integer, Integer> postingInput = new TreeMap<Integer, Integer>();

		BasicPostingList posting = new BasicPostingList(featureId);
		CompressedPostingList compressedPosting = new CompressedPostingList(featureId);

		int j = 0;
		int maxWeight = 0;
		int did = 0;
		while (tokenizer.hasMoreElements()) {
			Integer token = Integer.parseInt(tokenizer.nextToken());
			if (j % 2 == 0 && token != 0) {
				did = token;
				postingInput.put(token, 0);
			} else if (j % 2 == 1 && token != 0) {
				if (token > maxWeight) {
					maxWeight = token;
				}
				postingInput.put(did, token);
			}
			j++;
		}

		Iterator<Integer> iter = postingInput.keySet().iterator();
		int key;
		while (iter.hasNext()) {
			key = iter.next();
			posting.addDoc(key);
			posting.addWeight(postingInput.get(key));
			compressedPosting.addDoc(key);
			compressedPosting.addWeight(postingInput.get(key));
		}

		// System.out.println(posting);
		// System.out.println(compressedPosting);

		try {
			FileOutputStream postingStream = new FileOutputStream(postingFile, true);
			FileOutputStream compressedPostingStream = new FileOutputStream(compressedPostingFile, true);
			ObjectOutputStream postingOut = new ObjectOutputStream(postingStream);
			ObjectOutputStream compressedPostingOut = new ObjectOutputStream(compressedPostingStream);
			postingOut.writeObject(posting);
			postingOut.flush();
			postingOut.close();
			postingStream.close();
			compressedPostingOut.writeObject(compressedPosting);
			compressedPostingOut.flush();
			compressedPostingOut.close();
			compressedPostingStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return maxWeight;
	}
}
