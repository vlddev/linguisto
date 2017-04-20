package org.linguisto.beans;

import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.linguisto.beans.user.LoginBean;
import org.linguisto.db.obj.Inf;
import org.linguisto.db.obj.Translation;
import org.linguisto.utils.Archiver;
import org.linguisto.utils.UkrStrComparator;

@ManagedBean(name="sdict")
@ViewScoped
public class DictBean extends AbstractDbBean {
 
	public static final Logger log = Logger.getLogger(DictBean.class.getName());

	private static final long serialVersionUID = -7561381466848304780L;

    @ManagedProperty(value="#{login}")
    private LoginBean loginBean;

	private List<Inf> foundWords = new ArrayList<Inf>();
	private String searchWord;
	private String searchResultMessage = "";
	private String curWord;
	private Integer curWordId;
	private String langFrom = "de";
	private String langTo = "uk";
	private String browserPos = "aa";
	
	public DictBean() {
	}
	
	public String getDictName() {
		return getDictName("uk");
	}

	public String getDictName(String lang) {
		String ret = "";
		if ("en".equalsIgnoreCase(langFrom) && "uk".equalsIgnoreCase(langTo)) {
			ret = "Англійсько-український";
			if ("uk".equalsIgnoreCase(lang)) {
				ret = "Англійсько-український";
			} else if ("en".equalsIgnoreCase(lang)) {
				ret = "English-Ukrainian";
			}
		} else if ("de".equalsIgnoreCase(langFrom) && "uk".equalsIgnoreCase(langTo)) {
			ret = "Німецько-український";
			if ("uk".equalsIgnoreCase(lang)) {
				ret = "Німецько-український";
			} else if ("en".equalsIgnoreCase(lang)) {
				ret = "German-Ukrainian";
			}
		} else if ("fr".equalsIgnoreCase(langFrom) && "uk".equalsIgnoreCase(langTo)) {
			ret = "Французько-український";
			if ("uk".equalsIgnoreCase(lang)) {
				ret = "Французько-український";
			} else if ("en".equalsIgnoreCase(lang)) {
				ret = "French-Ukrainian";
			}
		}
		return ret;
	}
	
	public String getDictNameGen() {
		String ret = getDictName();
		if (ret != null && ret.length() > 0 ) {
			ret = ret.toLowerCase().replace("український", "українського");
		}
		return ret;
	}

//    @PostConstruct
//    public void init() {
//    	super.init();
//		FacesContext fc = FacesContext.getCurrentInstance();
//		Map<String,String> params = fc.getExternalContext().getRequestParameterMap();
//		if (params.get("word") != null) {
//			setWordHex(params.get("word"));
//		}
//		if (params.get("id") != null) {
//			setWordId(Integer.valueOf(params.get("id")));
//		}
//		if (params.get("lang") != null) {
//			setLangFrom(params.get("lang"));
//		}
//		if (curWord != null || curWordId != null) {
//			searchParamTranslation(curWord, curWordId, getLangFrom().getLanguage());
//		}
//    }
	
	public String getSearchWord() {
		return searchWord;
	}

	public void setSearchWord(String searchWord) {
		this.searchWord = searchWord;
	}

	public String getCurWord() {
		return curWord;
	}

	public void setCurWord(String word) {
		this.curWord = word;
	}

	public String getBrowserPos() {
		return browserPos;
	}

	public void setBrowserPos(String browserPos) {
		if (!isValidBrowserPrefix(browserPos)) {
			log.warning("BrowsePos '"+browserPos+"' contains unknown chars. Set browsePos to 'aa'.");
			this.browserPos = "aa";
		}
		if (browserPos.length() == 1) {
			this.browserPos = browserPos+"a";
		} else {
			this.browserPos = browserPos;
		}
	}

	public void searchTranslation(String word) {
		searchTranslation(word, langFrom);
	}

