package org.linguisto.hibernate.obj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.linguisto.semantic.Parser;
import org.linguisto.semantic.SChunk;


public class Translation extends BaseObj {

	private Short trOrder = 0;
	private String trLang;
	private String translation;
	private String example;
	private Word fromWord;
	
	public Translation() {
		
	}

	public Translation(Word fromWord, String trLang, String translation) {
		setFromWord(fromWord);
		setTrLang(trLang);
		setTranslation(translation);
	}

	public Short getTrOrder() {
		return trOrder;
	}

	public void setTrOrder(Short trOrder) {
		this.trOrder = trOrder;
	}
	public String getTrLang() {
		return trLang;
	}

	public void setTrLang(String trLang) {
		this.trLang = trLang;
	}
	public String getTranslation() {
		return translation;
	}
	public void setTranslation(String translation) {
		if (translation != null) {
			this.translation = translation.trim();
			// replace invisible chars
			String SOFT_HYPHEN_STRING = "\u00AD";
			if (this.translation.contains(SOFT_HYPHEN_STRING)) {
				System.out.println("WARNING: SOFT_HYPHEN will be replaced in translation '"+this.translation+"'");
				this.translation = this.translation.replace(SOFT_HYPHEN_STRING, "");
			}
		} else {
			this.translation = translation;
		}
	}

	public List<SChunk> getTranslationChunks() {
		return Parser.parseTranslation(translation);
	}

	public String getExample() {
		return example;
	}
	public void setExample(String example) {
		if (example != null) {
			this.example = example.trim();
			// replace invisible chars
			String SOFT_HYPHEN_STRING = "\u00AD";
			if (this.example.contains(SOFT_HYPHEN_STRING)) {
				System.out.println("WARNING: SOFT_HYPHEN will be replaced in example '"+this.example+"'");
				this.example = this.example.replace(SOFT_HYPHEN_STRING, "");
			}
		} else {
			this.example = example;
		}
	}

	public List<String> getExamples() {
		if (example != null) {
			String[] exampleList = example.split("\\|");
			return Arrays.asList(exampleList);
		} else {
			return new ArrayList<String>();
		}
	}

	public Word getFromWord() {
		return fromWord;
	}
	public void setFromWord(Word fromWord) {
		this.fromWord = fromWord;
	}
}
