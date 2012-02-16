/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class MyLayoutBlock implements MyLayoutDrawable {
	boolean verticalWritingMode;
	MyRectSize contentRectSize;
	MyPosition contentPos;
	double divePos;
	boolean pageRootFlag;
	Stack<MyPair<Double, Double>> earthStack = new Stack<MyPair<Double,Double>>();//earth = left or top
	Stack<MyPair<Double, Double>> skyStack = new Stack<MyPair<Double,Double>>();//sky = right or bottom
	protected MyLayoutBlock(MyLayoutStyle style, MyRectSize rectSize) {
		verticalWritingMode = style.isVerticalWritingMode();
		divePos = 0;
		contentRectSize = rectSize;
		pageRootFlag = false;
	}
	double getEarthStackTravel() {
		double eWidth = 0;
		if(!earthStack.isEmpty()) {
			eWidth = earthStack.lastElement().second;
		}
		return eWidth;
	}
	double getSkyStackTravel() {
		double sWidth = 0;
		if(!skyStack.isEmpty()) {
			sWidth = skyStack.lastElement().second;
		}
		return sWidth;
	}
	double getLineWidth() {
		double d = getEarthStackTravel() + getSkyStackTravel();
		if(!verticalWritingMode) {
			return contentRectSize.width - d;
		}
		else {
			return contentRectSize.height - d;
		}
	}
	boolean isPageRoot() {
		return pageRootFlag;
	}
	public void addFloatBlock(MyLayoutBlock childBlock, String fl) {
		assert(false) : "TODO: MyLayoutBlock.addFloatBlock()";
	}
	List<MyLayoutDrawable> drawableList = new ArrayList<MyLayoutDrawable>();
	public boolean addLine(MyLayoutLine line, boolean fourceBlockFlag) {
		double perpend = line.getPerpend();
		if(!fourceBlockFlag && getRemainDive() < perpend) {
			return false;
		}
		if(!verticalWritingMode) {
			line.setPos(0, divePos + line.getUpperPerpend());
		}
		else {
			line.setPos(0 + contentRectSize.width - divePos - line.getUpperPerpend(), 0);
		}
		drawableList.add(line);
		divePos += perpend;
		return true;
	}
	public double getRemainDive() {
		if(!verticalWritingMode) {
			return contentRectSize.height - divePos;
		}
		else {
			return contentRectSize.width - divePos;
		}
	}
	public void draw(MyLayoutPageContext pageContext, double x, double y) throws IOException {
		x += contentPos.x;
		y += contentPos.y;
		for(MyLayoutDrawable line : drawableList) {
			line.draw(pageContext, x, y);
		}
		pageContext.invokeRuby();
	}
	public boolean isVerticalWritingMode() {
		return verticalWritingMode;
	}
	public MyLayoutBlock makeChildFloatBlock(MyLayoutStyle style) {
		boolean childVertival = style.isVerticalWritingMode();
		if(!childVertival) {
			assert(style.hasWidth()) : "横書きの場合はwidth指定が必要";
		}
		else {
			assert(style.hasHeight()) : "縦書きの場合はheight指定が必要";
		}
		assert(!childVertival ? style.hasWidth() : style.hasHeight());
		if(!verticalWritingMode) {
			double width = style.hasWidth() ? style.getWidth() : getLineWidth();
			double height = style.hasHeight() ? style.getHeight() : getRemainDive();
			MyRectSize rectSize = new MyRectSize(width, height);
			MyLayoutBlock block = new MyLayoutBlock(style, rectSize);
			return block;
		}
		else {
			assert(style.hasHeight()) : "heightスタイルが必要";
			double width = style.hasWidth() ? style.getWidth() : getRemainDive();
			double height = style.hasHeight() ? style.getHeight() : getLineWidth();
			MyRectSize rectSize = new MyRectSize(width, height);
			MyLayoutBlock block = new MyLayoutBlock(style, rectSize);
			return block;
		}
	}
}
