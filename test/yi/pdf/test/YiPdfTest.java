package yi.pdf.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfFont;
import yi.pdf.YiPdfPage;
import yi.pdf.YiPdfTag;
import yi.pdf.font.YiPdfJGothicFontV;
import yi.pdf.font.YiPdfJMinchoFont;

import junit.framework.TestCase;

public class YiPdfTest extends TestCase {
	public void test1() throws Exception {
		OutputStream stream = new FileOutputStream("test-output/test1.pdf");
		YiPdfFile pdf = new YiPdfFile(stream);

		YiPdfPage page = pdf.newPage(800, 600);
		page = pdf.newPage(600, 800);
		pdf.close();

		stream.close();
	}
	public void test2() throws IOException {
		OutputStream stream = new FileOutputStream("test-output/test2.pdf");
		YiPdfFile pdf = new YiPdfFile(stream);
		YiPdfFont font = new YiPdfJMinchoFont();
		YiPdfFont gothicFont = new YiPdfJGothicFontV();

		YiPdfPage page = pdf.newPage(800, 600);
		page.setFont(font);
		page.setFontSize(10.5);
		page.drawText(100, 100, "Hello World!");
		page.drawText(100, 150, "こんにちは、世界!!");
		page.setFont(gothicFont);
		page.drawText(100, 200, "Hello World!");
		page.drawText(150, 200, "こんにちは、世界!!");
		page = pdf.newPage(600, 800);
		pdf.close();

		stream.close();
	}
	public void test3() throws IOException {
		OutputStream stream = new FileOutputStream("test-output/test3.pdf");
		YiPdfFile pdf = new YiPdfFile(stream);
		YiPdfFont font = new YiPdfJMinchoFont();
		YiPdfFont gothicFont = new YiPdfJGothicFontV();
		YiPdfTag document = pdf.getDocument();

		YiPdfPage page = pdf.newPage(800, 600);
		page.setFont(font);
		page.setFontSize(10.5);
		YiPdfTag table = document.makeChild("Table");
		YiPdfTag trA = table.makeChild("TR");
		YiPdfTag tdA1 = trA.makeChild("TD");
		page.drawText(100, 100, "Hello World!", tdA1);
		YiPdfTag tdA2 = trA.makeChild("TD");
		page.drawText(100, 150, "こんにちは、世界!!", tdA2);
		page.setFont(gothicFont);
		YiPdfTag trB = table.makeChild("TR");
		YiPdfTag tdB1 = trB.makeChild("TD");
		page.drawText(100, 200, "Hello World!", tdB1);
		YiPdfTag tdB2 = trB.makeChild("TD");
		page.drawText(150, 200, "こんにちは、世界!!", tdB2);
		page = pdf.newPage(600, 800);
		pdf.close();

		stream.close();
	}
}
