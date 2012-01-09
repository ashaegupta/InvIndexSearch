package com.invIndexSimSearch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class CompressedPostingList extends PostingList {

	private static final long serialVersionUID = 5783197536114138230L;
	private static final int SKIP_INTERVAL = 1000;

	private ArrayList<Byte> docs;
	private ArrayList<Integer> weights;
	private ArrayList<SkipNode> skipList;

	private int currentWeightIndex;
	private int currentBaseDid;
	private int currentDocIndex;
	private int currentSkipIndex;

	private transient int lastDid = 0;
	private transient int numDocs = 0;

	public CompressedPostingList(long featureId) {
		this.featureId = featureId;
		docs = new ArrayList<Byte>();
		weights = new ArrayList<Integer>();
		skipList = new ArrayList<SkipNode>();
		currentWeightIndex = 0;
		currentDocIndex = 0;
		currentBaseDid = 0;
		currentSkipIndex = 0;
	}

	public CompressedPostingList(long offset, String fileName) {
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(fileName);
			inputStream.skip(offset);
			ObjectInputStream input = new ObjectInputStream(inputStream);
			CompressedPostingList postingList = (CompressedPostingList) input.readObject();
			featureId = postingList.featureId;
			docs = (ArrayList<Byte>) (postingList.docs);
			weights = (ArrayList<Integer>) (postingList.weights);
			skipList = (ArrayList<SkipNode>) (postingList.skipList);
			currentWeightIndex = 0;
			currentDocIndex = 0;
			currentBaseDid = skipList.get(0).did;
			currentSkipIndex = 0;
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
				+ weights.toString() + "\n" + "- Skip List: " + skipList.toString();
	}

	// Position at the first doc in the posting list
	public void reset() {
		currentWeightIndex = 0;
		currentDocIndex = 0;
		currentBaseDid = skipList.get(0).did;
		currentSkipIndex = 0;
	}

	private int getDoc(boolean advance) {

		int index = currentDocIndex;
		if (index >= docs.size()) {
			return -1;
		}

		int delta = docs.get(index++);
		int doc = currentBaseDid + (delta & 0x7f);

		// Check if the 8th bit is set to continue extracting
		// deltas
		int k = 1;
		while ((delta & 0x80) != 0) {
			if (index >= docs.size()) {
				return -1;
			}
			delta = docs.get(index++);
			doc += (delta & 0x7f) << (k * 7);
			k++;
		}

		if (advance) {
			currentDocIndex = index;
			currentWeightIndex++;
			currentBaseDid = doc;
		}

		return doc;
	}

	// Returns the current doc
	public int current() {
		return getDoc(false);
	}

	// Moves to the next doc
	public int next() {
		if (hasNext()) {
			return getDoc(true);
		} else {
			return -1;
		}
	}

	// Checks if there are more docs
	public boolean hasNext() {
		if (currentWeightIndex < weights.size() - 1)
			return true;
		else
			return false;
	}

	// Move to or after a given doc
	// Returns the number of skips
	public int goTo(int did) {

		int startSkipIndex = 0;
		if (did > skipList.get(currentSkipIndex).did) {
			startSkipIndex = currentSkipIndex;
		}

		// Look up the skip list to get the did that is equal
		// or just greater than the incoming did
		int selectedSkipIndex = 0;
		for (int i = startSkipIndex; i < skipList.size(); i++) {
			if (skipList.get(i).did >= did) {
				if (i > 0) {
					selectedSkipIndex = i - 1;
				} else {
					selectedSkipIndex = i;
				}
				break;
			}
		}

		int currentDid = current();
		if (did < currentDid) {
			currentDocIndex = skipList.get(selectedSkipIndex).index;
			currentBaseDid = skipList.get(selectedSkipIndex).did;
			currentWeightIndex = skipList.get(selectedSkipIndex).weightIndex;
			currentSkipIndex = selectedSkipIndex;
		}

		// Start decompressing delta doc bytes
		while (hasNext() && currentDid < did) {
			next();
			currentDid = current();
		}

		// Special case to handle when the did is more than the
		// last doc id
		if (currentDid < did) {
			currentDocIndex = docs.size();
			currentWeightIndex = weights.size();
			currentBaseDid = current();
		}

		return (SKIP_INTERVAL * (selectedSkipIndex - startSkipIndex));
	}

	// Returns the weight at the current index
	public int getWeight() {
		if (currentWeightIndex < weights.size())
			return weights.get(currentWeightIndex);
		else
			return -1;
	}

	// Returns the size of the posting list
	public int size() {
		return weights.size();
	}

	// Adds a weight to the posting list
	public void addWeight(int weight) {
		weights.add(weight);
	}

	// Adds a document to the posting list
	public void addDoc(int did) {

		// Check if we need to add a skip list node
		if (numDocs == 0) {
			SkipNode node = new SkipNode(did, docs.size(), weights.size());
			skipList.add(node);
		} else if (numDocs % SKIP_INTERVAL == 0) {
			SkipNode node = new SkipNode(lastDid, docs.size(), weights.size());
			skipList.add(node);
		}

		int delta = 0;
		if (numDocs != 0) {
			delta = did - lastDid;
		}

		lastDid = did;

		// Delta must always be positive!
		if (delta < 0) {
			throw new RuntimeException("Unsorted docs!");
		}

		while (true) {
			// Extract the lower 7 bits
			byte data = (byte) (delta & 0x7f);
			// Shift the delta by 7 bits
			delta = delta >> 7;
			if (delta != 0) {
				// Set the 8th bit
				data |= (byte) (data | 0x80);
			}
			docs.add(data);
			if (delta == 0) {
				break;
			}
		}

		numDocs++;
	}

	class SkipNode implements Serializable {
		private static final long serialVersionUID = 7071233711957163163L;

		public int did;
		public int index;
		public int weightIndex;

		public SkipNode(int did, int index, int weightIndex) {
			this.did = did;
			this.index = index;
			this.weightIndex = weightIndex;
		}

		public String toString() {
			return "DID: " + did + " Index: " + index + " Weight Index: " + weightIndex;
		}
	}
}
