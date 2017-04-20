package org.linguisto.beans;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.sql.DataSource;

import org.linguisto.db.DBManager;

@ManagedBean(eager=true, name="dbBean")
@ApplicationScoped
public class DatabaseBean implements java.io.Serializable {

	private static final long serialVersionUID = 852207647590338472L;

	private static final Logger log = Logger.getLogger(DatabaseBean.class.getName());
	private static final Locale[] supportedLanguages = {new Locale("de"), new Locale("en"), new Locale("fr"), new Locale("uk")};
	
	@Resource(name="java:comp/env/jdbc/MySQLDS")
	private DataSource ds;
	
	private Map<Locale, WordTypeConverter> wordTypeConverterMap = new HashMap<Locale, WordTypeConverter>();

    public DatabaseBean() {
    	log.fine("Instance of "+DatabaseBean.class.getName()+" created.");
    }
 
	@PostConstruct
	private void init() {
		DBManager dbManager = new DBManager(ds);
		for (Locale lang : supportedLanguages) {
			WordTypeConverter conv = new WordTypeConverter();
			conv.init(lang, dbManager);
			wordTypeConverterMap.put(lang, conv);
		}
	}

	public WordTypeConverter getWordTypeConverterByStr(String lang) {
		return getWordTypeConverter(new Locale(lang));
	}

	public WordTypeConverter getWordTypeConverter(Locale lang) {
		return wordTypeConverterMap.get(lang);
	}
	
	public DataSource getDataSource() {
		return ds;
	}
}
