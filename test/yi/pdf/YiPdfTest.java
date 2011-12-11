package yi.pdf;

import java.io.FileOutputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

public class YiPdfTest extends TestCase {
	public void test1() throws Exception {
		OutputStream stream = new FileOutputStream("test-output/test1.pdf");
		YiPdf pdf = new YiPdf(stream);

		pdf.openPage(800, 600);
		pdf.openPage(600, 800);
		pdf.close();

		stream.close();
	}
}
