package org.linguisto.learn;

import java.util.List;

/**
 *
 * @author Volodymyr Vlad
 */
public class TChunk {

    public static final String CLASS_UNREC = "unrec";
    public static final String CLASS_KNOWN = "known";
    public static final String CLASS_DICT = "dict";

    private Integer id;
    private String value;
    private String clazz = "";
    private List<String> hints;

    public TChunk(String value) {
        this.value = value;
    }

    public TChunk(String value, String clazz) {
        this.value = value;
        this.clazz = clazz;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public List<String> getHints() {
        return hints;
    }

    public String getHintHtml() {
        StringBuffer ret = new StringBuffer();
        if (hints != null) {
            for (String s : hints) {
                ret.append("<p>");
                ret.append(s);
                ret.append("</p>");
            }
        }
        return ret.toString().trim();
    }

    public void setHints(List<String> hints) {
        this.hints = hints;
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append(String.format("v: '%s', id=%d\n", getValue(), getId()));
        return ret.toString();
    }
}
