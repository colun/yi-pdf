package yi.report;

import yi.pdf.YiPdfFile;

public class YiReportEngine {
	public static void build(YiDomNode dom, YiPdfFile pdfFile) {
		MyHtmlPdfLayouter layouter = new MyHtmlPdfLayouter();
		layouter.exec(dom, pdfFile);
	}

}
