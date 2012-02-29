/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

class MySextet<T1, T2, T3, T4, T5, T6> extends MyQuintet<T1, T2, T3, T4, T5> {
	MySextet(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6) {
		super(v1, v2, v3, v4, v5);
		sixth = v6;
	}
	final T6 sixth;
}
