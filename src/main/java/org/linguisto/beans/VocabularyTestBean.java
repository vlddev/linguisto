package org.linguisto.beans;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.tuple.Pair;
import org.linguisto.beans.user.LoginBean;
import org.linguisto.db.obj.TestWord;
import org.linguisto.utils.VocabularyTestUtil;
import org.primefaces.event.FlowEvent;

@ManagedBean(name="voctest")
@ViewScoped
public class VocabularyTestBean extends AbstractDbBean {
 
	private static final long serialVersionUID = 776678561027255486L;
	private final static int UK_MAX_RANK = 94000;
	private final static int MAX_RANK = 50000;
	private final static String[] fakeWords = {"дивиритись","водилавий","шалокайс","налилокс","дирулати",
			"димовіси","мозавискиба","коватанак","кезалінук","тобілинда"};

	//private static final Logger log = Logger.getLogger(VocabularyTestBean.class.getName());

	@ManagedProperty(value="#{login}")
	private LoginBean login;

	private String lang = "en";
	private boolean isNative = false;
	private String education;
	private Integer age;
	private List<TestWord> step1Words;
	private List<TestWord> step2Words;
	private long result;
	private int maxRank = MAX_RANK;
	private String testProtocol = ""; 
	private boolean hasKnownFakeWords = false;
	
	public VocabularyTestBean() {
	}

	public LoginBean getLogin() {
		return login;
	}

	public void setLogin(LoginBean login) {
		this.login = login;
	}
	
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public long getResult() {
		return result;
	}

	public String getTestProtocol() {
		return testProtocol;
	}

	public List<TestWord> getStep1Words() {
		return step1Words;
	}

	public List<TestWord> getStep2Words() {
		return step2Words;
	}

	public boolean isNative() {
		return isNative;
	}

	public void setNative(boolean isNative) {
		this.isNative = isNative;
	}

	public String getEducation() {
		return education;
	}

