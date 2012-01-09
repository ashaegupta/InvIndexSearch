package com.invIndexSimSearch;

import java.util.ArrayList;

// Class that implements the Term-at-a-time search method
public class TAAT extends Algorithm {

	private static final boolean DEBUG = false;
	public static Boolean doPrune = false;
	public static Boolean doTerminateEarly = true;
	public static Boolean doSelectiveMerge = true;

	public static int numQueries = 0;
	public static int numETQueries = 0;
	public static int numPRQueries = 0;
	public static Boolean pruned = false;

	public TAAT() {
		super();
	}

	// Create a new Dictionary class and a new heap of size k from input files
	public TAAT(Dictionary dictionary, int k) {
		super(dictionary, k);
		if (DEBUG) {
			System.out.println("New TAAT...");
			System.out.println("Dictionary: " + dictionary);
			System.out.println("Heap: " + heap);
		}
	}

	// Evaluate a specific query
	public void evaluateQuery(Query query) {
		ArrayList<Node<Integer>> current = new ArrayList<Node<Integer>>();
		long upperBound = getUpperBound(query);
		query = reverseSortQuery(query);
		query.resetItr();

		while (query.hasNext()) {

			if (doPrune && !doTerminateEarly) {
				current = prune(current, upperBound);
			} else if (doTerminateEarly && !doPrune) {
				if (terminateEarly(current, upperBound)) {
					if (DEBUG)
						System.out.println("Terminated Early");
					numETQueries++;
					break;
				}
			} else if (doPrune && doTerminateEarly) {
				current = pruneAndTerminateEarly(current, upperBound);
				if (current.size() > 1 && current.get(0).weight == -1) {
					if (DEBUG)
						System.out.println("Terminated Early");
					numETQueries++;
					break;
				}
			}

			Node<Long> n = query.getNextTerm();
			if (DEBUG)
				System.out.println("Next Query term to evaluate: " + n.id);

			PostingList postingList = dictionary.getPostingList(n.id);
			long currMaxWeight = dictionary.getMaxWeight(n.id) * n.weight;

			if (currMaxWeight < 0) {
				System.out.println("Error: The product of queryWeight * maxWeight causes overflow.");
				System.out.println("MaxWeight: " + dictionary.getMaxWeight(n.id));
				System.out.println("queryWeight: " + n.weight);
				System.out.println("Product of the two: " + currMaxWeight);
			}

			current = merge(current, postingList, n.weight, upperBound - currMaxWeight);
			addCurrentToHeap(current);
			upperBound -= currMaxWeight;
		}

		numQueries++;
		if (pruned)
			numPRQueries++;
	}

	// Get the upper bound for the given query
	private int getUpperBound(Query query) {
		// System.out.println("Getting Upper Bound...");
		int upperBound = 0;
		query.resetItr();
		while (query.hasNext()) {
			Node<Long> n = query.getNextTerm();
			try {
				long currMaxWeight = dictionary.getMaxWeight(n.id) * n.weight;
				if (currMaxWeight < 0) {
					System.out.println("Error: The product of queryWeight * maxWeight causes overflow.");
					System.out.println("docID: " + n.id);
					System.out.println("maxWeight: " + dictionary.getMaxWeight(n.id));
					System.out.println("queryWeight: " + n.weight);
					System.out.println("Product of the two: " + currMaxWeight);
				}

				upperBound += (currMaxWeight);

			} catch (NullPointerException e) {
				if (DEBUG)
					System.out.println("Feature: " + n.id + " is not in the dictionary.");
			}
		}
		return upperBound;

	}

	// Prunes & terminates early
	private ArrayList<Node<Integer>> pruneAndTerminateEarly(ArrayList<Node<Integer>> current, long upperBound) {
		int size = current.size();
		Node<Integer> node;
		if (size <= maxHeapSize) {
			return current;
		} else {
			int min = heap.getMinimum();
			int i = 0;
			int sumTerminateEarly = 0;
			while (i < current.size()) {
				node = current.get(i);
				if (node.weight + upperBound < min) {
					current.remove(i);
					pruned = true;
				} else {
					sumTerminateEarly++;
					i++;
				}
			}
			if (sumTerminateEarly <= maxHeapSize) {
				node = new Node<Integer>(-1, -1);
				if (current.size()>0) current.set(0, node);
				else current.add(node);
			}
			return current;
		}
	}

