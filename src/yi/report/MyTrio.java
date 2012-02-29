/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

class MyTrio<T1, T2, T3> extends MyPair<T1, T2> {
	MyTrio(T1 v1, T2 v2, T3 v3) {
		super(v1, v2);
		third = v3;
	}
	final T3 third;
}
