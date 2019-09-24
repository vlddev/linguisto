package org.linguisto.learn;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.linguisto.learn.db.Dictionary;
import org.linguisto.utils.CountHashtable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/** Input text. Contains set of Sentences. 
 */
public class TextPOS {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String DIVIDER_CHARS = ".,:;!?§$%&/()=[]\\#+*<>{}\"—…«»“”•~^‹› \t\r\n";

    /**
     * FIND_WORD_IGNORE_CASE: convert word from text to lower case
     *                    and convert words in DB to lower case
     */
    public static final int FIND_WORD_IGNORE_CASE = 0;
    /**
     * FIND_WORD_AS_IS: no converting, match all as is.
     */
    public static final int FIND_WORD_AS_IS = 1;
    /**
     * FIND_WORD_GERMAN: match ignoring case:
     *                     1) first word in sentence
     *                     2) words not matching german word pattern (all letters lowercase (Ex. arbeiten), first letter uppercase the rest lowercase (Ex. Arbeit)).
     *                   All the rest match as is.
     */
    public static final int FIND_WORD_GERMAN = 2;

    public static final Logger log = Logger.getLogger(TextPOS.class.getName());

    private Dictionary dict;

    private Locale lang;
    private Locale targetLang;
    private String content;

    private MaxentTagger posTagger;
    private EnPOSTagSet enPOSTagSet = new EnPOSTagSet();

    //Sentences
    List<SentencePOS> sentences;

    //number of words in text
    int wCount;
    //number of recognized words in text
    int totalRecWCount;
    //number of distinct recognized words in text
    int foundWords;
    //number of known words in the set of different recognized words
    int knownWords;

    //distinct words
    CountHashtable<String> distWords;

    //unrecognized words
    HashSet<String> unrecognizedWords = new HashSet<String>();

    // words without translation
    HashMap<Long, Word> notTranslated = new HashMap<Long, Word>();

    //mapping of word-forms to word-bases
    HashMap<String, Collection<Word>> wfMap = new HashMap<String, Collection<Word>>();

    //dictionary for this text
    HashMap<Word, List<String>> textDict = new HashMap<Word, List<String>>();

    //fb2 notes for this text
    HashMap<String, String> fb2Notes = new HashMap<String, String>();

    int noteCounter = 1;

    /** Constructor for texts
     */
    public TextPOS(String content, Locale lang, Dictionary d, MaxentTagger posTagger) throws IOException{
    	this(content, lang, "utf-8", d, posTagger);
    }

    /** Constructor for texts
     */
    public TextPOS(String content, Locale lang, String encoding, Dictionary d, MaxentTagger posTagger) throws IOException{
    	if(d==null){
	        throw new NullPointerException("Dictionary is null");
    	}
        this.dict = d;
        this.lang = lang;
        this.content = content;
        this.posTagger = posTagger;

        //init object
        init();
    }

    public List<SentencePOS> getSentences() {
        return sentences;
    }

    public HashMap<Word, List<String>> getTextDict() {
        return textDict;
    }

