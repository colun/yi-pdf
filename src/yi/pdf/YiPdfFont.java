/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.pdf;

public abstract class YiPdfFont extends YiPdfResource {
	public abstract byte[] encode(String text);
	public abstract String getEncoding();
	public abstract String getFontName();
	public abstract String getPanose();
	public abstract int getTravel(char c);
	public abstract int getLowerPerpend(char c);
	public abstract int getUpperPerpend(char c);
	public abstract int getAscent();
	public abstract int getDescent();
	public abstract int getXHeight();
	public abstract boolean isVertical();
}
