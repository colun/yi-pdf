/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfPage;

class MyLayoutBlock {
	double pageWidth;
	double pageHeight;
	double contentLeft;
	double contentTop;
	double contentRight;
	double contentBottom;
	double contentWidth;
	double contentHeight;
	double yPos;
	boolean pageRootFlag;
	Stack<MyPair<Double, Double>> earthStack = new Stack<MyPair<Double,Double>>();//earth = left or top
	Stack<MyPair<Double, Double>> skyStack = new Stack<MyPair<Double,Double>>();//sky = right or bottom
	private MyLayoutBlock() {
	}
	static MyLayoutBlock createPageRoot(Map<String, String> style) {
		MyLayoutBlock self = new MyLayoutBlock();
		self.pageWidth = MyUtil.evalUnit(style.get("page-width"));
		self.pageHeight = MyUtil.evalUnit(style.get("page-height"));
		self.contentLeft = MyUtil.evalUnit(style.get("margin-left"));
		self.contentTop = MyUtil.evalUnit(style.get("margin-top"));
		double marginRight = MyUtil.evalUnit(style.get("margin-right"));
		double marginBottom = MyUtil.evalUnit(style.get("margin-bottom"));
		self.contentRight = self.pageWidth - marginRight;
		self.contentBottom = self.pageHeight - marginBottom;
		self.contentWidth = self.contentRight - self.contentLeft;
		self.contentHeight = self.contentBottom - self.contentTop;
		self.yPos = self.contentTop;
		self.pageRootFlag = true;
		return self;
	}
	static MyLayoutBlock createChildBlock() {
		MyLayoutBlock self = new MyLayoutBlock();
		return self;
	}
	double getLineWidth() {
		double eWidth = 0;
		if(!earthStack.isEmpty()) {
			eWidth = earthStack.lastElement().second;
		}
		double sWidth = 0;
		if(!skyStack.isEmpty()) {
			sWidth = skyStack.lastElement().second;
		}
		return contentWidth - eWidth - sWidth;
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
		if(!fourceBlockFlag && contentBottom - yPos < perpend) {
			return false;
		}
		line.setPos(contentLeft, yPos);
		lineList.add(line);
		yPos += perpend;
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
}
