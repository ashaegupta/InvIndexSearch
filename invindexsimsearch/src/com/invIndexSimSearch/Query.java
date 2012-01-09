package com.invIndexSimSearch;

import java.util.ArrayList;
import java.util.Iterator;

public class Query implements Comparable<Query> {
	public ArrayList<Node<Long>> terms;
	public Iterator<Node<Long>> itr;

	public Query() {
		terms = new ArrayList<Node<Long>>();
	}

	public void addTerm(long featureId, int weight) {
		terms.add(new Node<Long>(featureId, weight));
	}

	public void resetItr() {
		itr = terms.iterator();
	}

	public Node<Long> getNextTerm() {
		if (itr.hasNext())
			return (Node<Long>) itr.next();
		else
			return null;
	}

	public Boolean hasNext() {
		return itr.hasNext();
	}

	public String toString() {
		String result = "Query: " + "\n";
		Iterator<Node<Long>> strItr;
		strItr = terms.iterator();
		while (strItr.hasNext()) {
			Node<Long> n = strItr.next();
			result += n.id + " " + n.weight + "\n";
		}
		return result;
	}

	public int compareTo(Query query) {
		Integer thisLength = this.terms.size();
		Integer compLength = query.terms.size();
		return thisLength.compareTo(compLength);
	}

}
