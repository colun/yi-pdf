/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.pdf;

public class YiPdfColor {
	public YiPdfColor(double r, double g, double b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	public boolean equals(Object obj) {
		if(obj instanceof YiPdfColor) {
			YiPdfColor other = (YiPdfColor)obj;
			return other.r==r && other.g==g && other.b==b;
		}
		return false;
	}
	final double r;
	final double g;
	final double b;
}
