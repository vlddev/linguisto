package org.linguisto.db.obj;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class Inf extends BaseObj {

	private String inf;
	private Integer type;
	private String transcription;
	private String language;
	private Integer rank;
	private List<Translation> trList = null;
	private List<WordForm> wfList = null;

	public Inf() {
	}

	public Inf(String inf, Integer type) {
		this();
		setInf(inf);
		setType(type);
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
		if (wfList != null) {
			for(WordForm wf : wfList) {
				wf.setFkInf(id);
			}
		}
	}

	public String getInf() {
		return inf;
	}
	
	public String getUrlInf() {
		return inf.replace(" ", "%20");
	}
	
	public void setInf(String inf) {
		checkChange(this.inf, inf);
		this.inf = inf;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		checkChange(this.type, type);
		this.type = type;
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

	public List<WordForm> getWfList() {
		return wfList;
	}
	
	public String getWf(String fid) {
		String ret = "-";
		if (getWfList() != null) {
			for (WordForm wf : getWfList()) {
				if (fid.equals(wf.getFid())) {
					ret = wf.getWf();
					break;
				}
			}
		}
		return ret;
	}
	
	public void setWfList(List<WordForm> wfList) {
		this.wfList = wfList;
		if (wfList != null ) {
			for(WordForm wf : wfList) {
				wf.setFkInf(id);
				wf.setLanguage(language);
			}
		}
	}

	public List<Translation> getTrList() {
		return trList;
	}

	public void setTrList(List<Translation> trList) {
		this.trList = trList;
	}

	/**
	 * @return
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language
	 */
	public void setLanguage(String language) {
		this.language = language;
		if (wfList != null) {
			for(WordForm wf : wfList) {
				wf.setLanguage(language);
			}
		}
	}

	public String getCurrentVersion() {
		StringBuilder sb = new StringBuilder();
		sb.append(getInf()).append(" ").append(getType());
		return sb.toString();
	}

	public int getArticleLineCount() {
		int ret = 2;
		if (getTrList() != null) {
			for (Translation tr : getTrList()) {
				if (tr != null) {
					ret++;
					List<String> lst = tr.getExamples();
					if (lst != null) {
						ret += lst.size();
					}
				}
			}
		}
		return ret;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		boolean ret = false;
		if (obj instanceof Inf) {
			Inf otherInf = (Inf)obj;
	        ret = new EqualsBuilder().
	                // if deriving: appendSuper(super.equals(obj)).
	                append(getId(), otherInf.getId()).
	                append(getLanguage(), otherInf.getLanguage()).
	                isEquals();
		}
		return ret;
	}
	
	@Override
	public int hashCode(){
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                append(getId()).
                append(getLanguage()).
                toHashCode();
	}

	public boolean equals2(Object obj) {
		if (this == obj) return true;
		boolean ret = false;
		if (obj instanceof Inf) {
			Inf otherInf = (Inf)obj;
	        ret = new EqualsBuilder().
	                // if deriving: appendSuper(super.equals(obj)).
	                append(getId(), otherInf.getId()).
	                append(getLanguage(), otherInf.getLanguage()).
	                append(getType(), otherInf.getType()).
	                append(getInf(), otherInf.getInf()).
	                append(getTranscription(), otherInf.getTranscription()).
	                isEquals();
		}
		return ret;
	}
}
