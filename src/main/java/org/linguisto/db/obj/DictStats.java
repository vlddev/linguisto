package org.linguisto.db.obj;

import java.util.HashMap;
import java.util.Map;

public class DictStats {

	public static final String WORD_COUNT = "word_count";
	public static final String TRANSLATION_COUNT = "translation_count";
	public static final String EXAMPLE_COUNT = "example_count";
	
	Map<String,String> statsMap;
	
	public DictStats() {
		statsMap = new HashMap<String, String>();
	}
	
	public String getWordCount() {
		return getStat(WORD_COUNT);
	}

	public String getTranslationCount() {
		return getStat(TRANSLATION_COUNT);
	}

	public String getExampleCount() {
		return getStat(EXAMPLE_COUNT);
	}

	public String getStat(String name) {
		String ret = statsMap.get(name);
		if (ret == null) {
			ret = "unknown";
		}
		return ret;
	}
	
	public void setStat(String name, String value) {
		statsMap.put(name, value);
	}
}
