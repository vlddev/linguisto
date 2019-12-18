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

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.linguisto.learn.db.DAO;
import org.linguisto.learn.db.DbDictionary;
import org.linguisto.learn.db.Dictionary;
import org.linguisto.learn.db.StDbTranslationDictionary2;
import org.linguisto.utils.IOUtil;

import java.sql.Connection;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BuilderPOS {

    public static final Logger log = Logger.getLogger(BuilderPOS.class.getName());

    public static String TagSeparator = "¿";

    /**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
        //setLevel(Level.ALL);
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

            Properties config = new Properties();
            config.setProperty("tagSeparator", TagSeparator);
            config.setProperty("tokenizerOptions", "asciiQuotes");
            //MaxentTagger posTagger = new MaxentTagger("/home/vlad/Dokumente/my_dev/pos-tagging/stanford-postagger/models/english-left3words-distsim.tagger", config);
            MaxentTagger posTagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger", config);

            makeTextUsingFiles(sFile, new Locale(lang1), new Locale(lang2), c, posTagger);

            DAO.closeConnection(c);
		} else {
			log.info("Usage: textFile lang targetLang");
		}
	}

    public static void setLevel(Level targetLevel) {
        Logger root = Logger.getLogger("");
        root.setLevel(targetLevel);
        for (Handler handler : root.getHandlers()) {
            handler.setLevel(targetLevel);
        }
        System.out.println("level set: " + targetLevel.getName());
    }
    public static void makeTextUsingFiles(String sFile, Locale locFrom, Locale locTo, Connection c, MaxentTagger posTagger) throws Exception {
        //read text
        String text = IOUtil.getFileContent(sFile, "utf-8");
        DbDictionary dict = new DbDictionary(c);
        dict.addTranslationDictionary("en", new StDbTranslationDictionary2(c));
        dict.addTranslationDictionary("de", new StDbTranslationDictionary2(c));
        DictUser user = new DictUser();
        user.setId(1);
        TextPOS textObj = makeText(text, locFrom, locTo, dict, user, posTagger);

        //store fb2
        IOUtil.storeString(sFile+".fb2", "utf-8", textObj.getFb2(Text.FIND_WORD_IGNORE_CASE));
        //store chunks
        //List<TChunk> chList = textObj.getAsChunks(false);
        //IOUtil.storeString(sFile+".chunks.txt", "utf-8",
        //        chList.stream().map(tChunk -> tChunk.toString()).collect(Collectors.joining()));

        //store unrecognized
        IOUtil.storeString(sFile+".unrec.txt", "utf-8", textObj.getUnrecognizedWordUsageStats());
        //store untranslated
        IOUtil.storeString(sFile+".untr.txt", "utf-8", textObj.getUntranslatedWords());

        IOUtil.storeString(sFile+".unknown.txt", "utf-8", textObj.getUnknownWords());
        IOUtil.storeString(sFile+".unknown.rank.txt", "utf-8", textObj.getUnknownWordsOrderByRank());
    }

    public static String makeTextAsHtml(String strText, Locale locFrom, Locale locTo, Dictionary dict, DictUser user, MaxentTagger posTagger, boolean completeHtmlPage) throws Exception {
        String ret = null;

        //read text in first language
        TextPOS text = makeText(strText, locFrom, locTo, dict, user, posTagger);

        //store HTML
        ret = text.getHtml(completeHtmlPage);

        return ret;
    }

    public static String makeTextAsChunks(String strText, Locale locFrom, Locale locTo, Dictionary dict, DictUser user, MaxentTagger posTagger, boolean completeHtmlPage) throws Exception {
        //read text in first language (locFrom)
        TextPOS text = makeText(strText, locFrom, locTo, dict, user, posTagger);

        List<TChunk> chList = text.getAsChunks(false);

        return chList.stream().map(tChunk -> tChunk.toString()).collect(Collectors.joining());
    }

    public static String makeTextAsFb2(String strText, Locale locFrom, Locale locTo, Dictionary dict, DictUser user, MaxentTagger posTagger) throws Exception {
        String ret = null;

        //read text in first language
        TextPOS text = makeText(strText, locFrom, locTo, dict, user, posTagger);

        //store HTML
        ret = text.getFb2(Text.FIND_WORD_GERMAN);

        return ret;
    }

    public static TextPOS makeText(String strText, Locale locFrom, Locale locTo, Dictionary dict, DictUser user, MaxentTagger posTagger) throws Exception {
        TextPOS ret = null;
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
        ret = new TextPOS(strText, locFrom, dict, posTagger);
        int textProcessingType = Text.FIND_WORD_GERMAN; //as is, first word lower case
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
