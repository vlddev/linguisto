package org.linguisto.hibernate.obj;


public class History extends BaseObj {

	private Word inf;
	private String langTo;
	private String val;
	private Integer uid;
	
	public History() {
		
	}

	public History(Word inf, String langTo, String val, Integer uid) {
		this.inf = inf;
		this.langTo = langTo;
		this.val = val;
		this.uid = uid;
	}

	public Word getInf() {
		return inf;
	}

	public void setInf(Word inf) {
		this.inf = inf;
	}

	public String getLangTo() {
		return langTo;
	}

	public void setLangTo(String langTo) {
		this.langTo = langTo;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

}
