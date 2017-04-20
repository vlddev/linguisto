package org.linguisto.beans.user;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

import org.linguisto.beans.AbstractDbBean;
import org.linguisto.db.obj.User;
import org.linguisto.utils.MailUtil;

@ManagedBean(name="signin")
@RequestScoped
public class SigninBean extends AbstractDbBean {

	private static final long serialVersionUID = 852207647590338472L;

	private static final Logger log = Logger.getLogger(SigninBean.class.getName());

    @Resource(mappedName = "java:jboss/mail/Default")
    private Session mailSession;
    
	private String userName;
	private String userEmail;
	private String pwd;
	private String pwd2;
	private String error;

	private String confirmId;
	private User confirmUser;
	
    public SigninBean() {
    }
 
    public void signin() throws IOException {
    	error = "";
    	//check user data
    	//1. is user name unique
    	if (getDBManager().hasUser(userName)) {
    		error = "Такий користувач вже існує, виберіть інше ім'я.";
//    		FacesContext.getCurrentInstance().addMessage(null, 
//    				new FacesMessage(FacesMessage.SEVERITY_WARN,
//    						"Username already exist",
//    						"Username already exist. Try another one."));
    		FacesContext.getCurrentInstance().addMessage(null, 
    				new FacesMessage(FacesMessage.SEVERITY_WARN,
    						"Такий користувач вже існує",
    						"Такий користувач вже існує, виберіть інше ім'я."));
    	}
    	//2. check eMail
    	if (userEmail != null) {
    		try {
				InternetAddress email = new InternetAddress(userEmail.trim());
				email.validate();
				if (getDBManager().hasEmail(email.toString())) {
		    		error += "\nКористувач з такою адресою електронної пошти вже існує.";
				}
			} catch (AddressException e) {
	    		error += "\nНекоректна адреса електронної пошти.";
//	    		FacesContext.getCurrentInstance().addMessage(null, 
//	    				new FacesMessage(FacesMessage.SEVERITY_ERROR,
//	    						"Email invalid",
//	    						"Please check your email."));
	    		FacesContext.getCurrentInstance().addMessage(null, 
	    				new FacesMessage(FacesMessage.SEVERITY_ERROR,
	    						"Некоректна адреса електронної пошти.",
	    						"Перевірте, будь ласка, адресу електронної пошти."));
			}
    	} else {
    		error += "\nНеобхідно вказати адресу електронної пошти.";
    	}
    	//3. check password
    	if (pwd != null && pwd2 != null) {
    		if (!pwd2.equals(pwd)) {
        		error += "\nПоля Пароль і Повторіть пароль не збігаються.";
    		}
    	} else {
    		error += "\nПароль не може бути порожнім.";
    	}
    	
    	//generate md5-hash for identity check
    	String hash = getDBManager().md5(userName + userEmail + System.currentTimeMillis());
    	
    	//store user data into DB
    	if (error.length() == 0) {
        	if (getDBManager().createUser(userName, pwd, userEmail, hash)) {

        		//TODO localize
//        		String mailContent = "Welcome to Linguistic studies!" +
//        	    		"\nTo finish your registration visit following URL: " +
//        	    		((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRequestURL() +
//        	    		"?hash="+hash+
//        	    		"\nBest regards.";
        		String mailSubject = "Ласкаво просимо до Лінгвісто";
        		String link = prepareConfirmURL(hash);
        		String mailContent = "Ласкаво просимо до проекту Лінгвісто на сайті linguisto.eu!" +
        	    		"\n<br/>Щоб завершити реєстрацію перейдіть на наступну сторінку: " +
        	    		"<a href=\""+link+"\">"+link+"</a>"+
        	    		"\n<br/>З найкращими побажаннями, linguisto.eu.";
            	//send eMail
            	try {
            		MailUtil mailUtil = new MailUtil();
            		mailUtil.sendMail(mailSubject, mailContent, userEmail);

//            		FacesContext.getCurrentInstance().addMessage(null, 
//            				new FacesMessage(FacesMessage.SEVERITY_INFO,
//            						"You are signed in.",
//            						"An email was send to you. Please read it for further instructions."));
            		FacesContext.getCurrentInstance().addMessage(null, 
            				new FacesMessage(FacesMessage.SEVERITY_INFO,
            						"Обліковий запис створено.",
            						"Вам відправлено електронного листа з подальшими інструкціями для завершення реєстрації."));
            	} catch (MessagingException e) {
            		FacesContext.getCurrentInstance().addMessage(null, 
            				new FacesMessage(FacesMessage.SEVERITY_ERROR,
            						"Неможливо відправити електронного листа.",
            						"Неможливо відправити електронного листа. " + e.getMessage()+"\nЗміст листа був таким: \n" + mailContent));
            		error += "\nНеможливо відправити електронного листа. " + e.getMessage();
            		error += "\nЗміст листа був таким: \n" + mailContent;
            	}
        	} else {
//        		FacesContext.getCurrentInstance().addMessage(null, 
//        				new FacesMessage(FacesMessage.SEVERITY_ERROR,
//        						"User can't be created.",
//        						"User can't be created."));
        		FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Неможливо створити користувача.",
							"Неможливо створити користувача."));
        		error += "\nНеможливо створити користувача.";
        	}
    	} else {
    		FacesContext.getCurrentInstance().addMessage("messages", 
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Помилка", error));
    	}

    	if (error.length() == 0) {
    		error = null;
    	}
    }
    
    private String prepareConfirmURL(String hash) {
    	HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
    	String ret = request.getRequestURL().toString();
    	log.info("Orig RequestURL: "+ret);
    	ret = ret.replace(request.getServerName(), "linguisto.eu");
    	ret = ret.replace(":"+request.getServerPort(), "");
    	log.info("Prepared RequestURL: "+ret);
    	
		//((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).
		ret += "?hash="+hash;
		return ret;
    }

    public void confirmRegistration(String confirmId) {
		if (confirmId != null) {
			log.fine("ConfirmId = "+confirmId);
			//TODO confirmation should be done not later as 5 days after creation of user account
			confirmUser = getDBManager().confirmRegistration(confirmId);
			if (confirmUser != null) {
				log.fine("Registration of user "+confirmUser.getName() + " confirmed.");
			} else {
				log.fine("Registration not confirmed.");
			}
	    	this.confirmId = null;
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

	public String getConfirmId() {
		return confirmId;
	}

	public void setConfirmId(String confirmId) {
		this.confirmId = confirmId;
	}

	public User getConfirmUser() {
		return confirmUser;
	}
}
