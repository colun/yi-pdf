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
	MyLayoutNest() {
		parent = null;
		backgroundColor = null;
	}
	MyLayoutNest(MyLayoutStyle nowStyle) {
		this(null, nowStyle);
	}
	MyLayoutNest(MyLayoutNest parent, MyLayoutStyle nowStyle) {
		this.parent = parent;
		backgroundColor = nowStyle.hasBackgroundColor() ? nowStyle.getBackgroundColor() : null;
	}
	void draw(MyLayoutPageContext pageContext, double x, double y, double start, double end, double travel, boolean verticalWritingMode) throws IOException {
		if(backgroundColor!=null) {
			YiPdfPage page = pageContext.getPdfPage();
			if(!verticalWritingMode) {
				page.setFillColor(backgroundColor);
				page.fillRect(x, y + start, travel, end - start);
			}
			else {
				page.setFillColor(backgroundColor);
				page.fillRect(x - end, y, end - start, travel);
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
