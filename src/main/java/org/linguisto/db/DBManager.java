package org.linguisto.db;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.linguisto.db.obj.ActionEntry;
import org.linguisto.db.obj.DictStats;
import org.linguisto.db.obj.HistoryEntry;
import org.linguisto.db.obj.Inf;
import org.linguisto.db.obj.Publication;
import org.linguisto.db.obj.TestWord;
import org.linguisto.db.obj.Translation;
import org.linguisto.db.obj.User;
import org.linguisto.db.obj.WordStats;
import org.linguisto.db.obj.WordType;
import org.linguisto.learn.Builder;
import org.linguisto.learn.DictUser;
import org.linguisto.learn.Text;
import org.linguisto.learn.Word;
import org.linguisto.learn.db.DbDictionary;
import org.linguisto.learn.db.StDbTranslationDictionary2;
import org.linguisto.utils.DiffMatchPatch;
import org.linguisto.utils.DiffMatchPatch.Diff;

public class DBManager {

	public static final Logger log = Logger.getLogger(DBManager.class.getName());
	
	private DataSource ds;
	
	private DbDictionary dict;
	
	private int maxResultCount = 100;
	
	public DBManager(DataSource ds) {
		this.ds = ds;
		
		dict = new DbDictionary();
	    dict.addTranslationDictionary("en", new StDbTranslationDictionary2());
	    dict.addTranslationDictionary("de", new StDbTranslationDictionary2());
	}
	
	public Connection getConnection() throws SQLException {
		return ds.getConnection();
	}
	
	public DbDictionary getDict() {
		return dict;
	}

