package org.linguisto.beans;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedProperty;

import org.linguisto.db.DBManager;

public abstract class AbstractDbBean implements Serializable {
 
	private static final long serialVersionUID = -3778817316876506907L;

	@ManagedProperty(value="#{dbBean}")
	private DatabaseBean db;
	
    private DBManager dbManager;

	public void setDb(DatabaseBean db) {
		this.db = db;
	}
	
	public DBManager getDBManager() {
		return dbManager;
	}
	
	@PostConstruct
	protected void init() {
		dbManager = new DBManager(db.getDataSource());
	}

	@PreDestroy
	public void close() {
	}
}
