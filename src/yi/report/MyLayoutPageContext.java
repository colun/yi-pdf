package yi.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yi.pdf.YiPdfPage;

class MyLayoutPageContext {
	YiPdfPage pdfPage;
	List<MyLayoutInline> rubyList = new ArrayList<MyLayoutInline>();
	MyLayoutPageContext(YiPdfPage pdfPage) {
		this.pdfPage = pdfPage;
	}
	YiPdfPage getPdfPage() {
		return pdfPage;
	}
	void addRuby(MyLayoutInline ruby) {
		rubyList.add(ruby);
	}
	void invokeRuby() throws IOException {
		for(MyLayoutInline ruby : rubyList) {
			ruby.draw(this);
		}
		rubyList.clear();
	}
}
