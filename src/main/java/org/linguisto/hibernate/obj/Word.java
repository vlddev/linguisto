package org.linguisto.hibernate.obj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Word extends BaseObj {

	private String word;
	private Short wordType;
	private String lang;
	private transient String langTo;
	private String transcription;
	private Integer rank;
	private List<Translation> translations;

	public Word() {}

	public Word(String word, Short wordType) {
		setWord(word);
		setWordType(wordType);
	}
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word.trim();
	}
	public Short getWordType() {
		return wordType;
	}
	public void setWordType(Short wordType) {
		this.wordType = wordType;
	}
	public String getLang() {
		return lang;
	}
	public String getLangTo() {
		return langTo;
	}
	public String getTranscription() {
		return transcription;
	}

	public void setTranscription(String transcription) {
		this.transcription = transcription;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
	public void setLangTo(String lang) {
		this.langTo = lang;
	}

	public List<Translation> getTranslations() {
		return translations;
	}
	public void setTranslations(List<Translation> translations) {
		this.translations = translations;
	}
	public void addTranslation(Translation translation) {
		if (translations == null) {
			translation.setTrOrder((short) 1);
			List<Translation> set = new ArrayList<Translation>();
			set.add(translation);
			setTranslations(set);
		} else {
			translation.setTrOrder((short)(translations.size()+1));
			this.translations.add(translation);
		}
	}

	public void delTranslation(Translation translation) {
		if (translations != null) {
			this.translations.remove(translation);
		}
		//recount
		for (int i = 0; i < translations.size(); i++) {
			translations.get(i).setTrOrder((short)(i+1));
		}
	}

	public void moveUpTranslation(Translation translation) {
		if (translations != null) {
			int ind = translation.getTrOrder();
			if (ind > 1 && ind < translations.size()+1) { //not first element
				Collections.swap(translations, ind - 2, ind -1);
				translations.get(ind - 2).setTrOrder((short)(ind-1));
				translations.get(ind - 1).setTrOrder((short)ind);
			}
		}
	}

	public void moveDownTranslation(Translation translation) {
		if (translations != null) {
			int ind = translation.getTrOrder();
			if (ind > 0 && ind < translations.size()) { //not last element
				Collections.swap(translations, ind - 1, ind);
				translations.get(ind - 1).setTrOrder((short)ind);
				translations.get(ind).setTrOrder((short)(ind+1));
			}
		}
	}

	public String asString() {
		StringBuffer sb = new StringBuffer();
		sb.append(word).append(" ").append(wordType).append("\n");
		if (translations != null) {
			for (Translation tr : translations) {
				if (tr != null) {
					sb.append(tr.getTrOrder()).append(". ").append(tr.getTranslation()).append("\n");
					sb.append("\t").append(tr.getExample()).append("\n");
				}
			}
		}
		return sb.toString();
	}
}
