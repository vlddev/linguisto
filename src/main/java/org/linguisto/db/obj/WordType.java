package org.linguisto.db.obj;

public class WordType extends BaseObj {

	private String desc;
	private String comment;
	
	public WordType() {}

	public WordType(Integer id) {
		setId(id);
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
	
	public String toString() {
		return getId()+" - "+getDesc()+" ("+getComment()+")";
	}
}
