package org.linguisto.beans.user;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

import org.linguisto.beans.AbstractDbBean;
import org.linguisto.db.obj.User;
import org.linguisto.utils.MailUtil;

@ManagedBean(name="resetpwdBean")
@ViewScoped
public class ResetPwdBean extends AbstractDbBean {

	private static final long serialVersionUID = 852207647590338472L;

	private static final Logger log = Logger.getLogger(ResetPwdBean.class.getName());

    @Resource(mappedName = "java:jboss/mail/Default")
    private Session mailSession;
    
	private String userName;
	private String userEmail;
	private String pwd;
	private String pwd2;
	private String newpwd;
	private String newpwd2;
	private String error;

	private String resetPwdId;
	private User resetPwdUser;

    public ResetPwdBean() {
    }
 
    @PostConstruct
    public void init() {
    	super.init();
		FacesContext fc = FacesContext.getCurrentInstance();
		Map<String,String> params = fc.getExternalContext().getRequestParameterMap();
		if (params.get("rid") != null) {
			resetPwdId = params.get("rid");
			prepareResetPwd(resetPwdId);
		}
    }

    private String prepareResetPwdURL(String id) {
    	HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
    	String ret = request.getRequestURL().toString();
    	log.info("Orig ResetPwdURL: "+ret);
    	ret = ret.replace(request.getServerName(), "linguisto.eu");
    	ret = ret.replace(":"+request.getServerPort(), "");
    	log.info("Prepared ResetPwdURL: "+ret);
    	
		ret += "?rid="+id;
		return ret;
    }

    public void prepareResetPwd(String resetId) {
		if (resetId != null) {
			log.fine("resetId = "+resetId);
			resetPwdUser = getDBManager().getUserByResetId(resetId);
			if (resetPwdUser != null) {
				log.fine("Reset Pwd of user "+resetPwdUser.getName() + " is in progress.");
			} else {
				log.fine("Pwd can't be reset for resetId = "+resetId);
			}
	    	this.resetPwdId = null;
		}
    }

    public void resetPwd() {
		log.fine("resetPwd executed");
		if (resetPwdUser != null) {
			if (pwd != null && pwd.equals(pwd2)) {
				if(getDBManager().changePwd(resetPwdUser, pwd)) {
					getDBManager().deleteUserRequest(resetPwdUser.getId(),"resetpwd");
		    		FacesContext.getCurrentInstance().addMessage(null, 
		    				new FacesMessage(FacesMessage.SEVERITY_INFO,
		    						"Пароль змінено.",
		    						"Пароль успішно збережено."));
					resetPwdUser = null;
				}
			} else {
	    		FacesContext.getCurrentInstance().addMessage(null, 
	    				new FacesMessage(FacesMessage.SEVERITY_INFO,
	    						"Помилка.",
	    						"Пароль порожній, або не співпадає з повторенням."));
			}
		} else {
			log.fine("Pwd can't be reset. No user.");
		}
    }

    public void requestResetPwd() {
    	error = "";
    	String eMail = null;
    	User user = null;
    	//check user data
    	//1. is user name exist
    	if (userName != null && userName.length() > 0) {
			user = getDBManager().getUserByName(userName);
    		if (user != null) {
    			eMail = user.getEmail();
    		} else {
        		error = "Такого користувача не існує.";
        		FacesContext.getCurrentInstance().addMessage(null, 
        				new FacesMessage(FacesMessage.SEVERITY_WARN,
        						"Такого користувача не існує",""));
    		}
    	}
    	if (eMail == null) {
        	//2. check eMail
        	if (userEmail != null && userEmail.length() > 0) {
        		try {
    				InternetAddress email = new InternetAddress(userEmail);
    				email.validate();
    				user = getDBManager().getUserByEmail(email.toString());
        			if (user != null) {
        				eMail = email.toString();
        			}
    			} catch (AddressException e) {
    	    		error += "\nНекоректна адреса електронної пошти.";
    	    		FacesContext.getCurrentInstance().addMessage(null, 
    	    				new FacesMessage(FacesMessage.SEVERITY_ERROR,
    	    						"Некоректна адреса електронної пошти.",
    	    						"Перевірте, будь ласка, адресу електронної пошти."));
    			}
        	}
    	}
    	
    	if (eMail != null) {
    		//generate password
    		SecureRandom random = new SecureRandom();
			String resetId = new BigInteger(120, random).toString(32);
    		
			// generate reset link and send it
			String link = prepareResetPwdURL(resetId);
			
			//store request in DB
        	if (getDBManager().createUserRequest(user.getId(), "resetpwd", resetId, 24)) {
        		String mailSubject = "Скинути пароль";
        		String mailContent = "Ви (або хтось інший) просили скинути пароль на сайті linguisto.eu." +
        	    		"\n<br/>Щоб ввести новий пароль перейдіть на наступну сторінку: " +
        	    		"<a href=\""+link+"\">"+link+"</a>"+
        	    		"\n<br/>Будь ласка, введіть новий пароль якнайшвидше. Через 24 години це посилання буде деактивовано." + 
        	    		"\n<br/>З найкращими побажаннями, linguisto.eu.";
            	//send eMail
            	try {
            		MailUtil mailUtil = new MailUtil();
            		mailUtil.sendMail(mailSubject, mailContent, eMail);

            		FacesContext.getCurrentInstance().addMessage(null, 
            				new FacesMessage(FacesMessage.SEVERITY_INFO,
            						"Пароль скинуто.",
            						"Вам відправлено електронного листа з подальшими інструкціями."));

            	} catch (MessagingException e) {
        			log.log(Level.SEVERE, e.getMessage(), e);
            		FacesContext.getCurrentInstance().addMessage(null, 
            				new FacesMessage(FacesMessage.SEVERITY_ERROR,
            						"Неможливо відправити електронного листа.",
            						"Неможливо відправити електронного листа. Спробуйте ще раз."));
            		error += "\nНеможливо відправити електронного листа. " + e.getMessage();
            	}
        	} else {
        		FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Неможливо скинути пароль.",
							"Неможливо скинути пароль."));
        		error += "\nНеможливо скинути пароль.";
        	}

    	} else {
    		error += "\nНеобхідно вказати користувача або адресу електронної пошти.";
    		FacesContext.getCurrentInstance().addMessage(null, 
    				new FacesMessage(FacesMessage.SEVERITY_ERROR,
    						"Необхідно вказати користувача або адресу електронної пошти.",""));
    	}

    	if (error.length() == 0) {
    		error = null;
    	}
    }

	public String getError() {
		return error;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getPwd2() {
		return pwd2;
	}

	public void setPwd2(String pwd2) {
		this.pwd2 = pwd2;
	}

	public String getNewpwd() {
		return newpwd;
	}

	public void setNewpwd(String newpwd) {
		this.newpwd = newpwd;
	}

	public String getNewpwd2() {
		return newpwd2;
	}

	public void setNewpwd2(String newpwd2) {
		this.newpwd2 = newpwd2;
	}

	public String getResetPwdId() {
		return resetPwdId;
	}

	public void setResetPwdId(String id) {
		this.resetPwdId = id;
	}

	public User getResetPwdUser() {
		return resetPwdUser;
	}
}
