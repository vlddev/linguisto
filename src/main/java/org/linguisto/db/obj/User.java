package org.linguisto.db.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private Integer id;
    private String name;
    private String email;
    private Map<String,String> wordClassUids = new HashMap<String,String>();
    private boolean confirmed;
    private List<Role> roles;

    public User(Integer id, String name) {
    	this.id = id;
    	this.name = name;
    }
    
    public boolean hasRole(Role role) {
        return roles == null ? false:roles.contains(role);
    }
    
    public void addRole(Role role) {
    	if (roles == null) {
    		roles = new ArrayList<Role>();
    	}
    	roles.add(role);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public Map<String,String> getWordClassUids() {
		return wordClassUids;
	}

	public void putWordClassUids(String language, String wordClassUids) {
		this.wordClassUids.put(language, wordClassUids);
	}
}
