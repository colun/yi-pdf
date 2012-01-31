package yi.report;

import java.io.IOException;

import yi.pdf.YiPdfColor;
import yi.pdf.YiPdfFont;
import yi.pdf.YiPdfPage;
import yi.pdf.YiPdfTag;

class MyLayoutInlineText extends MyLayoutInline {
	YiPdfFont font;
	double fontSize;
	YiPdfColor color;
	String text;
	double travel;
	YiPdfTag lineTag;
	public MyLayoutInlineText(YiPdfFont font, double fontSize, YiPdfColor color, String text, double travel, YiPdfTag lineTag) {
		this.font = font;
		this.fontSize = fontSize;
		this.color = color;
		this.text = text;
		this.travel = travel;
		this.lineTag = lineTag;
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
		page.beginTextTag(lineTag.makeChild("Span"));
		page.setFont(font);
		page.setFontSize(fontSize);
		page.drawText(posX, posY, text);
		page.endTextTag();
	}
}
