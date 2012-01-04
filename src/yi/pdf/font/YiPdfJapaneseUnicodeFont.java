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

}
