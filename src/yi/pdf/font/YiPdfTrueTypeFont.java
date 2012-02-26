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
	private void readCmap(RandomAccessFile file, long checkSum, long offset, long length) throws IOException {
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
		echoHex(file, (int)Math.min(length, 64));
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
			System.out.printf("[Tag: %s, chkSum: %d, offset: %d, length: %d]\n", tag, values[0], values[1], values[2]);
			file.seek(values[1]);
			if("cmap".equals(tag)) {
				readCmap(file, values[0], values[1], values[2]);
			}
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
				return name.endsWith(".ttf");
			}
		})) {
			String path = dir + "/" + filename;
			System.out.println(path);
			new YiPdfTrueTypeFont(path);
		}

	}

}
