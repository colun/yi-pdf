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
	public double getPreMargin(boolean verticalFlag) {
		if(!verticalFlag) {
			return margin.top;
		}
		else {
			return margin.right;
		}
	}
	public double getPrePadding(boolean verticalFlag) {
		if(!verticalFlag) {
			return padding.top + borderWidth.top;
		}
		else {
			return padding.right + borderWidth.right;
		}
	}
	public double getPostMargin(boolean verticalFlag) {
		if(!verticalFlag) {
			return margin.bottom;
		}
		else {
			return margin.left;
		}
	}
	public double getPostPadding(boolean verticalFlag) {
		if(!verticalFlag) {
			return padding.bottom + borderWidth.bottom;
		}
		else {
			return padding.left + borderWidth.left;
		}
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
	void draw(MyLayoutPageContext pageContext, double x, double y, double start, double end, double travel, boolean verticalWritingMode, boolean sFlag, boolean eFlag) throws IOException {
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
				double sx = x + earth;
				double ex = x + travel - sky;
				double sy = y + start;
				double ey = y + end;
				page.fillRect(sx, sy, ex-sx, ey-sy);
				page.setLineCap(2);
				page.setDrawColor(new YiPdfColor(0, 0, 0));
				if(0<borderWidth.left) {
					page.setLineWidth(borderWidth.left);
					double bw2 = borderWidth.left / 2;
					double xx = sx + bw2;
					page.drawLine(xx, sy + bw2, xx, ey - bw2);
				}
				if(0<borderWidth.right) {
					page.setLineWidth(borderWidth.right);
					double bw2 = borderWidth.right / 2;
					double xx = ex - bw2;
					page.drawLine(xx, sy + bw2, xx, ey - bw2);
				}
				if(0<borderWidth.top && !sFlag) {
					page.setLineWidth(borderWidth.top);
					double bw2 = borderWidth.top / 2;
					double yy = sy + bw2;
					page.drawLine(sx + bw2, yy, ex - bw2, yy);
				}
				if(0<borderWidth.bottom && !eFlag) {
					page.setLineWidth(borderWidth.bottom);
					double bw2 = borderWidth.bottom / 2;
					double yy = ey - bw2;
					page.drawLine(sx + bw2, yy, ex - bw2, yy);
				}
			}
			else {
				page.setFillColor(backgroundColor);
				double sx = x - end;
				double ex = x - start;
				double sy = y + earth;
				double ey = y + travel - sky;
				page.fillRect(sx, sy, ex-sx, ey-sy);
				page.setLineCap(2);
				page.setDrawColor(new YiPdfColor(0, 0, 0));
				if(0<borderWidth.left && !eFlag) {
					page.setLineWidth(borderWidth.left);
					double bw2 = borderWidth.left / 2;
					double xx = sx + bw2;
					page.drawLine(xx, sy + bw2, xx, ey - bw2);
				}
				if(0<borderWidth.right && !sFlag) {
					page.setLineWidth(borderWidth.right);
					double bw2 = borderWidth.right / 2;
					double xx = ex - bw2;
					page.drawLine(xx, sy + bw2, xx, ey - bw2);
				}
				if(0<borderWidth.top) {
					page.setLineWidth(borderWidth.top);
					double bw2 = borderWidth.top / 2;
					double yy = sy + bw2;
					page.drawLine(sx + bw2, yy, ex - bw2, yy);
				}
				if(0<borderWidth.bottom) {
					page.setLineWidth(borderWidth.bottom);
					double bw2 = borderWidth.bottom / 2;
					double yy = ey - bw2;
					page.drawLine(sx + bw2, yy, ex - bw2, yy);
				}
			}
		}
	}
	MyLayoutNest getParent() {
		return parent;
	}
	void registerNestRange(MyLayoutBlock block, double start, double end, double sMargin, double eMargin) {
		boolean verticalFlag = block.isVerticalWritingMode();
		double preMargin = getPreMargin(verticalFlag);
		double prePadding = getPrePadding(verticalFlag);
		double postPadding = getPostPadding(verticalFlag);
		double postMargin = getPostMargin(verticalFlag);

		if(prePadding!=0) {
			start -= sMargin + prePadding;
			sMargin = preMargin;
		}
		else {
			sMargin = Math.max(sMargin, preMargin);
		}
		if(postPadding!=0) {
			end += eMargin + postPadding;
			eMargin = postMargin;
		}
		else {
			eMargin = Math.max(eMargin, postMargin);
		}
		if(parent!=null) {
			parent.registerNestRange(block, start, end, sMargin, eMargin);
		}
		block.registerNestRange(this, start, end);
	}
	void registerNestRange(MyLayoutBlock block, double start, double end) {
		registerNestRange(block, start, end, 0, 0);
	}
}