	public List<Inf> getRandomWords(Locale lang) {
		List<Inf> ret = new ArrayList<Inf>();

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			ReaderDAO dbReader = new ReaderDAO();
			
			String sql = "SELECT inf.id, inf.inf, inf.type, inf.transcription, inf.rank " +
					"  FROM inf inf " +
					"    INNER JOIN (SELECT id AS sid FROM inf WHERE lang = ? and type > 0  ORDER BY RAND() LIMIT 20 ) tmp on inf.id = tmp.sid";
			log.fine("getRandomWords() SQL: "+sql);
			ps = con.prepareStatement(sql);
			ps.setString(1, lang.getLanguage());

			ResultSet rs = ps.executeQuery();

			Inf curWord = null;
			while (rs.next()) {
				curWord = dbReader.readInf(rs, lang);
				ret.add(curWord);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public String getTodaysWord(Locale lang) {
		String ret = null;

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			String sql = "SELECT  " +
					"  (SELECT word FROM daily_word WHERE lang = ? AND day = DAYOFYEAR(CURDATE())) word, " +
					"  (SELECT word FROM daily_word WHERE lang = ? AND day = 0) default_word "+
					"  FROM dual";
			log.fine("getTodaysWord() SQL: "+sql);
			ps = con.prepareStatement(sql);
    		ps.setString(1, lang.getLanguage());
    		ps.setString(2, lang.getLanguage());

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getString(1);
				if (ret == null) {
					ret = rs.getString(2);
				}
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public String getText(String id, String langCode, String defaultLangCode) {
		String ret = null;

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			String sql = "SELECT  " +
					"  (SELECT val FROM site_text WHERE id = ? AND lang = ?) txt, " +
					"  (SELECT val FROM site_text WHERE id = ? AND lang = ?) default_txt "+
					"  FROM dual";
			log.fine("getText() SQL: "+sql);
			ps = con.prepareStatement(sql);
    		ps.setString(1, id);
    		ps.setString(2, langCode);
    		ps.setString(3, id);
    		ps.setString(4, defaultLangCode);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getString(1);
				if (ret == null) {
					ret = rs.getString(2);
				}
			} else {
				ret = "~"+id+"~"+langCode;
			}
			if (ret == null) {
				ret = "~"+id+"~"+langCode;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public List<Publication> getPublications() {
		List<Publication> ret = new ArrayList<Publication>();
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();
			String sql = "SELECT * FROM publications WHERE visible = 'y' ORDER by publish_date desc";
			log.fine("getPublications() SQL: "+sql);
			ps = con.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				ret.add(dbReader.readPublication(rs, false));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public Publication getPublication(int id) {
		Publication ret = null;

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();
			String sql = "SELECT * FROM publications WHERE id = ?";
			log.fine("getPublication() SQL: "+sql);
			ps = con.prepareStatement(sql);
    		ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ret = dbReader.readPublication(rs, true);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public List<Inf> getInfLikeInf(String inf, Locale lang) {
		List<Inf> ret = new ArrayList<Inf>();

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT inf.id, inf.inf, inf.type, inf.transcription, inf.rank " +
					"  FROM inf inf" +
					"  WHERE inf.inf like ? AND lang = ? ";

			log.fine("getInfLikeInf() SQL: "+sql);

			ps = con.prepareStatement(sql);
    		ps.setString(1, inf);
    		ps.setString(2, lang.getLanguage());

			ResultSet rs = ps.executeQuery();

			Inf curWord = null;
			while (rs.next() && ret.size() < maxResultCount) {
				curWord = dbReader.readInf(rs, lang);
				ret.add(curWord);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public List<Inf> getInfByFrequency(Locale lang, int resultCount) {
		List<Inf> ret = new ArrayList<Inf>();

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT inf.id, inf.inf, inf.type, '' transcription " +
					"  FROM " + lang.getLanguage()+"_inf inf " +
					"  JOIN " + lang.getLanguage()+"_frequency freq on inf.inf = freq.word" +
					"  ORDER BY freq.rank, inf.type ";

			log.fine("getInfLikeInf() SQL: "+sql);

			ps = con.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			Inf curWord = null;
			while (rs.next() && ret.size() < resultCount) {
				curWord = dbReader.readInf(rs, lang);
				ret.add(curWord);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public Inf getInfWithTranslationById(Integer id, Locale langFrom, Locale langTo) {
		Inf ret = null;

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT inf.id, inf.inf, inf.type, inf.transcription, inf.rank " +
					"  FROM inf " +
					"  WHERE inf.id = ? and inf.lang = ? ";

			log.fine("getInfById() SQL: "+sql);

			ps = con.prepareStatement(sql);
    		ps.setInt(1, id);
    		ps.setString(2, langFrom.getLanguage());

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ret = dbReader.readInf(rs, langFrom);
				if (StringUtils.indexOfAny(langFrom.getLanguage(), "en", "de", "fr") > -1) {
					ret.setTrList(getTrList(ret, langFrom, langTo));
				}
			} else {
				//word with this id is not found
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public List<Inf> getTranslations(Inf word, Locale langFrom, Locale langTo) {
		List<Inf> ret = new ArrayList<Inf>();

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT toinf.id, toinf.inf, toinf.type " +
					"  FROM " + langFrom.getLanguage()+"_"+langTo.getLanguage()+" map, " +
					"       " + langTo.getLanguage()+"_inf toinf " +
					"  WHERE map."+langFrom.getLanguage()+"_id = ? AND " +
					"        toinf.id = map."+langTo.getLanguage()+"_id and map.del = 0";

			log.fine("getTranslations() SQL: "+sql);

			ps = con.prepareStatement(sql);
    		ps.setInt(1, word.getId());

			ResultSet rs = ps.executeQuery();

			Inf curWord = null;
			while (rs.next()) {
				curWord = dbReader.readInf(rs, langTo);
				ret.add(curWord);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public List<Translation> getTrList(Inf word, Locale langFrom, Locale langTo) {
		List<Translation> ret = new ArrayList<Translation>();

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT tr.id, tr.order_nr, tr.translation translation, tr.example example " +
					"  FROM tr " +
					"  WHERE tr.fk_inf = ? and tr.tr_lang = ? ORDER BY tr.order_nr ";

			log.fine("getTrList() SQL: "+sql);

			ps = con.prepareStatement(sql);
    		ps.setInt(1, word.getId());
    		ps.setString(2, langTo.getLanguage());

			ResultSet rs = ps.executeQuery();

			Translation tr = null;
			while (rs.next()) {
				tr = dbReader.readTranslation(rs);
				tr.setInfFrom(word);
				ret.add(tr);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public List<Inf> findInfsWithTranslationByWf(String wordForm, Locale langFrom, Locale langTo) {
		List<Inf> ret = new ArrayList<Inf>();

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT distinct inf.id, inf.inf, inf.type, inf.transcription, inf.rank " +
					"  FROM inf " +
					"  JOIN " + langFrom.getLanguage()+"_wf wf on inf.id = wf.fk_inf " +
					"  WHERE inf.lang = ? AND lower(wf.wf) like lower(?) ";

			log.fine("findInfsWithTranslationByWf() SQL: "+sql);

			ps = con.prepareStatement(sql);
    		ps.setString(1, langFrom.getLanguage());
    		ps.setString(2, wordForm);
			ResultSet rs = ps.executeQuery();

			Inf curWord = null;
			while (rs.next() && ret.size() < maxResultCount) {
				curWord = dbReader.readInf(rs, langFrom);
				if ("en".equals(langFrom.getLanguage())) {
					curWord.setTrList(getTrList(curWord, langFrom, langTo));
				} else if ("de".equals(langFrom.getLanguage())) {
					curWord.setTrList(getTrList(curWord, langFrom, langTo));
				}
				ret.add(curWord);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public List<Inf> findInfs(String prefix, Locale langFrom) {
		List<Inf> ret = new ArrayList<Inf>();

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT inf.id, inf.inf, inf.type, inf.transcription, inf.rank " +
					"  FROM inf " +
					" WHERE lang = ? AND lower(inf.inf) like ?";

			log.fine("findInfs() SQL: "+sql);

			ps = con.prepareStatement(sql);
    		ps.setString(1, langFrom.getLanguage());
    		ps.setString(2, prefix+"%");

			ResultSet rs = ps.executeQuery();

			Inf curWord = null;
			while (rs.next()) {
				curWord = dbReader.readInf(rs, langFrom);
				ret.add(curWord);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public WordStats getRandomWordStats(Locale langFrom) {
		WordStats ret = null;

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT * FROM stats_yearly where cnt2016 > 5 order by rand() limit 1";

			log.fine("getRandomWordStats() SQL: "+sql);

			ps = con.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ret = dbReader.readWordStats(rs);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public List<WordStats> findWordStats(String pattern, Locale langFrom) {
		List<WordStats> ret = new ArrayList<WordStats>();

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT * " +
					"  FROM stats_yearly " +
					" WHERE lower(word) like ?";

			log.fine("findWordStats() SQL: "+sql);

			ps = con.prepareStatement(sql);

			List<String> searchList = new ArrayList<String>();
			if (pattern.contains(",")) {
				searchList.addAll(Arrays.asList(pattern.split(",")));
			} else {
				searchList.add(pattern);
			}
			
			for (String str : searchList) {
				ps.setString(1, str);

				ResultSet rs = ps.executeQuery();

				WordStats curWord = null;
				while (rs.next()) {
					curWord = dbReader.readWordStats(rs);
					ret.add(curWord);
				}
				rs.close();
				ps.clearParameters();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public List<Inf> findInfsWithTranslation(Inf filter, Locale langFrom, Locale langTo) {
		List<Inf> ret = new ArrayList<Inf>();

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT inf.id, inf.inf, inf.type, inf.transcription, inf.rank " +
					"  FROM inf " +
					"  WHERE lang = ? ";

	    	if (filter.getType() != null && filter.getType() != 0) {
	    		sql+=" and inf.type = " + filter.getType();
	    	}
			if (filter.getInf() != null && filter.getInf().length() > 0) {
	    		sql+=" and lower(inf.inf) like lower(?) ";
			}
			if (filter.getType() == null && filter.getInf() == null ) {
	    		sql+=" and 0 ";
			}
			log.fine("findInfsWithTranslation() SQL: "+sql);

			ps = con.prepareStatement(sql);
    		ps.setString(1, langFrom.getLanguage());
			
			if (filter.getInf() != null && filter.getInf().length() > 0) {
	    		ps.setString(2, filter.getInf());
			}

			ResultSet rs = ps.executeQuery();

			Inf curWord = null;
			while (rs.next() && ret.size() < maxResultCount) {
				curWord = dbReader.readInf(rs, langFrom);
				curWord.setTrList(getTrList(curWord, langFrom, langTo));
				ret.add(curWord);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public List<Inf> findInfsWithTranslationByTr(String trword, Locale langFrom, Locale langTo) {
		List<Inf> ret = new ArrayList<Inf>();

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT distinct inf.id, inf.inf, inf.type, inf.transcription, inf.rank " +
					"  FROM inf " +
					"  JOIN tr on inf.id = tr.fk_inf " +
					" WHERE inf.lang = ? AND lower(tr.translation) REGEXP ?";

			ps = con.prepareStatement(sql);
    		ps.setString(1, langFrom.getLanguage());
    		ps.setString(2, "[[:<:]]"+trword.toLowerCase()+"[[:>:]]");

			ResultSet rs = ps.executeQuery();

			Inf curWord = null;
			while (rs.next() && ret.size() < maxResultCount) {
				curWord = dbReader.readInf(rs, langFrom);
				curWord.setTrList(getTrList(curWord, langFrom, langTo));
				ret.add(curWord);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

    public void updateInf(Inf inf) {
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			con.setAutoCommit(false);
			
			String sql = "UPDATE inf " +
					" SET inf = ?, type = ?, transcription = ? WHERE id = ? ";
			log.fine("updateInf() SQL: "+sql);

			ps = con.prepareStatement(sql);
			ps.setString(1, inf.getInf());
			ps.setInt(2, inf.getType());
			ps.setString(3, inf.getTranscription());
			ps.setInt(4, inf.getId());

			ps.executeUpdate();
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
    }

//=========== Actions ===================
    
	public List<ActionEntry> getActionHistory(long limit) {
		List<ActionEntry> ret = new ArrayList<ActionEntry>();
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT * " + 
					"FROM `sessions` s " +
					"WHERE action in ('search','emptysearch','emptyrevsearch','revsearch','freqsearch','emptyfreqsearch','voctest') " +
					"ORDER BY `atime` desc LIMIT " + limit;
			log.fine("getActionHistory() SQL: "+sql);

			ps = con.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			ActionEntry cur = null;
			while (rs.next()) {
				cur = dbReader.readActionEntry(rs);
				ret.add(cur);
			}
			rs.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

    public void insertSessionAction(String sessionId, String sessionIp, String agent, Locale defLocale, String action) {
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			con.setAutoCommit(false);
			
			String sql = "INSERT INTO sessions (id, ip, agent, def_lang, action)" +
					" values ("
					+ " SUBSTRING(?,1,50), "
					+ " SUBSTRING(?,1,50), "
					+ " SUBSTRING(?,1,100), "
					+ " SUBSTRING(?,1,30), "
					+ " SUBSTRING(?,1,30))";
			log.fine("insertSessionAction() SQL: "+sql);

			ps = con.prepareStatement(sql);
			ps.setString(1, sessionId);
			ps.setString(2, sessionIp);
			ps.setString(3, agent);
			ps.setString(4, defLocale==null?null:defLocale.toString());
			ps.setString(5, action);

			ps.executeUpdate();
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
    }

	public DictStats getDictStats(String langFrom, String langTo) {
		DictStats ret = new DictStats();
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ret.setStat(DictStats.WORD_COUNT, ""+DBUtil.exeLongQuery(con, "SELECT count(id) cnt FROM inf WHERE lang = '"+langFrom+"'"));
			ret.setStat(DictStats.TRANSLATION_COUNT, ""+DBUtil.exeLongQuery(con, "SELECT count(tr.id) cnt FROM tr, inf " +
					" WHERE tr.fk_inf = inf.id AND inf.lang = '"+langFrom+"' AND tr.tr_lang = '"+langTo+"'"));

			ret.setStat(DictStats.EXAMPLE_COUNT, ""+DBUtil.exeLongQuery(con, "select sum(LENGTH(tr.example) - LENGTH(REPLACE(tr.example, '|', ''))+1) cnt FROM tr, inf " +
					" WHERE tr.fk_inf = inf.id AND inf.lang = '"+langFrom+"' AND tr.tr_lang = '"+langTo+"' AND example is not null"));

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		
		return ret;
	}
	
	private String getSalt() {
		return "change_this";
	}

	public Long getUserCount() {
		Connection con = null;
		Long ret = null;
		try {
			con = ds.getConnection();
			ret = DBUtil.exeLongQuery(con, "SELECT count(id) cnt FROM user WHERE confirmed=1");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeConnection(con);
		}
		
		return ret;
	}

	public User getUser(String uName, String uPwd) {
		User ret = null;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			String hash = getPwdHash(uPwd);
			ps = con.prepareStatement(
				"SELECT u.id, u.name, u.confirmed, u.email " +
				"  FROM user u" +
				"  WHERE u.name = ? and u.pwd = ?");
			ps.setString(1, uName);
			ps.setString(2, hash);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ret = readUser(rs);
			}
			rs.close();

			if (ret != null) {
				ps.close();
				ps = con.prepareStatement(
					"SELECT uwc.lang, GROUP_CONCAT(uwc.wcl_uid) as wcl_uids " +
					"  FROM user_word_class uwc" +
					"  WHERE uwc.uid = ?" +
					"  GROUP BY uwc.lang");
				ps.setInt(1, ret.getId());

				rs = ps.executeQuery();
				while (rs.next()) {
					ret.putWordClassUids(rs.getString("lang"), rs.getString("wcl_uids"));
				}
				rs.close();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}
	
	private User readUser(ResultSet rs) throws SQLException {
		User ret = new User(rs.getInt("id"), rs.getString("name"));
		ret.setEmail(rs.getString("email"));
		ret.setConfirmed(rs.getBoolean("confirmed"));
		return ret;
	}

	public User getUserByName(String uName) {
		User ret = null;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			ps = con.prepareStatement(
				"SELECT u.id, u.name, u.confirmed, u.email " +
				"  FROM user u" +
				"  WHERE u.name = ?");
			ps.setString(1, uName);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ret = readUser(rs);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public User getUserByEmail(String email) {
		User ret = null;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			ps = con.prepareStatement(
				"SELECT u.id, u.name, u.confirmed, u.email " +
				"  FROM user u" +
				"  WHERE u.email = ?");
			ps.setString(1, email);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ret = readUser(rs);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public User getUserByResetId(String resetId) {
		User ret = null;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			ps = con.prepareStatement(
				"SELECT u.id, u.name, u.confirmed, u.email " +
				"  FROM user u, user_request ur" +
				"  WHERE u.id = ur.uid and ur.hash = ? and ur.expire > NOW()");
			ps.setString(1, resetId);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ret = readUser(rs);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}
	

	public boolean hasUser(String uName) {
		boolean ret = false;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			ps = con.prepareStatement(
				"SELECT u.id, u.name " +
				"  FROM user u" +
				"  WHERE u.name = ?");
			ps.setString(1, uName);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ret = true;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public boolean hasEmail(String eMail) {
		boolean ret = false;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			ps = con.prepareStatement(
				"SELECT u.id, u.name " +
				"  FROM user u" +
				"  WHERE u.email = ?");
			ps.setString(1, eMail);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ret = true;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	private String getPwdHash(String pwd) {
		String hash = null;
		String saltedPwd = getSalt() + pwd;
		hash = md5(saltedPwd);
		return hash;
	}
	
    public String md5(String s) {
        try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			BigInteger bigInt = new BigInteger(1,md.digest(s.getBytes("UTF-8")));
			String hash = bigInt.toString(16);
			// zero pad to 32 chars.
			while(hash.length() < 32 ){
			  hash = "0"+hash;
			}			
            return hash;         
        } catch (NoSuchAlgorithmException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
        return null;
    }

	
	public boolean createUser(String uName, String uPwd, String eMail, String confirmId) {
		boolean ret = false;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			con.setAutoCommit(false);
			
			String hash = getPwdHash(uPwd);
			ps = con.prepareStatement(
				"INSERT INTO user(name, pwd, email, confirm_id, confirmed) " +
				"  VALUES (?,?,?,?,?) ");
			ps.setString(1, uName);
			ps.setString(2, hash);
			ps.setString(3, eMail);
			ps.setString(4, confirmId);
			ps.setBoolean(5, false);

			int insCount = ps.executeUpdate();

			if (insCount == 1) {
				con.commit();
				ret = true;
			} else {
				con.rollback();
			}
		} catch (SQLException e) {
			DBUtil.rollback(con);
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
		
	}

	public boolean createUserRequest(int uid, String requestType, String requestId, int expireInHours) {
		boolean ret = false;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			con.setAutoCommit(false);
			
			ps = con.prepareStatement(
				"INSERT INTO user_request(uid, type, hash, expire) " +
				"  VALUES (?,?,?,NOW()+INTERVAL "+expireInHours+" HOUR) ");
			ps.setInt(1, uid);
			ps.setString(2, requestType);
			ps.setString(3, requestId);

			int insCount = ps.executeUpdate();

			if (insCount == 1) {
				con.commit();
				ret = true;
			} else {
				con.rollback();
			}
		} catch (SQLException e) {
			DBUtil.rollback(con);
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}
	
	public boolean deleteUserRequest(int uid, String requestType) {
		boolean ret = false;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			con.setAutoCommit(false);
			
			ps = con.prepareStatement(
				"DELETE FROM user_request " +
				"  WHERE uid = ? AND type = ?");
			ps.setInt(1, uid);
			ps.setString(2, requestType);

			int insCount = ps.executeUpdate();

			if (insCount > 0) {
				con.commit();
				ret = true;
			} else {
				con.rollback();
			}
		} catch (SQLException e) {
			DBUtil.rollback(con);
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public User confirmRegistration(String confirmId) {
		User ret = null;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			con.setAutoCommit(false);
			
			ps = con.prepareStatement(
				"SELECT u.id, u.name " +
				"  FROM user u" +
				"  WHERE u.confirm_id = ? and confirmed = 0");
			ps.setString(1, confirmId);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ret = new User(rs.getInt("id"), rs.getString("name"));
			}
			rs.close();
			
			if (ret != null) {
				DBUtil.closeStatement(ps);
				//mark user as confirmed
				ps = con.prepareStatement("UPDATE user SET confirmed = 1 WHERE confirmed = 0 and id = ?");
				ps.setInt(1, ret.getId());
				ps.executeUpdate();
				con.commit();
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
		
	}

	public boolean changePwd(User user, String newPwd) {
		boolean ret = false;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			con.setAutoCommit(false);
			
			String hash = getPwdHash(newPwd);
			
			ps = con.prepareStatement("UPDATE user SET pwd = ? WHERE id = ?");
			ps.setString(1, hash);
			ps.setInt(2, user.getId());

			int insCount = ps.executeUpdate();

			if (insCount == 1) {
				con.commit();
				ret = true;
			} else {
				con.rollback();
			}
		} catch (SQLException e) {
			DBUtil.rollback(con);
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public boolean changeUserEmail(User user, String newEmail) {
		boolean ret = false;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			con.setAutoCommit(false);
			
			ps = con.prepareStatement("UPDATE user SET email = ? WHERE id = ?");
			ps.setString(1, newEmail);
			ps.setInt(2, user.getId());

			int insCount = ps.executeUpdate();

			if (insCount == 1) {
				con.commit();
				ret = true;
			} else {
				con.rollback();
			}
		} catch (SQLException e) {
			DBUtil.rollback(con);
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	//============================== History =========================
	
	public List<HistoryEntry> getDictHistory(long limit) {
		List<HistoryEntry> ret = new ArrayList<HistoryEntry>();
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT distinct 1 h_id, h.inf_id, h.`changed`, u.name u_name, i.inf, i.lang lang_from, '' val " + 
					"FROM `history` h, user u, inf i " +
					"WHERE h.inf_id = i.id AND h.uid = u.id " +
					"ORDER BY `changed` desc, h_id desc LIMIT " + limit;
			log.fine("getDictHistory() SQL: "+sql);

			ps = con.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			HistoryEntry cur = null;
			while (rs.next()) {
				cur = dbReader.readHistoryEntry(rs);
				ret.add(cur);
			}
			rs.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public List<HistoryEntry> getWordHistory(String lang, int id, int limit) {
		List<HistoryEntry> ret = new ArrayList<HistoryEntry>();
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT h.id h_id, h.inf_id, h.`changed`, u.name u_name, i.inf, i.lang lang_from, h.val " + 
					"FROM `history` h, user u, inf i " +
					"WHERE h.inf_id = i.id AND h.uid = u.id AND i.id = ? AND i.lang = ? " +
					"ORDER BY `changed` desc, h_id desc LIMIT " + limit;
			log.fine("getWordHistory() SQL: "+sql);

			ps = con.prepareStatement(sql);
			ps.setInt(1, id);
			ps.setString(2, lang);

			ResultSet rs = ps.executeQuery();

			//use DiffMatchPatch to fill diffs
			DiffMatchPatch diffTool = new DiffMatchPatch();
			HistoryEntry cur = null;
			HistoryEntry prev = null;
			while (rs.next()) {
				prev = cur;
				cur = dbReader.readHistoryEntry(rs);
				if (prev != null) {
					LinkedList<Diff> diffs = diffTool.diff_main(cur.getVal(), prev.getVal());
					diffTool.diff_cleanupSemantic(diffs);
					String html = diffTool.diff_prettyHtml(diffs);
					prev.setDiff(html);
				}
				ret.add(cur);
			}
			rs.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	public List<WordType> getAllWordTypes(Locale lang) {
		List<WordType> ret = new ArrayList<WordType>();

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = ds.getConnection();
			
			ReaderDAO dbReader = new ReaderDAO();

			String sql = "SELECT * " +
					"  FROM word_type wt " +
					"  WHERE lang = ? " +
					"  ORDER BY id ";
			log.fine("getAllWordTypes() SQL: "+sql);

			ps = con.prepareStatement(sql);
			ps.setString(1, lang.getLanguage());

			ResultSet rs = ps.executeQuery();

			WordType cur = null;
			while (rs.next()) {
				cur = dbReader.readWordType(rs);
				ret.add(cur);
			}
			rs.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
		return ret;
	}

	/* MANAGEMENT OF user known words
	 */
    public List<String> getUserWords(User user, String lang) throws SQLException {
        List<String> ret = new ArrayList<String>();
        String sql = "select distinct w.inf from user_inf uw, inf w"
                + " WHERE uw.user_id = ? and w.lang = ? and uw.inf_id = w.id and uw.known = 1 "
                + " order by 1 ";
        if (user.getWordClassUids().get(lang) != null && user.getWordClassUids().get(lang).length() > 0) {
            sql = "select w.inf from user_inf uw, inf w"
                    + " where uw.user_id in (?,"+user.getWordClassUids().get(lang)+") and w.lang = ? and uw.inf_id = w.id"
                    + " group by  w.inf, uw.inf_id"
                    + " having min(uw.known) = 1"
            		+ " order by 1";
        }

		Connection con = null;
		PreparedStatement ps = null;
        ResultSet rs = null;
        try {
			con = ds.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, user.getId());
            ps.setString(2, lang);
            rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(rs.getString(1));
            }
            rs.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeStatement(ps);
			DBUtil.closeConnection(con);
		}
        return ret;
    }

    public void setUserKnows(User user, String lang, List<Word> words) throws SQLException {
		Connection c = null;
        PreparedStatement psSelect = null;
        PreparedStatement psInsert = null;
        PreparedStatement psDelete = null;
        boolean bAutocommit = false;
		String sql = "select user_id, known from user_inf"
                + " where user_id = ? and inf_id = ?";
        if (user.getWordClassUids().get(lang) != null && user.getWordClassUids().get(lang).length() > 0) {
			sql = "select user_id, known from user_inf"
                    + " where user_id in (?,"+user.getWordClassUids().get(lang)+") and inf_id = ?";
        }
        String sqlInsert = "insert into user_inf (user_id, inf_id, known) values (?,?,?)";
        String sqlDelete = "delete from user_inf where user_id = ? and inf_id = ?";
		try {
			c = ds.getConnection();
	        bAutocommit = c.getAutoCommit();
            if (bAutocommit) {
                c.setAutoCommit(false);
            }
            psSelect = c.prepareStatement(sql);
            psInsert = c.prepareStatement(sqlInsert);
            psDelete = c.prepareStatement(sqlDelete);
            for (Word w : words) {
            	psSelect.setInt(1, user.getId());
            	psSelect.setLong(2, w.getId());
            	ResultSet rs = psSelect.executeQuery();
            	HashMap<Integer,Boolean> dbState = new HashMap<Integer,Boolean>(2);
            	StringBuffer sbDbStates = new StringBuffer();
                while (rs.next()) {
                	dbState.put(rs.getInt(1), rs.getBoolean(2));
                	sbDbStates.append(rs.getInt(1)).append(",").append(rs.getBoolean(2)).append("; ");
                }
                rs.close();

                if (dbState.size() > 2) { //Error
        			log.log(Level.SEVERE, "Error. DB-State has more then 2 rows: ["+sbDbStates.toString()+"]. ");
                } else if (dbState.size() == 2) {
                	Integer subUserId = null;
                	for (Integer uid : dbState.keySet()) {
                		if (uid != user.getId()) {
                			subUserId = uid;
                		}
                	}
                	if (subUserId != null) {
                		if ( dbState.containsKey(user.getId()) && dbState.containsKey(subUserId) &&
                				dbState.get(subUserId) && !dbState.get(user.getId()) ) {
                        	if (w.isUserKnows()) { //in DB stored that sub_user knows and user override it to not knows -> delete overriding
	                			psDelete.setInt(1, user.getId());
	                			psDelete.setLong(2, w.getId());
	                			psDelete.addBatch();
                        	}
                		} else { // Error
                			log.log(Level.SEVERE, "Error 2-row DB-State: ["+sbDbStates.toString()+
                					"]. The only possible 2-row DB-State: [<sub_user>,true; <user>,false;]");
                		}
                	} else { //Error
            			log.log(Level.SEVERE, "SubUserId was not found. DB-State: ["+sbDbStates.toString()+"]");
                	}
                } else if (dbState.size() == 1) {
                	if (!w.isUserKnows()) {
                		if (dbState.containsKey(user.getId()) && dbState.get(user.getId())) { //in DB stored that user knows the word -> delete it
                			psDelete.setInt(1, user.getId());
                			psDelete.setLong(2, w.getId());
                			psDelete.addBatch();
                		} else {
                			Integer subUserId = dbState.keySet().iterator().next();
                			if (dbState.get(subUserId)) { //in DB stored that user inherits knowledge of the word -> override it
                        		psInsert.setInt(1, user.getId());
                        		psInsert.setLong(2, w.getId());
                        		psInsert.setBoolean(3, false);
                        		psInsert.addBatch();
                			} else { //Error
                    			log.log(Level.SEVERE, "Error 1-row DB-State: ["+sbDbStates.toString()+
                    					"]. Possible 1-row DB-States: [<sub_user>,true] or [<user>,true]");
                			}
                		}
                	} else {
                		// this part is ignored.
                		// If everything works correctly there must not be error situations.
                		
                		// 1. sub_user.knows == 1 -> Nothing to do
                		// 2. user.knows == 1 -> Nothing to do
                		// else -> Error
                	}
                } else if (dbState.size() == 0) {
                	if (w.isUserKnows()) {
                		psInsert.setInt(1, user.getId());
                		psInsert.setLong(2, w.getId());
                		psInsert.setBoolean(3, true);
                		psInsert.addBatch();
                	}
                }
            }
            psInsert.executeBatch();
            psDelete.executeBatch();
            c.commit();
		} catch (SQLException e) {
			DBUtil.rollback(c);
			log.log(Level.SEVERE, e.getMessage(), e);
            throw e;
		} finally {
        	DBUtil.closeStatement(psSelect);
        	DBUtil.closeStatement(psInsert);
        	DBUtil.closeStatement(psDelete);
            if (bAutocommit) {
                c.setAutoCommit(true);
            }
			DBUtil.closeConnection(c);
		}
    }
    
    public List<Word> getUserProposedWords(Locale lang, User user, Integer limit) throws SQLException {
        List<Word> ret = new ArrayList<Word>();
		Connection c = null;
        String sql = "SELECT distinct inf.* FROM inf " +
                "WHERE inf.lang = ? AND rank is not null " +
                "   AND inf.id not in (" +
        		"      SELECT inf_id FROM user_inf u" +
        		"      WHERE u.user_id = ?" +
        		"      GROUP BY u.inf_id" +
        		"      HAVING min(u.known) = 1" +
        		"   )" +
                "ORDER by rank LIMIT 0,"+limit;
        if (user.getWordClassUids().get(lang.getLanguage()) != null && user.getWordClassUids().get(lang.getLanguage()).length() > 0) {
            sql = "SELECT distinct inf.* FROM inf " +
                    "WHERE inf.lang = ? AND rank is not null " +
                    "   AND inf.id not in (" +
            		"      SELECT inf_id FROM user_inf u" +
            		"      WHERE u.user_id in (?," + user.getWordClassUids().get(lang.getLanguage()) + ")" +
            		"      GROUP BY u.inf_id" +
            		"      HAVING min(u.known) = 1" +
            		"   )" +
                    "ORDER by rank LIMIT 0,"+limit;
        }


        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
			c = ds.getConnection();
            ps = c.prepareStatement(sql);
            ps.setString(1, lang.getLanguage());
            ps.setInt(2, user.getId());
            rs = ps.executeQuery();
            while(rs.next()) {
                Word w = new Word(rs.getLong("id"), rs.getString("inf"));
                w.setType(rs.getInt("type"));
                w.setLanguage(lang.getLanguage());
                ret.add(w);
            }
        } finally {
        	DBUtil.closeResultSet(rs);
        	DBUtil.closeStatement(ps);
			DBUtil.closeConnection(c);
        }
        return ret;
    }

    
	/** 
	 * Prepare text for the user. 
	 * Calls ling2 library.
	 */  
    public Text makeText(String text, Locale langFrom, Locale langTo, User user) {
    	Text ret = null;
		Connection con = null;
		try {
			DictUser dictUser = new DictUser();
			if (user == null) {
				dictUser.setId(-1);
				dictUser.setName("dummy");
			} else {
				dictUser.setId(user.getId());
				dictUser.setName(user.getName());
				dictUser.setWordClassUids(user.getWordClassUids().get(langFrom.getLanguage()));
			}
			con = ds.getConnection();
			DbDictionary makerDict = new DbDictionary(con);
			makerDict.addTranslationDictionary("en", new StDbTranslationDictionary2(con));
			makerDict.addTranslationDictionary("de", new StDbTranslationDictionary2(con));
			ret = Builder.makeText(text, langFrom, langTo, makerDict, dictUser);
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeConnection(con);
		}
		return ret;
    }

    /* ========= Methods for TestWords ============== */
    public List<TestWord> getTestWordsByRank(List<Integer> ranks, String lang) {
        List<TestWord> ret = new ArrayList<TestWord>();
		Connection c = null;
        String sql = "SELECT * FROM "+lang+"_frequency fr " +
                "WHERE fr.rank = ?";

        PreparedStatement ps = null;
        ResultSet rs = null;
        ReaderDAO dbReader = new ReaderDAO();
        try {
			c = ds.getConnection();
            ps = c.prepareStatement(sql);
            for (Integer rank : ranks) {
                ps.setInt(1, rank);
                rs = ps.executeQuery();
                if(rs.next()) {
                	TestWord w = dbReader.readTestWord(rs);
                    ret.add(w);
                } else {
        			log.log(Level.WARNING, "There is no word with rank "+rank+" for language '"+lang+"'");
                }
                rs.close();
            }
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
        } finally {
        	DBUtil.closeResultSet(rs);
        	DBUtil.closeStatement(ps);
			DBUtil.closeConnection(c);
        }
        return ret;
    }
    
	/* ========= Methods from DbDictionary ============== */
	public List<Word> getBaseForm(String wf, Locale lang, boolean ignoreCase) {
		List<Word> ret = null;
		Connection con = null;
		try {
			con = ds.getConnection();
			ret = getDict().getBaseForm(con, wf, lang, ignoreCase);
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeConnection(con);
		}
		return ret;
	}
	
    public List<Word> checkUserKnow(List<Word> words, User user, String lang) throws SQLException {
		List<Word> ret = null;
		Connection con = null;
		try {
			con = ds.getConnection();
			ret = new ArrayList<Word>();
			DictUser dictUser = new DictUser();
			dictUser.setId(user.getId());
			dictUser.setName(user.getName());
			dictUser.setWordClassUids(user.getWordClassUids().get(lang));
			ret.addAll(getDict().checkUserKnow(con, words, dictUser, lang+"_"));
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DBUtil.closeConnection(con);
		}
		return ret;
    }
}
