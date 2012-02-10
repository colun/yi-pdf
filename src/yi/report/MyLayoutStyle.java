package yi.report;

import java.util.HashMap;
import java.util.Map;

import yi.pdf.YiPdfColor;
import yi.pdf.YiPdfFont;
import yi.pdf.font.YiPdfJGothicFont;
import yi.pdf.font.YiPdfJGothicFontV;
import yi.pdf.font.YiPdfJMinchoFont;
import yi.pdf.font.YiPdfJMinchoFontV;

public class MyLayoutStyle {
	Map<String, String> style = new HashMap<String, String>();
	Map<String, String> diff = null;
	MyLayoutStyle() {
	}
	MyLayoutStyle merge(Map<String, String> diff) {
		MyLayoutStyle result = new MyLayoutStyle();
		result.style.putAll(style);
		if(diff!=null) {
			result.style.putAll(diff);
		}
		result.diff = diff;
		return result;
	}
	static YiPdfFont gothicFont = new YiPdfJGothicFont();
	static YiPdfFont minchoFont = new YiPdfJMinchoFont();
	static YiPdfFont gothicFontV = new YiPdfJGothicFontV();
	static YiPdfFont minchoFontV = new YiPdfJMinchoFontV();
	YiPdfFont getFont() {
		String str = style.get("font-family");
		if(str!=null && 0<=str.toLowerCase().indexOf("min")) {
			if(isVerticalWritingMode()) {
				return minchoFontV;
			}
			else {
				return minchoFont;
			}
		}
		else {
			if(isVerticalWritingMode()) {
				return gothicFontV;
			}
			else {
				return gothicFont;
			}
		}
	}
	boolean isVerticalWritingMode() {
		String str = style.get("writing-mode");
		if(str==null) {
			return false;
		}
		if(0<=str.indexOf("tb-rl") || 0<=str.indexOf("vertical")) {
			return true;
		}
		return false;
	}
	double getFontSize() {
		String str = style.get("font-size");
		if(str==null) {
			return 10.5;
		}
		return MyUtil.evalUnit(str);
	}
	YiPdfColor getFontColor() {
		String str = style.get("color");
		if(str==null) {
			return new YiPdfColor(0, 0, 0);
		}
		return MyUtil.evalColor(str);
	}
	boolean getLineBreakHang() {
		String str = style.get("line-break-hang");
		if(str==null || str.isEmpty() || "0".equals(str) || "false".equals(str)) {
			return false;
		}
		return true;
	}
	double getPageWidth() {
		return MyUtil.evalUnit(style.get("page-width"));
	}
	double getPageHeight() {
		return MyUtil.evalUnit(style.get("page-height"));
	}
	double getMarginLeft() {
		return MyUtil.evalUnit(style.get("margin-left"));
	}
	double getMarginTop() {
		return MyUtil.evalUnit(style.get("margin-top"));
	}
	double getMarginRight() {
		return MyUtil.evalUnit(style.get("margin-right"));
	}
	double getMarginBottom() {
		return MyUtil.evalUnit(style.get("margin-bottom"));
	}
	boolean hasFloat() {
		return diff.containsKey("float");
	}
	boolean hasWritingMode() {
		return diff.containsKey("writing-mode");
	}
	boolean hasNewlyPage() {
		return diff.containsKey("page-width")
		|| diff.containsKey("page-height")
		|| diff.containsKey("resolution")
		|| diff.containsKey("page-break-before")
		;
	}
}