	// Prunes those docs in the current list that have no chance of exceeding
	// the min k on the heap thus far
	private ArrayList<Node<Integer>> prune(ArrayList<Node<Integer>> current, long upperBound) {
		int size = current.size();
		Node<Integer> node;
		if (size <= maxHeapSize) {
			return current;
		} else {
			int min = heap.getMinimum();
			int i = 0;
			while (i < current.size()) {
				node = current.get(i);
				if (node.weight + upperBound < min) {
					current.remove(i);
					pruned = true;
				} else {
					i++;
				}
			}
			return current;
		}
	}

	// Determines if we can terminate early given an the current list and
	// upperBound
	private Boolean terminateEarly(ArrayList<Node<Integer>> current, long upperBound) {
		if (current.size() <= maxHeapSize) {
			return false;
		}
		int k = heap.getMinimum();
		if (DEBUG) {
			System.out.println("In Terminate Early...");
			System.out.println("Min Heap: " + k);
		}
		int sum = 0;
		for (int i = 0; i < current.size(); i++) {
			if ((current.get(i).weight + upperBound) > k) {
				sum++;
			}
		}
		return sum <= maxHeapSize;
	}

	// Merge input with a postingList
	private ArrayList<Node<Integer>> merge(ArrayList<Node<Integer>> current, PostingList postingList,
			Integer queryWeight, long nextUpperBound) {
		ArrayList<Node<Integer>> mergedList = new ArrayList<Node<Integer>>();
		int i = 0;
		postingList.reset();
		Node<Integer> node;
		Node<Integer> currentNode;
		int postingListHasNext = 0;
		int min = heap.getMinimum();
		int size = current.size();

		if (DEBUG) {
			System.out.println("About to merge lists...");
			System.out.println("ArrayList to Merge: " + current.toString());
			System.out.println("PostingList to Merge: " + postingList.toString());
			System.out.println("QueryWeight: " + queryWeight);
		}

		while (postingListHasNext != -1 || i < size) {

			if (postingListHasNext != -1 && i < size) {
				currentNode = current.get(i);
				if (postingList.current() < currentNode.id) {
					if (!doSelectiveMerge
							|| (((Integer) postingList.getWeight() * queryWeight) + nextUpperBound >= min)
							|| size <= maxHeapSize) {
						node = new Node<Integer>((Integer) postingList.current(), (Integer) postingList.getWeight()
								* queryWeight);
						mergedList.add(node);
					}
					postingListHasNext = postingList.next();
				} else if (postingList.current() > currentNode.id) {
					if (!doSelectiveMerge || (currentNode.weight + nextUpperBound >= min) || (size <= maxHeapSize))
						mergedList.add(currentNode);
					i++;
				} else {
					currentNode.weight += (postingList.getWeight() * queryWeight);
					if (!doSelectiveMerge || (currentNode.weight + nextUpperBound >= min) || (size <= maxHeapSize))
						mergedList.add(currentNode);
					postingListHasNext = postingList.next();
					i++;
				}

			} else if (postingListHasNext != -1) {
				if (!doSelectiveMerge || (((Integer) postingList.getWeight() * queryWeight) + nextUpperBound >= min)
						|| size <= maxHeapSize) {
					node = new Node<Integer>((Integer) postingList.current(), (Integer) postingList.getWeight()
							* queryWeight);
					mergedList.add(node);
				}
				postingListHasNext = postingList.next();
			} else if (i < size) {
				currentNode = current.get(i);
				if (!doSelectiveMerge || (currentNode.weight + nextUpperBound >= min) || (size <= maxHeapSize))
					mergedList.add(currentNode);
				i++;
			}

		}

		if (DEBUG) {
			System.out.println("Finished merged.");
			System.out.println("mergedList : " + mergedList);
		}
		return mergedList;
	}

	// Add weights to the heap
	private void addCurrentToHeap(ArrayList<Node<Integer>> current) {
		if (DEBUG)
			System.out.println("AddingCurrentToHeap...");
		heap = new Heap(maxHeapSize);
		int size = current.size();
		for (int i = 0; i < size; i++) {
			heap.insert(current.get(i));
		}
		if (DEBUG)
			System.out.println("Heap: " + heap.toString());
	}

}