/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;

import yi.pdf.YiPdfFile;

public class YiReportEngine {
	public static void build(YiDomNode dom, YiPdfFile pdfFile) throws IOException {
		MyDomContext layouter = new MyDomContext();
		layouter.exec(dom, pdfFile);
	}

}
