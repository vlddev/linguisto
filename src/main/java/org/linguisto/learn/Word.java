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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * @author Vlad
 */
public class Word {
	long id;
	int type;
	String inf;
	String language;
    private int rank;
    boolean userKnows = false;
	List<WordForm> wfList = null;
	
	public Word(String s){
		id = -1;
		type = -1;
		inf = s;
	}
	
	public Word(long id, String s){
		this.id = id;
		inf = s;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
		if (wfList != null) {
			for(WordForm wf : wfList) {
				wf.setFkInf(id);
			}
		}
	}

    public boolean isUserKnows() {
        return userKnows;
    }

    public void setUserKnows(boolean userKnows) {
        this.userKnows = userKnows;
    }

    public String getInf(){
		return inf;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public List<WordForm> getWfList() {
		return wfList;
	}

	public void setWfList(List<WordForm> wfList) {
		this.wfList = wfList;
		if (wfList != null ) {
			for(WordForm wf : wfList) {
				wf.setFkInf(id);
				wf.setLanguage(language);
			}
		}
	}

	/**
	 * @return
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language
	 */
	public void setLanguage(String language) {
		this.language = language;
		if (wfList != null) {
			for(WordForm wf : wfList) {
				wf.setLanguage(language);
			}
		}
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        boolean ret = false;
        if (obj instanceof Word) {
            Word other = (Word)obj;
            ret = new EqualsBuilder().
                    // if deriving: appendSuper(super.equals(obj)).
                    append(getInf(), other.getInf()).
                    append(getType(), other.getType()).
                    isEquals();
        }
        return ret;
    }


    @Override
    public int hashCode(){
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                append(getInf()).
                append(getType()).
                toHashCode();
    }

}
