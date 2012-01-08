package yi.pdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import yi.pdf.font.YiPdfJGothicFontV;
import yi.pdf.font.YiPdfJMinchoFont;

import junit.framework.TestCase;

public class YiPdfTest extends TestCase {
	public void test1() throws Exception {
		OutputStream stream = new FileOutputStream("test-output/test1.pdf");
		YiPdfFile pdf = new YiPdfFile(stream);

		YiPdfPage page = new YiPdfPage(800, 600);
		pdf.writePage(page);
		page = new YiPdfPage(600, 800);
		pdf.writePage(page);
		pdf.close();

		stream.close();
	}
	public void test2() throws IOException {
		OutputStream stream = new FileOutputStream("test-output/test2.pdf");
		YiPdfFile pdf = new YiPdfFile(stream);
		YiPdfFont font = new YiPdfJMinchoFont();
		YiPdfFont gothicFont = new YiPdfJGothicFontV();

		YiPdfPage page = new YiPdfPage(800, 600);
		page.setFont(font);
		page.setFontSize(10.5);
		page.drawText(100, 100, "Hello World!");
		page.drawText(100, 150, "こんにちは、世界!!");
		page.setFont(gothicFont);
		page.drawText(100, 200, "Hello World!");
		page.drawText(150, 200, "こんにちは、世界!!");
		pdf.writePage(page);
		pdf.writePage(page);
		page = new YiPdfPage(600, 800);
		pdf.writePage(page);
		pdf.close();

		stream.close();
	}
}
