/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class MyLayoutBlock implements MyLayoutDrawable {
	boolean verticalWritingMode;
	MyRectSize contentRectSize;
	MyPosition contentPos;
	double divePos;
	boolean pageRootFlag;
	Stack<MyPair<Double, Double>> earthStack = new Stack<MyPair<Double,Double>>();//earth = left or top
	Stack<MyPair<Double, Double>> skyStack = new Stack<MyPair<Double,Double>>();//sky = right or bottom
	MyLayoutStyle nowStyle;
	boolean fullFlag;
	public MyLayoutBlock(MyLayoutStyle style, MyRectSize rectSize) {
		nowStyle = style;
		verticalWritingMode = style.isVerticalWritingMode();
		divePos = 0;
		contentRectSize = rectSize;
		pageRootFlag = false;
		fullFlag = false;
	}
	private double getEarthStackTravel() {
		double eWidth = 0;
		if(!earthStack.isEmpty()) {
			eWidth = earthStack.lastElement().second;
		}
		return eWidth;
	}
	private double getSkyStackTravel() {
		double sWidth = 0;
		if(!skyStack.isEmpty()) {
			sWidth = skyStack.lastElement().second;
		}
		return sWidth;
	}
	public double getLineWidth() {
		double d = getEarthStackTravel() + getSkyStackTravel();
		return getBlockTravel() - d;
	}
	public double getBlockTravel() {
		if(!verticalWritingMode) {
			return contentRectSize.width;
		}
		else {
			return contentRectSize.height;
		}
	}
	public boolean isPageRoot() {
		return pageRootFlag;
	}
	public void addFloatBlock(MyLayoutBlock childBlock, String fl) {
		double childWidth = childBlock.contentRectSize.width;
		double childHeight = childBlock.contentRectSize.height;
		if(!verticalWritingMode) {
			double di = divePos + childHeight;
			if("left".equals(fl)) {
				double stackTravel = getEarthStackTravel();
				childBlock.contentPos = new MyPosition(stackTravel, divePos);
				while(!earthStack.isEmpty() && earthStack.lastElement().first <= di) {
					earthStack.pop();
				}
				earthStack.push(new MyPair<Double, Double>(di, stackTravel + childWidth));
			}
			else if("right".equals(fl)) {
				double stackTravel = getSkyStackTravel();
				childBlock.contentPos = new MyPosition(contentRectSize.width - stackTravel - childWidth, divePos);
				while(!skyStack.isEmpty() && skyStack.lastElement().first <= di) {
					skyStack.pop();
				}
				skyStack.push(new MyPair<Double, Double>(di, stackTravel + childWidth));
			}
			else assert(false) : "横書きの場合、floatのスタイル指定をleftまたはrightにする必要があります";
		}
		else {
			double di = divePos + childWidth;
			if("top".equals(fl)) {
				double stackTravel = getEarthStackTravel();
				childBlock.contentPos = new MyPosition(-(divePos+childWidth), stackTravel);
				while(!earthStack.isEmpty() && earthStack.lastElement().first <= di) {
					earthStack.pop();
				}
				earthStack.push(new MyPair<Double, Double>(di, stackTravel + childHeight));
			}
			else if("bottom".equals(fl)) {
				double stackTravel = getSkyStackTravel();
				childBlock.contentPos = new MyPosition(-(divePos+childWidth), contentRectSize.height - stackTravel - childHeight);
				while(!skyStack.isEmpty() && skyStack.lastElement().first <= di) {
					skyStack.pop();
				}
				skyStack.push(new MyPair<Double, Double>(di, stackTravel + childHeight));
			}
			else assert(false) : "横書きの場合、floatのスタイル指定をleftまたはrightにする必要があります";
		}
		drawableList.add(childBlock);
	}
	Map<MyLayoutNest, MyPair<Double, Double>> nestRangeMap = new LinkedHashMap<MyLayoutNest, MyPair<Double,Double>>();
	public void registerNestRange(MyLayoutNest nest, Double start, Double end) {
		MyPair<Double, Double> range = nestRangeMap.get(nest);
		if(range==null) {
			nestRangeMap.put(nest, new MyPair<Double, Double>(start, end));
		}
		else {
			nestRangeMap.put(nest, new MyPair<Double, Double>(range.first, end));
		}
	}
	List<MyLayoutDrawable> drawableList = new ArrayList<MyLayoutDrawable>();
	public boolean addLine(MyLayoutLine line, boolean fourceBlockFlag, MyLayoutNest nest) {
		if(fullFlag) {
			return false;
		}
		double perpend = line.getPerpend();
		if(!fourceBlockFlag && getRemainDive() < perpend) {
			fullFlag = true;
			return false;
		}
		if(!verticalWritingMode) {
			line.setPos(getEarthStackTravel(), divePos + line.getUpperPerpend());
		}
		else {
			line.setPos(-(divePos + line.getUpperPerpend()), getEarthStackTravel());
		}
		drawableList.add(line);
		nest.registerNestRange(this, divePos, divePos + perpend);
		divePos += perpend;
		while(!earthStack.isEmpty() && earthStack.lastElement().first <= divePos) {
			earthStack.pop();
		}
		while(!skyStack.isEmpty() && skyStack.lastElement().first <= divePos) {
			skyStack.pop();
		}
		return true;
	}
	private double getRemainDive() {
		return getEndDive() - divePos;
	}
	private double getEndDive() {
		if(!verticalWritingMode) {
			return contentRectSize.height;
		}
		else {
			return contentRectSize.width;
		}
	}
	public void draw(MyLayoutPageContext pageContext, double x, double y) throws IOException {
		x += contentPos.x;
		y += contentPos.y;

		//if(!pageRootFlag && nowStyle.hasBackgroundColor()) {
		//	YiPdfPage page = pageContext.getPdfPage();
		//	page.setFillColor(nowStyle.getBackgroundColor());
		//	page.fillRect(x, y, contentRectSize.width, contentRectSize.height);
		//}
		if(verticalWritingMode) {
			x += contentRectSize.width;
		}
		for(MyLayoutNest nest : nestRangeMap.keySet()) {
			MyPair<Double, Double> range = nestRangeMap.get(nest);
			double start = range.first!=null ? range.first : 0;
			double end = range.second!=null ? range.second : getEndDive();
			nest.draw(pageContext, x, y, start, end, getBlockTravel(), verticalWritingMode);
		}
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
			return new MyLayoutBlock(style, rectSize);
		}
		else {
			assert(style.hasHeight()) : "heightスタイルが必要";
			double width = style.hasWidth() ? style.getWidth() : getRemainDive();
			double height = style.hasHeight() ? style.getHeight() : getLineWidth();
			MyRectSize rectSize = new MyRectSize(width, height);
			return new MyLayoutBlock(style, rectSize);
		}
	}
	public void justify(MyLayoutNest nest) {
		boolean jFlag = false;
		if(!fullFlag) {
			if(!verticalWritingMode) {
				if(!nowStyle.hasHeight()) {
					if(divePos < contentRectSize.height) {
						contentRectSize = new MyRectSize(contentRectSize.width, divePos);
						jFlag = true;
					}
				}
			}
			else {
				if(!nowStyle.hasWidth()) {
					if(divePos < contentRectSize.width) {
						contentRectSize = new MyRectSize(divePos, contentRectSize.height);
						jFlag = true;
					}
				}
			}
		}
		if(!jFlag) {
			nest.registerNestRange(this, 0.0, !verticalWritingMode ? contentRectSize.height : contentRectSize.width);
		}
	}
}
