package org.linguisto.beans;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import org.linguisto.beans.user.LoginBean;
import org.linguisto.hibernate.db.HibernateUtil;
import org.linguisto.hibernate.obj.History;
import org.linguisto.hibernate.obj.Translation;
import org.linguisto.hibernate.obj.Word;

@ManagedBean(name="editdict")
@ViewScoped
public class HibernateDictBean implements Serializable { 
 
	private static final long serialVersionUID = -7561381466848304780L;

	private static final Logger log = Logger.getLogger(HibernateDictBean.class.getName());

    @ManagedProperty(value="#{login}")
    // define getter and setter to make it work
    private LoginBean loginBean;

	private SessionFactory sessionFactory;
	private Integer wordId;
	private Word editWord;
	private String origWordState;
	private String langTo = "uk";
	
	public HibernateDictBean() {
    	log.fine("Instance of "+HibernateDictBean.class.getName()+" created.");
		sessionFactory = HibernateUtil.getSessionFactory();
	}
	
    @PostConstruct
    public void init() {
    	onload();
    }
    
	@PreDestroy
	public void close() {
		sessionFactory.close();
    	log.fine("Instance of "+HibernateDictBean.class.getName()+" destroyed.");
	}
	
	public void onload() {
		FacesContext fc = FacesContext.getCurrentInstance();
		Map<String,String> params = fc.getExternalContext().getRequestParameterMap();
		if (params.get("id") != null) {
			Integer idParam = Integer.valueOf(params.get("id"));
			setWordId(idParam, params.get("langf"));
		}
	}

	public String getDictName(String lang) {
		String ret = "";
		String langFrom = getEditWord().getLang();
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
		String ret = getDictName("uk");
		if (ret != null && ret.length() > 0 ) {
			ret = ret.toLowerCase().replace("український", "українського");
		}
		return ret;
	}

	public String newWord() {
		editWord = new Word();
		return "dicteditor";
	}

	public String startEditWord(Word word) {
		editWord = word;
		return "dicteditor";
	}
	
	public void save() {
		Session session = null;
		Transaction tx = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			//tx.begin();
			session.saveOrUpdate(getEditWord());
			if (getWordId() == null) {
				wordId = getEditWord().getId();
			}
			//is there history for this word?
			Query histCount =  session.createQuery("select count(*) from History where inf.id = ?");
			histCount.setInteger(0, wordId);
			if (((Long)histCount.iterate().next()).longValue() == 0L) {
				//history is empty
				//store initial state
				if (this.origWordState != null) {
					History histEntry = new History(getEditWord(), langTo, this.origWordState, loginBean.getUser().getId());
					session.saveOrUpdate(histEntry);
				}
			}
			//store history
			History histEntry = new History(getEditWord(), langTo, getEditWord().asString(), loginBean.getUser().getId());
			session.saveOrUpdate(histEntry);
			tx.commit();
			FacesContext context = FacesContext.getCurrentInstance();
	        context.addMessage(null, new FacesMessage("Зміни збережено.") );
		} catch (ConstraintViolationException e) {
			tx.rollback();
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage("Таке слово вже є в словнику. Зміни не збережено.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
	        context.addMessage(null, msg);
	        log.log(Level.WARNING, e.getMessage(), e);
		} finally {
			if (!tx.wasCommitted() && !tx.wasRolledBack()) {
				tx.rollback();
			}
			if (session != null) {
				session.flush();
				session.close();
			}
		}
	}
	
	public Word getEditWord() {
		return editWord;
	}

	public Integer getWordId() {
		return wordId;
	}

	public void setWordId(Integer wordId, String lang) {
		this.wordId = wordId;
		this.origWordState = null;
		if (wordId != null) {
			if (wordId == -7) {
				editWord = new Word();
				editWord.setLang(lang);
				this.wordId = null;
			} else {
				Session session = sessionFactory.openSession();
				session.enableFilter("langToFilter").setParameter("langToParam", langTo);
				editWord = (Word)session.get(Word.class, wordId);
				if (editWord != null) {
					this.origWordState = editWord.asString();
				}
				session.close();
			}
		}
	}

    public void addTranslation() {
    	if (getEditWord() != null) {
            Translation tr1 = new Translation(getEditWord(), langTo, "");
            getEditWord().addTranslation(tr1);
    	}
    }
    
    public void delTranslation(Translation translation) {
    	if (getEditWord() != null) {
            getEditWord().delTranslation(translation);
    	}
    }

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

}
