package yi.report;

import java.io.IOException;

import yi.pdf.YiPdfColor;
import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfPage;

public class MyLayoutPage extends MyLayoutBlock {
	double pageWidth;
	double pageHeight;
	YiPdfColor backgroundColor;

	private static MyRectSize makeContentRectSize(MyLayoutStyle style) {
		double left = style.getMarginLeft();
		double top = style.getMarginTop();
		double marginRight = style.getMarginRight();
		double marginBottom = style.getMarginBottom();
		double width = style.getPageWidth() - marginRight - left;
		double height = style.getPageHeight() - marginBottom - top;
		return new MyRectSize(width, height);
	}
	MyLayoutPage(MyLayoutStyle style, YiPdfColor backgroundColor) {
		super(style, makeContentRectSize(style));
		contentPos = new MyPosition(style.getMarginLeft(), style.getMarginTop());
		pageWidth = style.getPageWidth();
		pageHeight = style.getPageHeight();
		pageRootFlag = true;
		this.backgroundColor = backgroundColor;
	}
	public void drawPage(YiPdfFile pdfFile) throws IOException {
		YiPdfPage page = pdfFile.newPage(pageWidth, pageHeight);
		page.setBackgroundColor(backgroundColor);
		MyLayoutPageContext pageContext = new MyLayoutPageContext(page);
		draw(pageContext, 0, 0);
		page.close();
	}

}
