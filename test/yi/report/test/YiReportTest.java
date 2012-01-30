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
	}
}
