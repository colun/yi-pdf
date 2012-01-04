package yi.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public final class YiPdfPage {
	double width;
	double height;
	private YiPdfFont nowFont = null;
	private YiPdfFont beforeFont = null;
	private double nowFontSize = 10.5;
	private double beforeFontSize;
	ByteArrayOutputStream textStream = new ByteArrayOutputStream();
	ByteArrayOutputStream graphicsStream = new ByteArrayOutputStream();
	Set<YiPdfFont> fontSet = new LinkedHashSet<YiPdfFont>();

	public YiPdfPage(double width, double height) {
		this.width = width;
		this.height = height;
	}
	public double getWidth() {
		return width;
	}
	public double getHeight() {
		return height;
	}
	public void drawText(double x, double y, String text) throws IOException {
		if(nowFont!=beforeFont || nowFontSize!=beforeFontSize) {
			beforeFont = nowFont;
			beforeFontSize = nowFontSize;
			fontSet.add(nowFont);
			textStream.write(toBytesFromAscii(String.format("/F%d %f Tf\n", nowFont.getResourceId(), nowFontSize)));
		}
		textStream.write(toBytesFromAscii(String.format("1 0 0 1 %f %f Tm\n", x, height - y)));
		textStream.write(toBytesFromAscii("("));
		textStream.write(escapeStringBinary(nowFont.encode(text)));
		textStream.write(toBytesFromAscii(") Tj\n"));
	}
	private static byte[] toBytesFromAscii(String str) {
		char[] buf = str.toCharArray();
		byte[] data = new byte[buf.length];
		for(int i=0; i<buf.length; ++i) {
			data[i] = (byte)buf[i];
		}
		return data;
	}
	private static byte[] escapeStringBinary(byte[] str) {
		int count = 0;
		for(byte b : str) {
			if(b=='\\' || b=='(' || b==')' || b=='\r') {
				++count;
			}
		}
		if(count==0) {
			return str;
		}
		byte[] result = new byte[str.length + count];
		int pos = 0;
		for(byte b : str) {
			switch(b) {
			case '\\':
			case '(':
			case ')':
				result[pos++] = '\\';
				result[pos++] = b;
				break;
			case '\r':
				result[pos++] = '\\';
				result[pos++] = 'r';
				break;
			default:
				result[pos++] = b;
			}
		}
		return result;
	}
	public byte[] getStreamBytes() {
		if(textStream.size()==0) {
			return graphicsStream.toByteArray();
		}
		byte[] result = new byte[textStream.size() + graphicsStream.size() + 6];
		int pos = 0;
		for(byte b : graphicsStream.toByteArray()) {
			result[pos++] = b;
		}
		result[pos++] = 'B';
		result[pos++] = 'T';
		result[pos++] = '\n';
		for(byte b : textStream.toByteArray()) {
			result[pos++] = b;
		}
		result[pos++] = 'E';
		result[pos++] = 'T';
		result[pos++] = '\n';
		return result;
	}
	public void setFont(YiPdfFont font) {
		nowFont = font;
	}
	public void setFontSize(double fontSize) {
		nowFontSize = fontSize;
	}
	public double getStringWidth() {
		return 0;
	}
	public void drawLine(double x1, double y1, double x2, double y2) {
	}
	public void setFillColorRGB(double r, double g, double b) {
	}
	public void setDrawColorRGB(double r, double g, double b) {
	}
	public void setTextColorRGB(double r, double g, double b) {
	}
	public Set<YiPdfFont> getFontSet() {
		return fontSet;
	}
}
