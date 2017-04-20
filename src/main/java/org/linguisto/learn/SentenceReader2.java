package org.linguisto.learn;

import java.io.IOException;

public class SentenceReader2 {

    //code of unicode character '…' is \u2026
    //private static String END_OF_SENTENCE = ".!?\u2026~÷";
    private static String[] NOT_THE_END = {"Mrs.", "Dr.", "Mr.", ".."};
    private static String[] END_OF_SENTENCE = {".", "!", "?", "\u2026", "\n", "\r\n"};

    StringBuffer sbText;
    int pos = 0;


    public SentenceReader2(String text) {
        sbText = new StringBuffer(text);
    }

    /**
     * Read a sentence from text. A sentence is considered to be terminated by any one of following
     * chars: '.', '!', '?', '…', or any combination of these characters.
     *
     * @return A String containing the contents of the sentence, including any termination characters,
     *         or null if the end of the stream has been reached.
     */
    public String readSentence() throws IOException {
        StringBuffer ret = new StringBuffer();
        char ch;
        boolean bEOS = false; //End of sentence flag
        boolean bEOF = false; //End of file flag
        do {
            if (pos < sbText.length()) {
                ch = sbText.charAt(pos);
                ret.append(ch);
                bEOS = isEndOfSentence(ret.toString());
                pos++;
            } else {
                bEOF = true;
                bEOS = true;
            }
        } while (!bEOS);
        // read all subsiquent EOS or '"' or ' ' chars
        String sChar;
        while (true) {
            if (pos < sbText.length()) {
                sChar = sbText.substring(pos, pos+1);
                if (isEndOfSentence(sChar) || "\"".equals(sChar) || " ".equals(sChar)) {
                    ret.append(sChar);
                } else {
                    break;
                }
                pos++;
            } else {
                break;
            }
        }

        if (bEOF && ret.length() < 1) return null;
        return ret.toString();
    }

    private boolean isEndOfSentence(String str) {
        boolean ret = false;
        for (String eos : END_OF_SENTENCE) {
            if (str.endsWith(eos)) {
                ret = true;
                break;
            }
        }
        // TODO ignore numbers like 123.123
        //check exceptions
        if (ret) {
            for (String s : NOT_THE_END) {
                if (str.endsWith(s)) {
                    ret = false;
                    break;
                }
            }
        }
        return ret;
    }

}
