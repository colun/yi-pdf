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
	double lowerPerpend;
	double upperPerpend;
	double fontLowerPerpend;
	double fontUpperPerpend;
	Double lineHeight;
	public MyLayoutInlineText(YiPdfFont font, double fontSize, YiPdfColor color, String text, double travel, YiPdfTag lineTag, Double lineHeight) {
		this.font = font;
		this.fontSize = fontSize;
		this.color = color;
		this.text = text;
		this.travel = travel;
		this.lineTag = lineTag;
		this.lineHeight = lineHeight;
		lowerPerpend = fontSize * font.getLowerPerpend('A') / 1000;
		upperPerpend = fontSize * font.getUpperPerpend('A') / 1000;
		fontLowerPerpend = lowerPerpend;
		fontUpperPerpend = upperPerpend;
		if(lineHeight!=null) {
			double h = upperPerpend - lowerPerpend;
			if(h < lineHeight) {
				upperPerpend += (lineHeight - h) / 2;
				lowerPerpend = upperPerpend - lineHeight;
			}
		}
	}
	public void setTransparentFlag(boolean transparentFlag) {
		this.transparentFlag = transparentFlag;
	}
	public void changeScale(double rate) {
		fontSize *= rate;
		travel *= rate;
		lowerPerpend *= rate;
		upperPerpend *= rate;
		fontLowerPerpend *= rate;
		fontUpperPerpend *= rate;
		if(lineHeight!=null) {
			lineHeight *= rate;
		}
	}
	@Override
	public double getTravel() {
		return travel;
	}
	@Override
	public double getLowerPerpend() {
		return lowerPerpend;
	}
	@Override
	public double getUpperPerpend() {
		return upperPerpend;
	}
	@Override
	public void draw(MyLayoutPageContext pageContext) throws IOException {
		boolean verticalWritingMode = font.isVertical();
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
			if(!verticalWritingMode) {
				beforeRp.setPos(posX - beforeRp.getTravel(), posY - fontUpperPerpend + beforeRp.getLowerPerpend());
			}
			else {
				beforeRp.setPos(posX + fontUpperPerpend - beforeRp.getLowerPerpend(), posY - beforeRp.getTravel());
			}
			pageContext.addRuby(beforeRp);
		}
		if(rubyList!=null) {
			for(MyLayoutInlineText ruby : rubyList) {
				if(!verticalWritingMode) {
					ruby.setPos(posX + ruby.getRubyTravelDiff(), posY - fontUpperPerpend + ruby.getLowerPerpend());
				}
				else {
					ruby.setPos(posX + fontUpperPerpend - ruby.getLowerPerpend(), posY + ruby.getRubyTravelDiff());
				}
				pageContext.addRuby(ruby);
			}
		}
		if(afterRp!=null) {
			if(!verticalWritingMode) {
				afterRp.setPos(posX + this.getTravel(), posY - fontUpperPerpend + afterRp.getLowerPerpend());
			}
			else {
				afterRp.setPos(posX + fontUpperPerpend - afterRp.getLowerPerpend(), posY + getTravel());
			}
			pageContext.addRuby(afterRp);
		}
		if(rubyLastFlag) {
			pageContext.invokeRuby();
		}
	}
	public List<MyLayoutInlineText> explode() {
		List<MyLayoutInlineText> result = new ArrayList<MyLayoutInlineText>();
		for(int i=0; i<text.length(); ++i) {
			result.add(new MyLayoutInlineText(font, fontSize, color, text.substring(i, i+1), fontSize * font.getTravel(text.charAt(i)) / 1000, lineTag, lineHeight));
		}
		return result;
	}
	double rubyTravelDiff = 0;
	boolean rubyLastFlag = false;
	public double getRubyTravelDiff() {
		return rubyTravelDiff;
	}
	public void setRubyTravelDiff(double rubyTravelDiff) {
		this.rubyTravelDiff = rubyTravelDiff;
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
