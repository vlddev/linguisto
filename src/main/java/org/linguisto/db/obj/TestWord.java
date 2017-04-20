package org.linguisto.db.obj;



public class TestWord implements Comparable<TestWord> {

	private String word;
	private Integer rank;
	private boolean known;
	
	public TestWord(String word, Integer rank) {
		this.word = word;
		this.rank = rank;
		this.known = false;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public boolean isKnown() {
		return known;
	}

	public void setKnown(boolean known) {
		this.known = known;
	}
	
	public int compareTo(TestWord o) {
	     return(getRank() - o.getRank());
	}
}
