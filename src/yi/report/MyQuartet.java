/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

class MyQuartet<T1, T2, T3, T4> extends MyTrio<T1, T2, T3> {
	MyQuartet(T1 v1, T2 v2, T3 v3, T4 v4) {
		super(v1, v2, v3);
		fourth = v4;
	}
	final T4 fourth;
}
