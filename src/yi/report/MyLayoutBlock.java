package yi.report;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;

import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfPage;

class MyLayoutBlock {
	double pageWidth;
	double pageHeight;
	double pageLeft;
	double pageTop;
	double width;
	double height;
	boolean pageRootFlag;
	Stack<MyPair<Double, Double>> earthStack = new Stack<MyPair<Double,Double>>();//earth = left or top
	Stack<MyPair<Double, Double>> skyStack = new Stack<MyPair<Double,Double>>();//sky = right or bottom
	private MyLayoutBlock() {
	}
	static MyLayoutBlock createPageRoot(Map<String, String> style) {
		MyLayoutBlock self = new MyLayoutBlock();
		self.pageWidth = MyUtil.evalUnit(style.get("page-width"));
		self.pageHeight = MyUtil.evalUnit(style.get("page-height"));
		self.pageLeft = MyUtil.evalUnit(style.get("margin-left"));
		self.pageTop = MyUtil.evalUnit(style.get("margin-top"));
		double marginRight = MyUtil.evalUnit(style.get("margin-right"));
		double marginBottom = MyUtil.evalUnit(style.get("margin-bottom"));
		self.width = self.pageWidth - self.pageLeft - marginRight;
		self.height = self.pageHeight - self.pageTop - marginBottom;
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
		return width - eWidth - sWidth;
	}
	boolean isPageRoot() {
		return pageRootFlag;
	}
	public void addBlock(MyLayoutBlock childBlock) {
		assert(false) : "TODO: MyLayoutBlock.pushBlock()";
	}
	public void drawPage(YiPdfFile pdfFile) throws IOException {
		YiPdfPage page = pdfFile.newPage(pageWidth, pageHeight);
		page.close();
	}


}
