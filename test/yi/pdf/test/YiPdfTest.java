/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
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
		YiPdfTag p2 = trA.makeChild("TD").makeChild("P").makeChild("Span");
		page.beginTextTag(p2);
		page.drawText(100, 100, "Hello ");
		page.drawText(100, 300, "WorldA!");
		page.endTextTag();
		YiPdfTag p3 = trA.makeChild("TD").makeChild("P").makeChild("Span");
		page.beginTextTag(p3);
		page.drawText(500, 100, "こんにちは、世界B!!");
		page.endTextTag();
		page.setFont(gothicFont);
		YiPdfTag trB = table.makeChild("TR");
		YiPdfTag p4 = trB.makeChild("TD").makeChild("P").makeChild("Span");
		page.beginTextTag(p4);
		page.drawText(100, 200, "Hello WorldC!");
		page.endTextTag();
		YiPdfTag p5 = trB.makeChild("TD").makeChild("P").makeChild("Span");
		page.beginTextTag(p5);
		page.drawText(500, 200, "こんにちは、世界D!!");
		page.endTextTag();
		page = pdf.newPage(600, 800);
		pdf.close();

		stream.close();
	}
	public void test4() throws IOException {
		OutputStream stream = new FileOutputStream("test-output/test4.pdf");
		YiPdfFile pdf = new YiPdfFile(stream);
		YiPdfFont font = new YiPdfJMinchoFont();
		YiPdfFont gothicFont = new YiPdfJGothicFontV();
		YiPdfTag document = pdf.getDocument();

		YiPdfPage page = pdf.newPage(800, 600);
		page.setFont(font);
		page.setFontSize(10.5);
		YiPdfTag nowLine;

		nowLine = document.makeChild("P");
		page.beginTextTag(nowLine.makeChild("Span"));
		page.drawText(100, 60, "タイトル　作者名");
		page.endTextTag();

		nowLine = document.makeChild("P");
		nowLine.makeChild("Span");

		nowLine = document.makeChild("P");
		page.beginTextTag(nowLine.makeChild("Span"));
		page.drawText(100, 120, "　むかしむかしむかしむかし、あるところに、おじいさんとおばあさんと、おとうさんとおかあさん");
		page.endTextTag();
		page.beginTextTag(nowLine.makeChild("Span"));
		page.drawText(100, 140, "と、おにいちゃんとおねえちゃんと、おとうとといもうとと、むすことむすめと、まごとひまごが住ん");
		page.endTextTag();
		page.beginTextTag(nowLine.makeChild("Span"));
		page.drawText(100, 160, "でいました。");
		page.endTextTag();

		nowLine = document.makeChild("P");
		page.beginTextTag(nowLine.makeChild("Span"));
		page.drawText(100, 180, "　おじいちゃんは来年、八百歳を迎える予定です。");
		page.endTextTag();

		nowLine = document.makeChild("P");
		page.beginTextTag(nowLine.makeChild("Span"));
		page.drawText(100, 200, "　ぼくもそろそろ年です。");
		page.endTextTag();

		nowLine = document.makeChild("P");
		page.beginTextTag(nowLine.makeChild("Span"));
		page.drawText(100, 220, "　ひまごが、今度、夫になる男性を連れてくると言っています。今から、やしゃごが生まれるのが楽し");
		page.endTextTag();
		page.beginTextTag(nowLine.makeChild("Span"));
		page.drawText(100, 240, "みです。");
		page.endTextTag();

		pdf.close();

		stream.close();
	}
}
