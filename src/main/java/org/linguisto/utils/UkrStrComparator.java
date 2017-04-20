package org.linguisto.utils;

import java.util.Comparator;

/**
 * Comparator for Sorting Ukrainian strings
 *
 * @author V.Vlad
 */
public class UkrStrComparator implements Comparator<Object> {

    //Ukrainian Alphabet
    //
    public static final String UkrAlphabet =
            "\u0410\u0411\u0412\u0413\u0490\u0414\u0415\u0404\u0416\u0417" + //A,B,V,H,G,D,E,JE,ZH,Z
                    "\u0418\u0406\u0407\u0419\u041a\u041b\u041c\u041d\u041e\u041f" + //Y,I,JI,J,K,L,M,N,O,P
                    "\u0420\u0421\u0422\u0423\u0424\u0425\u0426\u0427\u0428\u0429" + //R,S,T,U,F,KH,TS,CH,SH,SHCH
                    "\u042c\u042e\u042f" + // SOFT,JU,JA
                    "\u0430\u0431\u0432\u0433\u0491\u0434\u0435\u0454\u0436\u0437" + //a,b,v,h,g,d,e,je,zh,z
                    "\u0438\u0456\u0457\u0439\u043a\u043b\u043c\u043d\u043e\u043f" + //y,i,ji,j,k,l,m,n,o,p
                    "\u0440\u0441\u0442\u0443\u0444\u0445\u0446\u0447\u0448\u0449" + //r,s,t,u,f,kh,ts,ch,sh,shch
                    "\u044c\u044e\u044f" + // soft,ju,ja
                    "\u0027\u002d"; // ' Apostrophe; - Hyphen, minus

    public int compare(Object obj1, Object obj2) {
        String str1 = obj1.toString();
        String str2 = obj2.toString();

        int len1 = str1.length();
        int len2 = str2.length();
        int n = Math.min(len1, len2);

        char v1[] = str1.toCharArray();
        char v2[] = str2.toCharArray();

        int i = 0;

        int k = i;
        int lim = n + i;
        int p1 = -1;
        int p2 = -1;
        char ch1;
        char ch2;
        while (k < lim) {
            ch1 = v1[k];
            ch2 = v2[k];
            if (ch1 != ch2) {
                if ((p1 = UkrAlphabet.indexOf(ch1)) > -1 && (p2 = UkrAlphabet.indexOf(ch2)) > -1) {
                    return p1 - p2;
                } else {
                    return ch1 - ch2;
                }
            }
            k++;
        }
        return len1 - len2;
    }

    public static boolean isUkrainianWord(String word) {
        boolean ret = true;
        for(int i = 0; i < word.length() && ret; i++) {
            if(UkrAlphabet.indexOf(word.charAt(i)) < 0) {
                ret = false;
            }
        }
        return ret;
    }

    public boolean isMixedUkrOtherWord(String word) {
        boolean hasUkrChars = false;
        boolean hasOtherChars = false;
        for(int i = 0; i < word.length() && !(hasOtherChars && hasUkrChars); i++) {
            if(UkrAlphabet.indexOf(word.charAt(i)) < 0) {
                hasOtherChars = true;
            } else {
                hasUkrChars = true;
            }
        }
        return hasOtherChars && hasUkrChars;
    }

    public String analyseNonUkrainianChars(String word) {
        StringBuffer ret = new StringBuffer();
        for(int i = 0; i < word.length(); i++) {
            if(UkrAlphabet.indexOf(word.charAt(i)) < 0) {
                ret.append("_").append(word.charAt(i)).append("_");
            } else {
                ret.append(word.charAt(i));
            }
        }
        return ret.toString();
    }

}