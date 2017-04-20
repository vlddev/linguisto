/*
#######################################################################
#
#  Linguisto Portal
#
#  Copyright (c) 2017 Volodymyr Vlad
#
#######################################################################
*/

package org.linguisto.learn;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class Sentence {

    private int startPosInText = 0;
    private int id = 0;
    private boolean newParagraph = false;
	private String content;
	private String contentLowerCase;
	private Locale lang;
	private List<String> elemList = new ArrayList<String>();
	private List<String> elemListLowerCase = new ArrayList<String>();
	private List<String> dividers = new ArrayList<String>();
	
	public Sentence(String text) {
		content = text;
		// parse sentence
		init();
	}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private void init() {
		StringTokenizer fIn = new StringTokenizer(content, Text.DIVIDER_CHARS, true); //return delimiters is on
		if(fIn.hasMoreTokens()) {
            String strWord = "";
            String strDivider = "";
            String token;
			do {
                token = fIn.nextToken();
                if (token.length() == 1 && Text.DIVIDER_CHARS.contains(token)) { //divider
                    strDivider += token;
                } else { //word
                    strWord = token;
                    while (strWord.startsWith("-"))  {
                        strWord = strWord.substring(1);
                        //add "-" to the last divider
                        strDivider += "-";
                    }
                    String nextDivider = "";
                    while (strWord.endsWith("-"))  {
                        strWord = strWord.substring(0, strWord.length()-1);
                        nextDivider += "-";
                    }
                    if (strWord.length() > 0) {
                        //if (elemList.size() != 0) { //ignore divider before first word in sentence
                        dividers.add(strDivider);
                        //}
                        strDivider = nextDivider; //reset

                        elemList.add(strWord);
                    }

                }
			} while(fIn.hasMoreTokens());
            dividers.add(strDivider); //last divider
		}
		// init elemListLoverCase
		for (String s : elemList) {
			elemListLowerCase.add(s.toLowerCase());
		}
		contentLowerCase = content.toLowerCase();
	}
	
	public String getNormalized() {
		StringBuffer sb = new StringBuffer(contentLowerCase);

		// remove first BOM in utf8
		if (sb.length() > 0) {
			byte[] bomArr = sb.substring(0, 1).getBytes();
			if (bomArr.length == 3 && bomArr[0] == (byte)0xEF && bomArr[1] == (byte)0xBB && bomArr[2] == (byte)0xBF) {
				//BOM in utf8
				sb.deleteCharAt(0);
			}
		}

		// remove first "char(63)"
		if (sb.length() > 0) {
			byte[] bomArr = sb.substring(0, 1).getBytes();
			if (bomArr.length == 1 && bomArr[0] == (byte)0x3F) {
				sb.deleteCharAt(0);
			}
		}
		
		//відкинути всі символи з множини Corpus.DEVIDER_CHARS на початку та в кінці тексту
		String sChar;
		while(sb.length() > 0) {
			sChar = sb.substring(0, 1);
			if (Text.DIVIDER_CHARS.indexOf(sChar) > -1 ) {
				sb.deleteCharAt(0);
			} else {
				break;
			}
		}
		while(sb.length() > 0) {
			sChar = sb.substring(sb.length()-1);
			if (Text.DIVIDER_CHARS.indexOf(sChar) > -1 ) {
				sb.deleteCharAt(sb.length()-1);
			} else {
				break;
			}
		}
		return sb.toString();
	}
	
	public boolean containsWordform(String wf, boolean ignoreCase) {
		boolean ret = false;
		if (ignoreCase) {
			String wfLoverCase = wf.toLowerCase();
			if (contentLowerCase.indexOf(wfLoverCase) > -1) {
				ret = elemListLowerCase.contains(wfLoverCase);
			}
		} else {
			if (content.indexOf(wf) > -1) {
				ret = elemList.contains(wf);
			}
		}
		return ret;
	}

	public int getWordformIndex(String wf, boolean ignoreCase) {
		int ret = -1;
		if (ignoreCase) {
			String wfLoverCase = wf.toLowerCase();
			if (contentLowerCase.indexOf(wfLoverCase) > -1) {
				ret = elemListLowerCase.indexOf(wfLoverCase);
			}
		} else {
			if (content.indexOf(wf) > -1) {
				ret = elemList.indexOf(wf);
			}
		}
		ret = ret > -1 ? ret+startPosInText : ret;
		return ret;
	}

	public String getContent() {
		return content;
	}

    /** Return HTML representation of this sentence.
     */
    public String getHtml(Text parent){
        StringBuffer sbHtml = new StringBuffer();

        String sWord;
        for(int i=0; i< elemList.size(); i++){
            sWord = elemList.get(i);
            //Search word
            sbHtml.append(dividers.get(i));
            sbHtml.append(parent.getWordHtml(sWord));
        }
        sbHtml.append(dividers.get(dividers.size()-1)); //last divider

        return sbHtml.toString();
    }

    /** Return HTML representation of this sentence.
     */
    public String getLinguistoHtml(Text parent, String linkBase){
        StringBuffer sbHtml = new StringBuffer();

        String sWord;
        for(int i=0; i< elemList.size(); i++){
            sWord = elemList.get(i);
            //Search word
            sbHtml.append(dividers.get(i));
            sbHtml.append(parent.getWordLinguistoHtml(sWord, linkBase));
        }
        sbHtml.append(dividers.get(dividers.size()-1)); //last divider

        return sbHtml.toString();
    }

    /** Return fb2 representation of this sentence.
     */
    public String getFb2(Text parent, int wordSearchMode){
        StringBuffer sb = new StringBuffer();

        if (getContent().trim().length() < 1) {
            sb.append("<empty-line/>");
        } else {
            String sWord;
            for(int i=0; i< elemList.size(); i++){
                sWord = elemList.get(i);
                //Search word
                sb.append(parent.escapeXml(dividers.get(i)));
                switch(wordSearchMode) {
                    case Text.FIND_WORD_AS_IS:
                    case Text.FIND_WORD_IGNORE_CASE:
                        sb.append(parent.getWordFb2(sWord, wordSearchMode));
                        break;
                    case Text.FIND_WORD_GERMAN:
                        if (i==0) {
                            //TODO: check german word pattern (all letters lowercase (Ex. arbeiten), first letter uppercase the rest lowercase (Ex. Arbeit))
                            // words not matching german word pattern process ignoring case
                            sb.append(parent.getWordFb2(sWord, Text.FIND_WORD_GERMAN));
                        } else {
                            sb.append(parent.getWordFb2(sWord, Text.FIND_WORD_AS_IS));
                        }
                        break;
                }
            }
            sb.append(parent.escapeXml(dividers.get(dividers.size()-1))); //last divider
        }

        return sb.toString();
    }

    public List<TChunk> getAsChunks(Text parent, boolean ignoreCase) {
        List<TChunk> ret = new ArrayList<TChunk>();
        TChunk chunk;
        int chunkId = 0;
        for(int i=0; i< elemList.size(); i++){
            chunk = new TChunk(dividers.get(i));
            chunk.setId(chunkId++);
            ret.add(chunk);
            chunk = parent.wordToChunk(elemList.get(i), ignoreCase);
            chunk.setId(chunkId++);
            ret.add(chunk);
        }
        chunk = new TChunk(dividers.get(dividers.size()-1));
        chunk.setId(chunkId++);
        ret.add(chunk); //last divider
        return ret;
    }


    public List<String> getAll() {
        List<String> ret = new ArrayList<String>();
        for(int i=0; i< elemList.size(); i++){
            ret.add(dividers.get(i));
            ret.add(elemList.get(i));
        }
        ret.add(dividers.get(dividers.size() - 1)); //last divider
        return ret;
    }

	public int getStartPosInText() {
		return startPosInText;
	}

	public void setStartPosInText(int startPosInText) {
		this.startPosInText = startPosInText;
	}

	public List<String> getElemList() {
		return elemList;
	}

    public boolean isNewParagraph() {
        return newParagraph;
    }

    public void setNewParagraph(boolean newParagraph) {
        this.newParagraph = newParagraph;
    }

}