	/**
	 * @param word
	 * @param langCode
	 */
	public void searchTranslation(String word, String langCode) {
		if (word != null) {
			//clear curWordId
			setCurWord(null);
			setCurWordId(null);
	
			String searchWord = word.trim();
			searchResultMessage = "";
			foundWords.clear();
			if (searchWord.length() < 1) {
				searchResultMessage = "Вкажіть слово для пошуку";
				return;
			}
			if ("uk".equals(langTo) && UkrStrComparator.isUkrainianWord(searchWord)) {
				foundWords = getDBManager().findInfsWithTranslationByTr(searchWord, new Locale(langCode), new Locale(getLangTo()));
				if (foundWords.size() < 1) {
					searchResultMessage = "Нічого не знайдено";
					loginBean.logUserAction("emptyrevsearch", searchWord, langCode);
				} else {
					loginBean.logUserAction("revsearch", searchWord, langCode);
				}
			} else {
				Inf filter = new Inf(searchWord.replace(" ", "%"), null);
				foundWords = getDBManager().findInfsWithTranslation(filter, new Locale(langCode), new Locale(getLangTo()));
				if (foundWords.size() < 1) {
					// Search in Wordforms
					foundWords = getDBManager().findInfsWithTranslationByWf(searchWord, new Locale(langCode), new Locale(getLangTo()));
					if (foundWords.size() < 1) {
						//TODO 2. Make suggestions for misspelled word
						// something like this 
						//   http://codereview.stackexchange.com/questions/48908/java-implementation-of-spell-checking-algorithm
						//   http://www.softcorporation.com/products/suggester/   
						//   https://www.languagetool.org/development/
						searchResultMessage = "Нічого не знайдено";
						loginBean.logUserAction("emptysearch", searchWord, langCode);
					} else {
						searchResultMessage = "Пошук по формі слова";
						loginBean.logUserAction("searchwf", searchWord, langCode);
					}
				} else {
					loginBean.logUserAction("search", searchWord, langCode);
				}
			}
		}
	}

	/**
	 * @param word
	 * @param langCode
	 */
	public void searchParamTranslation(String word, Integer id, String langCode) {
		if (id != null) {
			searchResultMessage = "";
			foundWords.clear();
			Inf foundInf = getDBManager().getInfWithTranslationById(id, new Locale(langCode), new Locale(getLangTo()));
			if (foundInf == null) {
				searchResultMessage = "Нічого не знайдено для id = "+id;
			} else {
				foundWords.add(foundInf);
			}
		} else {
			if (word != null && word.length() > 0) {
				searchResultMessage = "";
				foundWords.clear();
				Inf filter = new Inf(word, null);
				foundWords = getDBManager().findInfsWithTranslation(filter, new Locale(langCode), new Locale(getLangTo()));
				if (foundWords.size() < 1) {
					searchResultMessage = "Нічого не знайдено для "+word;
				}
			}
		}
		setCurWord(null);
		setCurWordId(null);
	}

	public List<Inf> getTranslation() {
		return foundWords;
	}
	
