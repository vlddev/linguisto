package org.linguisto.beans;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.linguisto.db.obj.ActionEntry;
import org.linguisto.db.obj.HistoryEntry;

@ManagedBean(name="history")
@ViewScoped
public class HistoryBean extends AbstractDbBean {
 
	private static final long serialVersionUID = -2078326075492571041L;
	public static final Logger log = Logger.getLogger(HistoryBean.class.getName());

	/**
	 * wordLangId must be formatted as "lln.+" where ll - language code, n - number
	 */
	private String wordLangId;

	public HistoryBean() {
	}
	
	public List<HistoryEntry> getDictHistory() {
		List<HistoryEntry> ret = getDBManager().getDictHistory(100);
		return ret;
	}

	public List<HistoryEntry> getDictHistory(long len) {
		List<HistoryEntry> ret = getDBManager().getDictHistory(len);
		return ret;
	}
	
	public List<ActionEntry> getActionHistory(long len) {
		List<ActionEntry> ret = getDBManager().getActionHistory(len);
		return ret;
	}

	public List<HistoryEntry> getWordHistory() {
		FacesContext fc = FacesContext.getCurrentInstance();
		Map<String,String> params = fc.getExternalContext().getRequestParameterMap();
		setWordLangId(params.get("lid"));
		return getWordHistory(getWordLang(), getWordId());
	}

	public List<HistoryEntry> getWordHistory(String lang, Integer id) {
		List<HistoryEntry> ret = getDBManager().getWordHistory(lang, id, 100);
		return ret;
	}

	public String getWordLangId() {
		return wordLangId;
	}

	public void setWordLangId(String wordLang) {
		this.wordLangId = wordLang;
	}

	public String getWordLang() {
		String ret = null;
		if (wordLangId != null) {
			if (wordLangId.length() > 2) {
				ret = wordLangId.substring(0, 2);
			}
		}
		return ret;
	}

	public Integer getWordId() {
		Integer ret = null;
		if (wordLangId != null) {
			if (wordLangId.length() > 2) {
				ret = Integer.valueOf(wordLangId.substring(2));
			}
		}
		return ret;
	}
}
