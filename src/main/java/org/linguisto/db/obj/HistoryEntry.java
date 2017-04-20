package org.linguisto.db.obj;

import java.util.Date;

public class HistoryEntry extends BaseObj {

	private String langFrom;
	private Integer infId;
	private String inf;
	private String val;
	private String diff;
	private String userName;
	private Date chDate;
	
	public HistoryEntry() {
	}

	public HistoryEntry(String langFrom, Integer infId, String inf,
			String userName, Date chDate) {
		super();
		this.langFrom = langFrom;
		this.infId = infId;
		this.inf = inf;
		this.userName = userName;
		this.chDate = chDate;
	}

	public String getLangFrom() {
		return langFrom;
	}

	public void setLangFrom(String langFrom) {
		this.langFrom = langFrom;
	}

	public Integer getInfId() {
		return infId;
	}

	public void setInfId(Integer infId) {
		this.infId = infId;
	}

	public String getInf() {
		return inf;
	}

	public void setInf(String inf) {
		this.inf = inf;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getChDate() {
		return chDate;
	}

	public void setChDate(Date chDate) {
		this.chDate = chDate;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	public String getDiff() {
		return diff;
	}

	public void setDiff(String diff) {
		this.diff = diff;
	}

}
