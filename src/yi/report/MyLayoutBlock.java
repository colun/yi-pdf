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

import yi.pdf.YiPdfColor;
import yi.pdf.YiPdfPage;
import yi.report.MyLayoutStyle.AlignType;

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
	MyRectSize getContentRectSize() {
		return contentRectSize;
	}
	private double getEarthStackTravel(MyLayoutNest nest) {
		double eWidth = 0;
		if(!earthStack.isEmpty()) {
			eWidth = earthStack.lastElement().second;
		}
		return Math.max(eWidth, nest.getEarthTravelMargin(verticalWritingMode));
	}
	private double getSkyStackTravel(MyLayoutNest nest) {
		double sWidth = 0;
		if(!skyStack.isEmpty()) {
			sWidth = skyStack.lastElement().second;
		}
		return Math.max(sWidth, nest.getSkyTravelMargin(verticalWritingMode));
	}
	public double getLineWidth(MyLayoutNest nest) {
		double d = getEarthStackTravel(nest) + getSkyStackTravel(nest);
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
	public void addCellBlock(MyLayoutBlock block, double pos, double width, double height, MyLayoutNest nest, MyLayoutStyle style, MyLayoutNest tableNest) {
		block.contentPos = new MyPosition(!verticalWritingMode ? getEarthStackTravel(tableNest) + block.contentPos.x : -(divePos+pos), !verticalWritingMode ? divePos+pos : getEarthStackTravel(tableNest) + block.contentPos.y);
		block.expand(width, height, nest, style);
		drawableList.add(block);
	}
	/*
	public void expandBottom(MyLayoutNest nest, Double exp) {
		MyPair<Double, Double> range = nestRangeMap.get(nest);
		if(range==null) {
			nestRangeMap.put(nest, new MyPair<Double, Double>(start, end));
		}
		else {
			nestRangeMap.put(nest, new MyPair<Double, Double>(range.first, end));
		}
	}
	*/
	private void expand(double width, double height, MyLayoutNest nest, MyLayoutStyle style) {
		double dw = width - contentRectSize.width;
		double dh = height - contentRectSize.height;
		contentRectSize = new MyRectSize(width, height);
		if(0<dw) {
			if(verticalWritingMode) {
				double dw2 = dw / 2;
				MyPair<Double, Double> range = nestRangeMap.get(nest);
				if(range!=null) {
					nestRangeMap.put(nest, new MyPair<Double, Double>(range.first - dw2, range.second + dw2));
				}
				contentPos = new MyPosition(contentPos.x - dw2, contentPos.y);
			}
		}
		if(0<dh) {
			if(!verticalWritingMode) {
				double dh2 = dh / 2;
				MyPair<Double, Double> range = nestRangeMap.get(nest);
				if(range!=null) {
					nestRangeMap.put(nest, new MyPair<Double, Double>(range.first - dh2, range.second + dh2));
				}
				contentPos = new MyPosition(contentPos.x, contentPos.y + dh2);
			}
		}
	}
	public void addFloatBlock(MyLayoutBlock childBlock, String fl, MyLayoutNest nest) {
		double childWidth = childBlock.contentRectSize.width;
		double childHeight = childBlock.contentRectSize.height;
		if(!verticalWritingMode) {
			double di = divePos + childHeight;
			if("left".equals(fl)) {
				double stackTravel = getEarthStackTravel(nest);
				childBlock.contentPos = new MyPosition(stackTravel, divePos);
				while(!earthStack.isEmpty() && earthStack.lastElement().first <= di) {
					earthStack.pop();
				}
				earthStack.push(new MyPair<Double, Double>(di, stackTravel + childWidth));
			}
			else if("right".equals(fl)) {
				double stackTravel = getSkyStackTravel(nest);
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
				double stackTravel = getEarthStackTravel(nest);
				childBlock.contentPos = new MyPosition(-(divePos+childWidth), stackTravel);
				while(!earthStack.isEmpty() && earthStack.lastElement().first <= di) {
					earthStack.pop();
				}
				earthStack.push(new MyPair<Double, Double>(di, stackTravel + childHeight));
			}
			else if("bottom".equals(fl)) {
				double stackTravel = getSkyStackTravel(nest);
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
		nest.unsetFirst();
	}
	List<MyLayoutDrawable> drawableList = new ArrayList<MyLayoutDrawable>();
	public boolean addLine(MyLayoutLine line, boolean fourceBlockFlag, MyLayoutNest nest) {
		if(fullFlag) {
			return false;
		}
		double perpend = line.getPerpend();
		if(!fourceBlockFlag && getRemainDive(nest) < perpend) {
			fullFlag = true;
			return false;
		}
		AlignType textAlign = nest.textAlign;
		double tt;
		if(textAlign==null) {
			tt = 0;
		}
		else if(textAlign==AlignType.ALIGN_CENTER) {
			double travel = line.getTravel();
			double canTravel = getLineWidth(nest);
			tt = (canTravel - travel) / 2;
		}
		else {
			double travel = line.getTravel();
			double canTravel = getLineWidth(nest);
			tt = canTravel - travel;
		}
		if(!verticalWritingMode) {
			line.setPos(getEarthStackTravel(nest) + tt, divePos + line.getUpperPerpend());
		}
		else {
			line.setPos(-(divePos + line.getUpperPerpend()), getEarthStackTravel(nest) + tt);
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
	private double getRemainDive(MyLayoutNest nest) {
		return getEndDive() - divePos - (pageRootFlag ? 0 : nest.getPostRecursionPadding(verticalWritingMode));
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
		YiPdfPage page = pageContext.getPdfPage();
		for(MyLayoutNest nest : nestRangeMap.keySet()) {
			MyPair<Double, Double> range = nestRangeMap.get(nest);
			double start = range.first!=null ? range.first : 0;
			double end = range.second!=null ? range.second : getEndDive();
			nest.draw(pageContext, x, y, start, end, getBlockTravel(), verticalWritingMode, range.first==null, range.second==null);
		}
		for(MyLayoutDrawable line : drawableList) {
			line.draw(pageContext, x, y);
		}
		for(MySextet<String, Double, Double, Double, Double, Double> border : borderList) {
			page.setDrawColor(new YiPdfColor(0, 0, 0));
			page.setLineCap(0);
			page.setLineWidth(border.second);
			if("dashed".equalsIgnoreCase(border.first)) {
				page.setDashPattern(new double[] { 3 }, 0);
			}
			else if("doted".equalsIgnoreCase(border.first)) {
				page.setDashPattern(new double[] { 1 }, 0);
			}
			else {
				page.setDashPattern(null, 0);
			}
			double xx1 = !verticalWritingMode ? border.third : border.fourth;
			double yy1 = !verticalWritingMode ? border.fourth : border.third;
			double xx2 = !verticalWritingMode ? border.fifth : border.sixth;
			double yy2 = !verticalWritingMode ? border.sixth : border.fifth;
			page.drawLine(x+xx1, y+yy1, x+xx2, y+yy2);
		}
		for(MyQuartet<MyQuartet<String, Double, Double, YiPdfColor>, Double, Double, Object> cross : crossList) {
			double xx = !verticalWritingMode ? cross.second : cross.third;
			double yy = !verticalWritingMode ? cross.third : cross.second;
			double width = !verticalWritingMode ? cross.first.second : cross.first.third;
			double height = !verticalWritingMode ? cross.first.third : cross.first.second;
			page.setFillColor(cross.first.fourth);
			page.fillRect(x+xx-width/2, y+yy-height/2, width, height);
		}
		pageContext.invokeRuby();
	}
	public boolean isVerticalWritingMode() {
		return verticalWritingMode;
	}
	public MyLayoutBlock makeChildFloatBlock(MyLayoutStyle style, MyLayoutNest nest) {
		boolean childVertival = style.isVerticalWritingMode();
		if(!childVertival) {
			assert(style.hasWidth()) : "横書きの場合はwidth指定が必要";
		}
		else {
			assert(style.hasHeight()) : "縦書きの場合はheight指定が必要";
		}
		assert(!childVertival ? style.hasWidth() : style.hasHeight());
		MyLayoutMargin margin = style.getMargin();
		MyLayoutMargin border = style.getBorderWidth();
		MyLayoutMargin padding = style.getPadding();
		if(!verticalWritingMode) {
			double width = style.hasWidth()
					? margin.left + border.left + padding.left
					+ style.getWidth()
					+ padding.right + border.right + margin.right
					: getLineWidth(nest);
			double height = style.hasHeight()
					? margin.top + border.top + padding.top
					+ style.getHeight()
					+ padding.bottom + border.bottom + margin.bottom
					: getRemainDive(nest);
			MyRectSize rectSize = new MyRectSize(width, height);
			return new MyLayoutBlock(style, rectSize);
		}
		else {
			assert(style.hasHeight()) : "heightスタイルが必要";
			double width = style.hasWidth()
					? margin.left + border.left + padding.left
					+ style.getWidth()
					+ padding.right + border.right + margin.right
					: getRemainDive(nest);
			double height = style.hasHeight()
					? margin.top + border.top + padding.top
					+ style.getHeight()
					+ padding.bottom + border.bottom + margin.bottom
					: getLineWidth(nest);
			MyRectSize rectSize = new MyRectSize(width, height);
			return new MyLayoutBlock(style, rectSize);
		}
	}
	public void justify(MyLayoutNest nest) {
		boolean flag = false;
		if(!fullFlag) {
			if(!verticalWritingMode) {
				if(!nowStyle.hasHeight()) {
					double dp = divePos + nest.getPostPadding(verticalWritingMode);
					if(divePos < contentRectSize.height) {
						contentRectSize = new MyRectSize(contentRectSize.width, dp);
						flag = true;
					}
				}
			}
			else {
				if(!nowStyle.hasWidth()) {
					double dp = divePos + nest.getPostPadding(verticalWritingMode);
					if(divePos < contentRectSize.width) {
						contentRectSize = new MyRectSize(dp, contentRectSize.height);
						flag = true;
					}
				}
			}
		}
		if(flag) {
			registerNestRange(nest, 0.0, !verticalWritingMode ? contentRectSize.height - nest.margin.bottom : contentRectSize.width - nest.margin.left);
		}
	}
	public void addPass(double pass, MyLayoutNest nest) {
		divePos += pass;
		if(nest!=null) {
			nest.registerNestRange(this, divePos, divePos);
		}
	}
	List<MyQuartet<MyQuartet<String, Double, Double, YiPdfColor>, Double, Double, Object>> crossList = new ArrayList<MyQuartet<MyQuartet<String,Double,Double,YiPdfColor>,Double,Double,Object>>();
	public void putCross(MyQuartet<String, Double, Double, YiPdfColor> cross, double travel, double dive, MyLayoutNest tableNest) {
		crossList.add(new MyQuartet<MyQuartet<String,Double,Double,YiPdfColor>, Double, Double, Object>(cross, getEarthStackTravel(tableNest)+travel, divePos+dive, null));
	}
	List<MySextet<String, Double, Double, Double, Double, Double>> borderList = new ArrayList<MySextet<String,Double,Double,Double,Double,Double>>();
	public void putBorder(String name, double width, double sTravel, double sDive, double eTravel, double eDive, MyLayoutNest tableNest) {
		double esTravel = getEarthStackTravel(tableNest);
		borderList.add(new MySextet<String, Double, Double, Double, Double, Double>(name, width, esTravel+sTravel, sDive+divePos, esTravel+eTravel, eDive+divePos));
	}
}
