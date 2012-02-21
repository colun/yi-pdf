package yi.report;

import yi.pdf.YiPdfColor;

public class MyLayoutPageStyle {
	public MyLayoutPageStyle(MyLayoutStyle nowStyle, MyLayoutPageStyle beforeStyle) {
		backgroundColor = nowStyle.hasBackgroundColor() ? nowStyle.getBackgroundColor() : beforeStyle!=null ? beforeStyle.backgroundColor : null;
		pageMargin = nowStyle.getPageMargin(beforeStyle!=null ? beforeStyle.pageMargin : new MyLayoutMargin(0, 0, 0, 0));
		width = nowStyle.hasPageWidth() ? nowStyle.getPageWidth() : beforeStyle!=null ? beforeStyle.width : 595.28;
		height = nowStyle.hasPageHeight() ? nowStyle.getPageHeight() : beforeStyle!=null ? beforeStyle.height : 841.89;
	}
	public final YiPdfColor backgroundColor;
	public final MyLayoutMargin pageMargin;
	public final double width;
	public final double height;
}
