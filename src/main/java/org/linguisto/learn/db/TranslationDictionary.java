package org.linguisto.learn.db;

import java.sql.Connection;
import java.util.List;
import java.util.Locale;

import org.linguisto.learn.Word;

/**
 * TranslationDictionary to translate from one language to another.
 */
public interface TranslationDictionary {

    List<String> checkDB(Connection c, Locale l1, Locale l2);
    List<String> checkDB(Locale l1, Locale l2);

    /**
     *  Gets translation articles from dictionary.
     *
     * @param word
     * @param langFrom
     * @param langTo
     * @return  list of Strings
     */
    List<String> getTranslation(Word word, Locale langFrom, Locale langTo);
    List<String> getTranslation(Connection c, Word word, Locale langFrom, Locale langTo);

}