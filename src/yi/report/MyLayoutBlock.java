/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfPage;

class MyLayoutBlock {
	boolean verticalWritingMode;
	double pageWidth;
	double pageHeight;
	double contentLeft;
	double contentTop;
	double contentRight;
	double contentBottom;
	double contentTravel;
	//double contentHeight;
	double divePos;
	double diveEnd;
	boolean pageRootFlag;
	Stack<MyPair<Double, Double>> earthStack = new Stack<MyPair<Double,Double>>();//earth = left or top
	Stack<MyPair<Double, Double>> skyStack = new Stack<MyPair<Double,Double>>();//sky = right or bottom
	private MyLayoutBlock() {
	}
	static MyLayoutBlock createPageRoot(MyLayoutStyle style) {
		MyLayoutBlock self = new MyLayoutBlock();
		self.verticalWritingMode = style.isVerticalWritingMode();
		self.pageWidth = style.getPageWidth();
		self.pageHeight = style.getPageHeight();
		self.contentLeft = style.getMarginLeft();
		self.contentTop = style.getMarginTop();
		double marginRight = style.getMarginRight();
		double marginBottom = style.getMarginBottom();
		self.contentRight = self.pageWidth - marginRight;
		self.contentBottom = self.pageHeight - marginBottom;
		if(!self.verticalWritingMode) {
			self.contentTravel = self.contentRight - self.contentLeft;
			self.divePos = self.contentTop;
			self.diveEnd = self.contentBottom;
		}
		else {
			self.contentTravel = self.contentBottom - self.contentTop;
			self.divePos = marginRight;
			self.diveEnd = self.pageWidth - self.contentLeft;
		}
		self.pageRootFlag = true;
		return self;
	}
	//static MyLayoutBlock createChildBlock() {
	//	MyLayoutBlock self = new MyLayoutBlock();
	//	return self;
	//}
	double getLineWidth() {
		double eWidth = 0;
		if(!earthStack.isEmpty()) {
			eWidth = earthStack.lastElement().second;
		}
		double sWidth = 0;
		if(!skyStack.isEmpty()) {
			sWidth = skyStack.lastElement().second;
		}
		return contentTravel - eWidth - sWidth;
	}
	boolean isPageRoot() {
		return pageRootFlag;
	}
	public void addBlock(MyLayoutBlock childBlock) {
		assert(false) : "TODO: MyLayoutBlock.addBlock()";
	}
	List<MyLayoutLine> lineList = new ArrayList<MyLayoutLine>();
	public boolean addLine(MyLayoutLine line, boolean fourceBlockFlag) {
		double perpend = line.getPerpend();
		if(!fourceBlockFlag && diveEnd - divePos < perpend) {
			return false;
		}
		if(!verticalWritingMode) {
			line.setPos(contentLeft, divePos + line.getUpperPerpend());
		}
		else {
			line.setPos(pageWidth - divePos - line.getUpperPerpend(), contentTop);
		}
		lineList.add(line);
		divePos += perpend;
		return true;
	}
	public void drawPage(YiPdfFile pdfFile) throws IOException {
		YiPdfPage page = pdfFile.newPage(pageWidth, pageHeight);
		MyLayoutPageContext pageContext = new MyLayoutPageContext(page);
		for(MyLayoutLine line : lineList) {
			line.draw(pageContext);
		}
		pageContext.invokeRuby();
		page.close();
	}
	public boolean isVerticalWritingMode() {
		return verticalWritingMode;
	}
}
