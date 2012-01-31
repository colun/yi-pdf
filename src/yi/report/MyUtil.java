package yi.report;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yi.pdf.YiPdfColor;

class MyUtil {
	final static Map<String, YiPdfColor> colorMap = new HashMap<String, YiPdfColor>();
	static {
		colorMap.put("black", new YiPdfColor(0, 0, 0));
		colorMap.put("red", new YiPdfColor(1, 0, 0));
		colorMap.put("green", new YiPdfColor(0, 1, 0));
		colorMap.put("blue", new YiPdfColor(0, 0, 1));
	}
	static Pattern unitPattern = Pattern.compile("([0-9]+(\\.[0-9]*)?)([%\\w]*)");
	static double evalUnit(String str, double base) {
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
			assert(false) : "em指定は現在未実装です。";
			double fontSize = 10.5;
			return fontSize * val;
		}
		assert(false) : "知らない単位が指定されました。";
		return 0;
	}
	static double evalUnit(String str) {
		return evalUnit(str, 0);
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

}