	public void setEducation(String education) {
		this.education = education;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	private void initStep1() {
		testProtocol = "";
		hasKnownFakeWords = false;
		if ("uk".equalsIgnoreCase(getLang())) {
			maxRank = UK_MAX_RANK;
		} else {
			maxRank = MAX_RANK;
		}
		VocabularyTestUtil test = new VocabularyTestUtil();
		List<Pair<Integer, Integer>> intervals = test.getProportionalIntervals(1,2000,18);
		intervals.addAll(test.getInverseProportionalIntervals(2000,7000,19));
		intervals.addAll(test.getProportionalIntervals(7000,20000,19));
		intervals.addAll(test.getProportionalIntervals(20000,maxRank,21));
		List<Integer> ranks = test.getRandomWordRanksFromIntervals(intervals);

		step1Words = getDBManager().getTestWordsByRank(ranks, getLang());
		
		//add random fake words
		if ("uk".equalsIgnoreCase(getLang())) {
			List<String> fakeList = Arrays.asList(fakeWords);
			Collections.shuffle(fakeList);
			for (int i = 0; i < 4; i++) {
				step1Words.add(new TestWord(fakeList.get(i),-1-i));
			}
		}
		Collections.shuffle(step1Words);
	}

	private void initStep2() {
		if (step1Words != null && step1Words.size() > 0) {
			//get first unknown
			//get last known
			int indFirstUnknown = -1;
			int indLastKnown = -1;
			testProtocol += "-------------------- STEP ONE -----------------<br/>";
			Collections.sort(step1Words);
			for (int i = 0; i < step1Words.size(); i++) {
				TestWord tw = step1Words.get(i);
				if (tw.getRank() > 0) { //ignore fake word
					testProtocol += tw.getRank()+": "+tw.getWord()+(tw.isKnown()?"":" - unknown");
					if (indFirstUnknown < 0 && !tw.isKnown()) {
						indFirstUnknown = i;
						testProtocol += "[ first unknown]";
					}
					if (tw.isKnown()) {
						indLastKnown = i;
					}
					testProtocol += "<br/>";
				} else {
					if (tw.isKnown()) {
						//known fake word -> ignore test result
						hasKnownFakeWords = true;
					}
				}
			}
			testProtocol += "-------------------------------------<br/>";
			int rankFrom = 1;
			int rankTo = 100;
			if (indLastKnown < 0) {
				//user knows nothing -> check [1,100)
			}
			if (indFirstUnknown > 0 && indFirstUnknown < indLastKnown) {
				//usual situation
				TestWord firstUnknownWord = step1Words.get(indFirstUnknown-1);
				if (firstUnknownWord.getRank() < 0) {
					rankFrom = 1;
				} else {
					rankFrom = step1Words.get(indFirstUnknown-1).getRank();
				}
				rankTo = step1Words.get(indLastKnown).getRank();
			} else if (indFirstUnknown > 0 && indFirstUnknown > indLastKnown) {
				TestWord firstUnknownWord = step1Words.get(indFirstUnknown-1);
				if (firstUnknownWord.getRank() < 0) {
					rankFrom = 1;
				} else {
					rankFrom = step1Words.get(indFirstUnknown-1).getRank();
				}
				rankTo = step1Words.get(indFirstUnknown).getRank();
			} else if (indFirstUnknown < 0) {
				//all words known
				rankFrom = step1Words.get(indLastKnown-2).getRank();
				rankTo = maxRank;
			}
			if (rankTo - rankFrom < 40) {
				rankTo = rankTo + 100;
				if (rankTo > maxRank) {
					rankTo = maxRank;
					rankFrom = rankFrom - 100;
				}
			}
			testProtocol += "rankFrom: "+rankFrom+"; rankTo: "+rankTo+"<br/>";
			VocabularyTestUtil test = new VocabularyTestUtil();
			List<Pair<Integer, Integer>> intervals = test.getProportionalIntervals(rankFrom,rankTo,38);
			List<Integer> ranks = test.getRandomWordRanksFromIntervals(intervals);

			step2Words = getDBManager().getTestWordsByRank(ranks, getLang());
			//add random fake words
			if ("uk".equalsIgnoreCase(getLang())) {
				List<String> fakeList = Arrays.asList(fakeWords);
				Collections.shuffle(fakeList);
				for (int i = 0; i < 2; i++) {
					step2Words.add(new TestWord(fakeList.get(i),-1-i));
				}
			}
			Collections.shuffle(step2Words);
		}
	}

	private void initResult() {
		if (step2Words != null) {
			//get first unknown
			//get last known
			long sumKnown = 0;
			int countKnown = 0;
			Collections.sort(step2Words);
			testProtocol += "-------------------- STEP TWO -----------------<br/>";
			for (int i = 0; i < step2Words.size(); i++) {
				TestWord tw = step2Words.get(i);
				if (tw.getRank() > 0) { //ignore fake word
					testProtocol += tw.getRank()+": "+tw.getWord()+(tw.isKnown()?"":" - unknown");
					if (tw.isKnown()) {
						countKnown++;
						sumKnown += tw.getRank();
					}
					testProtocol += "<br/>";
				} else {
					if (tw.isKnown()) {
						//known fake word -> ignore test result
						hasKnownFakeWords = true;
					}
				}
			}
			testProtocol += "-------------------------------------<br/>";
			if (countKnown > 0) {
				result = sumKnown/countKnown;
				testProtocol += "sumKnown: "+sumKnown+"; countKnown: "+countKnown+"; result = "+result+"<br/>";
				if (login.isLoggedIn()) {
					login.logUserAction("voctest", "user: "+login.getUserName()+", result="+result, getLang());
				} else {
					if (getAge() != null) {
						login.logUserAction("vocteststats", "fake="+hasKnownFakeWords+";native="+isNative()+";age="+getAge()+";education="+getEducation()+";result="+result, getLang());
					}
					login.logUserAction("voctest", "result="+result, getLang());
				}
			} else {
				result = 0;
			}
			testProtocol += "-------------------------------------<br/>";
		}
	}

	public String onFlowProcess(FlowEvent event) {
    	if ("step1".equals(event.getNewStep()) && "language".equals(event.getOldStep())) {
    		//init step1Words
    		initStep1();
    	}
    	if ("step2".equals(event.getNewStep())  && "step1".equals(event.getOldStep())) {
    		//init step2Words
    		initStep2();
    	}
    	if ("result".equals(event.getNewStep())   && "step2".equals(event.getOldStep())) {
    		//init result
    		initResult();
    	}
    	
        return event.getNewStep();
    }

}
