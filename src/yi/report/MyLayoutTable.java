/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

class MyLayoutTable {
	public static enum ModeType {
		MODE_SCAN1,
		MODE_SCAN2,
		MODE_VISIT
	}
	static class MyTableBorder implements Comparable<MyTableBorder> {
		static enum BorderStyle {
			NONE("none", false),
			DOTED("doted", true),
			DASHED("dashed", true),
			SOLID("solid", true),
			HIDDEN("hidden", false);
			private final String name;
			private final boolean visible;
			private BorderStyle(String name, boolean visible) {
				this.name = name;
				this.visible = visible;
			}
			static HashMap<String, BorderStyle> enumDic = null;
			public static HashMap<String, BorderStyle> getDic() {
				HashMap<String, BorderStyle> eDic = enumDic;
				if(eDic==null) {
					eDic = new HashMap<String, BorderStyle>();
					for(BorderStyle style : values()) {
						eDic.put(style.name, style);
					}
					enumDic = eDic;
				}
				return eDic;
			}
			public static BorderStyle fromString(String name) {
				BorderStyle result = getDic().get(name);
				return result;
			}
		}
		MyTableBorder(String style, double width) {
			this.style = BorderStyle.fromString(style);
			this.width = width;
		}
		final BorderStyle style;
		final double width;
		public int compareTo(MyTableBorder o) {
			assert(o!=null);
			if(style==BorderStyle.HIDDEN) {
				return o.style==BorderStyle.HIDDEN ? 0 : 1;
			}
			else if(o.style==BorderStyle.HIDDEN) {
				return -1;
			}
			else if(width!=o.width) {
				return width < o.width ? -1 : 1;
			}
			else if(style!=o.style) {
				return style.compareTo(o.style);
			}
			return 0;
		}
		public boolean isVisible() {
			return style!=null && style.visible;
		}
	}

	boolean verticalWritingMode;
	boolean hasTotalTravel;
	double totalTravel;
	final MyLayoutContext layoutContext;
	public MyLayoutTable(MyLayoutContext layoutContext) {
		this.layoutContext = layoutContext;
		MyLayoutStyle nowStyle = layoutContext.getNowStyle();
		verticalWritingMode = nowStyle.isVerticalWritingMode();
		hasTotalTravel = !verticalWritingMode ? nowStyle.hasWidth() : nowStyle.hasHeight();
		if(hasTotalTravel){
			totalTravel = !verticalWritingMode ? nowStyle.getWidth() : nowStyle.getHeight();
		}
	}

