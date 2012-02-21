package yi.report;

import java.io.IOException;

import yi.pdf.YiPdfColor;
import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfPage;

public class MyLayoutPage extends MyLayoutBlock {
	double pageWidth;
	double pageHeight;
	YiPdfColor backgroundColor;

	private static MyRectSize makeContentRectSize(MyLayoutPageStyle pageStyle) {
		double width = pageStyle.width - pageStyle.pageMargin.left - pageStyle.pageMargin.right;
		double height = pageStyle.height - pageStyle.pageMargin.top - pageStyle.pageMargin.bottom;
		return new MyRectSize(width, height);
	}
	MyLayoutPage(MyLayoutStyle style, MyLayoutPageStyle pageStyle) {
		super(style, makeContentRectSize(pageStyle));
		contentPos = new MyPosition(pageStyle.pageMargin.left, pageStyle.pageMargin.top);
		pageWidth = pageStyle.width;
		pageHeight = pageStyle.height;
		backgroundColor = pageStyle.backgroundColor;
		pageRootFlag = true;
	}
	public void drawPage(YiPdfFile pdfFile) throws IOException {
		YiPdfPage page = pdfFile.newPage(pageWidth, pageHeight);
		page.setBackgroundColor(backgroundColor);
		MyLayoutPageContext pageContext = new MyLayoutPageContext(page);
		draw(pageContext, 0, 0);
		page.close();
	}

}
