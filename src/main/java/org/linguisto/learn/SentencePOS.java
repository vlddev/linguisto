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

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SentencePOS {

    public static final Logger log = Logger.getLogger(SentencePOS.class.getName());

    private int startPosInText = 0;
    private int id = 0;
    private boolean newParagraph = false;
	private String content;
	private String contentLowerCase;
	private Locale lang;
	private List<SentElem> elemList = new ArrayList<>();
	private MaxentTagger posTagger;

	public SentencePOS(String text, MaxentTagger posTagger) {
		this.content = text;
		this.posTagger = posTagger;
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
        String taggedString = posTagger.tagString(content);
        log.log(Level.FINE, taggedString);

        SentElem prevElem = null;
        for (String str : taggedString.split(" ")) {
            String[] wordTag = str.split(BuilderPOS.TagSeparator);
            if (wordTag.length < 2) {
                System.out.println(String.format("Unexpected tagger output. Word-POSTag='%s' ", str));
            }
            String wf = wordTag[0];
            String tag = wordTag[1];
            if (EnPOSTagSet.getAllTags().contains(tag)) { //wordform
                if (prevElem != null && prevElem.getType() == SentElem.TYPE_WORD) {
                    elemList.add(new SentElem(wf.equals("'s")?"":" ", "", SentElem.TYPE_DIVIDER));
                }
                SentElem curElem = new SentElem(wf, tag);
                elemList.add(curElem);
                prevElem = curElem;
            } else { // divider
                prevElem = new SentElem(wf+" ", "", SentElem.TYPE_DIVIDER);
                elemList.add(prevElem);
            }
        }
        contentLowerCase = content.toLowerCase();
    }

    public String getNormalized() {
		StringBuilder sb = new StringBuilder(contentLowerCase);

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
			if (Text.DIVIDER_CHARS.contains(sChar)) {
				sb.deleteCharAt(0);
			} else {
				break;
			}
		}
		while(sb.length() > 0) {
			sChar = sb.substring(sb.length()-1);
			if (Text.DIVIDER_CHARS.contains(sChar)) {
				sb.deleteCharAt(sb.length()-1);
			} else {
				break;
			}
		}
		return sb.toString();
	}
	
	public String getContent() {
		return content;
	}

    /** Return HTML representation of this sentence.
     */
    public String getHtml(TextPOS parent){
        StringBuilder sbHtml = new StringBuilder();

        SentElem elem;
        for(int i=0; i< elemList.size(); i++){
            elem = elemList.get(i);
            if (elem.getType() == SentElem.TYPE_DIVIDER) {
                sbHtml.append(parent.escapeXml(elem.getValue()));
            } else if (elem.getType() == SentElem.TYPE_WORD) {
                sbHtml.append(parent.getWordHtml(elem.getValue()));
            }
        }

        return sbHtml.toString();
    }

    /** Return HTML representation of this sentence.
     */
    public String getLinguistoHtml(TextPOS parent, String linkBase){
        StringBuilder sbHtml = new StringBuilder();

        SentElem elem;
        for(int i=0; i< elemList.size(); i++){
            elem = elemList.get(i);
            if (elem.getType() == SentElem.TYPE_DIVIDER) {
                sbHtml.append(parent.escapeXml(elem.getValue()));
            } else if (elem.getType() == SentElem.TYPE_WORD) {
                sbHtml.append(parent.getWordLinguistoHtml(elem.getValue(), linkBase));
            }
        }

        return sbHtml.toString();
    }

    /** Return fb2 representation of this sentence.
     */
    public String getFb2(TextPOS parent, int wordSearchMode){
        StringBuilder sb = new StringBuilder();

        if (getContent().trim().length() < 1) {
            sb.append("<empty-line/>");
        } else {
            SentElem elem;
            for(int i=0; i < elemList.size(); i++){
                elem = elemList.get(i);
                if (elem.getType() == SentElem.TYPE_DIVIDER) {
                    sb.append(parent.escapeXml(elem.getValue()));
                } else if (elem.getType() == SentElem.TYPE_WORD) {
                    //Search word
                    switch(wordSearchMode) {
                        case Text.FIND_WORD_AS_IS:
                        case Text.FIND_WORD_IGNORE_CASE:
                            sb.append(parent.getWordFb2(elem.getValue(), elem.getTag(), wordSearchMode));
                            break;
                        case Text.FIND_WORD_GERMAN:
                            if (i==0) {
                                //TODO: check german word pattern (all letters lowercase (Ex. arbeiten), first letter uppercase the rest lowercase (Ex. Arbeit))
                                // words not matching german word pattern process ignoring case
                                sb.append(parent.getWordFb2(elem.getValue(), elem.getTag(), Text.FIND_WORD_GERMAN));
                            } else {
                                sb.append(parent.getWordFb2(elem.getValue(), elem.getTag(), Text.FIND_WORD_AS_IS));
                            }
                            break;
                    }
                }
            }
        }

        return sb.toString();
    }

    public List<TChunk> getAsChunks(TextPOS parent, boolean ignoreCase) {
        List<TChunk> ret = new ArrayList<>();

        SentElem elem;
        TChunk chunk;
        int chunkId = 0;
        for(int i=0; i< elemList.size(); i++){
            elem = elemList.get(i);
            if (elem.getType() == SentElem.TYPE_DIVIDER) {
                chunk = new TChunk(elem.getValue());
                chunk.setId(chunkId++);
                ret.add(chunk);
            } else if (elem.getType() == SentElem.TYPE_WORD) {
                chunk = parent.wordToChunk(elem.getValue(), elem.getTag(), ignoreCase);
                chunk.setId(chunkId++);
                ret.add(chunk);
            }
        }
        return ret;
    }


	public int getStartPosInText() {
		return startPosInText;
	}

	public void setStartPosInText(int startPosInText) {
		this.startPosInText = startPosInText;
	}

	public List<String> getElemList() {
        return elemList.stream().filter(sentElem -> sentElem.getType() == SentElem.TYPE_WORD)
                .map(sentElem -> sentElem.getValue())
                .collect(Collectors.toList());
	}

    public boolean isNewParagraph() {
        return newParagraph;
    }

    public void setNewParagraph(boolean newParagraph) {
        this.newParagraph = newParagraph;
    }

}
