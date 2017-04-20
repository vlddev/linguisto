package org.linguisto.semantic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import org.linguisto.webui.Styles;

public class Parser {

	private static Pattern patternLinkInText = Pattern.compile("\\[\\[(.+?)\\]\\]", Pattern.DOTALL);
	private static Pattern patternLinkLong = Pattern.compile("(.+)\\:\\:(.+)\\|(.+)");
	private static Pattern patternLinkShort = Pattern.compile("(.+)\\:\\:(.+)");
	private static Pattern patternRoundBracketsInText = Pattern.compile("\\((.+?)\\)", Pattern.DOTALL);
	
	public static final String[] parseTextTypes = new String[] {"{m}","{syn}","{sim}","{ant}","{sup}","{sub}","{wb}"};
	public static final String[] examplesTextTypes = new String[]{"{ex}","{idiom}","{proverb}","{ce}"};
	public static final List<String> parseTextTypeList = new ArrayList<String>(Arrays.asList(parseTextTypes));
	public static final List<String> examplesTextTypeList = new ArrayList<String>(Arrays.asList(examplesTextTypes));

	private static Styles styles = new Styles();
	private static Map<String, String> typeReplacement = new HashMap<String, String>();
	
	static {
		typeReplacement.put("{m}", "Значення:");
		typeReplacement.put("{syn}", "Синоніми:");
		typeReplacement.put("{sim}", "Споріднені слова:");
		typeReplacement.put("{ant}", "Антоніми:");
		typeReplacement.put("{sup}", "Надпоняття:");
		typeReplacement.put("{sub}", "Підпоняття:");
		typeReplacement.put("{wb}", "Словотворення:");
		typeReplacement.put("{ex}", "");
		typeReplacement.put("{idiom}", "Ідіоми:");
		typeReplacement.put("{proverb}", "Прислів'я:");
		typeReplacement.put("{ce}", "Типові фрази:");
	}

	public static List<SChunk> parseTranslation(String str) {
		List<SChunk> ret = new ArrayList<SChunk>();
		if (str == null) {
			ret.add(new SText(""));
		} else {
			Matcher m = patternLinkInText.matcher(str);
			int startPos = -1;
			int endPos = 0;
			//int prevStartPos = -1;
			int prevEndPos = 0;
			while (m.find()) {
				//prevStartPos = startPos;
				prevEndPos = endPos;
				startPos = m.start();
				endPos = m.end();
				//get text before link
				String sText = str.substring(prevEndPos, startPos);
				if (sText.length() > 0) {
					ret.addAll(parseText(sText));
				}
				String sLink = m.group(1);
				ret.add(parseLink(sLink));
			}
			if (startPos < 0) { // there was no matches
				ret.addAll(parseText(str));
			} else {
				// add text after last link
				String sText = str.substring(endPos);
				ret.addAll(parseText(sText));
			}
		}
		return ret;
	}

	public static List<SChunk> parseExample(String str) {
		List<SChunk> ret = new ArrayList<SChunk>();
		if (str == null) {
			ret.add(new SText(""));
		} else {
			String strType = "";
			//replace types
			String parseString = str;
			for (String type : typeReplacement.keySet()) {
				if (str.startsWith(type)) {
					SText chunk = new SText(typeReplacement.get(type));
					ret.add(chunk);
					parseString = StringUtils.replace(str, type, "", 1);
					strType = type;
					break;
				}
			}
			if (parseTextTypeList.contains(strType)) {
				ret.addAll(parseTranslation(parseString));
			} else {
				//split into example and translation of example
				String[] pair = parseString.split("→",2);
				if (pair.length == 1) {
					SText chunk = new SText(pair[0]);
					chunk.setStyleBegin(styles.getExampleBegin());
					chunk.setStyleEnd(styles.getExampleEnd());
					ret.add(chunk);
				} else if (pair.length == 2) {
					SText chunk = new SText(pair[0]);
					chunk.setStyleBegin(styles.getExampleBegin());
					chunk.setStyleEnd(styles.getExampleEnd());
					ret.add(chunk);
					ret.add(new SText(" – "));
					chunk = new SText(pair[1]);
					chunk.setStyleBegin(styles.getExampleTrBegin());
					chunk.setStyleEnd(styles.getExampleEnd());
					ret.add(chunk);
				}
			}
		}
		return ret;
	}

	private static List<SChunk> parseText(String str) {
		List<SChunk> ret = new ArrayList<SChunk>();
		Matcher m = patternRoundBracketsInText.matcher(str);
		int startPos = -1;
		int endPos = 0;
		//int prevStartPos = -1;
		int prevEndPos = 0;
		while (m.find()) {
			//prevStartPos = startPos;
			prevEndPos = endPos;
			startPos = m.start();
			endPos = m.end();
			//get text before bracket
			String sText = str.substring(prevEndPos, startPos);
			if (sText.length() > 0) {
				ret.add(new SText(sText));
			}
			String sInBrackets = m.group(1);
			SText commentText = new SText("("+sInBrackets+")");
			commentText.setType(SText.COMMENT);
			commentText.setStyleBegin(styles.getCommentBegin());
			commentText.setStyleEnd(styles.getCommentEnd());
			ret.add(commentText);
		}
		if (startPos < 0) { // there was no matches
			ret.add(new SText(str));
		} else {
			// add text after last link
			String sText = str.substring(endPos);
			ret.add(new SText(sText));
		}
		return ret;
	}

	private static SChunk parseLink(String link) {
		SChunk ret = null;
		Matcher m = patternLinkLong.matcher(link);
		if (m.matches()) {
			//check long link format like [[context::name|text]]
			ret = new SLink(m.group(1).trim(), m.group(2).trim(), m.group(3).trim());
		} else {
			//check short link format like [[context::name]]
			m = patternLinkShort.matcher(link);
			if (m.matches()) {
				ret = new SLink(m.group(1).trim(), m.group(2).trim(), m.group(2).trim());
			} else {
				//simple link like [[name]]
				ret = new SLink("link", link, link);
				//ret = new SText("#bad_link: [["+link+"]]");
			}
		}
		return ret;
	}

//	public static void main(String[] args) throws Exception {
//		List<SChunk> lst = Parser.parse(" Text 1 [[Category::test]] Text 2 [[Category::тест|blabla]] text at the end.");
//		for (SChunk chunk : lst) {
//			if (chunk instanceof SText) {
//				System.out.println("SText("+chunk.getText()+")");
//			} else if (chunk instanceof SLink) {
//				SLink sLink = (SLink)chunk;
//				System.out.println("SLink("+sLink.getProperty()+", "+sLink.getValue()+", "+sLink.getText()+") [linkId="+chunk.getLinkId()+"]");
//			}
//		}
//	}

}
