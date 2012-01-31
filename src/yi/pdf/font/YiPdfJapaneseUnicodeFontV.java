/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.pdf.font;

public abstract class YiPdfJapaneseUnicodeFontV extends YiPdfJapaneseUnicodeFont {
	@Override
	public String getEncoding() {
		return "UniJIS-UTF16-V";
	}
	public int getTravel(char c) {
		return ascent - descent;
	}
	public int getLowerPerpend(char c) {
		return -500;
	}
	public int getUpperPerpend(char c) {
		return 500;
	}

}
