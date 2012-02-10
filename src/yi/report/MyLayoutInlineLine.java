/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;

class MyLayoutInlineLine extends MyLayoutInline {
	boolean blockVerticalWritingMode;
	MyLayoutLine childLine;
	double travel;
	double upperPerpend;
	double lowerPerpend;
	double dx;
	double dy;
	public MyLayoutInlineLine(boolean blockVerticalWritingMode, MyLayoutLine childLine) {
		this.blockVerticalWritingMode = blockVerticalWritingMode;
		this.childLine = childLine;
		if(blockVerticalWritingMode==childLine.isVerticalWritingMode()) {
			travel = childLine.getTravel();
			upperPerpend = childLine.getUpperPerpend();
			lowerPerpend = childLine.getLowerPerpend();
			dx = 0;
			dy = 0;
		}
		else if(blockVerticalWritingMode) {
			double width = childLine.getTravel();
			double height = childLine.getUpperPerpend() - childLine.getLowerPerpend();
			travel = height;
			upperPerpend = width / 2;
			lowerPerpend = -width / 2;
			dx = lowerPerpend;
			dy = childLine.getUpperPerpend();
		}
		else {
			double width = childLine.getUpperPerpend() - childLine.getLowerPerpend();
			double height = childLine.getTravel();
			travel = width;
			upperPerpend = height;
			lowerPerpend = 0;
			dx = -childLine.getLowerPerpend();
			dy = -upperPerpend;
		}
	}
	@Override
	public double getTravel() {
		return travel;
	}
	@Override
	public double getLowerPerpend() {
		return lowerPerpend;
	}
	@Override
	public double getUpperPerpend() {
		return upperPerpend;
	}
	@Override
	public void setPos(double x, double y) {
		posX = x;
		posY = y;
		childLine.setPos(x + dx, y + dy);
	}
	@Override
	public void draw(MyLayoutPageContext pageContext) throws IOException {
		childLine.draw(pageContext);
	}
}
