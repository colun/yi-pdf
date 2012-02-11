/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.pdf.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

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
		pdf.setCreationDate(new Date(0));

		pdf.newPage(800, 600);
		pdf.newPage(600, 800);
		pdf.close();

		stream.close();
	}
	static YiPdfFont minchoFont = new YiPdfJMinchoFont();
	static YiPdfFont gothicFont = new YiPdfJGothicFontV();
	public void test2() throws IOException {
		OutputStream stream = new FileOutputStream("test-output/test2.pdf");
		YiPdfFile pdf = new YiPdfFile(stream);
		pdf.setCreationDate(new Date(0));

		YiPdfPage page = pdf.newPage(800, 600);
		page.setFont(minchoFont);
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
		pdf.setCreationDate(new Date(0));

		YiPdfTag document = pdf.getDocument();

		YiPdfPage page = pdf.newPage(800, 600);
		page.setFont(minchoFont);
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
		pdf.setCreationDate(new Date(0));

		YiPdfTag document = pdf.getDocument();

		YiPdfPage page = pdf.newPage(800, 600);
		page.setFont(minchoFont);
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
	public void test5() throws IOException {
		OutputStream stream = new FileOutputStream("test-output/test5.pdf");
		YiPdfFile pdf = new YiPdfFile(stream);
		pdf.setCreationDate(new Date(0));

		YiPdfTag document = pdf.getDocument();

		YiPdfPage page1 = pdf.newPage(800, 600);
		page1.setFont(minchoFont);
		page1.setFontSize(10.5);
		YiPdfPage page2 = pdf.newPage(800, 600);
		page2.setFont(minchoFont);
		page2.setFontSize(10.5);

		YiPdfTag nowLine;

		nowLine = document.makeChild("P");
		page1.beginTextTag(nowLine.makeChild("Span"));
		page1.drawText(100, 60, "AAA");
		page1.endTextTag();
		page2.beginTextTag(nowLine.makeChild("Span"));
		page2.drawText(100, 80, "BBB");
		page2.endTextTag();
		page1.beginTextTag(nowLine.makeChild("Span"));
		page1.drawText(100, 100, "CCC");
		page1.endTextTag();
		page2.beginTextTag(nowLine.makeChild("Span"));
		page2.drawText(100, 120, "DDD");
		page2.endTextTag();

		pdf.close();

		stream.close();
	}
	public void test6() throws IOException {
		OutputStream stream = new FileOutputStream("test-output/test6.pdf");
		YiPdfFile pdf = new YiPdfFile(stream);
		pdf.setCreationDate(new Date(0));

		YiPdfTag document = pdf.getDocument();

		YiPdfPage page1 = pdf.newPage(800, 600);
		page1.setFont(minchoFont);
		page1.setFontSize(10.5);
		YiPdfPage page2 = pdf.newPage(800, 600);
		page2.setFont(minchoFont);
		page2.setFontSize(10.5);

		YiPdfTag nowLine;

		nowLine = document.makeChild("P");
		page1.beginTextTag(nowLine.makeChild("Span"));
		page1.drawText(100, 60, "AAA");
		page1.endTextTag();
		page2.beginTextTag(nowLine.makeChild("Span"));
		page2.drawText(100, 80, "BBB");
		page2.endTextTag();
		page1.beginTextTag(nowLine.makeChild("Span"));
		page1.setTextRenderingMode(0);
		page1.drawText(100, 100, "A");
		page1.endTextTag();
		page1.beginTextTag(nowLine.makeChild("Span"));
		page1.setTextRenderingMode(3);
		page1.drawText(120, 100, "B");
		page1.endTextTag();
		page1.beginTextTag(nowLine.makeChild("Span"));
		page1.setTextRenderingMode(0);
		page1.drawText(140, 100, "C");
		page1.endTextTag();
		page2.beginTextTag(nowLine.makeChild("Span"));
		page2.drawText(100, 120, "DDD");
		page2.endTextTag();

		pdf.close();

		stream.close();
	}
	public void test7() throws Exception {
		OutputStream stream = new FileOutputStream("test-output/test7.pdf");
		YiPdfFile pdf = new YiPdfFile(stream);
		pdf.setCreationDate(new Date(0));

		pdf.setInfo("Title", "Title タイトル");
		pdf.setInfo("Author", "Author");
		pdf.setInfo("Subject", "さぶじぇくと");
		pdf.setInfo("Keywords", "key1 key2 キー3 キー4");
		pdf.setInfo("Creator", "クリエイター（情報源の作成プログラム）");
		pdf.setInfo("Producer", "My プロデューサー（変換プログラム）");
		pdf.newPage(800, 600);
		pdf.newPage(600, 800);
		pdf.close();

		stream.close();
	}
}
