/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;

abstract class MyLayoutInline {
	double posX;
	double posY;
	public void setPos(double x, double y) {
		posX = x;
		posY = y;
	}
	public abstract double getTravel();
	public abstract double getLowerPerpend();
	public abstract double getUpperPerpend();
	public abstract void draw(MyLayoutPageContext pageContext, double x, double y) throws IOException;
}
