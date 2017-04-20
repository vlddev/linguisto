package org.linguisto.semantic;


public class SText implements SChunk {

	private String text;
	private String styleBegin;
	private String styleEnd;
	private int type = TEXT;
	
	public SText(String text) {
		this.text = text;
	}
	
	@Override
	public boolean isLink() {
		return false;
	}

	@Override
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String getLinkId() {
		return null;
	}

	@Override
	public String getStyleBegin() {
		return styleBegin;
	}

	public void setStyleBegin(String styleBegin) {
		this.styleBegin = styleBegin;
	}

	@Override
	public String getStyleEnd() {
		return styleEnd;
	}

	public void setStyleEnd(String styleEnd) {
		this.styleEnd = styleEnd;
	}
}
