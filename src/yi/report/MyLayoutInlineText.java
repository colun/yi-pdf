package yi.report;

import java.io.IOException;

import yi.pdf.YiPdfColor;
import yi.pdf.YiPdfFont;
import yi.pdf.YiPdfPage;

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
		return fontSize * font.getLowerPerpend('A') / 1000;
	}
	@Override
	public double getUpperPerpend() {
		return fontSize * font.getUpperPerpend('A') / 1000;
	}
	@Override
	public void draw(YiPdfPage page) throws IOException {
		page.setFont(font);
		page.setFontSize(fontSize);
		page.drawText(posX, posY, text);
	}
}
