package org.linguisto.hibernate.obj;

public class WordType {

	private Short id;
	private String lang;
	private String desc;
	private String comment;
	
	public WordType() {}

	public WordType(Short id) {
		setId(id);
	}

	public Short getId() {
		return id;
	}

	public void setId(Short id) {
		this.id = id;
	}
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
}
