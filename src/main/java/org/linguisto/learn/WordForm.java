/*
#######################################################################
#
#  Linguisto Portal
#
#  Copyright (c) 2017 Volodymyr Vlad
#
#######################################################################
*/

package org.linguisto.learn;

/**
 * @author Vlad
 * Represents FordForm
 */
public class WordForm {
	long id;
	long fkInf;
	String wf;
	String language;
	//Form ID
	String fid;
	
	public WordForm(String s){
		wf = s;
	}
	
	public WordForm(long id, String s){
		this.id = id;
		wf = s;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String get(){
		return wf;
	}
	
	public String getFid() {
		return fid;
	}

	public void setFid(String fid) {
		this.fid = fid;
	}

	public boolean equals(Object obj) {
		boolean ret = false;
		if (obj instanceof WordForm){
			ret = get().equals(((WordForm)obj).get());
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
	public long getFkInf() {
		return fkInf;
	}

	/**
	 * @param fkInf
	 */
	public void setFkInf(long fkInf) {
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
