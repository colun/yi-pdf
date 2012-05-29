/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report.image;

import java.net.URL;

import yi.pdf.YiPdfImage;

import junit.framework.TestCase;

public class JpegTest extends TestCase {
	public void test1() throws Exception {
		URL url = new URL("file:./test-input/test.jpg");
		YiPdfImage image = YiPdfImage.getInstance(url);
		System.out.println(image.getWidth());
		System.out.println(image.getHeight());
	}
}
