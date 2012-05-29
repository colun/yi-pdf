package yi.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import yi.pdf.image.YiPdfJpeg;

public class YiPdfImage extends YiPdfResource {
	private int width;
	private int height;
	private byte[] data;
	public final int getWidth() {
		return width;
	}
	public final int getHeight() {
		return height;
	}
	public final byte[] getData() {
		return data;
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
}
