package org.linguisto.beans;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.linguisto.beans.user.LoginBean;
import org.linguisto.learn.Sentence;
import org.linguisto.learn.Text;
import org.linguisto.learn.Word;

@ManagedBean(name="learn")
@ViewScoped
public class LearnBean extends AbstractDbBean {
 
	private static final long serialVersionUID = 776678561027255485L;

	private static final Logger log = Logger.getLogger(LearnBean.class.getName());

	@ManagedProperty(value="#{login}")
	private LoginBean login;

	private String text;
	private String wordsToFind;
	private String langFrom = "en";
	private String langTo = "uk";
	private String dictName = "en_uk";
	private Future<Text> futureText;
	private Text resultText;
	private List<Word> words = null;

	
	public LearnBean() {
	}

	public LoginBean getLogin() {
		return login;
	}

	public void setLogin(LoginBean login) {
		this.login = login;
	}
	
	public boolean isShowResultText() {
		boolean ret = false;
		ret = (getResultText() != null && text.length() < 30000);
		return ret;
	}

	public void prepareTextAsync() {
		futureText = null;
		try {
			if (text == null || text.length() < 1) {
				return;
				//throw new Exception("Text-from is not set");
			}
			mapDictNameWithLangs();
			Callable<Text> makerTask = new Callable<Text>() {
				@Override
			    public Text call() throws Exception {
					return getDBManager().makeText(text, new Locale(langFrom), new Locale(langTo), login.getUser());
				}
			};
			ExecutorService executor = Executors.newSingleThreadExecutor();
			futureText = executor.submit(makerTask);
			executor.shutdown();
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public void prepareText() {
		resultText = null;
		try {
			if (text == null || text.length() < 1) {
				return;
				//throw new Exception("Text-from is not set");
			}
			mapDictNameWithLangs();

			resultText =  getDBManager().makeText(text, new Locale(langFrom), new Locale(langTo), login.getUser());
			String sDebug = "";
			for (Sentence sent : resultText.getSentences()) {
				sDebug += (sent.isNewParagraph()?"<p> ":"")+sent.getContent();
			}
			log.fine(sDebug);
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	private void mapDictNameWithLangs() {
		if ("en_uk".equals(dictName)) {
			langFrom = "en";
			langTo = "uk";
		} else if ("de_uk".equals(dictName)) {
			langFrom = "de";
			langTo = "uk";
		} else {
			throw new IllegalArgumentException("Unknown dictionary "+dictName);
		}
	}

	public void findUserWords() {
		List<Word> ret = new ArrayList<Word>();
		try {
			if (wordsToFind != null) {
				for (String word : wordsToFind.split("[,;\\s]+")) {
					if ( word != null && word.length() > 0 ) {
						for (Word w : getDBManager().getBaseForm(word, new Locale(langFrom), false)) {
							if (!ret.contains(w)) {
								ret.add(w);
							}
						}
					}
				}
				//setUser();
				if (ret.size() > 0) {
					words = getDBManager().checkUserKnow(ret, login.getUser(), langFrom);
					//setDbState();
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	/**
	 * Find next 20 words unknown for current user
	 * based on frequency
	 */
	public void findProposedWords() {
		try {
			words = getDBManager().getUserProposedWords(new Locale(langFrom), login.getUser(), 20);
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public void saveUserWords() {
		if (words != null && words.size() > 0) {
			try {
				getDBManager().setUserKnows(login.getUser(), langFrom, words);
			} catch (Exception e) {
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	public void saveResultTextAsFb2() {
	    try {
			if (getResultText() != null) {
				String filename = "result.fb2";

		        FacesContext fc = FacesContext.getCurrentInstance();
		        HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

		        response.reset();
		        response.setContentType("text/xml; charset=utf8");
		        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		        StringBuffer sbRet = new StringBuffer();

		        sbRet.append(getResultText().getFb2(Text.FIND_WORD_GERMAN));
		        byte[] outBytes = sbRet.toString().getBytes("utf-8");
		        response.setContentLength(outBytes.length);
		        OutputStream output = response.getOutputStream();
	            output.write(outBytes);

		        output.flush();
		        output.close();

		        fc.responseComplete();
			}
	    } catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
	    }		
	}

	public void saveResultTextAsHtml() {
	    try {
			if (getResultText() != null) {
				String filename = "result.html";

		        FacesContext fc = FacesContext.getCurrentInstance();
		        HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

		        response.reset();
		        response.setContentType("text/html; charset=utf8");
		        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		        StringBuffer sbRet = new StringBuffer();

		        String linkBase = "dict/"+getLangFrom()+"/";
		        sbRet.append(getResultText().getLinguistoHtml(linkBase));
		        byte[] outBytes = sbRet.toString().getBytes("utf-8");
		        response.setContentLength(outBytes.length);
		        OutputStream output = response.getOutputStream();
	            output.write(outBytes);

		        output.flush();
		        output.close();

		        fc.responseComplete();
			}
	    } catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
	    }		
	}

	public void saveResultTextUnknownWords() {
	    try {
			if (getResultText() != null) {
		        //store unknown words
				String filename = "result_unknown_words.txt";
				FacesContext fc = FacesContext.getCurrentInstance();
				HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

		        response.reset();
		        response.setContentType("text/comma-separated-values; charset=utf8");
		        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		        StringBuffer sbRet = new StringBuffer();

		        sbRet.append("--- unknown words ---\n");
		        sbRet.append(getResultText().getUnknownWords());
		        sbRet.append("--- untransladed words ---\n");
		        sbRet.append(getResultText().getUntranslatedWords());
		        sbRet.append("--- unrecognized words ---\n");
		        sbRet.append(getResultText().getUnrecognizedWordUsageStats());
		        byte[] outBytes = sbRet.toString().getBytes("utf-8");
		        response.setContentLength(outBytes.length);
		        OutputStream output = response.getOutputStream();
	            output.write(outBytes);

		        output.flush();
		        output.close();

		        fc.responseComplete();
			}
	    } catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
	    }		
	}

	public void exportUserWords() {
	    try {
			List<String> words = getDBManager().getUserWords(login.getUser(), langFrom);
			if (words != null && words.size() > 0) {
		        String filename = "userWords.csv";

		        FacesContext fc = FacesContext.getCurrentInstance();
		        HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

		        response.reset();
		        response.setContentType("text/comma-separated-values; charset=utf8");
		        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		        StringBuffer sbRet = new StringBuffer();

		        for (String s : words) {
		        	sbRet.append(s).append("\n");
		        }
		        byte[] outBytes = sbRet.toString().getBytes("utf-8");
		        response.setContentLength(outBytes.length);
		        OutputStream output = response.getOutputStream();
	            output.write(outBytes);

		        output.flush();
		        output.close();

		        fc.responseComplete();
			}

		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
	    } catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
	    }		
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLangFrom() {
		return langFrom;
	}


	public void setLangFrom(String langFrom) {
		this.langFrom = langFrom;
	}


	public String getLangTo() {
		return langTo;
	}


	public void setLangTo(String langTo) {
		this.langTo = langTo;
	}

	public void clearResult() {
		words = null;
		if (futureText != null) {
			futureText.cancel(true);
			futureText = null;
		}
	}

	public String getWordsToFind() {
		return wordsToFind;
	}

	public void setWordsToFind(String wordsToFind) {
		this.wordsToFind = wordsToFind;
	}

	public List<Word> getWords() {
		return words;
	}

	public void setWords(List<Word> words) {
		this.words = words;
	}

	public Future<Text> getFutureText() {
		return futureText;
	}

	public Text getResultText() {
		return resultText;
	}

	public String getDictName() {
		return dictName;
	}

	public void setDictName(String dictName) {
		this.dictName = dictName;
	}

}
