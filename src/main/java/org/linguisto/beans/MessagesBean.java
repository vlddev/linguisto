package org.linguisto.beans;

import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean(name="messages")
@ApplicationScoped
public class MessagesBean extends AbstractDbBean {
 
	private static final long serialVersionUID = 443162696206739780L;
	private static final String defaultLang = "uk"; 

//	private Map<String, Map<String,String>> lableMap = new HashMap<String,Map<String,String>>();
	private Map<String,String> msgMap = new HashMap<String, String>();
	
	public MessagesBean() {
		//initLables();
	}
	
	public void putMessage(String key, String msg) {
		msgMap.put(key, msg);
	}
	
	public String getMessage(String key) {
		return msgMap.get(key);
	}
	
	//connect to DB and get customer list
	// TODO remove this method
//	private void initLables() {
//		//uk - lables
//		Map<String,String> ukLables = new HashMap<String,String>();
//		//aligner.xhtml
//		ukLables.put("uk_text", "текст українською");
//		ukLables.put("en_text", "текст англійською");
//		ukLables.put("learn_text", "навчальний текст");
//		ukLables.put("do_align", "Паралелізувати");
//		//learn.xhtml
//		ukLables.put("text_lang", "мова тексту");
//		ukLables.put("dict_lang", "мова словника");
//		ukLables.put("lang_english", "англійська");
//		ukLables.put("lang_german", "німецька");
//		ukLables.put("lang_ukrainian", "українська");
//		ukLables.put("do_prepare", "Обробити");
//		ukLables.put("do_clear", "Очистити");
//		lableMap.put("uk", ukLables);
//		//en -lables
//		Map<String,String> enLables = new HashMap<String,String>();
//		enLables.put("uk_text", "ukrainian text");
//		enLables.put("en_text", "english text");
//		enLables.put("learn_text", "text to learn");
//		enLables.put("text_lang", "text language");
//		enLables.put("do_align", "align");
//		lableMap.put("en", enLables);
//	}

	public String getText(String id, String langCode) {
		return getDBManager().getText(id, langCode, defaultLang);
	}

//	public static String urlEncode(String value) {
//		String ret = value;
//	    try {
//			ret = URLEncoder.encode(value, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	    return ret;
//	}
}
