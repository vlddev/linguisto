package org.linguisto.beans.user;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.linguisto.beans.AbstractDbBean;
import org.linguisto.db.obj.User;

@ManagedBean(name="changeUserData")
@ViewScoped
public class ChangeUserDataBean extends AbstractDbBean {

	private static final long serialVersionUID = 852207647590338472L;

    @ManagedProperty(value="#{login}")
    // define getter and setter to make it work
    private LoginBean loginBean;
    
	private String userName;
	private String userEmail;
	private String pwd;
	private String newpwd;
	private String newpwd2;

    public ChangeUserDataBean() {
    }
 
	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
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

    public String changePwd() {
    	String ret = null;
    	if (loginBean.getUser() == null) return ret;
    	//1. check password
    	if (!checkPassword(loginBean.getUser(), getPwd())) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Невірний пароль.", null);
            FacesContext.getCurrentInstance().addMessage("growl", message);
            loginBean.logUserAction("change_email", "Wrong password. Username '"+userName+"'", null);
            pwd = null;
            return ret;
    	}
    	//3. check password
    	if (getNewpwd() != null && getNewpwd2() != null) {
    		if (getNewpwd().equals(getNewpwd2())) {
				if(getDBManager().changePwd(loginBean.getUser(), getNewpwd())) {
		    		FacesContext.getCurrentInstance().addMessage("growl", 
		    				new FacesMessage(FacesMessage.SEVERITY_INFO,
		    						"Пароль змінено.",
		    						"Пароль успішно збережено."));
		            pwd = null;
		            userEmail = null;
					loginBean.logUserAction("change_pwd", "User '"+loginBean.getUser().getName()+"' has changed password", null);
		            ret = "accountInfo";
				}
    		} else {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Поля Пароль і Повторіть пароль не збігаються.", null);
                FacesContext.getCurrentInstance().addMessage("growl", message);
    		}
    	} else {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Пароль не може бути порожнім.", null);
            FacesContext.getCurrentInstance().addMessage("growl", message);
    	}
    	return ret;
    }

    public String changeEmail() {
    	String ret = null;
    	if (loginBean.getUser() == null) return ret;
    	//1. check password
    	if (!checkPassword(loginBean.getUser(), getPwd())) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Невірний пароль.", null);
            FacesContext.getCurrentInstance().addMessage("growl", message);
            loginBean.logUserAction("change_email", "Wrong password. Username '"+userName+"'", null);
            pwd = null;
            return ret;
    	}
    	//2. check eMail
    	if (userEmail != null) {
    		try {
				InternetAddress email = new InternetAddress(userEmail.trim());
				email.validate();
				if (getDBManager().hasEmail(email.toString())) {
		            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN,
		            		"Користувач з такою адресою електронної пошти вже існує.", null);
		            FacesContext.getCurrentInstance().addMessage("growl", message);
				} else {
					getDBManager().changeUserEmail(loginBean.getUser(), userEmail);
					loginBean.logUserAction("change_email", "User '"+loginBean.getUser().getName()+"' has changed email from "+loginBean.getUser().getEmail()+" to "+userEmail, null);
            		loginBean.getUser().setEmail(userEmail);
		            pwd = null;
		            userEmail = null;
		            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
		            		"Адресу електронної пошти успішно змінено.", null);
		            FacesContext.getCurrentInstance().addMessage("growl", message);
		            ret = "accountInfo";
				}
			} catch (AddressException e) {
	    		FacesContext.getCurrentInstance().addMessage(null, 
	    				new FacesMessage(FacesMessage.SEVERITY_ERROR,
	    						"Некоректна адреса електронної пошти.",
	    						"Перевірте, будь ласка, адресу електронної пошти."));
			}
    	} else {
    		FacesContext.getCurrentInstance().addMessage(null, 
    				new FacesMessage(FacesMessage.SEVERITY_ERROR,
    						"Необхідно вказати адресу електронної пошти.",
    						"Перевірте, будь ласка, адресу електронної пошти."));
    	}
    	return ret;
    }
    
    private boolean checkPassword(User user, String pwd) {
    	boolean ret = false;
    	if (user != null && pwd != null) {
         	User tmpUser = getDBManager().getUser(user.getName(), pwd);
        	if (tmpUser != null) {
        		ret = true;
        	}
    	}
    	return ret;
    }
}
