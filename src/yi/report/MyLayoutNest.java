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
	final MyEdgeValues<String> borderStyle;
	MyLayoutNest() {
		parent = null;
		backgroundColor = null;
		margin = new MyLayoutMargin(0, 0, 0, 0);
		borderWidth = new MyLayoutMargin(0, 0, 0, 0);
		padding = new MyLayoutMargin(0, 0, 0, 0);
		borderStyle = new MyEdgeValues<String>(null, null, null, null);
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
		borderStyle = nowStyle.getBorderStyle();
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
	private void drawMyLine(YiPdfPage page, double sx, double sy, double ex, double ey, double width, double len, String style) throws IOException {
		boolean flag = false;
		if("solid".equals(style)) {
			page.setDashPattern(null, 0);
			flag = true;
		}
		else if("dashed".equals(style)) {
			double cycle = (len / (width * 6)) + 0.5;
			double phase = (Math.ceil(cycle) - cycle) * 3;
			page.setDashPattern(new double[] { 3 }, phase);
			flag = true;
		}
		else if("doted".equals(style)) {
			double cycle = (len / (width * 2)) + 0.5;
			double phase = (Math.ceil(cycle) - cycle);
			page.setDashPattern(new double[] { 1 }, phase);
			flag = true;
		}
		if(flag) {
			page.setLineWidth(width);
			page.drawLine(sx, sy, ex, ey);
		}
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
				double sx = x + earth;
				double ex = x + travel - sky;
				double sy = y + start;
				double ey = y + end;
				double leftBorder = 0<borderWidth.left ? borderWidth.left : 0;
				double rightBorder = 0<borderWidth.right ? borderWidth.right : 0;
				double topBorder = (0<borderWidth.top && !sFlag) ? borderWidth.top : 0;
				double bottomBorder = (0<borderWidth.bottom && !eFlag) ? borderWidth.bottom : 0;
				page.setFillColor(backgroundColor);
				page.fillRect(sx, sy, ex-sx, ey-sy);
				page.setLineCap(0);

				YiPdfColor borderColor = new YiPdfColor(0, 0, 0);
				page.setDrawColor(borderColor);
				page.setFillColor(borderColor);
				if(topBorder!=0 && leftBorder!=0) {
					page.fillRect(sx, sy, leftBorder, topBorder);
				}
				if(topBorder!=0 && rightBorder!=0) {
					page.fillRect(ex-rightBorder, sy, rightBorder, topBorder);
				}
				if(bottomBorder!=0 && leftBorder!=0) {
					page.fillRect(sx, ey-bottomBorder, leftBorder, bottomBorder);
				}
				if(bottomBorder!=0 && rightBorder!=0) {
					page.fillRect(ex-rightBorder, ey-bottomBorder, rightBorder, bottomBorder);
				}
				if(leftBorder!=0) {
					double bw2 = leftBorder / 2;
					double xx = sx + bw2;
					drawMyLine(page, xx, sy + topBorder, xx, ey - bottomBorder, leftBorder, end - start - topBorder - bottomBorder, borderStyle.left);
				}
				if(rightBorder!=0) {
					double bw2 = rightBorder / 2;
					double xx = ex - bw2;
					drawMyLine(page, xx, sy + topBorder, xx, ey - bottomBorder, rightBorder, end - start - topBorder - bottomBorder, borderStyle.right);
				}
				if(topBorder!=0) {
					double bw2 = topBorder / 2;
					double yy = sy + bw2;
					drawMyLine(page, sx + leftBorder, yy, ex - rightBorder, yy, topBorder, travel - sky - earth - leftBorder - rightBorder, borderStyle.top);
				}
				if(bottomBorder!=0) {
					double bw2 = bottomBorder / 2;
					double yy = ey - bw2;
					drawMyLine(page, sx + leftBorder, yy, ex - rightBorder, yy, bottomBorder, travel - sky - earth - leftBorder - rightBorder, borderStyle.bottom);
				}
			}
			else {
				double sx = x - end;
				double ex = x - start;
				double sy = y + earth;
				double ey = y + travel - sky;
				double leftBorder = (0<borderWidth.left && !eFlag) ? borderWidth.left : 0;
				double rightBorder = (0<borderWidth.right && !sFlag) ? borderWidth.right : 0;
				double topBorder = 0<borderWidth.top ? borderWidth.top : 0;
				double bottomBorder = 0<borderWidth.bottom ? borderWidth.bottom : 0;
				page.setFillColor(backgroundColor);
				page.fillRect(sx, sy, ex-sx, ey-sy);
				page.setLineCap(0);

				YiPdfColor borderColor = new YiPdfColor(0, 0, 0);
				page.setDrawColor(borderColor);
				page.setFillColor(borderColor);
				if(topBorder!=0 && leftBorder!=0) {
					page.fillRect(sx, sy, leftBorder, topBorder);
				}
				if(topBorder!=0 && rightBorder!=0) {
					page.fillRect(ex-rightBorder, sy, rightBorder, topBorder);
				}
				if(bottomBorder!=0 && leftBorder!=0) {
					page.fillRect(sx, ey-bottomBorder, leftBorder, bottomBorder);
				}
				if(bottomBorder!=0 && rightBorder!=0) {
					page.fillRect(ex-rightBorder, ey-bottomBorder, rightBorder, bottomBorder);
				}
				if(leftBorder!=0) {
					double bw2 = leftBorder / 2;
					double xx = sx + bw2;
					drawMyLine(page, xx, sy + topBorder, xx, ey - bottomBorder, leftBorder, end - start - topBorder - bottomBorder, borderStyle.left);
				}
				if(rightBorder!=0) {
					double bw2 = rightBorder / 2;
					double xx = ex - bw2;
					drawMyLine(page, xx, sy + topBorder, xx, ey - bottomBorder, rightBorder, end - start - topBorder - bottomBorder, borderStyle.right);
				}
				if(topBorder!=0) {
					double bw2 = topBorder / 2;
					double yy = sy + bw2;
					drawMyLine(page, sx + leftBorder, yy, ex - rightBorder, yy, topBorder, travel - sky - earth - leftBorder - rightBorder, borderStyle.top);
				}
				if(bottomBorder!=0) {
					double bw2 = bottomBorder / 2;
					double yy = ey - bw2;
					drawMyLine(page, sx + leftBorder, yy, ex - rightBorder, yy, bottomBorder, travel - sky - earth - leftBorder - rightBorder, borderStyle.bottom);
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
