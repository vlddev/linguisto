package org.linguisto.beans;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LineChartModel;

import org.linguisto.beans.user.LoginBean;
import org.linguisto.db.obj.Inf;
import org.linguisto.db.obj.WordStats;

@ManagedBean(name="freqdict")
@ViewScoped
public class FreqDictBean extends AbstractDbBean {
 
	public static final Logger log = Logger.getLogger(FreqDictBean.class.getName());

	private static final long serialVersionUID = -7561381466848304780L;

    @ManagedProperty(value="#{login}")
    private LoginBean loginBean;

	private List<WordStats> foundWords = new ArrayList<WordStats>();
	private Map<String,WordStats> randomWordsMap = new HashMap<String, WordStats>();
	private String searchWord;
	private String preRendWord = null;
	private String searchResultMessage = "";
	private String langFrom = "uk";
	private String browserPos = "aa";
	
	private LineChartModel lineModel;

	public FreqDictBean() {
	}
	
	public String getSearchWord() {
		return searchWord;
	}

	public void setSearchWord(String searchWord) {
		this.searchWord = searchWord;
	}

	public String getPreRendWord() {
		return preRendWord;
	}

	public void setPreRendWord(String preRendWord) {
		this.preRendWord = preRendWord;
	}

	public List<WordStats> getFoundWords() {
		return foundWords;
	}

    public LineChartModel getLineModel(String word) {
        if (foundWords.size()>0) {
        	WordStats wordStats = null;
        	for (WordStats ws : foundWords) {
        		if(word.equals(ws.getWord())) {
        			wordStats = ws;
                	prepareLineModel(wordStats);
        			break;
        		}
        	}
        }
        return lineModel;
    }
    
    public LineChartModel getRandomLineModel(String id) {
    	WordStats wordStats = randomWordsMap.get(id);
    	if (wordStats == null) {
        	wordStats = getDBManager().getRandomWordStats(new Locale(langFrom));
        	randomWordsMap.put(id, wordStats);
    	}
    	prepareLineModel(wordStats);
        return lineModel;
    }

	private void prepareLineModel(WordStats wordStats) {
		if (wordStats == null) return;
		String internWord = wordStats.getWord().replace('\'', 'ʼ');
		if (lineModel != null && lineModel.getSeries().size() > 0 && 
				lineModel.getSeries().get(0).getLabel().equals(internWord)) {
			//model ready
			return;
		}
        LineChartModel model = new LineChartModel();
        
        ChartSeries serie = new ChartSeries();
        
        serie.setLabel(internWord); //replace ASCII-апостроф with &#700; (Літера-апостроф)
        List<String> years = new ArrayList<String>();
        years.addAll(wordStats.getRankMap().keySet());
        Collections.sort(years);
        int minVal = (int)Math.floor(wordStats.getRankMap().get(years.get(0)));
        int maxVal = minVal;
        for (String year : years) {
        	double val = wordStats.getRankMap().get(year);
        	int floorVal = (int)Math.floor(val);
        	int ceilVal = (int)Math.ceil(val);
            serie.set(year, val);
            if (ceilVal > maxVal) maxVal = ceilVal;
            if (floorVal < minVal) minVal = floorVal;
        }
 
        model.addSeries(serie);
        model.setTitle("вживання слова «"+internWord+"»");
        //model.setLegendPosition("nw"); //no legend
        Axis xAxis = model.getAxis(AxisType.X);
        xAxis.setLabel("© linguisto.eu");
        xAxis.setTickAngle(-50);
        xAxis.setMin(Integer.valueOf(years.get(0))-1);
        xAxis.setMax(Integer.valueOf(years.get(years.size()-1))+1);
        xAxis.setTickInterval("1");
        //model.getAxes().put(AxisType.X, xAxis);
        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setLabel("частота на млн. слів");
        yAxis.setMin(minVal>0?minVal-1:minVal);
        yAxis.setMax(maxVal);
        model.setResetAxesOnResize(false);

        lineModel = model;
	}

