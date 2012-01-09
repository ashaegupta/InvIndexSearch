package com.invIndexSimSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DAAT extends Algorithm {

	ArrayList<PostingList> postings;
	int currDoc;
	HashMap<Long, Integer> queryWeights;

	public static int numQueries = 0;
	public static double numGotoCalls = 0;
	public static double sumSkips = 0;
	public static double numPostings = 0; 

	public DAAT() {
		super();
	}

	public DAAT(Dictionary dictionary, int k) {
		super(dictionary, k);
	}

	// Evaluate a specific query
	public void evaluateQuery(Query query) {
		numQueries++;
		int fullEvalDID;
		int score;
		queryWeights = new HashMap<Long, Integer>();
		heap = new Heap(maxHeapSize);
		wandInit(query);
		int upperBound = 0;
		Collections.sort(postings);
		
		while ((fullEvalDID = wandNext(upperBound)) >= 0) {
			score = performFullEvaluation(fullEvalDID);
			heap.insert(new Node<Integer>(fullEvalDID, score));
			upperBound = (heap.isFull()) ? heap.getMinimum() : 0;
		}
		System.out.println((double) (numGotoCalls/numPostings));
		System.out.println((double) (sumSkips / numGotoCalls));
	}

	private void wandInit(Query query) {
		postings = new ArrayList<PostingList>();
		currDoc = 0;
		Node<Long> term;
		query.resetItr();
		while ((term = query.getNextTerm()) != null) {
			queryWeights.put(term.id, term.weight);
			PostingList tmpPostingList = dictionary.getPostingList(term.id);
			tmpPostingList.goTo(0);
			numPostings += tmpPostingList.size();
			postings.add(tmpPostingList);
		}
		query.resetItr();
	}

	private int wandNext(int upperBound) {
		int pTerm, pivot, aterm;

		while (true) {

			pTerm = findPivotTerm(upperBound);
			if (pTerm < 0)
				return -1;
			pivot = postings.get(pTerm).current();
			if (pivot == -1)
				return -1; // lastID
			if (pivot <= currDoc) {
				aterm = pickTerm(pTerm - 1);
				sumSkips += postings.get(aterm).goTo(currDoc + 1);
				numGotoCalls++;
				if (postings.get(aterm).current() < 0)
					postings.remove(aterm);
				else
					sortPostingsBySwap(aterm);
			} else {
				if (postings.get(0).current() == pivot) {
					currDoc = pivot;
					return currDoc;
				} else {
					aterm = pickTerm(pTerm - 1);
					sumSkips += postings.get(aterm).goTo(pivot);
					numGotoCalls++;
					if (postings.get(aterm).current() < 0)
						postings.remove(aterm);
					else
						sortPostingsBySwap(aterm);
				}
			}

		}
	}

	private void sortPostingsBySwap(int aterm) {
		PostingList tmp;
		for (int i = aterm; i < postings.size() - 1; i++) {
			tmp = postings.get(i);
			if (i + 1 < postings.size() && tmp.current() > postings.get(i + 1).current()) {
				postings.set(i, postings.get(i + 1));
				postings.set(i + 1, tmp);
			}
		}
	}

	private int performFullEvaluation(int docID) {
		int score = 0;
		int i;
		for (i = 0; i < postings.size(); i++) {
			if (postings.get(i).current() == docID) {
				score += queryWeights.get(postings.get(i).getFeatureId()) * postings.get(i).getWeight();
			}
		}
		return score;
	}

	private int pickTerm(int pTerm) {
		/*
		 * int i; int aterm = -1; int currMaxWeight = 0; for (i = 0; i <= pTerm;
		 * i++) { if (postings.get(i).getWeight() > currMaxWeight) { aterm = i;
		 * currMaxWeight = postings.get(i).getWeight(); } }
		 */
		return 0;
	}

	private int findPivotTerm(int upperBound) {
		int i;
		int testBound = 0;
		long featureId;
		for (i = 0; i < postings.size(); i++) {
			featureId = postings.get(i).getFeatureId();
			testBound += queryWeights.get(postings.get(i).getFeatureId()) * dictionary.getMaxWeight(featureId);
			if (testBound >= upperBound)
				return i;
		}
		return -1;
	}
}
