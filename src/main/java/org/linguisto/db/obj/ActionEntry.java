package org.linguisto.db.obj;

import java.util.Date;

public class ActionEntry extends BaseObj {

	private String ip;
	private String agent;
	private String defLang;
	private String action;
	private Date aTime;
	
	public ActionEntry() {
	}

	public ActionEntry(String ip, String agent, String defLang,
			String action, Date aTime) {
		super();
		this.ip = ip;
		this.agent = agent;
		this.defLang = defLang;
		this.action = action;
		this.aTime = aTime;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getDefLang() {
		return defLang;
	}

	public void setDefLang(String defLang) {
		this.defLang = defLang;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Date getaTime() {
		return aTime;
	}

	public void setaTime(Date aTime) {
		this.aTime = aTime;
	}
	
	public String getAsString() {
		//'search','emptysearch','emptyrevsearch','revsearch','freqsearch','emptyfreqsearch','voctest'
		StringBuffer sb = new StringBuffer();
		if ("search".equals(getAction())) {
			sb.append("Пошук слова <b>").append(getAgent()).append("</b>");
		} else if ("revsearch".equals(getAction())) {
			sb.append("Пошук слова <b>").append(getAgent()).append("</b> в перекладах");
		} else if ("emptysearch".equals(getAction())) {
			sb.append("Слова <b>").append(getAgent()).append("</b> не знайдено");
		} else if ("freqsearch".equals(getAction())) {
			sb.append("Пошук слова <b>").append(getAgent()).append("</b> в частотному словнику");
		} else if ("emptyfreqsearch".equals(getAction())) {
			sb.append("Слова <b>").append(getAgent()).append("</b> немає в частотному словнику");
		} else if ("voctest".equals(getAction())) {
			sb.append("Тест словникового запасу. Результат: <b>").append(getAgent().replace("result=", "")).append("</b> слів.");
		} else {
			sb.append(getAction());
		}
		return sb.toString();
	}

}
