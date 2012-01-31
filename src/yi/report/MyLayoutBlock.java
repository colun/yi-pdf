package yi.report;

import java.util.Stack;

class MyLayoutBlock {
	double width;
	boolean pageRootFlag;
	Stack<MyPair<Double, Double>> earthStack = new Stack<MyPair<Double,Double>>();//earth = left or top
	Stack<MyPair<Double, Double>> skyStack = new Stack<MyPair<Double,Double>>();//sky = right or bottom
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


}
