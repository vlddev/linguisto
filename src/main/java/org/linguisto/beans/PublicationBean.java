package org.linguisto.beans;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.linguisto.db.obj.Publication;

@ManagedBean(name="publicationBean")
@ViewScoped
public class PublicationBean extends AbstractDbBean {
 
	private static final long serialVersionUID = 59718330623026045L;

	//private static final String defaultLang = "uk"; 
	private Integer id;
	private Publication publication;

	public PublicationBean() {
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Publication getPublication() {
		return publication;
	}

	public List<Publication> getPublications() {
		return getDBManager().getPublications();
	}
	
	public Publication getPublication(int id) {
		return getDBManager().getPublication(id);
	}

	public void loadPublication(Integer id) {
		publication = getPublication(id);
	}

}
