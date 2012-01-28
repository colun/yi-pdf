package yi.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
	YiPdfTag documentTag;
	//Map<String, Integer> imageObjIdMap;
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
		documentTag = new YiPdfTag(this, "document");
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
	protected int reserveObjId() throws IOException {
		offsets.add(0);
		return ++objCount;
	}
	private void openObj(int i) throws IOException {
		offsets.set(i-1, streamPos);
		writeAscii(String.format("%d 0 obj\n", i));
	}
	private void closeObj() throws IOException {
		writeAscii("endobj\n");
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
		writeAscii("/StructParents 0\n");
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
	private int putTag(YiPdfTag tag, int parentId) throws IOException {
		int myId = reserveObjId();
		Collection<YiPdfTag> childrenList = tag.getChildrenList();
		List<Integer> childrenIdList = new ArrayList<Integer>();
		for(YiPdfTag child : childrenList) {
			int childId = putTag(child, myId);
			childrenIdList.add(childId);
		}
		Collection<Integer> mcIdList = tag.getMcIdList();
		openObj(myId);
		writeAscii("<<\n");
		writeAscii("/Type /StructElem\n");
		writeAscii(String.format("/S /%s\n", tag.getTagName()));
		writeAscii(String.format("/P /%d\n", parentId));
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
		
		writeAscii(">>\n");
		closeObj();
		return myId;
	}
	private int putStructTree() throws IOException {
		int rootId = reserveObjId();
		int docId = putTag(documentTag, rootId);
		openObj(rootId);
		writeAscii("<<\n");
		writeAscii("/Type /StructTreeRoot\n");
		writeAscii("RoleMap <<\n");
		writeAscii("/Document /Document\n");
		writeAscii("/Table /Table\n");
		writeAscii("/TR /TR\n");
		writeAscii("/TD /TD\n");
		writeAscii("/TH /TH\n");
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
			writeAscii("<<\n");
			writeAscii("/Nums 0 [");
			for(int id : leafTagNodeList) {
				writeAscii(String.format(" %d 0 R", id));
			}
			writeAscii(" ]\n");
			writeAscii(">>\n");
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
		writeAscii("/FontBBox [ -150 -347 1100 853 ]\n");//TODO
		writeAscii("/ItalicAngle 0\n");//TODO
		writeAscii("/Ascent 853\n");//TODO
		writeAscii("/Descent -347\n");//TODO
		writeAscii("/Leading 0\n");//TODO
		writeAscii("/CapHeight 853\n");//TODO
		writeAscii("/XHeight 597\n");//TODO
		writeAscii("/StemV 92\n");//TODO
		writeAscii("/StemH 92\n");//TODO
		writeAscii("/AvgWidth 507\n");//TODO
		writeAscii("/MaxWidth 1000\n");//TODO
		writeAscii("/MissingWidth 507\n");//TODO
		writeAscii("/Style << /Panose <08 05 02 0B 06 09 00 00 00 00 00 00> >>\n");//TODO
		writeAscii(">>\n");
		closeObj();
		int cidId = openObj();
		writeAscii("<<\n");
		writeAscii("/Type /Font\n");
		writeAscii("/Subtype /CIDFontType2\n");
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
		writeAscii("/DW2 [ 880 -1200 ]\n");
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
		//int parentTreeId = openObj();
		//closeObj();
		int stId = putStructTree();
		openObj(3);
		writeAscii("<<\n");
		writeAscii("/Type /Catalog\n");
		writeAscii("/Pages 4 0 R\n");
		writeAscii(String.format("/StructTreeRoot %d 0 R\n", stId));
		writeAscii("/MarkInfo << /Marked true >>\n");
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
	public YiPdfTag getDocument() {
		return documentTag;
	}
	public YiPdfPage newPage(double width, double height) {
		YiPdfPage page = new YiPdfPage(this, width, height);
		reservedPageMap.put(page, ++pageCount);
		return page;
	}
}
