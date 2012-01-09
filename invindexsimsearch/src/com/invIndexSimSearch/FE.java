package com.invIndexSimSearch;

import java.util.HashMap;
import java.util.Iterator;

public class FE extends Algorithm {

	public FE() {
		super();
	}

	public FE(Dictionary dictionary, int k) {
		super(dictionary, k);
	}

	public void evaluateQuery(Query query) {
		HashMap<Integer, Integer> docs = new HashMap<Integer, Integer>();
		heap = new Heap(maxHeapSize);
		query.resetItr();
		while (query.hasNext()) {
			Node<Long> n = query.getNextTerm();
			long fid = n.id;
			int queryFeatureWeight = n.weight;
			PostingList posting = dictionary.getPostingList(fid);
			posting.reset();
			do {
				int did = posting.current();
				int weight = posting.getWeight();
				if (docs.containsKey(did)) {
					docs.put(did, docs.get(did) + weight * queryFeatureWeight);
				} else {
					docs.put(did, weight * queryFeatureWeight);
				}
				// posting.next();
			} while (posting.next() > 0);
			posting.reset();
		}
		query.resetItr();

		Iterator<Integer> iter = docs.keySet().iterator();
		while (iter.hasNext()) {
			int did = iter.next();
			int weight = docs.get(did);
			Node<Integer> node = new Node<Integer>(did, weight);
			heap.insert(node);
		}
	}
}
