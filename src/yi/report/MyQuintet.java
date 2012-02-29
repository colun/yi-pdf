/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

class MyQuintet<T1, T2, T3, T4, T5> extends MyQuartet<T1, T2, T3, T4> {
	MyQuintet(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
		super(v1, v2, v3, v4);
		fifth = v5;
	}
	final T5 fifth;
}