	ModeType mode;
	public void setMode(ModeType i) {
		mode = i;
	}
	public ModeType getMode() {
		return mode;
	}
	int nowCol;
	int nowRow;
	int colCount = 0;
	int rowCount = 0;
	int getColCount() {
		return colCount;
	}
	int getRowCount() {
		return rowCount;
	}
	public void initVisit() {
		nowCol = 0;
		nowRow = 0;
		existSet.clear();
	}
	public void incRow() {
		++nowRow;
		nowCol = 0;
	}
	Set<Integer> existSet = new HashSet<Integer>();
	Map<Integer, MyTableBorder> borderMap = new HashMap<Integer, MyLayoutTable.MyTableBorder>();
	protected int calcPos(int row, int col) {
		return row * 65536 + col;
	}
	int getSpan(Map<String, String> attr, String key) {
		String str = attr.get(key);
		int result = str==null ? 1 : Integer.parseInt(str);
		assert(1<=result);
		return result;
	}
	void putBorder(int pos, MyTableBorder border) {
		MyTableBorder oldBorder = borderMap.get(pos);
		if(oldBorder==null || oldBorder.compareTo(border)<0) {
			borderMap.put(pos, border);
		}
	}
	Map<Integer, Double> travelMap = new TreeMap<Integer, Double>();
	void putTravel(int mask, double travel) {
		Double oldValue = travelMap.get(mask);
		if(oldValue==null || oldValue<travel) {
			travelMap.put(mask, travel);
		}
	}
	public void beginMode(ModeType mode) {
		setMode(mode);
		initVisit();
	}
	double[] rowBorderWidthList;
	double[] colBorderWidthList;
	double[] columnWidthList;
	public void endMode() {
		if(mode==ModeType.MODE_SCAN1) {
			rowBorderWidthList = new double[rowCount+1];
			for(int y=0; y<=rowCount; ++y) {
				double maxWidth = 0;
				for(int x=0; x<colCount; ++x) {
					int pos = calcPos(y+y, x+x+1);
					MyTableBorder border = borderMap.get(pos);
					if(border!=null && border.isVisible()) {
						maxWidth = Math.max(maxWidth, border.width);
					}
				}
				rowBorderWidthList[y] = maxWidth;
			}
			colBorderWidthList = new double[colCount+1];
			for(int x=0; x<=colCount; ++x) {
				double maxWidth = 0;
				for(int y=0; y<rowCount; ++y) {
					int pos = calcPos(y+y+1, x+x);
					MyTableBorder border = borderMap.get(pos);
					if(border!=null && border.isVisible()) {
						maxWidth = Math.max(maxWidth, border.width);
					}
				}
				colBorderWidthList[x] = maxWidth;
			}
		}
		else if(mode==ModeType.MODE_SCAN2) {
			int size = travelMap.size() + (hasTotalTravel ? 1 : 0);
			int i = 0;
			int[] startCol = new int[size];
			int[] colspan = new int[size];
			double[] width = new double[size];
			for(Entry<Integer, Double> entry : travelMap.entrySet()) {
				int colspan_startcol = entry.getKey();
				colspan[i] = (colspan_startcol >> 16) & 65535;
				startCol[i] = colspan_startcol & 65535;
				width[i] = entry.getValue();
				++i;
			}
			if(hasTotalTravel) {
				colspan[i] = colCount;
				startCol[i] = 0;
				width[i] = totalTravel;
			}
			columnWidthList = calcColumnWidths(colCount, startCol, colspan, width);
		}
	}
	public static double[] calcColumnWidths(int colCount, int[] startCol, int[] colspan, double[] width) {
		double[] result = new double[colCount];
		int n = startCol.length;
		for(int i=0; i<n; ++i) {
			int cnt = colspan[i];
			assert(1<=cnt);
			int st = startCol[i];
			double w = width[i];
			int unknownCnt = 0;
			double knownSum = 0;
			for(int j=0; j<cnt; ++j) {
				if(result[st+j]==0) {
					++unknownCnt;
				}
				else {
					knownSum += result[st+j];
				}
			}
			if(unknownCnt!=0) {
				double unknownWidth = (w - knownSum) / unknownCnt;
				for(int j=0; j<cnt; ++j) {
					if(result[st+j]==0) {
						result[st+j] = unknownWidth;
					}
				}
			}
			else if(knownSum < w) {
				double addWidth = (w - knownSum) / cnt;
				for(int j=0; j<cnt; ++j) {
					result[st+j] += addWidth;
				}
			}
		}
		return result;
	}
	public void scan(Map<String, String> attr, ModeType mode) {
		MyLayoutStyle nowStyle = layoutContext.getNowStyle();
		while(true) {
			int pos = calcPos(nowRow, nowCol);
			if(!existSet.contains(pos)) {
				break;
			}
			++nowCol;
		}
		int colspan = getSpan(attr, "colspan");
		int rowspan = getSpan(attr, "rowspan");
		for(int y=0; y<rowspan; ++y) {
			for(int x=0; x<colspan; ++x) {
				int pos = calcPos(nowRow + y, nowCol + x);
				existSet.add(pos);
				if(colCount<=nowCol+x) {
					colCount = nowCol + x + 1;
				}
				if(rowCount<=nowRow+y) {
					rowCount = nowRow + y + 1;
				}
			}
		}
		if(mode==ModeType.MODE_SCAN1) {
			MyEdgeValues<String> borderStyle = nowStyle.getBorderStyle();
			MyLayoutMargin borderWidth = nowStyle.getBorderWidth();
			MyTableBorder earthBorder;
			MyTableBorder prevBorder;
			MyTableBorder skyBorder;
			MyTableBorder postBorder;
			if(!verticalWritingMode) {
				earthBorder = new MyTableBorder(borderStyle.left, borderWidth.left);
				prevBorder = new MyTableBorder(borderStyle.top, borderWidth.top);
				skyBorder = new MyTableBorder(borderStyle.right, borderWidth.right);
				postBorder = new MyTableBorder(borderStyle.bottom, borderWidth.bottom);
			}
			else {
				earthBorder = new MyTableBorder(borderStyle.top, borderWidth.top);
				prevBorder = new MyTableBorder(borderStyle.right, borderWidth.right);
				skyBorder = new MyTableBorder(borderStyle.bottom, borderWidth.bottom);
				postBorder = new MyTableBorder(borderStyle.left, borderWidth.left);
			}
			for(int y=0; y<rowspan; ++y) {
				int sCol = nowCol;
				int eCol = nowCol + colspan;
				int row = nowRow+y;
				putBorder(calcPos(row+row+1, sCol+sCol), earthBorder);
				putBorder(calcPos(row+row+1, eCol+eCol), skyBorder);
			}
			for(int x=0; x<colspan; ++x) {
				int sRow = nowRow;
				int eRow = nowRow + colspan;
				int col = nowCol+x;
				putBorder(calcPos(sRow+sRow, col+col+1), prevBorder);
				putBorder(calcPos(eRow+eRow, col+col+1), postBorder);
			}
		}
		else if(mode==ModeType.MODE_SCAN2) {
			MyLayoutMargin padding = nowStyle.getPadding();
			if(!verticalWritingMode ? nowStyle.hasWidth() : nowStyle.hasHeight()) {
				double travel = !verticalWritingMode ? nowStyle.getWidth() : nowStyle.getHeight();
				double earthWidth = 0;
				double skyWidth = 0;
				for(int y=0; y<rowspan; ++y) {
					int sCol = nowCol;
					int eCol = nowCol + colspan;
					int row = nowRow+y;
					MyTableBorder border1 = borderMap.get(calcPos(row+row+1, sCol+sCol));
					if(border1!=null && border1.isVisible()) {
						earthWidth = Math.max(earthWidth, border1.width);
					}
					MyTableBorder border2 = borderMap.get(calcPos(row+row+1, eCol+eCol));
					if(border2!=null && border2.isVisible()) {
						skyWidth = Math.max(skyWidth, border2.width);
					}
				}
				double earthPadding = !verticalWritingMode ? padding.left : padding.top;
				double skyPadding = !verticalWritingMode ? padding.right : padding.bottom;
				putTravel(calcPos(colspan, nowCol), travel + (earthWidth + skyWidth)/2 + earthPadding + skyPadding);
			}
		}
	}
	public void beginCell(Map<String, String> attr) throws IOException {
		while(true) {
			int pos = calcPos(nowRow, nowCol);
			if(!existSet.contains(pos)) {
				break;
			}
			++nowCol;
		}
		int colspan = getSpan(attr, "colspan");
		int rowspan = getSpan(attr, "rowspan");
		for(int y=0; y<rowspan; ++y) {
			for(int x=0; x<colspan; ++x) {
				int pos = calcPos(nowRow + y, nowCol + x);
				existSet.add(pos);
				if(colCount<=nowCol+x) {
					colCount = nowCol + x + 1;
				}
				if(rowCount<=nowRow+y) {
					rowCount = nowRow + y + 1;
				}
			}
		}

		double travel = 0;
		for(int x=0; x<colspan; ++x) {
			travel += columnWidthList[nowCol+x];
		}

		MyRectSize parentRectSize = layoutContext.getNowBlock().getContentRectSize();
		MyLayoutStyle nowStyle = layoutContext.getNowStyle();
		MyLayoutMargin padding = nowStyle.getPadding();

		double earthWidth = 0;
		double skyWidth = 0;
		for(int y=0; y<rowspan; ++y) {
			int sCol = nowCol;
			int eCol = nowCol + colspan;
			int row = nowRow+y;
			MyTableBorder border1 = borderMap.get(calcPos(row+row+1, sCol+sCol));
			if(border1!=null && border1.isVisible()) {
				earthWidth = Math.max(earthWidth, border1.width);
			}
			MyTableBorder border2 = borderMap.get(calcPos(row+row+1, eCol+eCol));
			if(border2!=null && border2.isVisible()) {
				skyWidth = Math.max(skyWidth, border2.width);
			}
		}
		double prevWidth = 0;
		double postWidth = 0;
		for(int x=0; x<colspan; ++x) {
			int sRow = nowRow;
			int eRow = nowRow + rowspan;
			int col = nowCol+x;
			MyTableBorder border1 = borderMap.get(calcPos(sRow+sRow, col+col+1));
			if(border1!=null && border1.isVisible()) {
				prevWidth = Math.max(prevWidth, border1.width);
			}
			MyTableBorder border2 = borderMap.get(calcPos(eRow+eRow, col+col+1));
			if(border2!=null && border2.isVisible()) {
				postWidth = Math.max(postWidth, border2.width);
			}
		}
		double width = !verticalWritingMode ? travel : nowStyle.hasWidth() ? padding.left+nowStyle.getWidth()+padding.right+(prevWidth+postWidth)/2 : parentRectSize.width;
		double height = verticalWritingMode ? travel : nowStyle.hasHeight() ? padding.top+nowStyle.getHeight()+padding.bottom+(prevWidth+postWidth)/2 : parentRectSize.height;

		Map<String, String> diff = new HashMap<String, String>();
		diff.put("padding-left", (padding.left + (!verticalWritingMode ? earthWidth : postWidth)) + "pt");
		diff.put("padding-top", (padding.top + (!verticalWritingMode ? prevWidth : earthWidth)) + "pt");
		diff.put("padding-right", (padding.right + (!verticalWritingMode ? skyWidth : prevWidth)) + "pt");
		diff.put("padding-bottom", (padding.bottom + (!verticalWritingMode ? postWidth : skyWidth)) + "pt");
		if(nowStyle.hasBackgroundColor()) {
			diff.put("background-color", nowStyle.diff.get("background-color"));
		}
		MyLayoutStyle blockStyle = nowStyle.merge(diff);
		layoutContext.pushNest(new MyLayoutNest(blockStyle));
		MyLayoutBlock block = new MyLayoutBlock(blockStyle, new MyRectSize(width, height));
		layoutContext.pushBlock(block);
	}
	public void endCell() throws IOException {
		layoutContext.clearNowLine();
		MyLayoutBlock block = layoutContext.getNowBlock();
		MyLayoutNest nest = layoutContext.getNowNest();
		block.justify(nest);
		layoutContext.popBlock();
		layoutContext.popNest();
		layoutContext.getNowBlock().addCellBlock(block, nest);
	}
}