package yi.pdf.font;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfFont;

public class YiPdfTrueTypeFont extends YiPdfFont {
	private char[] readCmap(RandomAccessFile file, long checkSum, long offset, long length) throws IOException {
		int version = file.readUnsignedShort();
		int numCmaps = file.readUnsignedShort();
		long unicodeOffset = -1;
		for(int i=0; i<numCmaps; ++i) {
			int platformId = file.readUnsignedShort();
			int encodingId = file.readUnsignedShort();
			long subOffset = file.readInt() & 0xFFFFFFFFL;
			if(platformId==0 && encodingId==3 || platformId==3 && encodingId==1) {
				unicodeOffset = subOffset;
				break;
			}
		}
		assert(unicodeOffset!=-1) : "UnicodeのCmapが見つからなかった";
		file.seek(offset + unicodeOffset);
		int subFormat = file.readUnsignedShort();
		assert(subFormat==4);
		int subLength = file.readUnsignedShort();
		int language = file.readUnsignedShort();
		assert(language==0);
		int segCount2 = file.readUnsignedShort();
		assert((segCount2&1)==0);
		int segCount = segCount2 / 2;
		int searchRange = file.readUnsignedShort();
		assert((searchRange & (searchRange-1))==0);
		assert(searchRange<=segCount2 && segCount<searchRange);
		int entrySelector = file.readUnsignedShort();
		assert((1 << (entrySelector+1))==searchRange);
		int rangeShift = file.readUnsignedShort();
		assert(rangeShift==segCount2-searchRange);
		int[] endCount = new int[segCount];
		for(int i=0; i<segCount; ++i) {
			endCount[i] = file.readUnsignedShort();
		}
		int reservedPad = file.readUnsignedShort();
		assert(reservedPad==0);
		int[] startCount = new int[segCount];
		for(int i=0; i<segCount; ++i) {
			startCount[i] = file.readUnsignedShort();
		}
		int[] idDelta = new int[segCount];
		for(int i=0; i<segCount; ++i) {
			idDelta[i] = file.readShort();
		}
		int[] idRangeOffset = new int[segCount];
		for(int i=0; i<segCount; ++i) {
			idRangeOffset[i] = file.readUnsignedShort();
		}
		char[] cmap = new char[65536];
		long basePos = file.getFilePointer();
		for(int i=0; i<segCount; ++i) {
			if(idRangeOffset[i]!=0) {
				assert(idDelta[i]==0);
				assert((idRangeOffset[i]&1)==0);
				assert(16 + 6 * segCount + idRangeOffset[i] + i + i + (endCount[i] - startCount[i])*2<subLength-1);
				file.seek(basePos + idRangeOffset[i] + i + i - segCount2);
				for(int c=startCount[i], e=endCount[i]; c<=e; ++c) {
					cmap[c] = (char)file.readShort();
				}
			}
			else {
				int d = idDelta[i];
				for(int c=startCount[i], e=endCount[i]; c<=e; ++c) {
					cmap[c] = (char)(c + d);
				}
			}
		}
		file.seek(basePos);
		return cmap;
	}
	protected void readHead(RandomAccessFile file, long checkSum, long offset, long length) throws IOException {
		int version = file.readInt();
		int fontRevision = file.readInt();
		long checkSumAdj = file.readInt() & 0xFFFFFFFFL;
		long magicNumber = file.readInt() & 0xFFFFFFFFL;
		assert(magicNumber==0x5F0F3CF5);
		int flags = file.readUnsignedShort();
		int unitPerEm = file.readUnsignedShort();
		assert(16<=unitPerEm && unitPerEm<=16384);
		System.out.printf("flags: %d, unitPerEm: %d\n", flags, unitPerEm);
		long created = file.readLong();
		long modified = file.readLong();
		int xMin = file.readShort();
		int yMin = file.readShort();
		int xMax = file.readShort();
		int yMax = file.readShort();
		int macStyle = file.readUnsignedShort();
		int lowestRecPPEM = file.readUnsignedShort();
		int fontDirectionHint = file.readShort();
		int indexToLocFormat = file.readShort();
		int glyphDataFormat = file.readShort();
		echoHex(file, 128);
	}
	protected void readGryf(RandomAccessFile file, long checkSum, long offset, long length) throws IOException {
		//echoHex(file, 128);
	}
	public YiPdfTrueTypeFont(String path) throws IOException {
		RandomAccessFile file = new RandomAccessFile(path, "r");
		Map<String, long[]> dic = new LinkedHashMap<String, long[]>();
		file.skipBytes(4);
		int numTables = file.readUnsignedShort();
		System.out.printf("numTables = %d\n", numTables);
		file.skipBytes(6);
		for(int i=0; i<numTables; ++i) {
			byte[] buf = new byte[4];
			file.readFully(buf);
			String tag = new String(buf);
			long checkSum = file.readInt() & 0xFFFFFFFFL;
			long offset = file.readInt() & 0xFFFFFFFFL;
			long length = file.readInt() & 0xFFFFFFFFL;
			dic.put(tag, new long[] {checkSum, offset, length});
		}
		for(String tag : dic.keySet()) {
			long[] values = dic.get(tag);
			//System.out.printf("[Tag: %s, chkSum: %d, offset: %d, length: %d]\n", tag, values[0], values[1], values[2]);
		}
		{
			long[] values = dic.get("cmap");
			assert(values!=null);
			file.seek(values[1]);
			readCmap(file, values[0], values[1], values[2]);
		}
		{
			long[] values = dic.get("head");
			assert(values!=null);
			file.seek(values[1]);
			readHead(file, values[0], values[1], values[2]);
		}
		file.close();
	}
	private void echoHex(RandomAccessFile file, int n) throws IOException {
		String line = "";
		for(int i=0; i<n; ++i) {
			if(i!=0 && (i&15)==0) {
				System.out.println(line);
				line = "";
			}
			int v = file.readByte() & 0x00FF;
			System.out.printf("%02X ", v);
			if(0x20<=v && v<=0x7E) {
				line += (char)v;
			}
			else {
				line += '.';
			}
		}
		System.out.println(line);
	}

	@Override
	public byte[] encode(String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTravel(char c) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getLowerPerpend(char c) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getUpperPerpend(char c) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isVertical() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected int putSelf(YiPdfFile pdfFile) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String dir = "font";
		for(String filename : new File(dir).list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".ttf") || name.endsWith(".otf");
			}
		})) {
			String path = dir + "/" + filename;
			System.out.println(path);
			new YiPdfTrueTypeFont(path);
		}

	}

}
