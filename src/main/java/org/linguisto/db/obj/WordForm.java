package org.linguisto.db.obj;


public class WordForm extends BaseObj {

	Integer fkInf;
	String wf;
	String language;
	String fid;
	
	public WordForm(String s){
		wf = s;
	}
	
	public WordForm(Integer id, String s){
		this.id = id;
		wf = s;
	}
	
	public String getFid() {
		return fid;
	}

	public void setFid(String fid) {
		this.fid = fid;
	}

	public boolean equals(Object obj) {
		boolean ret = false;
		if (obj instanceof WordForm) {
			if (getId() != null) {
				ret = getId().equals(((WordForm)obj).getId());
			}
		}
		return ret;
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
	}

	/**
	 * @return
	 */
	public Integer getFkInf() {
		return fkInf;
	}

	/**
	 * @param fkInf
	 */
	public void setFkInf(Integer fkInf) {
		this.fkInf = fkInf;
	}

	/**
	 * @return
	 */
	public String getWf() {
		return wf;
	}

	/**
	 * @param wf
	 */
	public void setWf(String wf) {
		this.wf = wf;
	}
}
