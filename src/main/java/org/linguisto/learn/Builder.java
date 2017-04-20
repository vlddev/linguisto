/*
#######################################################################
#
#  Linguisto Portal
#
#  Copyright (c) 2017 Volodymyr Vlad
#
#######################################################################
*/

package org.linguisto.learn;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.linguisto.learn.db.DAO;
import org.linguisto.learn.db.DbDictionary;
import org.linguisto.learn.db.Dictionary;
import org.linguisto.learn.db.StDbTranslationDictionary2;
import org.linguisto.utils.IOUtil;

public class Builder {

    public static final Logger log = Logger.getLogger(Builder.class.getName());

    /**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		if (args.length == 3) {
			String sFile = args[0];
			String lang1 = args[1];
			String lang2 = args[2];
			//check input data
			String[] langList = Locale.getISOLanguages();
			
			if (Arrays.binarySearch(langList, lang1) < 0 ) {
				log.severe("Unknown language '"+lang1+"'.");
				System.exit(1);
			}
			if (Arrays.binarySearch(langList, lang2) < 0 ) {
				log.severe("Unknown language '"+lang2+"'.");
				System.exit(1);
			}

            String dbUser = System.getProperty("jdbc.user");
            String dbPwd = System.getProperty("jdbc.password");
            String dbUrlTr = System.getProperty("jdbc.url");
            String dbJdbcDriver = System.getProperty("jdbc.driver");

            Connection c = DAO.getConnection(dbUser, dbPwd, dbUrlTr, dbJdbcDriver);

            makeTextUsingFiles(sFile, new Locale(lang1), new Locale(lang2), c);

            DAO.closeConnection(c);
		} else {
			log.info("Usage: textFile lang targetLang");
		}
	}

    public static void makeTextUsingFiles(String sFile, Locale locFrom, Locale locTo, Connection c) throws Exception {
        //read text
        String text = IOUtil.getFileContent(sFile, "utf-8");
        DbDictionary dict = new DbDictionary(c);
        dict.addTranslationDictionary("en", new StDbTranslationDictionary2(c));
        dict.addTranslationDictionary("de", new StDbTranslationDictionary2(c));
        DictUser user = new DictUser();
        user.setId(1);
        Text textObj = makeText(text, locFrom, locTo, dict, user);

        //store fb2
        IOUtil.storeString(sFile+".fb2", "utf-8", textObj.getFb2(Text.FIND_WORD_GERMAN));
        //store unrecognized
        IOUtil.storeString(sFile+".unrec.txt", "utf-8", textObj.getUnrecognizedWordUsageStats());
        //store untranslated
        IOUtil.storeString(sFile+".untr.txt", "utf-8", textObj.getUntranslatedWords());

        IOUtil.storeString(sFile+".unknown.txt", "utf-8", textObj.getUnknownWords());
    }

    public static String makeTextAsHtml(String strText, Locale locFrom, Locale locTo, Dictionary dict, DictUser user, boolean completeHtmlPage) throws Exception {
        String ret = null;

        //read text in first language
        Text text = makeText(strText, locFrom, locTo, dict, user);

        //store HTML
        ret = text.getHtml(completeHtmlPage);

        return ret;
    }

    public static String makeTextAsFb2(String strText, Locale locFrom, Locale locTo, Dictionary dict, DictUser user) throws Exception {
        String ret = null;

        //read text in first language
        Text text = makeText(strText, locFrom, locTo, dict, user);

        //store HTML
        ret = text.getFb2(Text.FIND_WORD_GERMAN);

        return ret;
    }

    public static Text makeText(String strText, Locale locFrom, Locale locTo, Dictionary dict, DictUser user) throws Exception {
        Text ret = null;
        Date start = new Date();

        List<String> errorList = dict.checkDB(locFrom, locTo);
        if (errorList != null && errorList.size() > 0) {
            for (String s : errorList) {
                log.severe(s);
            }
            log.severe("ERROR: DB-inconsistencies detected.");
            return ret;
        }

        //read text in first language
        ret = new Text(strText, locFrom, dict);
        int textProcessingType = Text.FIND_WORD_IGNORE_CASE;
        if ("de".equals(locFrom.getLanguage())) {
            log.info("German text processing");
            textProcessingType = Text.FIND_WORD_GERMAN;
        }

        ret.prepareDict(locTo, user, textProcessingType);

        long runTime = ((new Date()).getTime() - start.getTime())/1000;
        log.info("Builder.makeText() done in "+ runTime + " sec.");
        return ret;
    }

}