	public void exportDict(String langCode) {
	    try {
	    	int resultCount = 10000;
	    	Locale localeFrom = new Locale(langCode);
	    	Locale localeTo = new Locale("uk");
			List<Inf> words = getDBManager().getInfByFrequency(localeFrom, resultCount);
			if (words != null && words.size() > 0) {
		        String filename = langCode+"_dict.xml.gz";

		        FacesContext fc = FacesContext.getCurrentInstance();
		        HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

		        response.reset();
		        //response.setContentType("text/comma-separated-values; charset=utf8");
		        response.setContentType("application/x-gzip");
		        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		        StringBuffer sbRet = new StringBuffer();
		        
		        sbRet.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		        sbRet.append("<vlad.vdict>\n");
		        sbRet.append("  <lang.from>").append(langCode).append("</lang.from>\n");
		        sbRet.append("  <lang.to>").append(localeTo.getLanguage()).append("</lang.to>\n");

		        for (Inf word : words) {
		        	List<Translation> trList = getDBManager().getTrList(word, localeFrom, localeTo);
		        	//TODO get wordforms
		        	
			        sbRet.append("    <word>\n");
			        sbRet.append("      <inf>").append(word.getInf()).append("</inf>").append("\n");
			        sbRet.append("      <type>").append(word.getType()).append("</type>").append("\n");
			        if (trList != null && trList.size() > 0) {
				        for (Translation tr : trList) {
					        sbRet.append("      <tr>\n");
					        sbRet.append("        <nr>").append(tr.getOrderNr()).append("</nr>").append("\n");
					        sbRet.append("        <word>").append(tr.getTranslation()).append("</word>").append("\n");
					        List<String> exs = tr.getExamples();
					        if (listHasNonEmptyString(exs)) {
						        sbRet.append("        <exs>\n");
					        	for (String s : exs) {
					        		if (s != null && s.length() > 0) {
								        sbRet.append("          <ex>").append(s).append("</ex>").append("\n");
					        		}
					        	}
						        sbRet.append("        </exs>\n");
					        }
					        sbRet.append("      </tr>\n");
				        }
			        }
			        sbRet.append("    </word>\n");
		        }

		        sbRet.append("</vlad.vdict>");

		        byte[] outBytes = Archiver.compress(sbRet.toString());

		        response.setContentLength(outBytes.length);
		        OutputStream output = response.getOutputStream();
	            output.write(outBytes);

		        output.flush();
		        output.close();

		        fc.responseComplete();
			}

	    } catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}		
	}
	
	private boolean listHasNonEmptyString(List<String> list) {
		boolean ret = false;
		if (list != null && list.size() > 0) {
			for (String s : list) {
				if(s != null && s.length() > 0) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}
	
	//--------- DictBrowser methods --------------
	public List<List<Map.Entry<String,String>>> getBrowserPrefixes() {
		List<List<Map.Entry<String,String>>> ret = new ArrayList<List<Map.Entry<String,String>>>();
		List<String> alfabet = Arrays.asList("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z");
		if (getLangFrom().equals("de")) {
			alfabet = Arrays.asList("a","ä","b","c","d","e","f","g","h","i","j","k","l","m","n","o","ö","p","q","r","s","t","u","ü","v","w","x","y","z");
		}
		List<Map.Entry<String,String>> level1 = new ArrayList<Map.Entry<String,String>>();
		List<Map.Entry<String,String>> level2 = new ArrayList<Map.Entry<String,String>>();
		for (String str : alfabet) {
			level1.add(new AbstractMap.SimpleEntry<String, String>(str, str));
			String strLevel2 = browserPos.charAt(0)+str;
			level2.add(new AbstractMap.SimpleEntry<String, String>(strLevel2, strLevel2));
		}
		ret.add(level1);
		ret.add(level2);
		return ret;
	}
	
	private boolean isValidBrowserPrefix(String prefix) {
		boolean ret = true;
		if (prefix != null && prefix.length() > 0) {
			List<String> alfabet = Arrays.asList("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z");
			if (getLangFrom().equals("de")) {
				alfabet = Arrays.asList("a","ä","b","c","d","e","f","g","h","i","j","k","l","m","n","o","ö","p","q","r","s","t","u","ü","v","w","x","y","z");
			}
			for (int i = 0; i < prefix.length(); i++) {
				if (!alfabet.contains(String.valueOf(prefix.charAt(i)))) {
					//unknown char -> exit
					ret = false;
					break;
				}
			}
		} else {
			ret = false;
		}
		return ret;
	}
	
	public List<Inf> getBrowserWords() {
		return getDBManager().findInfs(browserPos, new Locale(getLangFrom()));
	}
	//--------- end of DictBrowser methods --------------
	

	public Integer getCurWordId() {
		return curWordId;
	}

	public void setCurWordId(Integer wordId) {
		this.curWordId = wordId;
	}

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

	public String getLangFrom() {
		return langFrom;
	}

	/**
	 * this method is expected to be used in render property of the jsf-tag
	 * something like <h:outputText rendered="#{sdict.setLangFrom('en')}" value=""/>
	 * 
	 * @param langCode - two-letter ISO 639‑1 code
	 * @return false - to hide the calling component
	 */
//	public boolean setLangFrom(String langCode) {
//		this.langFrom = langCode;
//		return false;
//	}

	public void setLangFrom(String langFrom) {
		this.langFrom = langFrom;
	}

	public String getLangTo() {
		return langTo;
	}

//	public void setLangTo(Locale langTo) {
//		this.langTo = langTo;
//	}

	public String getSearchResultMessage() {
		return searchResultMessage;
	}

	public void setSearchResultMessage(String searchResultMessage) {
		this.searchResultMessage = searchResultMessage;
	}

}
