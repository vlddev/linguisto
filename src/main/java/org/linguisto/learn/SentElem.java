package org.linguisto.learn;

public class SentElem {
    public static final int TYPE_WORD = 0;
    public static final int TYPE_DIVIDER = 1;

    private int type;
    private String value;
    private String tag;

    public SentElem(String value, String tag) {
        this(value, tag, TYPE_WORD);
    }

    public SentElem(String value, String tag, int type) {
        this.type = type;
        this.value = value;
        this.tag = tag;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
