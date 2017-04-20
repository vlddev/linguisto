package org.linguisto.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.linguisto.db.obj.Inf;

@ManagedBean(name="infEditor")
@ViewScoped
public class InfEditorBean extends AbstractDbBean {
 
	public static final Logger log = Logger.getLogger(InfEditorBean.class.getName());

	private static final long serialVersionUID = -2676900493832158118L;

	private List<Inf> origFoundWords = new ArrayList<Inf>();
	private List<Inf> foundWords = new ArrayList<Inf>();

	private List<String> messages = new ArrayList<String>();

	private int filterTypeId;
	private String filterInf;
	
	private String langFrom = "de";
	
	public InfEditorBean() {
	}

	/**
	 * @param word
	 * @param langCode
	 */
	public void search(Integer type, String inf) {
		foundWords = getDBManager().getInfLikeInf(inf, new Locale(getLangFrom()));
		origFoundWords = getDBManager().getInfLikeInf(inf, new Locale(getLangFrom()));
		messages.clear();
	}

	public String getLangFrom() {
		return langFrom;
	}

	public void setLangFrom(String langFrom) {
		this.langFrom = langFrom;
	}

	public List<Inf> getFoundWords() {
		return foundWords;
	}

	public void setFoundWords(List<Inf> foundWords) {
		this.foundWords = foundWords;
	}

	public int getFilterTypeId() {
		return filterTypeId;
	}

	public void setFilterTypeId(int filterTypeId) {
		this.filterTypeId = filterTypeId;
	}

	public String getFilterInf() {
		return filterInf;
	}

	public void setFilterInf(String filterInf) {
		this.filterInf = filterInf;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void save(List<Inf> infList) {
		messages.clear();
		for (int i = 0; i < infList.size(); i++) {
			Inf inf = infList.get(i);
			Inf origInf = origFoundWords.get(i);
			if (!inf.equals2(origInf)) {
				getDBManager().updateInf(inf);
				messages.add(inf.getCurrentVersion());
			}
		}
		reset();
	}
	
	public void reset() {
		setFilterInf("");
		origFoundWords.clear();
		foundWords.clear();
	}
	
	public static String infToUrl(String inf) {
		return inf.replace(" ", "%20");
	}
}
