/*
#######################################################################
#
#  Linguisto Portal
#
#  Copyright (c) 2017 Volodymyr Vlad
#
#######################################################################
*/

package org.linguisto.learn.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.linguisto.learn.Word;

/**
 * This implementation of TranslationDictionary
 * use DB to find (word, translation word) pairs
 * 
 * following tables must exist in the DB:
 *   inf (id, lang, inf, type)
 *   tr (id, fk_inf, order_nr, tr_lang, translation)
 * 
 * WARNING: current implementation can be used only for [lang2] = 'uk'
 * 
 * @author Vlad
 */
public class StDbTranslationDictionary2 implements org.linguisto.learn.db.TranslationDictionary {

	private Connection ce;

    public StDbTranslationDictionary2() {}

    public StDbTranslationDictionary2(Connection c) {
		this.ce = c;
	}

	/**
	 * check if database contains all needed tables for
	 * two passed locales to make text alignment. 
	 * @param l1 - locale1
	 * @param l2 - locale2
	 * @return String[] - the list of found inconsistencies. If no inconsistencies found
	 * the list must be empty or null.
	 */
    public List<String> checkDB(Locale l1, Locale l2) {
        return checkDB(ce, l1, l2);
    }

    public List<String> checkDB(Connection c, Locale l1, Locale l2) {
		List<String> ret = new ArrayList<String>();

		//check table inf
		String sTable = "inf";
		String sql = "select * from " + sTable + " where id = -1";
		try {
			c.createStatement().executeQuery(sql);
		} catch (SQLException e) {
			ret.add(e.getMessage());
		}

		//check table tr
		sTable = "tr";
		sql = "select * from " + sTable + " where id = -1";
		try {
			c.createStatement().executeQuery(sql);
		} catch (SQLException e) {
			ret.add(e.getMessage());
		}
		
		return ret;
	}

//	private String getTrMapTable(Locale l1, Locale l2) {
//		String ret = "";
//		String lang1 = l1.getLanguage();
//		String lang2 = l2.getLanguage();
//		if (lang1.compareTo(lang2) < 0) {
//			ret = lang1+"_"+lang2;
//		} else {
//			ret = lang2+"_"+lang1;
//		}
//		return ret;
//	}

    public List<String> getTranslation(
            Word word,
            Locale langFrom,
            Locale langTo) {
        return getTranslation(ce, word, langFrom, langTo);
    }

    public List<String> getTranslation(
            Connection c,
            Word word,
            Locale langFrom,
            Locale langTo) {
		List<String> ret = new ArrayList<String>();
		if (word == null) return ret;
		//Translation
		String sql = "select tr.translation "
			+ " from inf, tr "
			+ " where inf.id = ? and tr.fk_inf = inf.id "
            + " order by tr.order_nr";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = c.prepareStatement(sql);
			ps.clearParameters();
			ps.setLong(1, word.getId());
			rs = ps.executeQuery();
			while(rs.next()) {
                ret.add(rs.getString("translation"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DAO.closeResultSet(rs);
			DAO.closeStatement(ps);
		}
		return ret;
	}
}
