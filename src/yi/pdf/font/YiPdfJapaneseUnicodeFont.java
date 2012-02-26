/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.pdf.font;

import java.io.IOException;

import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfFont;

public abstract class YiPdfJapaneseUnicodeFont extends YiPdfFont {
	protected abstract String getFontName();
	protected abstract String getPanose();
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
	protected int putSelf(YiPdfFile pdfFile) throws IOException {
		String familyName = getFontName();
		String encoding = getEncoding();
		int descriptorId = openObj(pdfFile);
		writeAscii(pdfFile, "<<\n");
		writeAscii(pdfFile, "/Type /FontDescriptor\n");
		writeAscii(pdfFile, String.format("/FontName /%s\n", familyName));
		writeAscii(pdfFile, "/Flags 6\n");
		writeAscii(pdfFile, String.format("/FontBBox [ 0 %d 1000 %d ]\n", getDescent(), getAscent()));
		writeAscii(pdfFile, "/ItalicAngle 0\n");
		writeAscii(pdfFile, String.format("/Ascent %d\n", getAscent()));//TODO
		writeAscii(pdfFile, String.format("/Descent %d\n", getDescent()));//TODO
		writeAscii(pdfFile, "/Leading 0\n");//TODO
		writeAscii(pdfFile, String.format("/CapHeight %d\n", getAscent()));//TODO
		writeAscii(pdfFile, String.format("/XHeight %d\n", getXHeight()));//TODO
		writeAscii(pdfFile, "/StemV 92\n");//TODO
		writeAscii(pdfFile, "/StemH 92\n");//TODO
		writeAscii(pdfFile, "/AvgWidth 507\n");//TODO
		writeAscii(pdfFile, "/MaxWidth 1000\n");//TODO
		writeAscii(pdfFile, "/MissingWidth 507\n");//TODO
		writeAscii(pdfFile, String.format("/Style << /Panose <%s> >>\n", getPanose()));
		writeAscii(pdfFile, ">>\n");
		closeObj(pdfFile);
		int cidId = openObj(pdfFile);
		writeAscii(pdfFile, "<<\n");
		writeAscii(pdfFile, "/Type /Font\n");
		writeAscii(pdfFile, "/Subtype /CIDFontType0\n");
		writeAscii(pdfFile, String.format("/BaseFont /%s\n", familyName));
		writeAscii(pdfFile, "/CIDSystemInfo\n");
		writeAscii(pdfFile, "<<\n");
		writeAscii(pdfFile, "/Registry(Adobe)\n");
		writeAscii(pdfFile, "/Ordering(Japan1)\n");
		writeAscii(pdfFile, "/Supplement 4\n");//TODO
		writeAscii(pdfFile, ">>\n");
		writeAscii(pdfFile, String.format("/FontDescriptor %d 0 R\n", descriptorId));
		writeAscii(pdfFile, "/DW 1000\n");
		writeAscii(pdfFile, "/W\n");//TODO
		writeAscii(pdfFile, "[\n");
		writeAscii(pdfFile, "1 632 500\n");
		writeAscii(pdfFile, "]\n");
		writeAscii(pdfFile, String.format("/DW2 [ %d %d ]\n", getAscent(), getDescent() - getAscent()));
		writeAscii(pdfFile, ">>\n");
		closeObj(pdfFile);
		int id = openObj(pdfFile);
		writeAscii(pdfFile, "<<\n");
		writeAscii(pdfFile, "/Type /Font\n");
		writeAscii(pdfFile, "/Subtype /Type0\n");
		writeAscii(pdfFile, String.format("/BaseFont /%s-%s\n", familyName, encoding));
		writeAscii(pdfFile, String.format("/DescendantFonts [ %d 0 R ]\n", cidId));
		writeAscii(pdfFile, String.format("/Encoding /%s\n", encoding));
		writeAscii(pdfFile, ">>\n");
		closeObj(pdfFile);
		return id;
	}
	protected String getEncoding() {
		return "UniJIS-UTF16-H";
	}
	protected int getAscent() {
		return ascent;
	}
	protected int getDescent() {
		return descent;
	}
	protected int getXHeight() {
		return xHeight;
	}
	final static int ascent = 853;
	final static int descent = -347;
	final static int xHeight = 597;
	@Override
	public int getTravel(char c) {
		if(c<128) {
			return 500;
		}
		else {
			return 1000;
		}
	}
	@Override
	public int getLowerPerpend(char c) {
		return descent;
	}
	@Override
	public int getUpperPerpend(char c) {
		return ascent;
	}
	@Override
	public boolean isVertical() {
		return false;
	}
}
