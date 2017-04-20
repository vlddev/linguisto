package org.linguisto.utils;

import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailUtil {

	public static final String ADMIN_MAIL = "admin@yoursite.com"; 
	
	public void sendMail(String subject, String content, String addressTo) throws MessagingException {
		String mUser = "mail.user";
		String mPwd = "pwd";
		String smtpHost = "smtp.yoursite.com";
	    // Get system properties
	    Properties properties = new Properties();
	    // Setup mail server
	    properties.put("mail.smtp.starttls.enable", "true");
	    properties.put("mail.smtp.port", "465");
	    properties.put("mail.smtp.auth", "true");
	    properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	    properties.put("mail.smtp.ssl","true");
	    properties.put("mail.smtp.host", smtpHost);
	    properties.put("mail.smtp.user", mUser);
	    properties.put("mail.smtp.password", mPwd);
	    //properties.put("mail.debug", "true");

	    // Get the default Session object.
	    Session mailSession = Session.getDefaultInstance(properties);
	    //mailSession.setDebug(true);
	    Transport trans = mailSession.getTransport("smtp");
	    trans.connect(smtpHost, mUser, mPwd); 

	    MimeMessage  m = new MimeMessage(mailSession);
	    m.setFrom(new InternetAddress(mUser));
	    Address[] to = new InternetAddress[] {new InternetAddress(addressTo)};
	    m.setRecipients(Message.RecipientType.TO, to);
	    m.setSubject(subject);
	    //m.setSentDate(new Date());
	    m.setContent(content, "text/html; charset=utf-8");

	    trans.sendMessage(m, m.getAllRecipients());
	    trans.close();
	}
	
	public void sendMailWithAttachments(String subject, String content, String addressTo, List<MimeBodyPart> attachments) throws MessagingException {
		String mUser = "mail.user";
		String mPwd = "pwd";
		String smtpHost = "smtp.yoursite.com";

	    Properties properties = new Properties();
	    // Setup mail server
	    properties.put("mail.smtp.starttls.enable", "true");
	    properties.put("mail.smtp.port", "465");
	    properties.put("mail.smtp.auth", "true");
	    properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	    properties.put("mail.smtp.ssl","true");
	    properties.put("mail.smtp.host", smtpHost);
	    properties.put("mail.smtp.user", mUser);
	    properties.put("mail.smtp.password", mPwd);
	    //properties.put("mail.debug", "true");

	    // Get the default Session object.
	    Session mailSession = Session.getDefaultInstance(properties);
	    //mailSession.setDebug(true);
	    Transport trans = mailSession.getTransport("smtp");
	    trans.connect(smtpHost, mUser, mPwd); 

	    MimeMessage  m = new MimeMessage(mailSession);
	    m.setFrom(new InternetAddress(mUser));
	    Address[] to = new InternetAddress[] {new InternetAddress(addressTo)};
	    m.setRecipients(Message.RecipientType.TO, to);
	    m.setSubject(subject);

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(content, "text/html; charset=utf-8");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        if (attachments != null) {
        	for (MimeBodyPart part : attachments) {
                multipart.addBodyPart(part);
        	}
        }
        m.setContent(multipart);

	    trans.sendMessage(m, m.getAllRecipients());
	    trans.close();

	}
	
}
