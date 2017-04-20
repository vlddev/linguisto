package org.linguisto.semantic;

public interface SChunk {

	public static final int TEXT = 0;
	public static final int LINK = 1;
	public static final int COMMENT = 2;

	boolean isLink();
	String getText();
	String getStyleBegin();
	String getStyleEnd();
	int getType();
	String getLinkId();
}
