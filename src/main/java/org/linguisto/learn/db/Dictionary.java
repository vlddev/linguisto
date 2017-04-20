package org.linguisto.learn.db;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.linguisto.learn.DictUser;
import org.linguisto.learn.Word;

/**
 * Common dictionary interface.
 */
public interface Dictionary {

    List<String> checkDB(Locale l1, Locale l2);

    /**
     * Find word base
     * @param wf
     * @param lang
     * @return
     */
    List<Word> getBaseForm(String wf, Locale lang, boolean ignoreCase);

    /**
     *  Gets translation articles (in HTML) from dictionary.
     *
     * @param word
     * @param langFrom
     * @param langTo
     * @return  list of Strings as HTML
     */
    List<String> getTranslation(Word word, Locale langFrom, Locale langTo);

    List<Word> getTranslation(List<Word> wfList, Locale langFrom, Locale langTo);

    DictUser getUserByEMail(String eMail);

    Collection<Word> checkUserKnow(Collection<Word> words, DictUser user, String prefix) throws SQLException;

}