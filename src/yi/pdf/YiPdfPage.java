/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public final class YiPdfPage {
	private YiPdfFile pdfFile;
	int pageId;
	final double width;
	final double height;
	ByteArrayOutputStream textStream = new ByteArrayOutputStream();
	ByteArrayOutputStream graphicsStream = new ByteArrayOutputStream();
	Set<YiPdfFont> fontSet = new LinkedHashSet<YiPdfFont>();

	protected YiPdfPage(YiPdfFile pdfFile, int pageId, double width, double height) {
		this.pdfFile = pdfFile;
		this.pageId = pageId;
		this.width = width;
		this.height = height;
	}
	public void close() throws IOException {
		pdfFile.writePage(this);
		nowFont = null;
		beforeFont = null;
		textStream = null;
		graphicsStream = null;
		fontSet = null;
		pdfFile = null;
	}
	public double getWidth() {
		return width;
	}
	public double getHeight() {
		return height;
	}

	private YiPdfFont nowFont = null;
	private YiPdfFont beforeFont = null;
	public void setFont(YiPdfFont font) {
		nowFont = font;
	}
	public YiPdfFont getFont() {
		return nowFont;
	}
	private double nowFontSize = 10.5;
	private Double beforeFontSize = null;
	public void setFontSize(double fontSize) {
		nowFontSize = fontSize;
	}
	public double getFontSize() {
		return nowFontSize;
	}
	private void updateFont() throws IOException {
		assert(nowFont!=null) : "フォントを指定する必要があります";
		if(beforeFont==null || nowFont!=beforeFont || beforeFontSize==null || nowFontSize!=beforeFontSize) {
			beforeFont = nowFont;
			beforeFontSize = nowFontSize;
			fontSet.add(nowFont);
			textStream.write(toBytesFromAscii(String.format("/F%d %f Tf\n", nowFont.getResourceId(), nowFontSize)));
		}
	}

	private YiPdfColor nowTextColor = new YiPdfColor(0, 0, 0);
	private YiPdfColor beforeTextColor = null;
	public void setTextColor(YiPdfColor color) {
		nowTextColor = color;
	}
	public YiPdfColor getTextColor() {
		return nowTextColor;
	}
	private void updateTextColor() throws IOException {
		if(beforeTextColor==null || !beforeTextColor.equals(nowTextColor)) {
			beforeTextColor = nowTextColor;
			textStream.write(toBytesFromAscii(String.format("%f %f %f rg\n", nowTextColor.r, nowTextColor.g, nowTextColor.b)));
		}
	}

	private YiPdfColor nowFillColor = new YiPdfColor(0, 0, 0);
	private YiPdfColor beforeFillColor = null;
	public void setFillColor(YiPdfColor color) {
		nowFillColor = color;
	}
	public YiPdfColor getFillColor() {
		return nowFillColor;
	}
	private void updateFillColor() throws IOException {
		if(beforeFillColor==null || !beforeFillColor.equals(nowFillColor)) {
			beforeFillColor = nowFillColor;
			graphicsStream.write(toBytesFromAscii(String.format("%f %f %f rg\n", nowFillColor.r, nowFillColor.g, nowFillColor.b)));
		}
	}

	private YiPdfColor nowDrawColor = new YiPdfColor(0, 0, 0);
	private YiPdfColor beforeDrawColor = null;
	public void setDrawColor(YiPdfColor color) {
		nowDrawColor = color;
	}
	public YiPdfColor getDrawColor() {
		return nowDrawColor;
	}
	private void updateDrawColor() throws IOException {
		if(beforeDrawColor==null || !beforeDrawColor.equals(nowDrawColor)) {
			beforeDrawColor = nowDrawColor;
			graphicsStream.write(toBytesFromAscii(String.format("%f %f %f RG\n", nowDrawColor.r, nowDrawColor.g, nowDrawColor.b)));
		}
	}

	private int nowTextRenderingMode = 0;
	private Integer beforeTextRenderingMode = null;
	public void setTextRenderingMode(int mode) {
		nowTextRenderingMode = mode;
	}
	public int getTextRenderingMode() {
		return nowTextRenderingMode;
	}
	private void updateTextRenderingMode() throws IOException {
		if(beforeTextRenderingMode==null || nowTextRenderingMode!=beforeTextRenderingMode) {
			beforeTextRenderingMode = nowTextRenderingMode;
			textStream.write(toBytesFromAscii(String.format("%d Tr\n", nowTextRenderingMode)));
		}
	}

	public void drawText(double x, double y, String text) throws IOException {
		//textStream.write(toBytesFromAscii("BT\n"));
		updateFont();
		updateTextColor();
		updateTextRenderingMode();
		textStream.write(toBytesFromAscii(String.format("1 0 0 1 %f %f Tm\n", x, height - y)));
		textStream.write(toBytesFromAscii("("));
		textStream.write(escapeStringBinary(nowFont.encode(text)));
		textStream.write(toBytesFromAscii(") Tj\n"));
		//textStream.write(toBytesFromAscii("ET\n"));
	}
	public void beginTextTag(YiPdfTag tag) throws IOException {
		textStream.write(toBytesFromAscii(String.format("/%s << /MCID %d >> BDC\n", tag.getTagName(), tag.publishMcId(pageId))));
	}
	public void endTextTag() throws IOException {
		textStream.write(toBytesFromAscii("EMC\n"));
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
	public double getStringWidth() {
		return 0;
	}

	double nowLineWidth = 1;
	Double beforeLineWidth = null;
	public void setLineWidth(double v) {
		nowLineWidth = v;
	}
	public double getLineWidth() {
		return nowLineWidth;
	}
	private void updateLineWidth() throws IOException {
		if(beforeLineWidth==null || nowLineWidth!=beforeLineWidth) {
			beforeLineWidth = nowLineWidth;
			graphicsStream.write(toBytesFromAscii(String.format("%f w\n", nowLineWidth)));
		}
	}

	int nowLineCap = 0;
	Integer beforeLineCap = null;
	public void setLineCap(int v) {
		nowLineCap = v;
	}
	public int getLineCap() {
		return nowLineCap;
	}
	private void updateLineCap() throws IOException {
		if(beforeLineCap==null || nowLineCap!=beforeLineCap) {
			beforeLineCap = nowLineCap;
			graphicsStream.write(toBytesFromAscii(String.format("%d J\n", nowLineCap)));
		}
	}

	public void drawLine(double x1, double y1, double x2, double y2) throws IOException {
		updateDrawColor();
		updateLineWidth();
		updateLineCap();
		graphicsStream.write(toBytesFromAscii(String.format("%f %f m %f %f l S\n", x1, height - y1, x2, height - y2)));
	}
	public void fillRect(double x, double y, double width, double height) throws IOException {
		updateFillColor();
		graphicsStream.write(toBytesFromAscii(String.format("%f %f %f %f re f\n", x, this.height - y - height, width, height)));
	}
	public Set<YiPdfFont> getFontSet() {
		return fontSet;
	}
}
