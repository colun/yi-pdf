package yi.report;

import java.util.ArrayList;
import java.util.List;

class MyLayoutLine {
	double width;
	double inlineWidthSum;
	double upperPerpend;
	double lowerPerpend;
	public MyLayoutLine(double width) {
		this.width = width;
		inlineWidthSum = 0;
		lowerPerpend = 0;
		upperPerpend = 0;
	}
	double getRemainingWidth() {
		return width - inlineWidthSum;
	}
	List<MyLayoutInline> inlineList = new ArrayList<MyLayoutInline>();
	public void addInline(MyLayoutInline myLayoutInline) {
		inlineList.add(myLayoutInline);
		inlineWidthSum += myLayoutInline.getTravel();
		lowerPerpend = Math.min(lowerPerpend, myLayoutInline.getLowerPerpend());
		upperPerpend = Math.min(upperPerpend, myLayoutInline.getUpperPerpend());
	}
}
