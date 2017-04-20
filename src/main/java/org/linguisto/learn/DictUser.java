package org.linguisto.learn;

/**
 *
 * @author Volodymyr Vlad
 */
public class DictUser {
    private Integer id;
    private String name;

    /* comma separated list of UIDs with known words lists*/
    private String wordClassUids;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWordClassUids() {
        return wordClassUids;
    }

    public void setWordClassUids(String wordClassUids) {
        this.wordClassUids = wordClassUids;
    }
}
