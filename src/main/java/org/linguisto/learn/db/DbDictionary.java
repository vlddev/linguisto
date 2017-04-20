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
import java.util.*;

import org.linguisto.learn.DictUser;
import org.linguisto.learn.Word;

/**
 * This implementation of TranslatorInterface
 * use DB to find (wordForm,word) pairs and (word, translation word) pairs
 * 
 * @author Vlad
 */
public class DbDictionary implements org.linguisto.learn.db.Dictionary {

	private Connection ce;

    private Map<String, List<TranslationDictionary>> trDictsMap;

    public DbDictionary() {
    }

    public DbDictionary(Connection c) {
		this.ce = c;
	}

    public DbDictionary(Connection c, Map<String, List<TranslationDictionary>> trDictsMap) {
        this.ce = c;
        this.trDictsMap = trDictsMap;
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
		//<lang>_inf
		String sTable = "inf";
		String sql = "select * from " + sTable + " where id = -1";
		try {
			c.createStatement().executeQuery(sql);
		} catch (SQLException e) {
			ret.add(e.getMessage());
		}

//		sTable = l2.getLanguage() + "_inf";
//		sql = "select * from " + sTable + " where id = -1";
//		try {
//			c.createStatement().executeQuery(sql);
//		} catch (SQLException e) {
//			ret.add(e.getMessage());
//		}

		//<lang>_wf
		sTable = l1.getLanguage() + "_wf";
		sql = "select * from " + sTable + " where id = -1";
		try {
			c.createStatement().executeQuery(sql);
		} catch (SQLException e) {
			ret.add(e.getMessage());
		}
//		sTable = l2.getLanguage() + "_wf";
//		sql = "select * from " + sTable + " where id = -1";
//		try {
//			c.createStatement().executeQuery(sql);
//		} catch (SQLException e) {
//			ret.add(e.getMessage());
//		}

		//tr
		sTable = "tr";
		sql = "select * from " + sTable + " where id = -1";
		try {
			c.createStatement().executeQuery(sql);
		} catch (SQLException e) {
			ret.add(e.getMessage());
		}

        //TODO: check dicts in map
//        for (TranslationDictionary dict : trDictsMap.values()) {
//            dict.checkDB(l1, l2);
//        }

		return ret;
	}

    /** 
	 * implementation of TranslatorInterface#getBaseForm
	 * @see org.linguisto.learn.db.Dictionary#getBaseForm(String, java.util.Locale, boolean)
	 */
	public List<Word> getBaseForm(Connection c, String wf, Locale lang, boolean ignoreCase) {
		List<Word> ret = new ArrayList<Word>();
		String prefix = lang.getLanguage() + "_";
		try {
			ret = getBaseForm(c, wf, prefix, ignoreCase);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}

    public List<Word> getBaseForm(String wf, Locale lang, boolean ignoreCase) {
        return getBaseForm(ce, wf, lang, ignoreCase);
    }

    private List<Word> getBaseForm(Connection c, String wf, String prefix, boolean ignoreCase) throws SQLException {
		List<Word> ret = new ArrayList<Word>();
        String sql = "select distinct inf.* from "+prefix+"wf wf, inf"
                + " where wf.wf = ? and wf.fk_inf = inf.id ";
        if (ignoreCase) {
            sql = "select distinct inf.* from "+prefix+"wf wf, inf"
                    + " where lower(wf.wf) = ? and wf.fk_inf = inf.id ";
        }
		//MySql make case-insensitive search
		//Case will be checked in loop
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = c.prepareStatement(sql);
			ps.setString(1, wf);
			rs = ps.executeQuery();
			long id;
			int type;
			String inf;
			String lang;
			while(rs.next()) {
				id = rs.getLong("id");
				inf = rs.getString("inf");
				type = rs.getInt("type");
				lang = rs.getString("lang");
                if (ignoreCase) { //case insensitive check
                    Word w = new Word(id, inf);
                    w.setType(type);
                    w.setLanguage(lang);
                    ret.add(w);
                } else { //case sensitive check
                    if (Character.isUpperCase(inf.charAt(0)) == Character.isUpperCase(wf.charAt(0))) {
                        Word w = new Word(id, inf);
                        w.setType(type);
                        w.setLanguage(lang);
                        ret.add(w);
                    }
                }
			}
		} finally {
			DAO.closeResultSet(rs);
			DAO.closeStatement(ps);
		}
		return ret;
	}

    public Collection<Word> checkUserKnow(Collection<Word> words, DictUser user, String prefix) throws SQLException {
        return checkUserKnow(ce, words, user, prefix);
    }

    public Collection<Word> checkUserKnow(Connection c, Collection<Word> words, DictUser user, String prefix) throws SQLException {
        String sql = "select inf_id, min(known) known from user_inf "
                + " where user_id = ? and inf_id = ?"
                + " group by inf_id";
        if (user.getWordClassUids() != null && user.getWordClassUids().length() > 0) {
            sql = "select inf_id, min(known) known from user_inf "
                    + " where user_id in (?,"+user.getWordClassUids()+") and inf_id = ? "
                    + " group by inf_id";
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement(sql);
            for (Word w : words) {
                ps.setInt(1, user.getId());
                ps.setLong(2, w.getId());
                rs = ps.executeQuery();
                if (rs.next()) {
                    w.setUserKnows(rs.getInt("known")==1);
                }
                DAO.closeResultSet(rs);
                ps.clearParameters();
            }
        } finally {
            DAO.closeStatement(ps);
        }
        return words;
    }

