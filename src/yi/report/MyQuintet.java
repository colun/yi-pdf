/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

class MyQuintet<T1, T2, T3, T4, T5> {
	MyQuintet(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
		first = v1;
		second = v2;
		third = v3;
		fourth = v4;
		fifth = v5;
	}
	final T1 first;
	final T2 second;
	final T3 third;
	final T4 fourth;
	final T5 fifth;
}
