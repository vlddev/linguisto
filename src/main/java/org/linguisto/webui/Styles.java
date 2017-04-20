package org.linguisto.webui;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean(name="style")
@ApplicationScoped
public class Styles {
	private String transcriptionBegin = "<span style=\"color: grey;\">";
	private String transcriptionEnd = "</span>";
	private String rankBegin = "<span style=\"color: blue; font-size:smaller;\">";
	private String typeBegin = "<span style=\"color: green; font-style: italic;\">";
	private String typeEnd = "</span>";
	private String exampleBegin = "<span style=\"color: sienna;\">";
	private String exampleEnd = "</span>";
	private String commentBegin = "<span style=\"color: grey;\">";
	private String commentEnd = "</span>";
	private String exampleTrBegin = "<span style=\"color: SteelBlue;\">";

	public String getTranscriptionBegin() {
		return transcriptionBegin;
	}
	public void setTranscriptionBegin(String transcriptionBegin) {
		this.transcriptionBegin = transcriptionBegin;
	}
	public String getTranscriptionEnd() {
		return transcriptionEnd;
	}
	public void setTranscriptionEnd(String transcriptionEnd) {
		this.transcriptionEnd = transcriptionEnd;
	}
	public String getRankBegin() {
		return rankBegin;
	}
	public String getTypeBegin() {
		return typeBegin;
	}
	public void setTypeBegin(String typeBegin) {
		this.typeBegin = typeBegin;
	}
	public String getTypeEnd() {
		return typeEnd;
	}
	public void setTypeEnd(String typeEnd) {
		this.typeEnd = typeEnd;
	}
	public String getExampleBegin() {
		return exampleBegin;
	}
	public void setExampleBegin(String exampleBegin) {
		this.exampleBegin = exampleBegin;
	}
	public String getExampleEnd() {
		return exampleEnd;
	}
	public void setExampleEnd(String exampleEnd) {
		this.exampleEnd = exampleEnd;
	}
	public String getCommentBegin() {
		return commentBegin;
	}
	public String getCommentEnd() {
		return commentEnd;
	}
	public String getExampleTrBegin() {
		return exampleTrBegin;
	}
}
