/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.pdf.font;

import yi.pdf.YiPdfFont;

public abstract class YiPdfJapaneseUnicodeFont extends YiPdfFont {
	@Override
	public byte[] encode(String text) {
		byte[] result = new byte[text.length() * 2];
		int pos = 0;
		for(char c : text.toCharArray()) {
			result[pos++] = (byte)((c >> 8) & 0xff);
			result[pos++] = (byte)(c & 0xff);
		}
		return result;
	}
	@Override
	public String getEncoding() {
		return "UniJIS-UTF16-H";
	}
	final static int ascent = 853;
	final static int descent = -347;
	final static int xHeight = 597;
	@Override
	public int getAscent() {
		return ascent;
	}
	@Override
	public int getDescent() {
		return descent;
	}
	@Override
	public int getXHeight() {
		return xHeight;
	}
	public int getTravel(char c) {
		if(c<128) {
			return 500;
		}
		else {
			return 1000;
		}
	}
	public int getLowerPerpend(char c) {
		return descent;
	}
	public int getUpperPerpend(char c) {
		return ascent;
	}

}
