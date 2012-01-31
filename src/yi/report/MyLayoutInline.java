package yi.report;

import java.io.IOException;

import yi.pdf.YiPdfPage;

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
	public abstract void draw(YiPdfPage page) throws IOException;
}
