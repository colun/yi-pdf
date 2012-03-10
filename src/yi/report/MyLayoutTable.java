/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import yi.pdf.YiPdfColor;
import yi.report.MyLayoutTable.MyTableBorder.BorderStyle;

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
	int maxRow = 0;
	int baseRow = 0;
	int getColCount() {
		return colCount;
	}
	int getRowCount() {
		return rowCount;
	}
	public void initVisit() {
		maxRow = 0;
		nowCol = 0;
		nowRow = 0;
		baseRow = 0;
		existSet.clear();
	}
	public void incRow() {
		++nowRow;
		nowCol = 0;
	}
	Set<Integer> existSet = new HashSet<Integer>();
	Map<Integer, MyTableBorder> borderMap = new HashMap<Integer, MyTableBorder>();
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
	void putTravel(int pos, double travel) {
		Double oldValue = travelMap.get(pos);
		if(oldValue==null || oldValue<travel) {
			travelMap.put(pos, travel);
		}
	}
	public void beginMode(ModeType mode) {
		setMode(mode);
		initVisit();
	}
	double[] columnPosList;
	MyTableBorder getMapBorderWithoutHidden(int pos) {
		MyTableBorder result = borderMap.get(pos);
		return (result==null || result.style==BorderStyle.HIDDEN || result.style==BorderStyle.NONE) ? null : result;
	}
	Map<Integer, MyQuartet<String, Double, Double, YiPdfColor>> crossMap = new HashMap<Integer, MyQuartet<String, Double, Double, YiPdfColor>>();
	public void endMode() {
		if(mode==ModeType.MODE_SCAN1) {
			for(int y=0; y<=rowCount; ++y) {
				int y2=y+y;
				for(int x=0; x<=colCount; ++x) {
					int x2=x+x;
					MyTableBorder bx1 = getMapBorderWithoutHidden(calcPos(y2, x2-1));
					MyTableBorder bx2 = getMapBorderWithoutHidden(calcPos(y2, x2+1));
					MyTableBorder bx = bx1==null ? bx2 : bx2==null ? bx1 : 0<bx1.compareTo(bx2) ? bx1 : bx2;
					MyTableBorder by1 = getMapBorderWithoutHidden(calcPos(y2-1, x2));
					MyTableBorder by2 = getMapBorderWithoutHidden(calcPos(y2+1, x2));
					MyTableBorder by = by1==null ? by2 : by2==null ? by1 : 0<by1.compareTo(by2) ? by1 : by2;
					double xWidth = bx==null ? 0 : bx.width;
					double yWidth = by==null ? 0 : by.width;
					if(0<xWidth && 0<yWidth) {
						MyTableBorder bb = bx==null ? by : by==null ? bx : 0<bx.compareTo(by) ? bx : by;
						crossMap.put(calcPos(y2, x2), new MyQuartet<String, Double, Double, YiPdfColor>(bb.style.name, xWidth, yWidth, new YiPdfColor(0, 0, 0)));
					}
				}
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
			columnPosList = calcColumnPosList(colCount, startCol, colspan, width);
		}
	}
	public static double[] calcColumnPosList(int colCount, int[] startCol, int[] colspan, double[] width) {
		double[] widthList = calcColumnWidths(colCount, startCol, colspan, width);
		double[] result = new double[colCount+1];
		result[0] = 0;
		for(int i=0; i<colCount; ++i) {
			result[i+1] = result[i] + widthList[i];
		}
		return result;
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
			}
		}
		if(colCount<nowCol+colspan) {
			colCount = nowCol + colspan;
		}
		if(rowCount<nowRow+rowspan) {
			rowCount = nowRow + rowspan;
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
				int eRow = nowRow + rowspan;
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
			}
		}
		if(maxRow<nowRow+rowspan-1) {
			maxRow = nowRow + rowspan-1;
		}

		double travel = columnPosList[nowCol+colspan] - columnPosList[nowCol];

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
		double leftPadding = padding.left + (!verticalWritingMode ? earthWidth : postWidth) / 2;
		double topPadding = padding.top + (!verticalWritingMode ? prevWidth : earthWidth) / 2;
		double rightPadding = padding.right + (!verticalWritingMode ? skyWidth : prevWidth) / 2;
		double bottomPadding = padding.bottom + (!verticalWritingMode ? postWidth : skyWidth) / 2;
		double width = !verticalWritingMode ? travel : nowStyle.hasWidth() ? nowStyle.getWidth()+leftPadding+rightPadding : parentRectSize.width;
		double height = verticalWritingMode ? travel : nowStyle.hasHeight() ? nowStyle.getHeight()+topPadding+bottomPadding : parentRectSize.height;

		Map<String, String> diff = new HashMap<String, String>(nowStyle.diff);
		diff.put("padding-left", String.format("%fpt", leftPadding));
		diff.put("padding-top", String.format("%fpt", topPadding));
		diff.put("padding-right", String.format("%fpt", rightPadding));
		diff.put("padding-bottom", String.format("%fpt", bottomPadding));
		diff.remove("margin-left");
		diff.remove("margin-top");
		diff.remove("margin-right");
		diff.remove("margin-bottom");
		diff.remove("border-left-width");
		diff.remove("border-top-width");
		diff.remove("border-right-width");
		diff.remove("border-bottom-width");
		layoutContext.pushStyle(diff);
		MyLayoutBlock block = new MyLayoutBlock(layoutContext.getNowStyle(), new MyRectSize(width, height));

		block.contentPos = new MyPosition(!verticalWritingMode ? columnPosList[nowCol] : 0, !verticalWritingMode ? 0 : columnPosList[nowCol]);
		layoutContext.pushBlock(block);
		layoutContext.popStyle();
		blockList.add(new MyQuintet<MyLayoutBlock, Integer, Integer, MyLayoutNest, MyLayoutStyle>(block, nowRow, rowspan, layoutContext.getNowNest(), layoutContext.getNowStyle()));
	}
	List<MyQuintet<MyLayoutBlock, Integer, Integer, MyLayoutNest, MyLayoutStyle>> blockList = new ArrayList<MyQuintet<MyLayoutBlock,Integer,Integer,MyLayoutNest, MyLayoutStyle>>();
	public void endCell(Map<String, String> attr) throws IOException {
		layoutContext.popBlock();
		//layoutContext.getNowBlock().addCellBlock(block);
	}
	public void beginRow() {
	}
	public void endRow() {
		if(mode==ModeType.MODE_VISIT) {
			if(maxRow==nowRow) {
				Map<Integer, Double> diveMap = new TreeMap<Integer, Double>();
				for(MyQuintet<MyLayoutBlock, Integer, Integer, MyLayoutNest, MyLayoutStyle> q : blockList) {
					MyLayoutBlock block = q.first;
					int row = q.second;
					int rowspan = q.third;
					int pos = calcPos(rowspan, row-baseRow);
					double value = !verticalWritingMode ? block.contentRectSize.height : block.contentRectSize.width;
					Double oldValue = diveMap.get(pos);
					if(oldValue==null || oldValue<value) {
						diveMap.put(pos, value);
					}
				}
				int size = diveMap.size();
				int[] start = new int[size];
				int[] span = new int[size];
				double[] width = new double[size];
				int i = 0;
				for(Entry<Integer, Double> entry : diveMap.entrySet()) {
					int pair = entry.getKey();
					span[i] = (pair >> 16) & 65535;
					start[i] = pair & 65535;
					width[i] = entry.getValue();
					++i;
				}
				double[] rowPosList = calcColumnPosList(1+maxRow-baseRow, start, span, width);
				for(MyQuintet<MyLayoutBlock, Integer, Integer, MyLayoutNest, MyLayoutStyle> q : blockList) {
					MyLayoutBlock block = q.first;
					int row = q.second;
					int rowspan = q.third;
					MyLayoutNest nest = q.fourth;
					MyLayoutStyle style = q.fifth;
					double t = rowPosList[row-baseRow];
					double dive = rowPosList[rowspan+row-baseRow] - t;
					layoutContext.getNowBlock().addCellBlock(block, t, !verticalWritingMode ? block.contentRectSize.width : dive, !verticalWritingMode ? dive : block.contentRectSize.height, nest, style);
				}
				for(int y=baseRow; y<=maxRow; ++y) {
					int rowPos = y+y+1;
					for(int x=0; x<=colCount; ++x) {
						MyTableBorder border = getMapBorderWithoutHidden(calcPos(rowPos, x+x));
						if(border!=null && border.style!=null) {
							layoutContext.getNowBlock().putBorder(border.style.name, border.width, columnPosList[x], rowPosList[y-baseRow], columnPosList[x], rowPosList[1+y-baseRow]);
						}
					}
				}
				for(int x=0; x<colCount; ++x) {
					int colPos = x+x+1;
					for(int y=baseRow+(baseRow==0 ? 0 : 1); y<=maxRow+1; ++y) {
						MyTableBorder border = getMapBorderWithoutHidden(calcPos(y+y, colPos));
						if(border!=null && border.style!=null) {
							layoutContext.getNowBlock().putBorder(border.style.name, border.width, columnPosList[x], rowPosList[y-baseRow], columnPosList[1+x], rowPosList[y-baseRow]);
						}
					}
				}
				for(int y=baseRow+(baseRow==0 ? 0 : 1); y<=maxRow+1; ++y) {
					for(int x=0; x<=colCount; ++x) {
						MyQuartet<String, Double, Double, YiPdfColor> cross = crossMap.get(calcPos(y+y, x+x));
						if(cross!=null) {
							layoutContext.getNowBlock().putCross(cross, columnPosList[x], rowPosList[y-baseRow]);
						}
					}
				}
				layoutContext.getNowBlock().addPass(rowPosList[1+maxRow-baseRow], layoutContext.getNowNest());
				baseRow = maxRow;
				blockList.clear();
			}
		}
		incRow();
	}
}
