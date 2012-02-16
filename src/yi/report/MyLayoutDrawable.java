/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;

interface MyLayoutDrawable {
	public void draw(MyLayoutPageContext pageContext, double x, double y) throws IOException;
}
