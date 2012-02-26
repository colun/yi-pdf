package yi.pdf.font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfFont;

public class YiPdfTrueTypeFont extends YiPdfFont {
	public YiPdfTrueTypeFont(String path) throws IOException {
		FileInputStream stream = new FileInputStream(path);
		echoHex(stream, 64);
		stream.close();
	}
	private void echoHex(FileInputStream stream, int n) throws IOException {
		String line = "";
		for(int i=0; i<n; ++i) {
			if(i!=0 && (i&15)==0) {
				System.out.println(line);
				line = "";
			}
			int v = stream.read();
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
