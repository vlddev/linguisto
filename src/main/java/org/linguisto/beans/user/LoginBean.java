package org.linguisto.beans.user;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.linguisto.beans.AbstractDbBean;
import org.linguisto.db.obj.Role;
import org.linguisto.db.obj.User;
import org.linguisto.utils.UserAgentInfo;


@ManagedBean(name="login")
@SessionScoped
public class LoginBean extends AbstractDbBean {

	private static final long serialVersionUID = 5617610507264584189L;

	private static final Logger log = Logger.getLogger(LoginBean.class.getName());

	private User user;
	private String userName;
	private String userPwd;
	private String loginError;
	private Boolean bMobile = null;
	
	private static Map<String, Locale> languages;
	private String locale = "uk";
	
//    @ManagedProperty(value="#{messages}")
//	private MessagesBean messagesBean;
	
	static{
		languages = new LinkedHashMap<String, Locale>();
		languages.put("English", Locale.ENGLISH);
		languages.put("German", Locale.GERMAN);
		languages.put("Ukrainian", new Locale("uk"));
	}

    public LoginBean() {
    	//set locale to browser default locale
//    	log.fine("Instance of "+LoginBean.class.getName()+" created.");
//    	Locale browserLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
//		log.fine("Browser locale " + browserLocale);
//		if (languages.containsValue(browserLocale)) {
//			setLocale(browserLocale.getLanguage());
//		}
    	//set locale to ukrainian
    	Locale ukLoc = languages.get("Ukrainian");
		setLocale(ukLoc.getLanguage());
		FacesContext.getCurrentInstance().getViewRoot().setLocale(ukLoc);
		//log.info("Locale changed to " + locale);
    }

//	public void setMessagesBean(MessagesBean messagesBean) {
//		this.messagesBean = messagesBean;
//	}

    public Map<String, Locale> getLanguages() {
        return languages;
    }

    public String getLocale() {
        return locale;
     }

    public void setLocale(String locale) {
    	log.fine("Change application locale from " + this.locale + " to " + locale);
     	this.locale = locale;
    }

    //value change event listener
    public void localeChanged(ValueChangeEvent e){
        String newLocaleValue = e.getNewValue().toString();
        for (Map.Entry<String, Locale> entry : languages.entrySet()) {
           if(entry.getValue().toString().equals(newLocaleValue)){
              FacesContext.getCurrentInstance()
              	.getViewRoot().setLocale(entry.getValue());         
           }
        }
    }

	public void ajaxLocaleChanged(AjaxBehaviorEvent e) {
		for (Map.Entry<String, Locale> entry : languages.entrySet()) {
			if (entry.getValue().toString().equals(this.locale)) {
				FacesContext.getCurrentInstance().getViewRoot().setLocale(entry.getValue());
				log.info("Locale changed to " + locale);
			}
		}
	}	