	public String getBrowserPos() {
		return browserPos;
	}

	public void setBrowserPos(String browserPos) {
		if (!isValidBrowserPrefix(browserPos)) {
			log.warning("BrowsePos '"+browserPos+"' contains unknown chars. Set browsePos to 'aa'.");
			this.browserPos = "aa";
		}
		if (browserPos.length() == 1) {
			this.browserPos = browserPos+"a";
		} else {
			this.browserPos = browserPos;
		}
	}

	public void search(String word) {
		search(word, langFrom);
	}

	
	public void preRenderSearch(String word) {
		if (getSearchWord() != null && getSearchWord().length() > 0)
			return;
		if (word != null && word.length() > 0) {
			search(word, langFrom);
		}
	}

	/**
	 * @param word
	 * @param langCode
	 */
	public void search(String word, String langCode) {
		if (word != null) {
			String searchWord = word.trim().toLowerCase();
			searchResultMessage = "";
			foundWords.clear();
			if (searchWord.length() < 1) {
				searchResultMessage = "Вкажіть слово для пошуку";
				return;
			}
			foundWords = getDBManager().findWordStats(searchWord, new Locale(langCode));
			if (foundWords.size() < 1) {
				searchResultMessage = "Нічого не знайдено";
				loginBean.logUserAction("emptyfreqsearch", searchWord, "uk");
			} else {
				loginBean.logUserAction("freqsearch", searchWord, "uk");
			}
		}
	}

	//--------- DictBrowser methods --------------
	public List<List<Map.Entry<String,String>>> getBrowserPrefixes() {
		List<List<Map.Entry<String,String>>> ret = new ArrayList<List<Map.Entry<String,String>>>();
		List<String> alfabet = Arrays.asList("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z");
		if (getLangFrom().equals("de")) {
			alfabet = Arrays.asList("a","ä","b","c","d","e","f","g","h","i","j","k","l","m","n","o","ö","p","q","r","s","t","u","ü","v","w","x","y","z");
		}
		List<Map.Entry<String,String>> level1 = new ArrayList<Map.Entry<String,String>>();
		List<Map.Entry<String,String>> level2 = new ArrayList<Map.Entry<String,String>>();
		for (String str : alfabet) {
			level1.add(new AbstractMap.SimpleEntry<String, String>(str, str));
			String strLevel2 = browserPos.charAt(0)+str;
			level2.add(new AbstractMap.SimpleEntry<String, String>(strLevel2, strLevel2));
		}
		ret.add(level1);
		ret.add(level2);
		return ret;
	}
	
	private boolean isValidBrowserPrefix(String prefix) {
		boolean ret = true;
		if (prefix != null && prefix.length() > 0) {
			List<String> alfabet = Arrays.asList("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z");
			if (getLangFrom().equals("de")) {
				alfabet = Arrays.asList("a","ä","b","c","d","e","f","g","h","i","j","k","l","m","n","o","ö","p","q","r","s","t","u","ü","v","w","x","y","z");
			}
			for (int i = 0; i < prefix.length(); i++) {
				if (!alfabet.contains(String.valueOf(prefix.charAt(i)))) {
					//unknown char -> exit
					ret = false;
					break;
				}
			}
		} else {
			ret = false;
		}
		return ret;
	}
	
	public List<Inf> getBrowserWords() {
		return getDBManager().findInfs(browserPos, new Locale(getLangFrom()));
	}
	//--------- end of DictBrowser methods --------------
	

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

	public String getLangFrom() {
		return langFrom;
	}

	public void setLangFrom(String langFrom) {
		this.langFrom = langFrom;
	}

	public String getSearchResultMessage() {
		return searchResultMessage;
	}

	public void setSearchResultMessage(String searchResultMessage) {
		this.searchResultMessage = searchResultMessage;
	}
}
