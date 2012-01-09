package com.invIndexSimSearch;

import java.io.Serializable;

public abstract class PostingList implements Serializable, Comparable<PostingList> {

	private static final long serialVersionUID = 4700576334217717606L;

	protected long featureId;

	// Position at the first doc in the posting list
	public abstract void reset();

	// Returns the current doc
	public abstract int current();

	// Moves to the next doc
	public abstract int next();

	// Checks if there are more docs
	public abstract boolean hasNext();

	// Move to or after a given doc
	public abstract int goTo(int did);

	// Returns the weight at the current index
	public abstract int getWeight();

	// Returns the size of the posting list
	public abstract int size();

	// Adds a weight to the posting list
	public abstract void addWeight(int weight);

	// Adds a document to the posting list
	public abstract void addDoc(int did);

	// Override for comparator operator
	public int compareTo(PostingList cmpPostingList) {
		Integer refPostingDID = this.current();
		Integer cmpPostingDID = cmpPostingList.current();
		return refPostingDID.compareTo(cmpPostingDID);
	}

	public long getFeatureId() {
		return featureId;
	}
}
