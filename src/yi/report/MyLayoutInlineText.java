/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	boolean transparentFlag = false;
	public MyLayoutInlineText(YiPdfFont font, double fontSize, YiPdfColor color, String text, double travel, YiPdfTag lineTag) {
		this.font = font;
		this.fontSize = fontSize;
		this.color = color;
		this.text = text;
		this.travel = travel;
		this.lineTag = lineTag;
	}
	public void setTransparentFlag(boolean transparentFlag) {
		this.transparentFlag = transparentFlag;
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
	public void draw(MyLayoutPageContext pageContext) throws IOException {
		YiPdfPage page = pageContext.getPdfPage();
		page.beginTextTag(lineTag.makeChild("Span"));
		page.setFont(font);
		page.setFontSize(fontSize);
		page.setTextColor(color);
		if(transparentFlag) {
			page.setTextRenderingMode(3);
		}
		page.drawText(posX, posY, text);
		if(transparentFlag) {
			page.setTextRenderingMode(0);
		}
		page.endTextTag();
		if(beforeRp!=null) {
			beforeRp.setPos(posX - beforeRp.getTravel(), posY - getUpperPerpend() + beforeRp.getLowerPerpend());
			pageContext.addRuby(beforeRp);
		}
		if(rubyList!=null) {
			for(MyLayoutInlineText ruby : rubyList) {
				ruby.setPos(posX + ruby.getDx(), posY - getUpperPerpend() + ruby.getLowerPerpend());
				pageContext.addRuby(ruby);
			}
		}
		if(afterRp!=null) {
			afterRp.setPos(posX + this.getTravel(), posY - getUpperPerpend() + afterRp.getLowerPerpend());
			pageContext.addRuby(afterRp);
		}
		if(rubyLastFlag) {
			pageContext.invokeRuby();
		}
	}
	public List<MyLayoutInlineText> explode() {
		List<MyLayoutInlineText> result = new ArrayList<MyLayoutInlineText>();
		for(int i=0; i<text.length(); ++i) {
			result.add(new MyLayoutInlineText(font, fontSize, color, text.substring(i, i+1), fontSize * font.getTravel(text.charAt(i)) / 1000, lineTag));
		}
		return result;
	}
	double dx = 0;
	boolean rubyLastFlag = false;
	public double getDx() {
		return dx;
	}
	public void setDx(double dx) {
		this.dx = dx;
	}
	List<MyLayoutInlineText> rubyList;
	public void setRuby(List<MyLayoutInlineText> rubyList) {
		this.rubyList = rubyList;
	}
	public void setRubyLastFlag(boolean rubyLastFlag) { 
		this.rubyLastFlag = rubyLastFlag;
	}
	MyLayoutInlineText beforeRp = null;
	public void setBeforeRp(MyLayoutInlineText beforeRp) {
		this.beforeRp = beforeRp;
	}
	MyLayoutInlineText afterRp = null;
	public void setAfterRp(MyLayoutInlineText afterRp) {
		this.afterRp = afterRp;
	}
}
