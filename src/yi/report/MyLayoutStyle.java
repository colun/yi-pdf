/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yi.pdf.YiPdfColor;
import yi.pdf.YiPdfFont;
import yi.pdf.font.YiPdfJGothicFont;
import yi.pdf.font.YiPdfJGothicFontV;
import yi.pdf.font.YiPdfJMinchoFont;
import yi.pdf.font.YiPdfJMinchoFontV;

class MyLayoutStyle {
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
		if(str!=null && str.toLowerCase().contains("min")) {
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
	static Pattern unitPattern = Pattern.compile("([0-9]+(\\.[0-9]*)?)([%\\w]*)");
	double evalUnit(String str, boolean fsFlag, double base) {
		if(str==null) {
			return 0;
		}
		Matcher m = unitPattern.matcher(str);
		boolean f = m.matches();
		assert(f) : "不正な数値[単位]が指定されました。";
		double val = Double.valueOf(m.group(1));
		String unit = m.group(3);
		if(unit.isEmpty() || "pt".equals(unit)) {
			return val;
		}
		if("in".equals(unit)) {
			return val * 72;
		}
		if("mm".equals(unit)) {
			return val * (72/25.4);
		}
		if("cm".equals(unit)) {
			return val * (72/2.54);
		}
		if("%".equals(unit)) {
			return base * val / 100;
		}
		if("em".equals(unit)) {
			double fontSize = fsFlag ? 10.5 : getFontSize();
			return fontSize * val;
		}
		assert(false) : "知らない単位が指定されました。";
		return 0;
	}
	double evalUnit(String str, boolean fsFlag) {
		return evalUnit(str, fsFlag, 0);
	}
	double evalUnit(String str) {
		return evalUnit(str, false, 0);
	}
	final static Map<String, YiPdfColor> colorMap = new HashMap<String, YiPdfColor>();
	static {
		colorMap.put("black", new YiPdfColor(0, 0, 0));
		colorMap.put("red", new YiPdfColor(1, 0, 0));
		colorMap.put("green", new YiPdfColor(0, 1, 0));
		colorMap.put("blue", new YiPdfColor(0, 0, 1));
	}
	static Pattern colorPattern = Pattern.compile("#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");
	static YiPdfColor evalColor(String color) {
		YiPdfColor result = colorMap.get(color);
		if(result!=null) {
			return result;
		}
		Matcher m = colorPattern.matcher(color);
		boolean f = m.matches();
		assert(f) : "不正な色が指定されました。";
		double r = Integer.valueOf(m.group(1), 16) / 255.0;
		double g = Integer.valueOf(m.group(2), 16) / 255.0;
		double b = Integer.valueOf(m.group(3), 16) / 255.0;
		return new YiPdfColor(r, g, b);
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
		return evalUnit(str, true);
	}
	Double getLineHeight() {
		String str = style.get("line-height");
		if(str==null) {
			return null;
		}
		return evalUnit(str);
	}
	YiPdfColor getFontColor() {
		String str = style.get("color");
		if(str==null) {
			return new YiPdfColor(0, 0, 0);
		}
		return evalColor(str);
	}
	boolean getLineBreakHang() {
		String str = style.get("line-break-hang");
		if(str==null || str.isEmpty() || "0".equals(str) || "false".equals(str)) {
			return false;
		}
		return true;
	}
	boolean hasPageWidth() {
		return diff.containsKey("page-width");
	}
	double getPageWidth() {
		return evalUnit(diff.get("page-width"));
	}
	boolean hasPageHeight() {
		return diff.containsKey("page-height");
	}
	double getPageHeight() {
		return evalUnit(diff.get("page-height"));
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
	boolean hasWidth() {
		return diff.containsKey("width");
	}
	boolean hasHeight() {
		return diff.containsKey("height");
	}
	double getWidth() {
		return evalUnit(diff.get("width"));
	}
	double getHeight() {
		return evalUnit(diff.get("height"));
	}
	public String getFloat() {
		return diff.get("float");
	}
	public boolean hasBackgroundColor() {
		return diff.containsKey("background-color");
	}
	YiPdfColor getBackgroundColor() {
		String colorStr = diff.get("background-color");
		if(colorStr==null) {
			return null;
		}
		return evalColor(colorStr);
	}
	public MyLayoutMargin getPageMargin(MyLayoutMargin beforeMargin) {
		String str;
		str = diff.get("margin-left");
		double left = str==null ? beforeMargin.left : evalUnit(str);
		str = diff.get("margin-top");
		double top = str==null ? beforeMargin.top : evalUnit(str);
		str = diff.get("margin-right");
		double right = str==null ? beforeMargin.right : evalUnit(str);
		str = diff.get("margin-bottom");
		double bottom = str==null ? beforeMargin.bottom : evalUnit(str);
		return new MyLayoutMargin(left, top, right, bottom);
	}
	public MyLayoutMargin getMargin() {
		double left = evalUnit(diff.get("margin-left"));
		double top = evalUnit(diff.get("margin-top"));
		double right = evalUnit(diff.get("margin-right"));
		double bottom = evalUnit(diff.get("margin-bottom"));
		return new MyLayoutMargin(left, top, right, bottom);
	}
	public MyLayoutMargin getPadding() {
		double left = evalUnit(diff.get("padding-left"));
		double top = evalUnit(diff.get("padding-top"));
		double right = evalUnit(diff.get("padding-right"));
		double bottom = evalUnit(diff.get("padding-bottom"));
		return new MyLayoutMargin(left, top, right, bottom);
	}
}
