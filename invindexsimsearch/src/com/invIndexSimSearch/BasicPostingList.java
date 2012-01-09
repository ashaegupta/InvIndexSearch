package com.invIndexSimSearch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class BasicPostingList extends PostingList {

	private static final long serialVersionUID = 6377298414582882054L;

	private ArrayList<Integer> docs;
	private ArrayList<Integer> weights;
	private int currentIndex;

	public BasicPostingList(long featureId) {
		this.featureId = featureId;
		docs = new ArrayList<Integer>();
		weights = new ArrayList<Integer>();
		currentIndex = 0;
	}

	public BasicPostingList(long offset, String fileName) {
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(fileName);
			inputStream.skip(offset);
			ObjectInputStream input = new ObjectInputStream(inputStream);
			BasicPostingList postingList = (BasicPostingList) input.readObject();
			featureId = postingList.featureId;
			docs = (ArrayList<Integer>) (postingList.docs);
			weights = (ArrayList<Integer>) (postingList.weights);
			currentIndex = 0;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		return "Feature ID: " + featureId + "\n" + "- Docs: " + docs.toString() + "\n" + "- Weights: "
				+ weights.toString();
	}

	// Position at the first doc in the posting list
	public void reset() {
		currentIndex = 0;
	}

	// Move to the next doc
	public int next() {
		if (hasNext()) {
			return docs.get(currentIndex++);
		} else {
			return -1;
		}
	}

	// Checks if there is an element at the current index
	public boolean hasNext() {
		if (currentIndex < docs.size() - 1)
			return true;
		else
			return false;
	}

	// Returns the current doc
	public int current() {
		if (currentIndex < docs.size())
			return docs.get(currentIndex);
		else
			return -1;
	}

	// Move to or after a given doc
	public int goTo(int did) {
		while (currentIndex < docs.size() && docs.get(currentIndex) < did)
			currentIndex++;
		if (currentIndex < docs.size())
			return docs.get(currentIndex);
		else
			return -1;
	}

	// Returns the weight at the current index
	public int getWeight() {
		if (currentIndex < docs.size())
			return weights.get(currentIndex);
		else
			return -1;
	}

	// Returns the size of the posting list
	public int size() {
		return docs.size();
	}

	// Adds a weight to the posting list
	public void addWeight(int weight) {
		weights.add(weight);
	}

	// Adds a document to the posting list
	public void addDoc(int did) {
		docs.add(did);
	}
}
