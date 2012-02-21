/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;

import yi.pdf.YiPdfColor;
import yi.pdf.YiPdfPage;

class MyLayoutNest {
	final MyLayoutNest parent;
	final YiPdfColor backgroundColor;
	final MyLayoutMargin margin;
	final MyLayoutMargin borderWidth;
	final MyLayoutMargin padding;
	MyLayoutNest() {
		parent = null;
		backgroundColor = null;
		margin = new MyLayoutMargin(0, 0, 0, 0);
		borderWidth = new MyLayoutMargin(0, 0, 0, 0);
		padding = new MyLayoutMargin(0, 0, 0, 0);
	}
	MyLayoutNest(MyLayoutStyle nowStyle) {
		this(null, nowStyle);
	}
	MyLayoutNest(MyLayoutNest parent, MyLayoutStyle nowStyle) {
		this.parent = parent;
		backgroundColor = nowStyle.hasBackgroundColor() ? nowStyle.getBackgroundColor() : null;
		margin = nowStyle.getMargin();
		borderWidth = nowStyle.getBorderWidth();
		padding = nowStyle.getPadding();
	}
	private double getEarthTravelMargin(boolean verticalFlag, double childMargin) {
		double nowMargin;
		double nowPadding;
		if(!verticalFlag) {
			nowMargin = margin.left;
			nowPadding = padding.left + borderWidth.left;
		}
		else {
			nowMargin = margin.top;
			nowPadding = padding.top + borderWidth.top;
		}
		double mixPadding = childMargin + nowPadding;
		double mixMargin = nowMargin;
		if(parent!=null) {
			return mixPadding + parent.getEarthTravelMargin(verticalFlag, mixMargin);
		}
		else {
			return mixPadding + mixMargin;
		}
	}
	private double getSkyTravelMargin(boolean verticalFlag, double childMargin) {
		double nowMargin;
		double nowPadding;
		if(!verticalFlag) {
			nowMargin = margin.right;
			nowPadding = padding.right + borderWidth.right;
		}
		else {
			nowMargin = margin.bottom;
			nowPadding = padding.bottom + borderWidth.bottom;
		}
		double mixPadding = childMargin + nowPadding;
		double mixMargin = nowMargin;
		if(parent!=null) {
			return mixPadding + parent.getSkyTravelMargin(verticalFlag, mixMargin);
		}
		else {
			return mixPadding + mixMargin;
		}
	}
	double getEarthTravelMargin(boolean verticalFlag) {
		return getEarthTravelMargin(verticalFlag, 0);
	}
	double getSkyTravelMargin(boolean verticalFlag) {
		return getSkyTravelMargin(verticalFlag, 0);
	}
	void draw(MyLayoutPageContext pageContext, double x, double y, double start, double end, double travel, boolean verticalWritingMode) throws IOException {
		if(backgroundColor!=null) {
			double earth;
			double sky;
			if(parent!=null) {
				if(!verticalWritingMode) {
					earth = parent.getEarthTravelMargin(verticalWritingMode, margin.left);
					sky = parent.getSkyTravelMargin(verticalWritingMode, margin.right);
				}
				else {
					earth = parent.getEarthTravelMargin(verticalWritingMode, margin.top);
					sky = parent.getSkyTravelMargin(verticalWritingMode, margin.bottom);
				}
			}
			else {
				if(!verticalWritingMode) {
					earth = margin.left;
					sky = margin.right;
				}
				else {
					earth = margin.top;
					sky = margin.bottom;
				}
			}
			YiPdfPage page = pageContext.getPdfPage();
			if(!verticalWritingMode) {
				page.setFillColor(backgroundColor);
				page.fillRect(x + earth, y + start, travel - (earth + sky), end - start);
			}
			else {
				page.setFillColor(backgroundColor);
				page.fillRect(x - end, y + earth, end - start, travel - (earth + sky));
			}
		}
	}
	MyLayoutNest getParent() {
		return parent;
	}
	void registerNestRange(MyLayoutBlock block, Double start, Double end) {
		if(parent!=null) {
			parent.registerNestRange(block, start, end);
		}
		block.registerNestRange(this, start, end);
	}
}
