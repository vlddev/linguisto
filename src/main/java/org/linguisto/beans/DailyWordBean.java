package org.linguisto.beans;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.linguisto.db.obj.DictStats;
import org.linguisto.db.obj.Inf;
import org.linguisto.db.obj.Translation;

@ManagedBean(name="dailyword")
@ApplicationScoped
public class DailyWordBean extends AbstractDbBean {
 
	public static final Logger log = Logger.getLogger(DailyWordBean.class.getName());

	private static final long serialVersionUID = -7561381466848304781L;

	private Map<String, List<Inf>> mapRandomWords = new HashMap<String, List<Inf>>();
	private Map<String, List<Inf>> mapDailyWords = new HashMap<String, List<Inf>>();
	private Map<String, Boolean> mapDailyWordTooLong = new HashMap<String, Boolean>();

	private Map<String, DictStats> mapDictStats = new HashMap<String, DictStats>();
	private Map<String, String> mapAppStats = new HashMap<String, String>();

	private Date lastRead = null;
	
	public DailyWordBean() {
	}
	
	public void updateLastRead() {
		lastRead = new Date();
	}
	
	public boolean isItTimeToRead() {
		if (lastRead == null) return true;
		Calendar curCal = Calendar.getInstance();
		Calendar lrCal = Calendar.getInstance();
		lrCal.setTime(lastRead);
		boolean sameDay = (curCal.get(Calendar.YEAR) == lrCal.get(Calendar.YEAR) &&
				curCal.get(Calendar.DAY_OF_YEAR) == lrCal.get(Calendar.DAY_OF_YEAR));
		return !sameDay;
	}
	
	private void reload() {
		if (isItTimeToRead()) {
			SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
			if (lastRead != null) {
				log.info("It is time to reload daily stuff. [Last read: "+fmt.format(lastRead)+", now: "+fmt.format(new Date()));
			}
			mapRandomWords.clear();
			mapDailyWords.clear();
			mapDailyWordTooLong.clear();
			mapDictStats.clear();
			mapAppStats.clear();
			
			loadRandomWords("de");
			loadDailyWords("de", 25);
			loadDictStats("de");

			loadRandomWords("en");
			loadDailyWords("en", 25);
			loadDictStats("en");

			loadDictStats("fr");

			loadAppStats();
			updateLastRead();
		}
	}
	
	public List<Inf> getRandomWords(String langCode) {
		reload();
		List<Inf> ret = mapRandomWords.get(langCode);
		if (ret == null || ret.size() == 0) {
			log.warning("Empty randomWords for langCode = "+langCode);
			if (ret == null) {
				ret = new ArrayList<Inf>();
			}
		}
		return ret;
	}

	public List<Inf> getDailyWords(String langCode, int maxLineCount) {
		reload();
		List<Inf> ret = mapDailyWords.get(langCode);
		if (ret == null || ret.size() == 0) {
			log.warning("Empty dailyWords for langCode = "+langCode);
			if (ret == null) {
				ret = new ArrayList<Inf>();
			}
		}
		return ret;
	}
	
	public DictStats getDictStats(String langCode) {
		reload();
		DictStats ret = mapDictStats.get(langCode);
		if (ret == null) {
			log.warning("Empty DictStats for langCode = "+langCode);
			ret = new DictStats();
		}
		return ret;
	}
	
	public String getAppStats(String name) {
		reload();
		String ret = mapAppStats.get(name);
		if (ret == null) {
			log.warning("Empty AppStats for name = "+name);
			ret = "";
		}
		return ret;
	}

	private void loadRandomWords(String langCode) {
		log.fine("Init randomWords for langCode = "+langCode);
		List<Inf> ret = getDBManager().getRandomWords(new Locale(langCode));
		log.fine("randomWords inited with "+ret.size()+" records.");
		mapRandomWords.put(langCode, ret);
	}

	public void loadDailyWords(String langCode, int maxLineCount) {
		log.fine("Init dailyWords for langCode = "+langCode);
		List<Inf> ret = new ArrayList<Inf>();
		Inf filter = null;
		Locale lang = new Locale(langCode);
		if ("de".equals(langCode)) {
			filter = new Inf(getDBManager().getTodaysWord(lang), null);
		} else if ("en".equals(langCode)) {
			filter = new Inf(getDBManager().getTodaysWord(lang), null);
		}
		
		List<Inf> infList = getDBManager().findInfsWithTranslation(filter, lang, new Locale("uk"));
		log.fine("dailyWords inited with "+infList.size()+" records.");
		int lineCount = 0;
		int owerflowInd = -1;
		for (int i = 0; i < infList.size(); i++) {
			lineCount += infList.get(i).getArticleLineCount();
			if (lineCount > maxLineCount) {
				owerflowInd = i;
				break;
			}
		}
		if (lineCount > maxLineCount) {
			mapDailyWordTooLong.put(langCode, true);
			//cut the article
			if (owerflowInd == 0) {
				Inf inf = infList.get(0);
				int lineCnt = 2;
				int cutInd = -1;
				if (inf.getTrList() != null) {
					for (int i = 0; i < inf.getTrList().size(); i++) {
						Translation tr = inf.getTrList().get(i);
						if (tr != null) {
							lineCnt++;
							List<String> lst = tr.getExamples();
							if (lst != null) {
								lineCnt += lst.size();
							}
							if (lineCnt > maxLineCount) {
								cutInd = i;
								List<Translation> trList = new ArrayList<Translation>();
								for (int j = 0; j < cutInd; j++) {
									trList.add(inf.getTrList().get(j));
								}
								inf.setTrList(trList);
								break;
							}
						}
					}
				}
				ret.add(inf);
			} else if (owerflowInd > 0) {
				List<Inf> tmpList = new ArrayList<Inf>();
				for (int j = 0; j < owerflowInd; j++) {
					tmpList.add(infList.get(j));
				}
				ret = tmpList;

			}
		} else {
			ret = infList;
			mapDailyWordTooLong.put(langCode, false);
		}
		mapDailyWords.put(langCode, ret);
	}
	
	private void loadDictStats(String langCode) {
		mapDictStats.put(langCode, getDBManager().getDictStats(langCode, "uk"));
	}

	private void loadAppStats() {
		mapAppStats.put("users", ""+getDBManager().getUserCount());
	}
}
