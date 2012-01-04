package yi.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class YiPdfFile {
	OutputStream stream;
	int streamPos;
	int objCount;
	List<Integer> offsets;
	List<Integer> pageObjIdList;
	Set<YiPdfFont> fontObjSet;
	//Map<String, Integer> imageObjIdMap;
	public YiPdfFile(OutputStream stream) throws IOException {
		this.stream = stream;
		streamPos = 0;
		objCount = 4;
		offsets = new ArrayList<Integer>();
		for(int i=0; i<objCount; ++i) {
			offsets.add(0);
		}
		pageObjIdList = new ArrayList<Integer>();
		fontObjSet = new LinkedHashSet<YiPdfFont>();
		//imageObjIdMap = new LinkedHashMap<String, Integer>();
		writeAscii("%PDF-1.4\n\0\0\0\0\0\0\0\n"); // There are seven null characters. Because saying that this file is binary with all 4bytes alignment.
	}
	private static byte[] toBytesFromAscii(String str) {
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
	public void writePage(YiPdfPage page) throws IOException {
		assert(page!=null);
		double width = page.getWidth();
		double height = page.getHeight();
		byte[] streamBytes = page.getStreamBytes();
		Set<YiPdfFont> pageFontSet = page.getFontSet();
		if(pageFontSet!=null) {
			fontObjSet.addAll(pageFontSet);
		}

		int contentsId = openObj();
		writeAscii("<<\n");
		writeAscii(String.format("/Length %d\n", streamBytes!=null ? streamBytes.length : 0));
		writeAscii(">>\n");
		writeAscii("stream\n");
		if(streamBytes!=null) {
			write(streamBytes);
		}
		writeAscii("endstream\n");
		closeObj();
		int id = openObj();
		pageObjIdList.add(id);
		writeAscii("<<\n");
		writeAscii("/Type /Page\n");
		writeAscii("/Parent 4 0 R\n");
		writeAscii(String.format("/MediaBox [ 0 0 %f %f ]\n", width, height));
		writeAscii("/Resources 2 0 R\n");
		writeAscii(String.format("/Contents %d 0 R\n", contentsId));
		writeAscii(">>\n");
		closeObj();
	}
	public void close() throws IOException {
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
	private int putFont(YiPdfFont font) throws IOException {
		String familyName = font.getFontName();
		String encoding = font.getEncoding();
		int descriptorId = openObj();
		writeAscii("<<\n");
		writeAscii("/Type /FontDescriptor\n");
		writeAscii(String.format("/FontName /%s\n", familyName));
		writeAscii("/Flags 39\n");//TODO
		writeAscii("/FontBBox [ -150 -147 1100 853 ]\n");//TODO
		writeAscii("/MissingWidth 507\n");//TODO
		writeAscii("/StemV 92\n");//TODO
		writeAscii("/StemH 92\n");//TODO
		writeAscii("/ItalicAngle 0\n");//TODO
		writeAscii("/CapHeight 853\n");//TODO
		writeAscii("/XHeight 597\n");//TODO
		writeAscii("/Ascent 853\n");//TODO
		writeAscii("/Descent -147\n");//TODO
		writeAscii("/Leading 0\n");//TODO
		writeAscii("/MaxWidth 1000\n");//TODO
		writeAscii("/AvgWidth 507\n");//TODO
		writeAscii("/Style << /Panose <0805020B0609000000000000> >>\n");//TODO
		writeAscii(">>\n");
		closeObj();
		int cidId = openObj();
		writeAscii("<<\n");
		writeAscii("/Type /Font\n");
		writeAscii("/Subtype /CIDFontType2\n");
		writeAscii(String.format("/BaseFont /%s\n", familyName));
		writeAscii("/WinCharSet 128\n");//TODO
		writeAscii(String.format("/FontDescriptor %d 0 R\n", descriptorId));
		writeAscii("/CIDSystemInfo\n");
		writeAscii("<<\n");
		writeAscii("/Registry(Adobe)\n");//TODO
		writeAscii("/Ordering(Japan1)\n");//TODO
		writeAscii("/Supplement 2\n");//TODO
		writeAscii(">>\n");
		writeAscii("/DW 1000\n");//TODO
		writeAscii("/W\n");//TODO
		writeAscii("[\n");
		writeAscii("231 389 500\n");
		writeAscii("631 631 500\n");
		writeAscii("]\n");
		writeAscii(">>\n");
		closeObj();
		int id = openObj();
		writeAscii("<<\n");
		writeAscii("/Type /Font\n");
		writeAscii("/Subtype /Type0\n");
		writeAscii(String.format("/BaseFont /%s-%s\n", familyName, encoding));
		writeAscii(String.format("/DescendantFonts [ %d 0 R ]\n", cidId));
		writeAscii(String.format("/Encoding /%s\n", encoding));
		writeAscii(">>\n");
		closeObj();
		return id;
	}
	private void putResources() throws IOException {
		StringBuilder fontStringLine = null;
		if(!fontObjSet.isEmpty()) {
			fontStringLine = new StringBuilder();
			fontStringLine.append("/Font <<");
			for(YiPdfFont font : fontObjSet) {
				int resourceId = font.getResourceId();
				int objId = putFont(font);
				fontStringLine.append(String.format(" /F%d %d 0 R", resourceId, objId));
			}
			fontStringLine.append(" >>\n");
		}
		openObj(2);
		writeAscii("<<\n");
		if(fontStringLine!=null) {
			writeAscii(fontStringLine.toString());
		}
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
