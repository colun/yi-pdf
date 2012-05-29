package yi.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import yi.pdf.image.YiPdfJpeg;

public class YiPdfImage extends YiPdfResource {
	private int width;
	private int height;
	private byte[] data;
	private String colorSpace;
	private int bitsPerComponent;
	private String filter;
	public final int getWidth() {
		return width;
	}
	public final int getHeight() {
		return height;
	}
	protected final byte[] getData() {
		return data;
	}
	protected final String getColorSpace() {
		return colorSpace;
	}
	protected final int getBitsPerComponent() {
		return bitsPerComponent;
	}
	protected final String getFilter() {
		return filter;
	}
	protected final void setWidth(int w) {
		width = w;
	}
	protected final void setHeight(int h) {
		height = h;
	}
	protected final void setData(byte[] d) {
		data = d;
	}
	protected final void setColorSpace(String cs) {
		colorSpace = cs;
	}
	protected final void setBitsPerComponent(int bpc) {
		bitsPerComponent = bpc;
	}
	protected final void setFilter(String f) {
		filter = f;
	}
	public static YiPdfImage getInstance(URL url) throws IOException {
		InputStream stream = url.openStream();
		stream.mark(4096);
		byte[] head = new byte[16];
		int rdCnt = stream.read(head);
		if(rdCnt!=16) {
			throw new RuntimeException("不正な画像ファイル");
		}
		stream.reset();
		if(head[0]==(byte)0xFF && head[1]==(byte)0xD8) {
			return new YiPdfJpeg(stream);
		}
		throw new RuntimeException("未実装");
	}
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
	protected int putSelf(YiPdfFile pdfFile) throws IOException {
		int id = openObj(pdfFile);
		byte[] data = getData();
		writeAscii(pdfFile, "<<\n");
		writeAscii(pdfFile, "/Type /XObject\n");
		writeAscii(pdfFile, "/Subtype /Image\n");
		writeAscii(pdfFile, String.format("/Width %d\n", getWidth()));
		writeAscii(pdfFile, String.format("/Height %d\n", getHeight()));
		writeAscii(pdfFile, String.format("/ColorSpace /%s\n", getColorSpace()));
		writeAscii(pdfFile, String.format("/BitsPerComponent %d\n", getBitsPerComponent()));
		writeAscii(pdfFile, String.format("/Length %d\n", data.length));
		writeAscii(pdfFile, String.format("/Filter /%s\n", getFilter()));
		writeAscii(pdfFile, ">>\n");
		writeAscii(pdfFile, "stream\n");
		write(pdfFile, data);
		writeAscii(pdfFile, "\nendstream\n");
		closeObj(pdfFile);
		return id;
	}
}
