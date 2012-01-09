package com.invIndexSimSearch;

public class Node<T> implements Comparable<Node<T>> {

	public T id;
	public int weight;

	public Node(T id, int weight) {
		this.id = id;
		this.weight = weight;
	}

	public String toString() {
		return String.valueOf(id) + "::" + String.valueOf(weight);
	}

	public int compareTo(Node<T> node) {
		return new Integer(this.weight).compareTo(node.weight);
	}

	public Boolean equals(Node<T> node) {
		return this.id.equals(node.id);
	}
}
