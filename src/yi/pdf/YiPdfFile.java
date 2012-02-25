/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class YiPdfFile {
	OutputStream stream;
	int streamPos;
	int objCount;
	int pageCount;
	List<Integer> offsets;
	Map<Integer, Integer> pageId2ObjIdMap = new TreeMap<Integer, Integer>();
	Map<YiPdfPage, Integer> reservedPageMap = new LinkedHashMap<YiPdfPage, Integer>();
	Set<YiPdfFont> fontObjSet;
	YiPdfTag documentTag = null;
	//Map<String, Integer> imageObjIdMap;
	Map<String, String> infoMap = new HashMap<String, String>();
	public YiPdfFile(OutputStream stream) throws IOException {
		this.stream = stream;
		streamPos = 0;
		objCount = 4;
		pageCount = 0;
		offsets = new ArrayList<Integer>();
		for(int i=0; i<objCount; ++i) {
			offsets.add(0);
		}
		fontObjSet = new LinkedHashSet<YiPdfFont>();

		setInfo("Producer", "yi-pdf");
		setCreationDate(new Date());
		//imageObjIdMap = new LinkedHashMap<String, Integer>();
		writeAscii("%PDF-1.4\n%\0\0\0\0\0\0\0\n\n"); // There are seven null characters. Because saying that this file is binary with all 4bytes alignment.
	}
	public void setInfo(String key, String val) {
		infoMap.put(key.toLowerCase(), val);
	}
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	public void setCreationDate(Date date) {
		setInfo("CreationDate", String.format("D:%s%+03d'%02d'", dateFormat.format(date), dateFormat.getTimeZone().getRawOffset() / 3600000, (Math.abs(dateFormat.getTimeZone().getRawOffset()) % 3600000) / 60000));
	}
	int mcIdSequence = 0;
	protected int publishMcId() {
		return mcIdSequence++;
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
	protected int reserveObjId() throws IOException {
		offsets.add(0);
		return ++objCount;
	}
	private void openObj(int i) throws IOException {
		offsets.set(i-1, streamPos);
		writeAscii(String.format("%d 0 obj\n", i));
	}
	private void closeObj() throws IOException {
		writeAscii("endobj\n\n");
	}
	protected void writePage(YiPdfPage page) throws IOException {
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
		pageId2ObjIdMap.put(reservedPageMap.get(page), id);
		writeAscii("<<\n");
		writeAscii("/Type /Page\n");
		writeAscii("/Parent 4 0 R\n");
		writeAscii(String.format("/MediaBox [ 0 0 %f %f ]\n", width, height));
		writeAscii("/Resources 2 0 R\n");
		if(documentTag!=null) {
			writeAscii("/StructParents 0\n");
		}
		writeAscii(String.format("/Contents %d 0 R\n", contentsId));
		writeAscii(">>\n");
		closeObj();
		reservedPageMap.remove(page);
	}
	public void close() throws IOException {
		for(YiPdfPage page : new ArrayList<YiPdfPage>(reservedPageMap.keySet())) {
			page.close();
		}
		putPages();
		putCatalog();
		putResources();
		putInfo();
		putCrossRef();
		stream.flush();
	}
	Set<Integer> leafTagNodeList = new LinkedHashSet<Integer>();
	private int putTag(YiPdfTag tag, int parentId, int pageId) throws IOException {
		int myId = reserveObjId();
		Collection<YiPdfTag> childrenList = tag.getChildrenList();
		List<Integer> childrenIdList = new ArrayList<Integer>();
		for(YiPdfTag child : childrenList) {
			if(child.pageId!=-1) {
				pageId = child.pageId;
			}
			int childId = putTag(child, myId, pageId);
			childrenIdList.add(childId);
		}
		Collection<Integer> mcIdList = tag.getMcIdList();
		if(tag.pageId!=-1) {
			pageId = tag.pageId;
		}

		int aId = 0;
		/*
		String tagName = tag.getTagName();
		if(!"Document".equals(tagName) && !"Span".equals(tagName)) {
			aId = openObj();
			writeAscii("<<\n");
			writeAscii("/O /Layout\n");
			writeAscii("TD".equals(tagName) ? "/Placement /Inline\n" : "/Placement /Block\n");
			writeAscii(">>\n");
			closeObj();
		}
		//*/

		openObj(myId);
		writeAscii("<<\n");
		writeAscii("/Type /StructElem\n");
		writeAscii(String.format("/S /%s\n", tag.getTagName()));
		writeAscii(String.format("/P %d 0 R\n", parentId));
		writeAscii(String.format("/Pg %d 0 R\n", pageId2ObjIdMap.get(pageId)));
		if(aId!=0) {
			writeAscii(String.format("/A %d 0 R\n", aId));
		}
		if(!mcIdList.isEmpty()) {
			writeAscii("/K [");
			for(int mcId : mcIdList) {
				if(mcId < 0) {
					int childId = childrenIdList.get(-1 - mcId);
					writeAscii(String.format(" %d 0 R", childId));
				}
				else {
					leafTagNodeList.add(myId);
					writeAscii(String.format(" %d", mcId));
				}
			}
			writeAscii(" ]\n");
		}
		
		writeAscii(">>\n");
		closeObj();
		return myId;
	}
	private int putStructTree() throws IOException {
		if(documentTag==null) {
			return 0;
		}
		int rootId = reserveObjId();
		int docId = putTag(documentTag, rootId, 0);
		openObj(rootId);
		writeAscii("<<\n");
		writeAscii("/Type /StructTreeRoot\n");
		writeAscii("/RoleMap <<\n");
		writeAscii("/Document /Document\n");
		writeAscii("/Div /Div\n");
		writeAscii("/H /H\n");
		writeAscii("/H1 /H1\n");
		writeAscii("/H2 /H2\n");
		writeAscii("/H3 /H3\n");
		writeAscii("/H4 /H4\n");
		writeAscii("/H5 /H5\n");
		writeAscii("/H6 /H6\n");
		writeAscii("/P /P\n");
		writeAscii("/L /L\n");
		writeAscii("/LI /LI\n");
		writeAscii("/Lbl /Lbl\n");
		writeAscii("/LBody /LBody\n");
		writeAscii("/Span /Span\n");
		writeAscii("/Table /Table\n");
		writeAscii("/TR /TR\n");
		writeAscii("/TH /TH\n");
		writeAscii("/TD /TD\n");
		writeAscii(">>\n");
		int parentId = 0;
		if(!leafTagNodeList.isEmpty()) {
			parentId = reserveObjId();
			writeAscii(String.format("/ParentTree %d 0 R\n", parentId));
		}
		writeAscii(String.format("/K [ %d 0 R ]\n", docId));
		writeAscii(">>\n");
		closeObj();
		if(parentId!=0) {
			openObj(parentId);
			writeAscii("<< /Nums [\n");
			writeAscii("0 [");
			for(int id : leafTagNodeList) {
				writeAscii(String.format(" %d 0 R", id));
			}
			writeAscii(" ]\n");
			writeAscii("] >>\n");
			closeObj();
		}
		return rootId;
	}
	private void putPages() throws IOException {
		openObj(4);
		writeAscii("<<\n");
		writeAscii("/Type /Pages\n");
		writeAscii("/Kids [");
		for(int i : pageId2ObjIdMap.values()) {
			writeAscii(String.format(" %d 0 R", i));
		}
		writeAscii(" ]\n");
		writeAscii(String.format("/Count %d\n", pageId2ObjIdMap.size()));
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
		writeAscii("/Flags 6\n");
		writeAscii(String.format("/FontBBox [ 0 %d 1000 %d ]\n", font.getDescent(), font.getAscent()));
		writeAscii("/ItalicAngle 0\n");
		writeAscii(String.format("/Ascent %d\n", font.getAscent()));//TODO
		writeAscii(String.format("/Descent %d\n", font.getDescent()));//TODO
		writeAscii("/Leading 0\n");//TODO
		writeAscii(String.format("/CapHeight %d\n", font.getAscent()));//TODO
		writeAscii(String.format("/XHeight %d\n", font.getXHeight()));//TODO
		writeAscii("/StemV 92\n");//TODO
		writeAscii("/StemH 92\n");//TODO
		writeAscii("/AvgWidth 507\n");//TODO
		writeAscii("/MaxWidth 1000\n");//TODO
		writeAscii("/MissingWidth 507\n");//TODO
		writeAscii(String.format("/Style << /Panose <%s> >>\n", font.getPanose()));
		writeAscii(">>\n");
		closeObj();
		int cidId = openObj();
		writeAscii("<<\n");
		writeAscii("/Type /Font\n");
		writeAscii("/Subtype /CIDFontType0\n");
		writeAscii(String.format("/BaseFont /%s\n", familyName));
		writeAscii("/CIDSystemInfo\n");
		writeAscii("<<\n");
		writeAscii("/Registry(Adobe)\n");
		writeAscii("/Ordering(Japan1)\n");
		writeAscii("/Supplement 4\n");//TODO
		writeAscii(">>\n");
		writeAscii(String.format("/FontDescriptor %d 0 R\n", descriptorId));
		writeAscii("/DW 1000\n");
		writeAscii("/W\n");//TODO
		writeAscii("[\n");
		writeAscii("1 632 500\n");
		writeAscii("]\n");
		writeAscii(String.format("/DW2 [ %d %d ]\n", font.getAscent(), font.getDescent() - font.getAscent()));
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
	private void putInfoItem(String itemName) throws IOException {
		String str = infoMap.get(itemName.toLowerCase());
		if(str==null) {
			return;
		}
		boolean asciiFlag = true;
		for(int i=0; i<str.length(); ++i) {
			char c = str.charAt(i);
			if(c=='\\' || c=='(' || c==')' || c=='\r' || c<0 || 128<=c) {
				asciiFlag = false;
				break;
			}
		}
		if(asciiFlag) {
			writeAscii(String.format("/%s (%s)\n", itemName, str));
		}
		else {
			StringBuilder builder = new StringBuilder();
			builder.append("FEFF");
			for(int i=0; i<str.length(); ++i) {
				char c = str.charAt(i);
				builder.append(String.format("%04X", (int)c & 0xFFFF));
			}
			writeAscii(String.format("/%s <%s>\n", itemName, builder.toString()));
		}
	}
	private void putInfo() throws IOException {
		openObj(1);
		writeAscii("<<\n");
		putInfoItem("Title");
		putInfoItem("Author");
		putInfoItem("Subject");
		putInfoItem("Keywords");
		putInfoItem("Creator");
		putInfoItem("Producer");
		putInfoItem("CreationDate");
		writeAscii(">>\n");
		closeObj();
	}
	private void putCatalog() throws IOException {
		int stId = putStructTree();
		openObj(3);
		writeAscii("<<\n");
		writeAscii("/Type /Catalog\n");
		writeAscii("/Pages 4 0 R\n");
		if(stId!=0) {
			writeAscii(String.format("/StructTreeRoot %d 0 R\n", stId));
			writeAscii("/MarkInfo << /Marked true >>\n");
		}
		writeAscii(">>\n");
		closeObj();
	}
	private void putCrossRef() throws IOException {
		int now = streamPos;
		writeAscii("xref\n");
		writeAscii(String.format("0 %d\n", objCount + 1));
		writeAscii(String.format("%010d %05d f \n", 0, 65535));
		for(int i : offsets) {
			writeAscii(String.format("%010d %05d n \n", i, 0));
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
	public YiPdfTag getDocument() {
		if(documentTag==null) {
			documentTag = new YiPdfTag(this, null, "Document");
		}
		return documentTag;
	}
	public YiPdfPage newPage(double width, double height) {
		int pageId = pageCount++;
		YiPdfPage page = new YiPdfPage(this, pageId, width, height);
		reservedPageMap.put(page, pageId);
		return page;
	}
}
