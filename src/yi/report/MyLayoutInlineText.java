package yi.report;

import yi.pdf.YiPdfColor;
import yi.pdf.YiPdfFont;

class MyLayoutInlineText extends MyLayoutInline {
	YiPdfFont font;
	double fontSize;
	YiPdfColor color;
	String text;
	double travel;
	public MyLayoutInlineText(YiPdfFont font, double fontSize, YiPdfColor color, String text, double travel) {
		this.font = font;
		this.fontSize = fontSize;
		this.color = color;
		this.text = text;
		this.travel = travel;
	}
	@Override
	public double getTravel() {
		return travel;
	}
	@Override
	public double getLowerPerpend() {
		return font.getLowerPerpend('A');
	}
	@Override
	public double getUpperPerpend() {
		return font.getUpperPerpend('A');
	}
}
