package org.linguisto.db.obj;

import java.util.logging.Logger;

public abstract class BaseObj {

	public static final Logger log = Logger.getLogger(BaseObj.class.getName());

	protected Integer id;
	protected boolean edited = false;
	protected boolean inited = false;

	protected boolean deleted = false;

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof BaseObj)) return false;
        BaseObj anotherObj = (BaseObj) obj;
        if (id != null ? !id.equals(anotherObj.id) : anotherObj.id != null) return false;
        return true;
    }
    
    public boolean isEdited() {
    	log.fine(this.getClass().getName()+": "+(" (id = "+id+"), edited = "+edited));
    	return edited;
    }
    
    final protected void edit() {
    	if (inited) {
        	log.fine(this.getClass().getName()+": (id = "+id+") was changed.");
    		edited = true;
    	}
    }
    
    protected void checkChange(Object oldObj, Object newObj) {
    	if (inited) {
    		if (oldObj != null) {
    			if (!oldObj.equals(newObj)) {
    	        	log.fine(this.getClass().getName()+": (id = "+id+") " +oldObj+" --> "+newObj);
    				edit();
    			}
    		} else {
    			if (newObj != null) {
    	        	log.fine(this.getClass().getName()+": (id = "+id+") null --> "+newObj);
    				edit();
    			}
    		}
    	}
    }
    
    public void inited() {
    	inited = true;
    }
    
    public boolean isNew() {
    	log.fine(this.getClass().getName()+": "+((id == null)?" Object is new.":" (id = "+id+") is not new."));
    	return id == null;
    }

}
