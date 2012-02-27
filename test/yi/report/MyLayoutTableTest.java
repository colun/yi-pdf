package yi.report;

import junit.framework.TestCase;

public class MyLayoutTableTest extends TestCase {
	public void test1() {
		System.out.println("test1");
		double[] w = MyLayoutTable.calcColumnWidths(3
				, new int[] { 0, 1, 2 }
				, new int[] { 1, 1, 1 }
				, new double[] { 10, 10, 10}
		);
		for(double d : w) {
			System.out.println(d);
		}
	}
	public void test2() {
		System.out.println("test2");
		double[] w = MyLayoutTable.calcColumnWidths(3
				, new int[] { 0, 1 }
				, new int[] { 2, 2 }
				, new double[] { 20, 15}
		);
		for(double d : w) {
			System.out.println(d);
		}
	}

}