	public void loginAction(ActionEvent actionEvent) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "login action",  null);
        FacesContext.getCurrentInstance().addMessage("growl", message);
        login();
    }
	
    public void login() {
    	loginError = null;
    	if (user != null) return;
    	if (userName != null) {
         	User tmpUser = getDBManager().getUser(userName, userPwd);
        	if (tmpUser != null) {
        		if (!tmpUser.isConfirmed()) {
            		loginError = "Помилка. Реєстрацію користувача не підтверджено." +
            				" Для підтвердження реєстрації знайдіть лист надісланий Вам після реєстрації і виконайте дії вказані в листі.";
            		logUserAction("login", "Login error. Not confirmed user '"+userName+"'", null);
        		} else { //Successful login
            		user = tmpUser;
            		if (tmpUser.getId() == 1) {
            			user.addRole(Role.ADMIN);
            		}
            		loginError = null;
            		log.info("Login: "+getUserInfo());
            		logUserAction("login", userName, null);
        		}
        	} else {
        		//loginError = "Login error. Try again.";
        		loginError = "Помилка. Невірні кристувач або пароль.";
        		logUserAction("login", "Login error. Username '"+userName+"'", null);
        	}
    	} else {
    		loginError = "Не вказано користувача.";
    		logUserAction("login", "Login error. Empty Username '"+userName+"'", null);
    	}
    	userPwd = null;
    	userName = null;
    	if (loginError != null) {
    		log.warning("Login error ["+getUserInfo()+"]: "+loginError);
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, loginError,
            		"Якщо ви забули пароль, скористайтеся можливістю <a href='requestResetpwd.xhtml'>скинути пароль</a>.");
            FacesContext.getCurrentInstance().addMessage("growl", message);
    	}
    }
    
    public void logUserAction(String action, String info, String langCode) {
    	FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        if (session != null) {
        	Locale locale = null;
        	if (langCode != null) {
        		locale = new Locale(langCode);
        	}
        	getDBManager().insertSessionAction(session.getId(), getUserAddress(), info, locale, action);
        } else {
        	log.warning("Session was NULL");
        }
    }

    public void logout() {
		logUserAction("logout", user.getName(), null);
    	user = null;
    	FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
    	ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
    	//HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

    	//String ret = "/index.xhtml?faces-redirect=true";
    	String ret = "/index.xhtml";
    	try {
			externalContext.redirect(ret);
		} catch (IOException e) {
			log.severe(e.getMessage());
		}
    }
 
    public String getUserAddress() {
    	String ret = null;
    	Object rObj = FacesContext.getCurrentInstance().getExternalContext().getRequest();
    	if (rObj != null && rObj instanceof HttpServletRequest) {
    		HttpServletRequest request = (HttpServletRequest)rObj;
			//is client behind something?
			String ipAddress = request.getHeader("X-FORWARDED-FOR");  
			if (ipAddress == null) {  
				ipAddress = request.getRemoteAddr();  
			}
			ret = ipAddress;
    	}
    	return ret;
    }
    
    public boolean isMobile() {
    	if (bMobile == null) {
        	Object rObj = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        	if (rObj != null && rObj instanceof HttpServletRequest) {
        		HttpServletRequest request = (HttpServletRequest)rObj;
    			//is client behind something?
    			String userAgent = request.getHeader("User-Agent");
    			String accept = request.getHeader("Accept");
    			if (userAgent != null && accept != null) {
    				UserAgentInfo agent = new UserAgentInfo(userAgent, accept);
    				if (agent.isMobileDevice()) {
		        	   bMobile = true;
	                	log.info("Mobile User-Agent '"+agent+"'");
    				} else {
    					bMobile = false;
                    	log.info("Desktop User-Agent '"+agent+"'");
    				}
    			} else {
                	log.warning("User-Agent or Accept is NULL.");
    			}
        	} else {
            	log.warning("HttpRequest is NULL or not an instance of HttpServletRequest");
        	}
    	}
    	if (bMobile == null) {
    		return false;
    	} else {
    		return bMobile;
    	}
    }
    
    public String getUserInfo() {
    	String ret = "";
		if (isLoggedIn()) {
			ret = getUser().getName();
		} else {
			ret = "<anonimous>";
		}
		ret += " ("+getUserAddress()+")";
		return ret;
    }
 
    /**
     * hidden member for onLoad/Init event. 
     *@return always return the string pageLoaded
     */
    public String getOnLoad() {
    	//verify();
        return "pageLoaded";
    }
    
    public void setOnLoad(String val) {
    	//dummy
    }
    
    public Integer getMaxLearnTextLength() {
    	Integer ret = 1000;
    	if (isLoggedIn()) {
    		ret = 50000; 
    	}
    	return ret;
    }
 
    /**
     * Getter and Setter Method
     */
    public User getUser() {
        return user;
    }

    public boolean isLoggedIn() {
    	boolean ret = (getUser()!=null);
    	return ret;
    }

	public String getLoginError() {
		return loginError;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String uName) {
		this.userName = uName;
	}

	public String getUserPwd() {
		return userPwd;
	}

	public void setUserPwd(String uPwd) {
		this.userPwd = uPwd;
	}
}