    private String getTrMapTable(Locale l1, Locale l2) {
		String ret = "";
		String lang1 = l1.getLanguage();
		String lang2 = l2.getLanguage();
		if (lang1.compareTo(lang2) < 0) {
			ret = lang1+"_"+lang2;
		} else {
			ret = lang2+"_"+lang1;
		}
		return ret;
	}
	
	/**
	 * implementation of TranslatorInterface#getTranslation
	 * @see org.linguisto.learn.db.Dictionary#getTranslation(java.util.List, java.util.Locale, java.util.Locale)
	 */
	public List<Word> getTranslation(
		List<Word> wList,
		Locale langFrom,
		Locale langTo) {
			List<Word> ret = new ArrayList<Word>();
			if (wList == null) return ret;
			if (wList.size() < 1) return ret;
			String sTrMapTable = getTrMapTable(langFrom, langTo);
			String sql = "select to_inf.* "
				+ " from "+langTo.getLanguage()+"_inf to_inf, " + sTrMapTable + " map "
				+ " where map." + langFrom.getLanguage() + "_id = ? and "
				+ " map." + langTo.getLanguage() + "_id = to_inf.id";
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				ps = ce.prepareStatement(sql);
				for (Word w : wList) {
					ps.clearParameters();
					ps.setLong(1, w.getId());
					rs = ps.executeQuery();
					while(rs.next()) {
						Word wTr = new Word(rs.getLong("id"), rs.getString("inf"));
						wTr.setType(rs.getInt("type"));
						ret.add(wTr);
					}
					DAO.closeResultSet(rs);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DAO.closeResultSet(rs);
				DAO.closeStatement(ps);
			}
			return ret;
	}

    public DictUser getUserByEMail(String eMail) {
        DictUser ret = null;
        String sql = "select * "
                + " from user "
                + " where email = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = ce.prepareStatement(sql);
            ps.clearParameters();
            ps.setString(1, eMail);
            rs = ps.executeQuery();
            if ( rs.next() ) {
                ret = new DictUser();
                ret.setId(rs.getInt("id"));
                ret.setName(rs.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DAO.closeResultSet(rs);
            DAO.closeStatement(ps);
        }
        return ret;
    }

    public DictUser getUserByName(String name) {
        DictUser ret = null;
        String sql = "select * "
                + " from user "
                + " where name = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = ce.prepareStatement(sql);
            ps.clearParameters();
            ps.setString(1, name);
            rs = ps.executeQuery();
            if ( rs.next() ) {
                ret = new DictUser();
                ret.setId(rs.getInt("id"));
                ret.setName(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DAO.closeResultSet(rs);
            DAO.closeStatement(ps);
        }
        return ret;
    }

    public List<String> getTranslation(
				Word word,
				Locale langFrom,
				Locale langTo) {
		List<String> ret = new ArrayList<String>();
        List<TranslationDictionary> trDicts = trDictsMap.get(langFrom.getLanguage());
		if (word == null || trDicts == null) return ret;

        for (TranslationDictionary dict : trDicts) {
            ret.addAll(dict.getTranslation(word, langFrom, langTo));
        }

        return ret;
	}
	
	public List<Word> getTranslation (
			String word,
			Locale langFrom,
			Locale langTo) {
		List<Word> ret = new ArrayList<Word>();
		if (word == null) return ret;
		//Translation
		String sTrMapTable = getTrMapTable(langFrom, langTo);
		String sql = "select distinct to_inf.* "
			+ " from " + langFrom.getLanguage()+"_inf from_inf, "
			+ langTo.getLanguage()+"_inf to_inf, " + sTrMapTable + " map "
			+ " where from_inf.inf = ? and "
			+ " map." + langFrom.getLanguage() + "_id = from_inf.id and "
			+ " map." + langTo.getLanguage() + "_id = to_inf.id";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = ce.prepareStatement(sql);
			ps.clearParameters();
			ps.setString(1, word);
			rs = ps.executeQuery();
			while(rs.next()) {
				Word wTr = new Word(rs.getLong("id"), rs.getString("inf"));
				wTr.setType(rs.getInt("type"));
				ret.add(wTr);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DAO.closeResultSet(rs);
			DAO.closeStatement(ps);
		}
		return ret;
	}

    public void addTranslationDictionary(String lang, TranslationDictionary dict) {
        if (trDictsMap == null) trDictsMap = new HashMap<String, List<TranslationDictionary>>();

        List<TranslationDictionary> trDicts = trDictsMap.get(lang);
        if (trDicts != null) {
            trDicts.add(dict);
        } else {
            trDicts = new ArrayList<TranslationDictionary>();
            trDicts.add(dict);
            trDictsMap.put(lang, trDicts);
        }
    }
}
