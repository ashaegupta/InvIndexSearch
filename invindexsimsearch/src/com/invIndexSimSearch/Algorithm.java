package com.invIndexSimSearch;

import java.util.Collections;
import java.util.Comparator;

public abstract class Algorithm {

	public Dictionary dictionary;
	public Heap heap;
	public int maxHeapSize;

	public Algorithm() {
		dictionary = new Dictionary();
		heap = null;
		maxHeapSize = 0;
	}

	public Algorithm(Dictionary dictionary, int k) {
		this.dictionary = dictionary;// new Dictionary(dictionaryFileName,
										// postingListFileName);
		maxHeapSize = k;
		heap = new Heap(maxHeapSize);
	}

	public abstract void evaluateQuery(Query query);

	public Node[] returnResults() {
		return heap.getReverseSortedHeap();
	}

	public Query reverseSortQuery(Query query) {
		class QueryComparator implements Comparator<Node<Long>> {
			public int compare(Node<Long> arg1, Node<Long> arg2) {
				Integer w1 = arg1.weight * dictionary.getMaxWeight(arg1.id);
				Integer w2 = arg2.weight * dictionary.getMaxWeight(arg2.id);
				return w2.compareTo(w1);
			}
		}
		try {
			Collections.sort(query.terms, new QueryComparator());
		} catch (NullPointerException e) {
			System.out.println("Could not reverse sort due to terms missing in the dictionary.");
		}
		return query;
	}

}
