package yi.report;

import yi.pdf.YiPdfFile;

public class YiReportEngine {
	public static void build(YiDomNode dom, YiPdfFile pdfFile) {
		MyDomContext layouter = new MyDomContext();
		layouter.exec(dom, pdfFile);
	}

}
