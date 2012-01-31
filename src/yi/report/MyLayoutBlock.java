package yi.report;

import java.util.Stack;

class MyLayoutBlock {
	double width;
	boolean pageRootFlag;
	Stack<MyPair<Double, Double>> earthStack;//earth = left or top
	Stack<MyPair<Double, Double>> skyStack;//sky = right or bottom
	private MyLayoutBlock() {
		
	}
	static MyLayoutBlock createPageRoot() {
		MyLayoutBlock self = new MyLayoutBlock();
		return self;
	}
	static MyLayoutBlock createChildBlock() {
		MyLayoutBlock self = new MyLayoutBlock();
		return self;
	}
	double getLineWidth() {
		MyPair<Double, Double> earthTop = earthStack.lastElement();
		MyPair<Double, Double> skyTop = skyStack.lastElement();
		return width - earthTop.second - skyTop.second;
	}
	boolean isPageRoot() {
		return pageRootFlag;
	}
	public void addBlock(MyLayoutBlock childBlock) {
		assert(false) : "TODO: MyLayoutBlock.pushBlock()";
	}


}
