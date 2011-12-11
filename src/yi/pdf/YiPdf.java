package yi.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YiPdf {
	OutputStream stream;
	int streamPos;
	int objCount;
	List<Integer> offsets;
	List<Integer> pageObjIdList;
	Map<String, Integer> fontObjIdMap;
	Map<String, Integer> imageObjIdMap;
	YiPdfPage nowPage;
	public YiPdf(OutputStream stream) throws IOException {
		this.stream = stream;
		streamPos = 0;
		objCount = 4;
		offsets = new ArrayList<Integer>();
		for(int i=0; i<objCount; ++i) {
			offsets.add(0);
		}
		pageObjIdList = new ArrayList<Integer>();
		fontObjIdMap = new LinkedHashMap<String, Integer>();
		imageObjIdMap = new LinkedHashMap<String, Integer>();
		writeAscii("%PDF-1.4\n");
	}
	private byte[] toBytesFromAscii(String str) {
		char[] buf = str.toCharArray();
		byte[] data = new byte[buf.length];
		for(int i=0; i<buf.length; ++i) {
			data[i] = (byte)buf[i];
		}
		return data;
	}
	private void write(byte[] data) throws IOException {
		stream.write(data);
		streamPos += data.length;
	}
	private void writeAscii(String str) throws IOException {
		write(toBytesFromAscii(str));
	}
	private int openObj() throws IOException {
		offsets.add(streamPos);
		++objCount;
		writeAscii(String.format("%d 0 obj\n", objCount));
		return objCount;
	}
	private void openObj(int i) throws IOException {
		offsets.set(i-1, streamPos);
		writeAscii(String.format("%d 0 obj\n", i));
	}
	private void closeObj() throws IOException {
		writeAscii("endobj\n");
	}
	public void openPage(double width, double height) throws IOException {
		closePage();
		nowPage = new YiPdfPage(width, height);
	}
	public void closePage() throws IOException {
		if(nowPage!=null) {
			int id = openObj();
			pageObjIdList.add(id);
			writeAscii("<<\n");
			writeAscii("/Type /Page\n");
			writeAscii("/Parent 4 0 R\n");
			writeAscii(String.format("/MediaBox [ 0 0 %f %f ]\n", nowPage.getWidth(), nowPage.getHeight()));
			writeAscii("/Resources 2 0 R\n");
			writeAscii(String.format("/Contents %d 0 R\n", id+1));
			writeAscii(">>\n");
			closeObj();
			openObj();
			writeAscii("<<\n");
			writeAscii(String.format("/Length %d\n", 0));
			writeAscii(">>\n");
			writeAscii("stream\n");
			writeAscii("endstream\n");
			closeObj();
		}
	}
	public void drawText(double x, double y, String text) {
	}
	public void setFont() {
	}
	public double getStringWidth() {
		return 0;
	}
	public void drawLine() {
	}
	public void setFillColorRGB(double r, double g, double b) {
	}
	public void setDrawColorRGB(double r, double g, double b) {
	}
	public void setTextColorRGB(double r, double g, double b) {
	}
	public void close() throws IOException {
		closePage();
		putPages();
		putCatalog();
		putResources();
		putInfo();
		putCrossRef();
		stream.flush();
	}
	private void putPages() throws IOException {
		openObj(4);
		writeAscii("<<\n");
		writeAscii("/Type /Pages\n");
		writeAscii("/Kids [");
		for(int i : pageObjIdList) {
			writeAscii(String.format(" %d 0 R", i));
		}
		writeAscii(" ]\n");
		writeAscii(String.format("/Count %d\n", pageObjIdList.size()));
		writeAscii(">>\n");
		closeObj();
	}
	private void putResources() throws IOException {
		openObj(2);
		writeAscii("<<\n");
		writeAscii(">>\n");
		closeObj();
	}
	private void putInfo() throws IOException {
		openObj(1);
		writeAscii("<<\n");
		writeAscii("/Producer (yi-pdf)\n");
		writeAscii(String.format("/CreationDate (D:%s)\n", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())));
		writeAscii(">>\n");
		closeObj();
	}
	private void putCatalog() throws IOException {
		openObj(3);
		writeAscii("<<\n");
		writeAscii("/Type /Catalog\n");
		writeAscii("/Pages 4 0 R\n");
		writeAscii(">>\n");
		closeObj();
	}
	private void putCrossRef() throws IOException {
		int now = streamPos;
		writeAscii("xref\n");
		writeAscii(String.format("0 %d\n", objCount + 1));
		writeAscii(String.format("%010d %05d f\n", 0, 65535));
		for(int i : offsets) {
			writeAscii(String.format("%010d %05d n\n", i, 0));
		}
		writeAscii("trailer\n");
		writeAscii("<<\n");
		writeAscii("/Info 1 0 R\n");
		writeAscii("/Root 3 0 R\n");
		writeAscii(String.format("/Size %d\n", objCount + 1));
		writeAscii(">>\n");
		writeAscii("startxref\n");
		writeAscii(String.format("%d\n", now));
		writeAscii("%%EOF\n");
	}
}
