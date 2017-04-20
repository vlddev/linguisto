package org.linguisto.db.obj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.linguisto.semantic.Parser;
import org.linguisto.semantic.SChunk;


public class Translation extends BaseObj {

	private Integer orderNr = 0;
	private Inf infFrom;
	private String trLang;
	private String translation;
	private String example;
	private List<List<SChunk>> parsedExamples = null;

	public Translation() {
	}

	public Translation(Inf infFrom, String translation, String example) {
		setInfFrom(infFrom);
		setTranslation(translation);
		setExample(example);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		boolean ret = false;
		if (obj instanceof Translation) {
			Translation otherObj = (Translation)obj;
	        ret = new EqualsBuilder().
	                // if deriving: appendSuper(super.equals(obj)).
	                append(getInfFrom().getId(), otherObj.getInfFrom().getId()).
	                append(getInfFrom().getInf(), otherObj.getInfFrom().getInf()).
	                append(getTranslation(), otherObj.getTranslation()).
	                append(getExample(), otherObj.getExample()).
	                isEquals();
		}
		return ret;
	}
	
	@Override
	public int hashCode(){
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                append(getInfFrom().getId()).
                append(getInfFrom().getInf()).
                append(getTranslation()).
                append(getExample()).
                toHashCode();
	}

	public Inf getInfFrom() {
		return infFrom;
	}

	public void setInfFrom(Inf infFrom) {
		this.infFrom = infFrom;
	}

	public int getOrderNr() {
		return orderNr;
	}

	public void setOrderNr(Integer orderNr) {
		this.orderNr = orderNr;
	}

	public String getTrLang() {
		return trLang;
	}

	public void setTrLang(String trLang) {
		this.trLang = trLang;
	}

	public List<String> getExamples() {
		if (example != null) {
			String[] exampleList = example.split("\\|");
			return Arrays.asList(exampleList);
		} else {
			return new ArrayList<String>();
		}
	}

	public List<List<SChunk>> getExamplesChunks() {
		if (parsedExamples == null) {
			//parse examples
			parsedExamples = new ArrayList<List<SChunk>>();
			if (example != null) {
				String[] exampleList = example.split("\\|");
				for (String str : exampleList) {
					parsedExamples.add(Parser.parseExample(str));
				}
			}
			return parsedExamples;
		} else {
			//return previously parsed examples
			return parsedExamples;
		}
	}

	public String getTranslation() {
		return translation;
	}

	public List<SChunk> getTranslationChunks() {
		return Parser.parseTranslation(translation);
	}

	public void setTranslation(String translation) {
		this.translation = translation;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String context) {
		this.example = context;
	}

}