    /**
     */
    private void init() throws IOException {
        long start = System.currentTimeMillis();
        // remove first BOM in utf8
        if (content.length() > 0) {
            byte[] bomArr = content.substring(0, 1).getBytes();
            if (bomArr.length == 3 && bomArr[0] == (byte)0xEF && bomArr[1] == (byte)0xBB && bomArr[2] == (byte)0xBF) {
                //BOM in utf8
                content = content.substring(1);
            }
        }

        sentences = new ArrayList<SentencePOS>();

        //1. replace "..." with "…"
        content = content.replace("...","…");
        //2. replace "’" with "'"
        content = content.replace("’","'");

        SentenceReader2 sr = new SentenceReader2(content);
        try {
            String str = sr.readSentence();
            SentencePOS sent;
            int prevSentPos = 0;
            boolean newParagraph = false;
            boolean prevSentEndsWithEmptyLine = false;
            boolean sentEndsWithEmptyLine = false;
            while (str != null) {
                newParagraph = sentences.size() == 0 || prevSentEndsWithEmptyLine;
                sentEndsWithEmptyLine = str.endsWith("\n") || str.endsWith("\r\n");
                str = str.replace(LINE_SEPARATOR," ").trim();
                if (str.length() > 0) {
                    sent = new SentencePOS(str, posTagger);
                    sent.setStartPosInText(prevSentPos);
                    sent.setId(sentences.size());
                    if (newParagraph) {
                        sent.setNewParagraph(true);
                    }
                    sentences.add(sent);
                    prevSentPos += sent.getElemList().size();
                } else {
                    sentEndsWithEmptyLine = true;
                }
                str = sr.readSentence();
                prevSentEndsWithEmptyLine = sentEndsWithEmptyLine;
                //TODO: check words count in sentence.
                // If no words, add string to the previous sentence
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.info("Text.init() took "+(System.currentTimeMillis() - start)+" ms.");
    }
    
    /**
     * prepare dictionary for this text
     */
    public void prepareDict(Locale targetLang, DictUser user, int wordSearchMode){
        long start = System.currentTimeMillis();
        this.targetLang = targetLang;
        distWords =  getWordUsageStats(wordSearchMode);
        if (distWords == null) {
            log.warning("Text.distWord is Null");
            return;
        } else {
            log.info("Text contains "+ distWords.size()+" distinct words.");
        }
        
        for(String word : distWords.keySet()) {
            try {
                //System.out.println(word);
                HashSet<Word> searchRes = new HashSet<Word>();
                switch(wordSearchMode) {
                    case TextPOS.FIND_WORD_AS_IS:
                        searchRes.addAll(dict.getBaseForm(word, lang, false));
                    case TextPOS.FIND_WORD_IGNORE_CASE:
                        searchRes.addAll(dict.getBaseForm(word.toLowerCase(), lang, false));
                        searchRes.addAll(dict.getBaseForm(word, lang, false));
                        break;
                    case TextPOS.FIND_WORD_GERMAN:
                        searchRes.addAll(dict.getBaseForm(word, lang, false));
                        if (searchRes.size() == 0) {
                            searchRes.addAll(dict.getBaseForm(word.toLowerCase(), lang, false));
                        }
                        break;
                }
                if (user != null) {
                    dict.checkUserKnow(searchRes, user, lang.getLanguage()+"_");
                }
                if(searchRes != null && searchRes.size() > 0) {
                    wfMap.put(word, searchRes);
                    for(Word w : searchRes) {
                        List<String> translations = dict.getTranslation(w, lang, targetLang);
                        if (translations != null && translations.size() > 0) {
                            textDict.put(w, translations);
                        }
                    }
                } else {
                    unrecognizedWords.add(word);
                }
                //if(nCount%100 == 0) LogUtil.writeLog("another 100");
            } catch(Exception e){
                log.log(Level.SEVERE, "Error processing word '" + word + "'", e);
            }
        }
        log.info("Text.init() took "+(System.currentTimeMillis() - start)+" ms.");
    }

    public CountHashtable<String> getWordUsageStats(boolean bIgnoreCase) {
        CountHashtable<String> ret = new CountHashtable<String>();
        StringTokenizer st = new StringTokenizer(content, DIVIDER_CHARS);
        String s;
        while (st.hasMoreTokens()) {
            s = st.nextToken();
            if (bIgnoreCase) {
                s = s.toLowerCase();
            }
            if(!s.equals("")) {
                ret.add(s);
            }
        }
        return ret;
    }

    public CountHashtable<String> getWordUsageStats(int wordSearchMode) {
        CountHashtable<String> ret = new CountHashtable<String>();
        String s;
        for (SentencePOS sent : getSentences()) {
            for (int i = 0; i < sent.getElemList().size(); i++) {
                s = sent.getElemList().get(i);
                if (s.length() > 0) {
                    switch(wordSearchMode) {
                        case TextPOS.FIND_WORD_AS_IS:
                            ret.add(s);
                        case TextPOS.FIND_WORD_IGNORE_CASE:
                            ret.add(s.toLowerCase());
                            if (!s.toLowerCase().equals(s)) {
                                ret.add(s);
                            }
                            break;
                        case TextPOS.FIND_WORD_GERMAN:
                            //TODO: check german word pattern (all letters lowercase (Ex. arbeiten), first letter uppercase the rest lowercase (Ex. Arbeit))
                            // words not matching german word pattern process ignoring case
                            if (i == 0) {
                                ret.add(s.toLowerCase());
                                if (!s.toLowerCase().equals(s)) {
                                    ret.add(s);
                                }
                            } else {
                                ret.add(s);
                            }
                            break;
                    }
                }
            }
        }
        return ret;
    }

	public String getUnrecognizedWordUsageStats() {
        StringBuilder ret = new StringBuilder();
        List<Pair<String, Integer>> lstOut = new ArrayList<Pair<String, Integer>>();
        for (String word : unrecognizedWords) {
        	lstOut.add(new ImmutablePair<String, Integer>(word, distWords.get(word)));
        }
        Collections.sort(lstOut, new Comparator<Pair<String, Integer>>() {
            @Override
            public int compare(final Pair<String, Integer> o1, final Pair<String, Integer> o2) {
                int ret = (o1.getValue().compareTo(o2.getValue()));
                if (ret == 0) {
                	ret = (o1.getKey().compareTo(o2.getKey()));
                }
                return ret*-1;
            }
        });
        for (Pair<String, Integer> pair : lstOut) {
            ret.append(pair.getKey()).append("\t").append(pair.getValue()).append("\n");
        }
        return ret.toString();
    }

    public String getUntranslatedWords() {
        StringBuilder ret = new StringBuilder();
        List<String> lstOut = new ArrayList<String>();
        for (Word w : notTranslated.values()) {
        	lstOut.add(w.getInf()+"\t"+w.getType());
        }
        Collections.sort(lstOut);
        for (String str : lstOut) {
            ret.append(str).append("\n");
        }
        return ret.toString();
    }

    public String getUnknownWords() {
        StringBuilder ret = new StringBuilder();

        List<String> unknownWords = new ArrayList<String>();
        for (Word w : textDict.keySet()) {
            if (!w.isUserKnows() && !unknownWords.contains(w.getInf()))
                unknownWords.add(w.getInf());
        }
        Collections.sort(unknownWords);

        for (String word : unknownWords) {
            ret.append(word).append("\n");
        }

        return ret.toString();
    }


    public String getHtml(boolean completeHtmlPage) {
        StringBuilder sbRet = new StringBuilder();
        //header of html file
        if (completeHtmlPage) {
            sbRet.append("<html><head><meta charset=\"utf-8\">");
        }
        sbRet.append("<link rel=\"stylesheet\" href=\"http://code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css\">");
        sbRet.append("<script src=\"http://code.jquery.com/jquery-1.10.2.js\"></script>");
        sbRet.append("<script src=\"http://code.jquery.com/ui/1.10.4/jquery-ui.js\"></script>");
        sbRet.append("<script>$(function () {\n" +
                "      $(document).tooltip({\n" +
                "          content: function () {\n" +
                "              return $(this).prop('title');\n" +
                "          }\n" +
                "      });\n" +
                "  });</script>");
        sbRet.append("<style>\n" +
                "  .tooltip {\n" +
                "    display:inline-block;\n" +
                "    font-size:12px;\n" +
                "    color:#eee;\n" +
                "  }\n");
        if (completeHtmlPage) {
            sbRet.append("  .dict {\n" +
                "    font-weight: bold;\n" +
                "    border-bottom: 1px black dotted;\n" +
                "  }\n"
            );
        }
        sbRet.append("</style>");
        if (completeHtmlPage) {
            sbRet.append("</head><body>");
        }

        int sentInd = 0;
        for(SentencePOS sent : sentences){
            if (sent.isNewParagraph()) {
                if (sentInd>0) {
                    sbRet.append("</p><p>");
                } else {
                    sbRet.append("<p>");
                }
            }
            sbRet.append(sent.getHtml(this));
            sentInd++;
        }
        sbRet.append("</p>");
        if (completeHtmlPage) {
            sbRet.append("</body></html>");
        }
        return sbRet.toString();
    }

    public String getLinguistoHtml(String linkBase) {
        StringBuilder sbRet = new StringBuilder();
        //header of html file
        sbRet.append("<html><head><meta charset=\"utf-8\">");
        sbRet.append("<base href=\"http://linguisto.eu/\">");
        sbRet.append("<link rel=\"stylesheet\" href=\"http://code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css\">");
        sbRet.append("<script src=\"http://code.jquery.com/jquery-1.10.2.js\"></script>");
        sbRet.append("<script src=\"http://code.jquery.com/ui/1.10.4/jquery-ui.js\"></script>");
        sbRet.append("<script>$(function () {\n" +
                "      $(document).tooltip({\n" +
                "          content: function () {\n" +
                "              return $(this).prop('title');\n" +
                "          }\n" +
                "      });\n" +
                "  });</script>");
        sbRet.append("<style>\n" +
                "  .tooltip {\n" +
                "    display:inline-block;\n" +
                "    font-size:12px;\n" +
                "    color:#eee;\n" +
                "  }\n");
        sbRet.append("  .dict {\n" +
            "    font-weight: bold;\n" +
            "    border-bottom: 1px black dotted;\n" +
            "  }\n"
        );
        sbRet.append("</style>");
        sbRet.append("</head><body>");

        int sentInd = 0;
        for(SentencePOS sent : sentences){
            if (sent.isNewParagraph()) {
                if (sentInd>0) {
                    sbRet.append("</p><p>");
                } else {
                    sbRet.append("<p>");
                }
            }
            sbRet.append(sent.getLinguistoHtml(this, linkBase));
            sbRet.append("\n");
            sentInd++;
        }
        sbRet.append("</p>");
        sbRet.append("</body></html>");

        return sbRet.toString();
    }

    public String getFb2(int wordSearchMode) {
        StringBuilder sbRet = new StringBuilder();
        //header of fb2 file
        sbRet.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sbRet.append("<FictionBook xmlns=\"http://www.gribuser.ru/xml/fictionbook/2.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
        sbRet.append("\n<description><title-info><lang>"+lang.getLanguage()+"</lang></title-info></description>");
        sbRet.append("\n<body>\n<section>");

        int sentInd = 0;
        for(SentencePOS sent : sentences){
            if (sent.isNewParagraph()) {
                if (sentInd>0) {
                    sbRet.append("</p>\n<p>");
                } else {
                    sbRet.append("\n<p>");
                }
            }
            sbRet.append(sent.getFb2(this, wordSearchMode));
            sentInd++;
        }
        sbRet.append("</p></section></body>");

        // user dictionary
        sbRet.append("\n<body name=\"notes\">");
        sbRet.append("<title><p>Notes</p></title>");

        //sort dict alphabetically
        List<String> dictNotes = new ArrayList<String>(fb2Notes.keySet());
        Collections.sort(dictNotes);

        for (String key : dictNotes) {
            sbRet.append("\n<section id=\"").append(key).append("\">");
            sbRet.append("<title><p>").append(key).append("</p></title>");
            sbRet.append(fb2Notes.get(key));
            sbRet.append("</section>");
        }

        sbRet.append("</body></FictionBook>");
        return sbRet.toString();
    }
    
    public String escapeXml(String str) {
    	return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); //.replace('\a', '')
    }

    /** Return HTML representation of this word.
     */
    public String getWordHtml(String word){
        StringBuilder sbHtml = new StringBuilder();
        Collection<Word> wordBases = wfMap.get(word);
        if(wordBases == null || wordBases.isEmpty()) { //unrecognized word -> search lowercase
            wordBases = wfMap.get(word.toLowerCase());
        }
        
        if(wordBases == null || wordBases.isEmpty()) { //unrecognized word
            sbHtml.append("<span class=\"unrec\">");
            sbHtml.append(word);
            sbHtml.append("</span>");
        } else { // recognized
            boolean bUserKnows = true;
            for (Word w : wordBases) {
                bUserKnows &= w.isUserKnows();
                if (!bUserKnows) break;
            }
            if(bUserKnows){ //user knows this word
                if (wordBases.size() == 1) {
                    int wordType = wordBases.iterator().next().getType();
                    if (wordType == 2 || wordType == 3 || wordType == 4) {
                        sbHtml.append("<span class=\"known").append(wordType).append("\">")
                                .append(word).append("</span>");
                    } else {
                        sbHtml.append(word);
                    }
                } else {
                    sbHtml.append(word);
                }
            }else { //add all unknown words
                StringBuilder dictArticle = new StringBuilder();
                List<String> dictArticles = new ArrayList<String>();
                for (Word w : wordBases) {
                    if (!w.isUserKnows()) {
                        for (String s : getDictionary().getTranslation(w, lang, targetLang)) {
                            if (!dictArticles.contains(s)) {
                                dictArticles.add(s);
                            }
                        }
                    }
                }
                for (String s : dictArticles) {
                    dictArticle.append("<p>");
                    dictArticle.append(s);
                    dictArticle.append("</p>");
                }
                if (wordBases.size() == 1) {
                    int wordType = wordBases.iterator().next().getType();
                    if (wordType == 2 || wordType == 3 || wordType == 4) {
                        sbHtml.append("<span class=\"dict").append(wordType).append("\" title=\"").append(dictArticle).append("\">");
                    } else {
                        sbHtml.append("<span class=\"dict\" title=\"").append(dictArticle).append("\">");
                    }
                } else {
                    sbHtml.append("<span class=\"dict\" title=\"").append(dictArticle).append("\">");
                }
                sbHtml.append(word);
                sbHtml.append("</span>");
            }
        }
        return sbHtml.toString();
    }

    /** Return HTML representation of this word.
     */
    public String getWordLinguistoHtml(String word, String linkBase){
        StringBuilder sbHtml = new StringBuilder();
        Collection<Word> wordBases = wfMap.get(word);
        if(wordBases == null || wordBases.isEmpty()) { //unrecognized word -> search lowercase
            wordBases = wfMap.get(word.toLowerCase());
        }

        if(wordBases == null || wordBases.isEmpty()) { //unrecognized word
            sbHtml.append("<span class=\"unrec\">");
            sbHtml.append(word);
            sbHtml.append("</span>");
        } else { // recognized
            boolean bUserKnows = true;
            for (Word w : wordBases) {
                bUserKnows &= w.isUserKnows();
                if (!bUserKnows) break;
            }
            if(bUserKnows){ //user knows this word
                if (wordBases.size() == 1) {
                    int wordType = wordBases.iterator().next().getType();
                    if (wordType == 2 || wordType == 3 || wordType == 4) {
                        sbHtml.append("<span class=\"known").append(wordType).append("\">")
                                .append(word).append("</span>");
                    } else {
                        sbHtml.append(word);
                    }
                } else {
                    sbHtml.append(word);
                }
            }else { //add all unknown words
                StringBuilder dictArticle = new StringBuilder();
                List<String> dictArticles = new ArrayList<String>();
                for (Word w : wordBases) {
                    if (!w.isUserKnows()) {
                        for (String s : getDictionary().getTranslation(w, lang, targetLang)) {
                            if (!dictArticles.contains(s)) {
                                dictArticles.add(s);
                            }
                        }
                    }
                }
                for (String s : dictArticles) {
                    dictArticle.append("<p>");
                    dictArticle.append(s);
                    dictArticle.append("</p>");
                }
                if (wordBases.size() == 1) {
                    int wordType = wordBases.iterator().next().getType();
                    if (wordType == 2 || wordType == 3 || wordType == 4) {
                        sbHtml.append("<span class=\"dict").append(wordType).append("\" title=\"").append(dictArticle).append("\">");
                    } else {
                        sbHtml.append("<span class=\"dict\" title=\"").append(dictArticle).append("\">");
                    }
                } else {
                    sbHtml.append("<span class=\"dict\" title=\"").append(dictArticle).append("\">");
                }
                String urlWord = word;
                if ("de".equals(lang.getLanguage())) { //encode url
                    try {
                    	urlWord = URLEncoder.encode(word, "ISO-8859-1");
    				} catch (UnsupportedEncodingException e) {
    					// ignore
    				}
                }
                sbHtml.append("<a href=\"").append(linkBase).append(urlWord).append("\">").append(word).append("</a>");
                sbHtml.append("</span>");
            }
        }
        return sbHtml.toString();
    }

    /** Return Fb2 representation of this word.
     */
    public String getWordFb2(String word, String posTag, int wordSearchMode){
        StringBuilder sb = new StringBuilder();
        String searchWord = word;
        Collection<Word> wordBases = null;
        switch(wordSearchMode) {
            case TextPOS.FIND_WORD_IGNORE_CASE:
                searchWord = word.toLowerCase();
                wordBases = wfMap.get(searchWord);
                break;
            case TextPOS.FIND_WORD_AS_IS:
            case TextPOS.FIND_WORD_GERMAN:
            default:
                wordBases = wfMap.get(word);
                break;
        }

        if(wordBases == null || wordBases.isEmpty()) { //unrecognized word
            if (wordSearchMode == TextPOS.FIND_WORD_GERMAN) {
                // ignore case if nothing found
                sb.append(getWordFb2(word, "", TextPOS.FIND_WORD_IGNORE_CASE));
            } else {
                sb.append(word);
            }
        } else { // recognized
            if (posTag != null && posTag.length() > 0) {
                if (lang.getLanguage().equals("en")) {
                    String strWordTypes = EnPOSTagSet.getWordType(posTag);
                    if (strWordTypes.length() > 0) {
                        Integer[] wordTypes = convertStrToInt(strWordTypes);
                        Collection<Word> filteredWordBases = filterWordBases(wordBases, wordTypes);
                        if (filteredWordBases.size() > 0) {
                            wordBases = filteredWordBases;
                        } else {
                            log.info(String.format("Ignore POSTag. There was no WordBases for word '%s', wordTypes '%s' (POSTag '%s')", word, strWordTypes, posTag));
                        }
                    }
                }
            }
            boolean bUserKnows = true;
            for (Word w : wordBases) {
                bUserKnows &= w.isUserKnows();
                if (!bUserKnows) break;
            }
            if(bUserKnows){ //user knows this word
                if (wordBases.size() == 1) {
                    int wordType = wordBases.iterator().next().getType();
                    if (lang.getLanguage().equals("de")) {
                        if (wordType == 2 || wordType == 3 || wordType == 4) { //German noun
                        	switch (wordType) {
                        	case 2:
                                sb.append(word).append("<sup>m</sup>");
                                break;
                        	case 3:
                                sb.append(word).append("<sup>f</sup>");
                                break;
                        	case 4:
                                sb.append(word).append("<sup>n</sup>");
                                break;
                        	}
                        } else {
                            sb.append(word);
                        }
                    } else {
                        sb.append(word);
                    }
                } else {
                    sb.append(word);
                }
            } else { //add all unknown words
                boolean wordHasNote = false;
                String wordNoteLink = searchWord;
                if (wordBases.size() == 1) {
                    Word wordBase = wordBases.iterator().next();
                    wordNoteLink += wordBase.getInf()+"_" + wordBase.getType();
                }
                if (!fb2Notes.containsKey(wordNoteLink)) { //generate note
                    List<String> dictArticles = new ArrayList<String>();
                    for (Word w : wordBases) {
                        if (!w.isUserKnows()) {
                            StringBuilder sbDictArticle = new StringBuilder();
                            if (textDict.containsKey(w)) {
                                for (String s : textDict.get(w)) {
                                    if (s != null && s.length() > 0) {
                                        if (sbDictArticle.length() > 0) {
                                            sbDictArticle.append(", ");
                                        }
                                        sbDictArticle.append(s);
                                    }
                                }
                            } else {
                                if (!notTranslated.containsKey(w.getId())) {
                                    notTranslated.put(w.getId(),w);
                                }
                            }
                            if (sbDictArticle.length() > 0) {
                                if (word.equalsIgnoreCase(w.inf) && wordBases.size() == 1) {
                                    dictArticles.add(" - "+sbDictArticle.toString());
                                } else {
                                    dictArticles.add("<strong>"+w.inf+"</strong> - "+sbDictArticle.toString());
                                }
                            }
                        }
                    }
                    StringBuilder dictArticle = new StringBuilder();
                    for (String s : dictArticles) {
                        dictArticle.append("<p>");
                        dictArticle.append(s);
                        dictArticle.append("</p>");
                    }

                    if (dictArticle.toString().trim().length() > 0) {
                        fb2Notes.put(wordNoteLink, dictArticle.toString());
                        wordHasNote = true;
                    }
                } else {
                    wordHasNote = true;
                }
                sb.append(word);
                boolean addNoteLink = false;
                if (wordBases.size() == 1) {
                    int wordType = wordBases.iterator().next().getType();
                    if (lang.getLanguage().equals("de")) {
                        if (wordType == 2 || wordType == 3 || wordType == 4) { //German noun
                        	switch (wordType) {
                        	case 2:
                                sb.append("<sup>m</sup>");
                                break;
                        	case 3:
                                sb.append("<sup>f</sup>");
                                break;
                        	case 4:
                                sb.append("<sup>n</sup>");
                                break;
                        	}
                            addNoteLink = true;
                        } else {
                            addNoteLink = true;
                        }
                    } else {
                        addNoteLink = true;
                    }
                } else {
                    addNoteLink = true;
                }
                if (wordHasNote && addNoteLink) {
                    sb.append(" <a l:href=\"#" + (wordNoteLink) + "\" type=\"note\">*</a>");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Converts a string containing comma separated list of integers to array
     * @param str - comma separated list of integers
     */
    private Integer[] convertStrToInt(String str) {
        return Arrays.stream(str.split(","))
                .map(Integer::valueOf)
                .toArray(Integer[]::new);
    }

    private Collection<Word> filterWordBases(Collection<Word> words, Integer[] wordTypes) {
        return words.stream()
                .filter(word -> ArrayUtils.contains(wordTypes, word.getType()))
                .collect(Collectors.toList());
    }

    public TChunk wordToChunk(String word, String posTag, boolean ignoreCase) {
        TChunk ret = new TChunk(word);
        Collection<Word> wordBases = wfMap.get(ignoreCase?word.toLowerCase():word);
        if(wordBases == null || wordBases.isEmpty()) { //unrecognized word
            ret.setClazz(TChunk.CLASS_UNREC);
        } else { // recognized
            if (posTag != null && posTag.length() > 0) {
                if (lang.getLanguage().equals("en")) {
                    String strWordTypes = EnPOSTagSet.getWordType(posTag);
                    if (strWordTypes.length() > 0) {
                        Integer[] wordTypes = convertStrToInt(strWordTypes);
                        Collection<Word> filteredWordBases = filterWordBases(wordBases, wordTypes);
                        if (filteredWordBases.size() > 0) {
                            wordBases = filteredWordBases;
                        } else {
                            log.info(String.format("Ignore POSTag. There was no WordBases for word '%s', wordTypes '%s' (POSTag '%s')", word, strWordTypes, posTag));
                        }
                    }
                }
            }
            boolean bUserKnows = true;
            for (Word w : wordBases) {
                bUserKnows &= w.isUserKnows();
                if (!bUserKnows) break;
            }
            if(bUserKnows){ //user knows this word
                if (wordBases.size() == 1) {
                    if (lang.getLanguage().equals("de")) {
                        int wordType = wordBases.iterator().next().getType();
                        if (wordType == 2 || wordType == 3 || wordType == 4) {
                            ret.setClazz(TChunk.CLASS_KNOWN+wordType);
                        }
                    }
                }
            }else { //add all unknown words
                //StringBuffer dictArticle = new StringBuffer();
                List<String> dictArticles = new ArrayList<String>();
                for (Word w : wordBases) {
                    if (!w.isUserKnows()) {
                        if (textDict.containsKey(w)) {
                            for (String s : textDict.get(w)) {
                                if (s != null && s.length() > 0) {
                                    if (!dictArticles.contains(s)) {
                                        dictArticles.add(s);
                                    }
                                }
                            }
                        } else {
                            if (!notTranslated.containsKey(w.getId())) {
                                notTranslated.put(w.getId(),w);
                            }
                        }
                    }
                }
                if (dictArticles.size() > 0) {
                    ret.setHints(dictArticles);
                }
                if (wordBases.size() == 1) {
                    int wordType = wordBases.iterator().next().getType();
                    if (wordType == 2 || wordType == 3 || wordType == 4) {
                        if (lang.getLanguage().equals("de")) {
                            ret.setClazz(TChunk.CLASS_DICT+wordType);
                        }
                    } else {
                        ret.setClazz(TChunk.CLASS_DICT);
                    }
                } else {
                    ret.setClazz(TChunk.CLASS_DICT);
                }
            }
        }
        return ret;
    }

    public List<TChunk> getAsChunks(boolean ignoreCase) {
        List<TChunk> ret = new ArrayList<TChunk>();
        int sentInd = 0;
        for(SentencePOS sent : sentences){
            if (sent.isNewParagraph()) {
                if (sentInd>0) {
                    // TChunk paragraph "</p><p>"
                    //TChunk chunk = new TChunk("</p><p>");
                    TChunk chunk = new TChunk("<br/><br/>");
                    chunk.setId(-1);
                    ret.add(chunk);
                } else {
                    // TChunk paragraph "<p>"
                    //TChunk chunk = new TChunk("<p>");
                    TChunk chunk = new TChunk("<br/><br/>");
                    chunk.setId(-1);
                    ret.add(chunk);
                }
            }
            ret.addAll(sent.getAsChunks(this, ignoreCase));
            sentInd++;
        }
        // TChunk paragraph "</p>"
//        TChunk chunk = new TChunk("</p>");
//        chunk.setId(-1);
//        ret.add(chunk);
        return ret;
    }

    /**
     * Returns statistics on last processed test
     */
    public Dictionary getDictionary(){
        return dict;
    }
    
    /** Return number of words in this text.
     *  Use this method after compile()
     */
    public int getWordCount(){
        return wCount;
    }
    
    /** Return number of recognized words in this text.
     *  Use this method after getHtmlForm()
     */
    public int getRecognizedWordCount(){
        return wCount;
    }

    public Hashtable<?,?> getDistinctWF() {
        return distWords;
    }

}