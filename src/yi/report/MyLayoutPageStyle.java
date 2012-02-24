package yi.report;

import yi.pdf.YiPdfColor;

public class MyLayoutPageStyle {
	public MyLayoutPageStyle(MyLayoutStyle style) {
		backgroundColor = style.getBackgroundColor();
		pageMargin = style.getMargin();
		width = style.getSizeWidth();
		height = style.getSizeHeight();
		resolution = style.getResolution();
	}
	public final YiPdfColor backgroundColor;
	public final MyLayoutMargin pageMargin;
	public final double width;
	public final double height;
	public final double resolution;
}
