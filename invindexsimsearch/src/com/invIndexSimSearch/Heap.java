package com.invIndexSimSearch;

import java.util.Arrays;
import java.util.Collections;

public class Heap {
	private Node[] heap;
	private int heapSize;

	public Heap(int size) {
		heap = new Node[size];
		heapSize = 0;
	}

	public boolean isFull() {
		if (heap[heapSize - 1] != null)
			return true;
		else
			return false;
	}

	public int getMinimum() {
		if (isEmpty())
			return 0;
		else
			return heap[0].weight;
	}

	public boolean isEmpty() {
		return (heapSize == 0);
	}

	private int getLeftChildIndex(int nodeIndex) {
		return 2 * nodeIndex + 1;
	}

	private int getRightChildIndex(int nodeIndex) {
		return 2 * nodeIndex + 2;
	}

	private int getParentIndex(int nodeIndex) {
		return (nodeIndex - 1) / 2;
	}

	public class HeapException extends RuntimeException {
		private static final long serialVersionUID = -6067756644306318640L;

		public HeapException(String message) {
			super(message);
		}
	}

	public void insert(Node value) {
		if (heapSize == heap.length)
			if (value.weight > getMinimum()) {
				removeMin();
			} else {
				return;
			}
		heapSize++;
		heap[heapSize - 1] = value;
		siftUp(heapSize - 1);
	}

	private void siftUp(int nodeIndex) {
		int parentIndex;
		Node temp;
		if (nodeIndex != 0) {
			parentIndex = getParentIndex(nodeIndex);
			if (heap[parentIndex].weight > heap[nodeIndex].weight) {
				temp = heap[parentIndex];
				heap[parentIndex] = heap[nodeIndex];
				heap[nodeIndex] = temp;
				siftUp(parentIndex);
			}
		}
	}

	public void removeMin() {
		if (isEmpty())
			throw new HeapException("Empty");
		else {
			heap[0] = heap[heapSize - 1];
			heapSize--;
			if (heapSize > 0)
				siftDown(0);
		}
	}

	private void siftDown(int nodeIndex) {
		int leftChildIndex, rightChildIndex, minIndex;
		Node temp;
		leftChildIndex = getLeftChildIndex(nodeIndex);
		rightChildIndex = getRightChildIndex(nodeIndex);
		if (rightChildIndex >= heapSize) {
			if (leftChildIndex >= heapSize)
				return;
			else
				minIndex = leftChildIndex;
		} else {
			if (heap[leftChildIndex].weight <= heap[rightChildIndex].weight)
				minIndex = leftChildIndex;
			else
				minIndex = rightChildIndex;
		}
		if (heap[nodeIndex].weight > heap[minIndex].weight) {
			temp = heap[minIndex];
			heap[minIndex] = heap[nodeIndex];
			heap[nodeIndex] = temp;
			siftDown(minIndex);
		}
	}

	public Node[] getReverseSortedHeap() {
		Node[] result = Arrays.copyOf(heap, heap.length);
		Arrays.sort(result);
		Collections.reverse(Arrays.asList(result));
		return result;
	}

	public String toString() {
		return Arrays.toString(heap);
	}
}
