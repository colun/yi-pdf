/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.pdf;

import java.io.IOException;

public abstract class YiPdfFont extends YiPdfResource {
	public abstract byte[] encode(String text);
	//public abstract String getEncoding();
	//public abstract String getFontName();
	//public abstract String getPanose();
	public abstract int getTravel(char c);
	public abstract int getLowerPerpend(char c);
	public abstract int getUpperPerpend(char c);
	//public abstract int getAscent();
	//public abstract int getDescent();
	//public abstract int getXHeight();
	public abstract boolean isVertical();
	protected abstract int putSelf(YiPdfFile pdfFile) throws IOException;
	protected int openObj(YiPdfFile pdfFile) throws IOException {
		return pdfFile.openObj();
	}
	protected void closeObj(YiPdfFile pdfFile) throws IOException {
		pdfFile.closeObj();
	}
	protected int reserveObjId(YiPdfFile pdfFile) {
		return pdfFile.reserveObjId();
	}
	protected void writeAscii(YiPdfFile pdfFile, String str) throws IOException {
		pdfFile.writeAscii(str);
	}
	protected void write(YiPdfFile pdfFile, byte[] data) throws IOException {
		pdfFile.write(data);
	}
}
