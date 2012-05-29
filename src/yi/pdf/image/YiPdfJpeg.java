package yi.pdf.image;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import yi.pdf.YiPdfImage;

public class YiPdfJpeg extends YiPdfImage {
	private final void copyStream(InputStream inputStream, OutputStream outputStream, byte[] buf, int count) throws IOException {
		while(0<count) {
			int v = inputStream.read(buf, 0, count);
			if(v<0) {
				throw new RuntimeException("不正なJPEG");
			}
			outputStream.write(buf, 0, v);
			count -= v;
		}
	}
	public YiPdfJpeg(InputStream inputStream) throws IOException {
		DataInputStream stream = (inputStream instanceof DataInputStream) ? (DataInputStream)inputStream : new DataInputStream(inputStream);
		boolean loopFlag = true;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream outStream = new DataOutputStream(byteArrayOutputStream);
		byte[] buf = new byte[65536];
		while(loopFlag) {
			int marker;
			try {
				marker = ((int)stream.readShort()) & 0xFFFF;
				outStream.writeShort(marker);
			}
			catch(IOException e) {
				loopFlag = false;
				break;
			}
			switch(marker) {
			case 0xFF01:
			case 0xFFD0: case 0xFFD1: case 0xFFD2: case 0xFFD3:
			case 0xFFD4: case 0xFFD5: case 0xFFD6: case 0xFFD7:
			case 0xFFD8:
				break;
			case 0xFFDA:
			case 0xFFD9:
				loopFlag = false;
				break;
			case 0xFFE0: case 0xFFE1: case 0xFFE2: case 0xFFE3:
			case 0xFFE4: case 0xFFE5: case 0xFFE6: case 0xFFE7:
			case 0xFFE8: case 0xFFE9: case 0xFFEA: case 0xFFEB:
			case 0xFFEC: case 0xFFED: case 0xFFEE:
			case 0xFFDB:
			case 0xFFC4:
			case 0xFFDD:
			case 0xFFFE:
				{
					int skip = stream.readShort();
					outStream.writeShort(skip);
					skip -= 2;
					copyStream(stream, outStream, buf, skip);
				}
				break;
			case 0xFFC0://サイズとかの情報
			case 0xFFC1://サイズとかの情報
			case 0xFFC2://サイズとかの情報
				int skip = stream.readShort();
				outStream.writeShort(skip);
				skip -= 2;
				if(stream.readByte()!=0x08) {
					throw new RuntimeException("不正なJPEG");
				}
				outStream.writeByte(0x08);
				int height = stream.readShort();
				int width = stream.readShort();
				outStream.writeByte(height);
				outStream.writeByte(width);
				setHeight(height);
				setWidth(width);
				skip -= 5;
				copyStream(stream, outStream, buf, skip);
				break;
			case 0xFF00://???
			default:
				throw new RuntimeException("不正なJPEG");
			}
		}
		while(true) {
			int v = inputStream.read(buf);
			if(v<0) {
				break;
			}
			outStream.write(buf, 0, v);
		}
		setData(byteArrayOutputStream.toByteArray());
	}
}
