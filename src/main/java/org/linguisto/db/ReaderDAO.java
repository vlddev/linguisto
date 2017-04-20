package org.linguisto.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import org.linguisto.db.obj.ActionEntry;
import org.linguisto.db.obj.HistoryEntry;
import org.linguisto.db.obj.Inf;
import org.linguisto.db.obj.Publication;
import org.linguisto.db.obj.TestWord;
import org.linguisto.db.obj.Translation;
import org.linguisto.db.obj.WordStats;
import org.linguisto.db.obj.WordType;

public class ReaderDAO {
	
	public ReaderDAO() {
	}

	public WordType readWordType(ResultSet rs) throws SQLException {
		WordType ret = new WordType();
		ret.setId(rs.getInt("id"));
		ret.setDesc(rs.getString("desc"));
		ret.setComment(rs.getString("comment"));
		ret.inited();
		
		return ret;
	}

	public Inf readInf(ResultSet rs, Locale lang) throws SQLException {
		Inf ret = new Inf();
		ret.setId(rs.getInt("id"));
		ret.setInf(rs.getString("inf"));
		ret.setTranscription(rs.getString("transcription"));
		ret.setType(rs.getInt("type"));
		ret.setRank(rs.getInt("rank"));
		ret.setLanguage(lang.getLanguage());
		ret.inited();
		
		return ret;
	}
	
	public Translation readTranslation(ResultSet rs) throws SQLException {
		Translation ret = new Translation();
		ret.setId(rs.getInt("id"));
		ret.setOrderNr(rs.getInt("order_nr"));
		ret.setTranslation(rs.getString("translation"));
		ret.setExample(rs.getString("example"));
		ret.inited();
		
		return ret;
	}
	
	public TestWord readTestWord(ResultSet rs) throws SQLException {
		TestWord ret = new TestWord(rs.getString("word"), rs.getInt("rank"));
		return ret;
	}

	public HistoryEntry readHistoryEntry(ResultSet rs) throws SQLException {
		HistoryEntry ret = new HistoryEntry();
		ret.setId(rs.getInt("h_id"));
		ret.setInf(rs.getString("inf"));
		ret.setInfId(rs.getInt("inf_id"));
		ret.setVal(rs.getString("val"));
		ret.setUserName(rs.getString("u_name"));
		ret.setLangFrom(rs.getString("lang_from"));
		ret.setChDate(rs.getTimestamp("changed"));
		ret.inited();
		
		return ret;
	}

	public ActionEntry readActionEntry(ResultSet rs) throws SQLException {
		ActionEntry ret = new ActionEntry();
		ret.setIp(rs.getString("ip"));
		ret.setAgent(rs.getString("agent"));
		ret.setDefLang(rs.getString("def_lang"));
		ret.setAction(rs.getString("action"));
		ret.setaTime(rs.getTimestamp("atime"));
		ret.inited();
		
		return ret;
	}

	public WordStats readWordStats(ResultSet rs) throws SQLException {
		WordStats ret = new WordStats();
		ret.setWord(rs.getString("word"));
		for(int i = 1991; i < 2017; i++) {
			ret.setRank(""+i, rs.getDouble("cnt"+i));
		}
		ret.inited();
		
		return ret;
	}

	public Publication readPublication(ResultSet rs, boolean loadContent) throws SQLException {
		Publication ret = new Publication();
		ret.setId(rs.getInt("id"));
		ret.setLang(rs.getString("lang"));
		ret.setTitle(rs.getString("title"));
		ret.setHeader(rs.getString("header"));
		if (loadContent) {
			ret.setContent(rs.getString("content"));
		}
		ret.setAuthor(rs.getString("author"));
		ret.setPublishDate(rs.getDate("publish_date"));
		ret.inited();
		return ret;
	}

}
