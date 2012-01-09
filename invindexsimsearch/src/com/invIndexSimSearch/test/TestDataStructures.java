package com.invIndexSimSearch.test;

import java.util.Arrays;

import com.invIndexSimSearch.CompressedPostingList;
import com.invIndexSimSearch.Dictionary;
import com.invIndexSimSearch.Heap;
import com.invIndexSimSearch.Node;
import com.invIndexSimSearch.BasicPostingList;

public class TestDataStructures {

	public static final String DICTIONARY_FILENAME = "data/dictionary.ser";
	public static final String POSTING_LIST_FILENAME = "data/posting.ser";

	public static final String COMPRESSED_DICTIONARY_FILENAME = "data/compressedDictionary.ser";
	public static final String COMPRESSED_POSTING_LIST_FILENAME = "data/compressedPosting.ser";

	static void testHeap() {
		System.out.println("**************************** TESTING HEAP *************************");
		Heap heap = new Heap(5);
		heap.insert(new Node<Integer>(0, 4));
		heap.insert(new Node<Integer>(1, 6));
		heap.insert(new Node<Integer>(2, 9));
		heap.insert(new Node<Integer>(3, 2));
		heap.insert(new Node<Integer>(4, 33));
		heap.insert(new Node<Integer>(5, 77));
		heap.insert(new Node<Integer>(6, 234));
		heap.insert(new Node<Integer>(7, 1));
		heap.insert(new Node<Integer>(8, 0));
		heap.insert(new Node<Integer>(9, 7));
		System.out.println(heap);
		System.out.println(Arrays.toString(heap.getReverseSortedHeap()));
	}

	static void testPostingList() {
		System.out.println("**************************** TESTING POSTING LIST *************************");

		Dictionary postingDictionary = new Dictionary(DICTIONARY_FILENAME, POSTING_LIST_FILENAME, false);
		// 526037697014L, 207346837654L
		long postingOffset = postingDictionary.map.get(302297782803L).offset;
		BasicPostingList postingList = new BasicPostingList(postingOffset, POSTING_LIST_FILENAME);
		postingList.reset();

		Dictionary compressedPostingDictionary = new Dictionary(COMPRESSED_DICTIONARY_FILENAME,
				COMPRESSED_POSTING_LIST_FILENAME, true);
		// 526037697014L, 207346837654L
		long compressedPostingOffset = compressedPostingDictionary.map.get(302297782803L).offset;
		CompressedPostingList compressedPostingList = new CompressedPostingList(compressedPostingOffset,
				COMPRESSED_POSTING_LIST_FILENAME);
		compressedPostingList.reset();

		System.out.println("Current: " + postingList.current() + " -- " + compressedPostingList.current());
		System.out.println("Current Weight: " + postingList.getWeight() + " -- " + compressedPostingList.getWeight());
		System.out.println("Next: " + postingList.next() + " -- " + compressedPostingList.next());
		System.out.println("Current: " + postingList.current() + " -- " + compressedPostingList.current());
		System.out.println("Has Next: " + postingList.hasNext() + " -- " + compressedPostingList.hasNext());
		System.out.println("Go To: " + postingList.goTo(4044) + " -- " + compressedPostingList.goTo(4044));
		System.out.println("Current: " + postingList.current() + " -- " + compressedPostingList.current());
		System.out.println("Go To: " + postingList.goTo(5061) + " -- " + compressedPostingList.goTo(5061));
		System.out.println("Current: " + postingList.current() + " -- " + compressedPostingList.current());
		System.out.println("Next: " + postingList.next() + " -- " + compressedPostingList.next());
		System.out.println("Go To: " + postingList.goTo(29100) + " -- " + compressedPostingList.goTo(29100));
		System.out.println("Current: " + postingList.current() + " -- " + compressedPostingList.current());
		System.out.println("Go To: " + postingList.goTo(52910) + " -- " + compressedPostingList.goTo(52910));
		System.out.println("Current: " + postingList.current() + " -- " + compressedPostingList.current());
		System.out.println("Next: " + postingList.next() + " -- " + compressedPostingList.next());
	}

	public static void main(String[] args) {
		testHeap();
		testPostingList();
	}
}
