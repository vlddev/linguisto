package org.linguisto.semantic;

public class SLink implements SChunk {

	private String text;
	private String property;
	private String value;

	public SLink(String property, String value, String text) {
		this.text = text;
		this.property = property;
		this.value = value;
	}

	@Override
	public boolean isLink() {
		return true;
	}

	@Override
	public int getType() {
		return LINK;
	}

	@Override
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getLinkId() {
		return value;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getStyleBegin() {
		return null;
	}

	@Override
	public String getStyleEnd() {
		return null;
	}
}
