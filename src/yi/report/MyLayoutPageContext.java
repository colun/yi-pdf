/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yi.pdf.YiPdfPage;

class MyLayoutPageContext {
	YiPdfPage pdfPage;
	List<MyQuartet<MyLayoutInline, Double, Double, Object>> rubyList = new ArrayList<MyQuartet<MyLayoutInline,Double,Double,Object>>();
	MyLayoutPageContext(YiPdfPage pdfPage) {
		this.pdfPage = pdfPage;
	}
	YiPdfPage getPdfPage() {
		return pdfPage;
	}
	void addRuby(MyLayoutInline ruby, double x, double y) {
		rubyList.add(new MyQuartet<MyLayoutInline, Double, Double, Object>(ruby, x, y, null));
	}
	void invokeRuby() throws IOException {
		for(MyQuartet<MyLayoutInline, Double, Double, Object> ruby : rubyList) {
			ruby.first.draw(this, ruby.second, ruby.third);
		}
		rubyList.clear();
	}
}
