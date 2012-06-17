/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.pdf.font;

public abstract class YiPdfJapaneseUnicodeFontV extends YiPdfJapaneseUnicodeFont {
	@Override
	public String getEncoding() {
		return "UniJIS-UTF16-V";
	}
	@Override
	public int getTravel(char c) {
		return 1000;//ascent - descent;
	}
	@Override
	public int getLowerPerpend(char c) {
		return -500;
	}
	@Override
	public int getUpperPerpend(char c) {
		return 500;
	}
	@Override
	public boolean isVertical() {
		return true;
	}
}
