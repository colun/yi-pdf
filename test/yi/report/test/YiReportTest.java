/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import yi.pdf.YiPdfFile;
import yi.report.YiDomNode;
import yi.report.YiReportEngine;
import junit.framework.TestCase;

public class YiReportTest extends TestCase {
	public void test1() throws Exception {
		String html = readTextFile("test-input/test-report1.html");
		YiDomNode dom = YiDomNode.parse(html);
		for(YiDomNode node : dom.getChildren()) {
			showNode(node, 0);
		}
	}
	private static String readTextFile(String filename) throws FileNotFoundException, IOException {
		File file = new File(filename);
		int size = (int)file.length();
		byte[] buf = new byte[size];
		size = new FileInputStream(file).read(buf);
		return new String(buf, 0, size, Charset.forName("utf-8"));
	}
	private static void showNode(YiDomNode nowNode, int tab) {
		for(int i=0; i<tab; ++i) {
			System.out.print(' ');
		}
		switch(nowNode.getNodeType()) {
		case YiDomNode.TYPE_OF_TEXT:
			System.out.printf("%s\n", nowNode.getText());
			break;
		case YiDomNode.TYPE_OF_TAG:
		case YiDomNode.TYPE_OF_EXT_TAG:
			System.out.printf("<%s%s", nowNode.getNodeType()==YiDomNode.TYPE_OF_EXT_TAG ? "!" : "", nowNode.getTagName());
			Map<String, String> attr = nowNode.getAttr();
			for(String attrName : attr.keySet()) {
				System.out.printf(" %s", attrName);
				String val = attr.get(attrName);
				if(val!=null) {
					System.out.printf("=\"%s\"", val);
				}
			}
			if(nowNode.getChildren()==null) {
				System.out.print(" /");
			}
			System.out.println('>');
		}
		if(nowNode.getChildren()!=null) {
			for(YiDomNode node : nowNode.getChildren()) {
				showNode(node, tab + 2);
			}
			for(int i=0; i<tab; ++i) {
				System.out.print(' ');
			}
			System.out.printf("</%s>\n", nowNode.getTagName());
		}
	}
	public void test2() throws Exception {
		String html = readTextFile("test-input/test-report1.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report2.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test3() throws Exception {
		String html = readTextFile("test-input/test-report3.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report3.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test4() throws Exception {
		String html = readTextFile("test-input/test-report4.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report4.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test5() throws Exception {
		String html = readTextFile("test-input/test-report5.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report5.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test6() throws Exception {
		String html = readTextFile("test-input/test-report6.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report6.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test7() throws Exception {
		String html = readTextFile("test-input/test-report7.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report7.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test8() throws Exception {
		String html = readTextFile("test-input/test-report8.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report8.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test9() throws Exception {
		String html = readTextFile("test-input/test-report9.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report9.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test10() throws Exception {
		String html = readTextFile("test-input/test-report10.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report10.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test11() throws Exception {
		String html = readTextFile("test-input/test-report11.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report11.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test12() throws Exception {
		String html = readTextFile("test-input/test-report12.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report12.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test13() throws Exception {
		String html = readTextFile("test-input/test-report13.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report13.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test14() throws Exception {
		String html = readTextFile("test-input/test-report14.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report14.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test15() throws Exception {
		String html = readTextFile("test-input/test-report15.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report15.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test16() throws Exception {
		String html = readTextFile("test-input/test-report16.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report16.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test17() throws Exception {
		String html = readTextFile("test-input/test-report17.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report17.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test18() throws Exception {
		String html = readTextFile("test-input/test-report18.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report18.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test19() throws Exception {
		String html = readTextFile("test-input/test-report19.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report19.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test20() throws Exception {
		String html = readTextFile("test-input/test-report20.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report20.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test21() throws Exception {
		String html = readTextFile("test-input/test-report21.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report21.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test22() throws Exception {
		String html = readTextFile("test-input/test-report22.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report22.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test23() throws Exception {
		String html = readTextFile("test-input/test-report23.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report23.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
	public void test24() throws Exception {
		String html = readTextFile("test-input/test-report24.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-report24.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
}
