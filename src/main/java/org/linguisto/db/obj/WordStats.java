package org.linguisto.db.obj;

import java.util.HashMap;
import java.util.Map;


public class WordStats extends BaseObj {

	private String word;
	private Map<String, Double> rankMap = new HashMap<String, Double>(); 
	
	public WordStats() {
	}

	public WordStats(String word) {
		this();
		setWord(word);
	}

	public String getWord() {
		return word;
	}
	
	public void setWord(String word) {
		this.word = word;
	}

	public Double getRank(String key) {
		return rankMap.get(key);
	}

	public void setRank(String key, Double rank) {
		rankMap.put(key, rank);
	}

	public Map<String, Double> getRankMap() {
		return rankMap;
	}
}
