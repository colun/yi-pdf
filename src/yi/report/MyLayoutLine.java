/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yi.pdf.YiPdfFont;
import yi.pdf.YiPdfTag;

class MyLayoutLine {
	boolean verticalWritingMode;
	double width;
	double totalTravel;
	double upperPerpend;
	double lowerPerpend;
	boolean blankFlag = true;
	public MyLayoutLine(double width, boolean verticalWritingMode) {
		this.verticalWritingMode = verticalWritingMode;
		this.width = width;
		totalTravel = 0;
		lowerPerpend = 0;
		upperPerpend = 0;
	}
	static MyLayoutLine createInfLine(boolean verticalWritingMode) {
		return new MyLayoutLine(1000000000, verticalWritingMode);
	}
	double getRemainingTravel() {
		return width - totalTravel;
	}
	List<MyLayoutInline> inlineList = new ArrayList<MyLayoutInline>();
	boolean isEmpty() {
		return inlineList.isEmpty();
	}
	List<MyLayoutInline> getInlineList() {
		return inlineList;
	}
	public void addInline(MyLayoutInline myLayoutInline) {
		blankFlag = false;
		inlineList.add(myLayoutInline);
		totalTravel += myLayoutInline.getTravel();
		lowerPerpend = Math.min(lowerPerpend, myLayoutInline.getLowerPerpend());
		upperPerpend = Math.max(upperPerpend, myLayoutInline.getUpperPerpend());
	}
	double getPerpend() {
		return upperPerpend - lowerPerpend;
	}
	public void setPos(double x, double y) {
		if(!verticalWritingMode) {
			y += upperPerpend;
		}
		else {
			x -= upperPerpend;
		}
		for(MyLayoutInline item : inlineList) {
			double travel = item.getTravel();
			item.setPos(x, y);
			if(!verticalWritingMode) {
				x += travel;
			}
			else {
				y += travel;
			}
		}
	}
	public void draw(MyLayoutPageContext pageContext) throws IOException {
		for(MyLayoutInline item : inlineList) {
			item.draw(pageContext);
		}
	}
	public void addBlankText(YiPdfFont nowFont, double nowFontSize, YiPdfTag lineTag) {
		if(blankFlag) {
			blankFlag = false;
			lowerPerpend = Math.min(lowerPerpend, nowFontSize * nowFont.getLowerPerpend('A') / 1000);
			upperPerpend = Math.max(upperPerpend, nowFontSize * nowFont.getUpperPerpend('A') / 1000);
			lineTag.makeChild("Span");
		}
	}
	public boolean isVerticalWritingMode() {
		return verticalWritingMode;
	}
}
