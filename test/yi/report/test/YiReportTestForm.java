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

import yi.pdf.YiPdfFile;
import yi.report.YiDomNode;
import yi.report.YiReportEngine;
import junit.framework.TestCase;

public class YiReportTestForm extends TestCase {
	private static String readTextFile(String filename) throws FileNotFoundException, IOException {
		File file = new File(filename);
		int size = (int)file.length();
		byte[] buf = new byte[size];
		size = new FileInputStream(file).read(buf);
		return new String(buf, 0, size, Charset.forName("utf-8"));
	}
	public void test1() throws Exception {
		String html = readTextFile("test-input/test-form1.html");
		YiDomNode dom = YiDomNode.parse(html);
		FileOutputStream fos = new FileOutputStream("test-output/test-form1.pdf");
		YiPdfFile pdfFile = new YiPdfFile(fos);
		YiReportEngine.build(dom, pdfFile);
		pdfFile.close();
	}
}
