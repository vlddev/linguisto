package org.linguisto.webui.listener;

import java.util.Locale;
import java.util.logging.Logger;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.linguisto.beans.MessagesBean;

public class SessionListener implements HttpSessionListener {

	private static final Logger log = Logger.getLogger(SessionListener.class.getName());

	private MessagesBean messages;
	
	private int sessionCount = 0;
	
	public int getSessionCount() {
		return sessionCount;
	}
	
	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		sessionCount++;
		if (messages == null) {
	        FacesContext ctx = FacesContext.getCurrentInstance();
	        Application app = ctx.getApplication();
	        //ValueExpression bind = app.getExpressionFactory().createValueExpression("#{messages}");
	        ValueBinding bind = app.createValueBinding("#{messages}");
	        messages = (MessagesBean) bind.getValue(ctx);
		}
		if (messages != null) {
			messages.putMessage("sessions", ""+sessionCount);
		}

		//get session infos
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	
		//is client behind something?
		String ipAddress = request.getHeader("X-FORWARDED-FOR");  
		if (ipAddress == null) {  
			ipAddress = request.getRemoteAddr();  
		}
		
		String userAgent = request.getHeader("User-Agent");
    	Locale browserLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		if (messages != null) {
			messages.getDBManager().insertSessionAction(arg0.getSession().getId(), ipAddress, userAgent, browserLocale, "create");
		}
    	
		
	}
	
	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		sessionCount--;
		if (messages != null) {
			messages.putMessage("sessions", ""+sessionCount);
			//messages.getDBManager().insertSessionAction(arg0.getSession().getId(), null, null, null, "destroy");
		}
		//log.info("Session destroyed. Active session count: "+sessionCount);
	}

	public MessagesBean getMessages() {
		return messages;
	}

	public void setMessages(MessagesBean messages) {
		this.messages = messages;
	}
}