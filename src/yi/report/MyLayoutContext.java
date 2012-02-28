/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import yi.pdf.YiPdfColor;
import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfFont;
import yi.pdf.YiPdfTag;

public class MyLayoutContext {
	YiPdfFile pdfFile;
	MyLayoutStyle nowStyle = new MyLayoutStyle();
	Stack<MyLayoutStyle> styleStack = new Stack<MyLayoutStyle>();
	MyLayoutBlock nowBlock;
	MyLayoutLine nowLine;
	YiPdfTag nowTag;
	YiPdfTag nowLineTag;
	boolean taggedMode = true;

	public void setTaggedMode(boolean taggedMode) {
		this.taggedMode = taggedMode;
	}
	public boolean getTaggedMode() {
		return taggedMode;
	}

	MyLayoutTable nowTable;
	Stack<MyLayoutTable> tableStack = new Stack<MyLayoutTable>();
	MyLayoutTable pushTable() {
		tableStack.push(nowTable);
		nowTable = new MyLayoutTable(this);
		return nowTable;
	}
	MyLayoutTable getNowTable() {
		return nowTable;
	}
	void popTable() {
		//nowTableを反映
		nowTable = tableStack.pop();
	}
	YiPdfFile getPdfFile() {
		return pdfFile;
	}
	MyLayoutContext(YiPdfFile pdfFile) {
		this.pdfFile = pdfFile;
	}
	MyLayoutPageStyle nowPageStyle;
	Stack<MyLayoutPageStyle> pageStyleStack = new Stack<MyLayoutPageStyle>();
	void pushPageStyle(Map<String, String> pageStyleDic) {
		pageStyleStack.push(nowPageStyle);
		nowPageStyle = new MyLayoutPageStyle(nowStyle.merge(pageStyleDic));
		pushStyle(new MyLayoutStyle(nowStyle, nowPageStyle));
	}
	void popPageStyle() {
		popStyle();
		nowPageStyle = pageStyleStack.pop();
	}
	Stack<YiPdfTag> tagStack = new Stack<YiPdfTag>();
	YiPdfTag pushPdfTag(String tagName) {
		clearLineTag();
		YiPdfTag oldTag = getNowTag();
		if(oldTag==null) {
			return null;
		}
		tagStack.push(oldTag);
		nowTag = oldTag.makeChild(tagName);
		return nowTag;
	}
	void popPdfTag() {
		clearLineTag();
		nowTag = tagStack.pop();
	}
	YiPdfTag getNowTag() {
		if(nowTag==null && taggedMode) {
			nowTag = pdfFile.getDocument();
		}
		return nowTag;
	}
	YiPdfTag getLineTag() {
		if(nowLineTag==null) {
			YiPdfTag tag = getNowTag();
			if(tag!=null) {
				nowLineTag = tag.makeChild("P");
			}
		}
		return nowLineTag;
	}
	Stack<MyLayoutNest> nestStack = new Stack<MyLayoutNest>();
	MyLayoutNest nowNest = new MyLayoutNest();
	double diveMargin = 0;
	double divePass = 0;
	MyLayoutNest getNowNest() {
		return nowNest;
	}
	private void addMargin(double margin, double padding) {
		if(diveMargin < margin) {
			divePass += margin - diveMargin;
			diveMargin = margin;
		}
		if(padding!=0) {
			divePass += padding;
			diveMargin = 0;
		}
	}
	void pushNest(MyLayoutNest newNest) {
		nestStack.push(nowNest);
		nowNest = newNest;
		if(nowNest!=null) {
			boolean verticalWritingMode = nowStyle.isVerticalWritingMode();
			addMargin(nowNest.getPreMargin(verticalWritingMode), nowNest.getPrePadding(verticalWritingMode));
		}
	}
	void pushChildNest() {
		MyLayoutNest n = new MyLayoutNest(nowNest, nowStyle);
		pushNest(n);
	}
	void pushNewNest() {
		MyLayoutNest n = new MyLayoutNest(nowStyle);
		pushNest(n);
	}
	MyLayoutNest popNest() throws IOException {
		clearNowLine();
		if(nowNest!=null) {
			boolean verticalWritingMode = nowStyle.isVerticalWritingMode();
			addMargin(nowNest.getPostMargin(verticalWritingMode), nowNest.getPostPadding(verticalWritingMode));
		}
		MyLayoutNest result = nowNest;
		nowNest = nestStack.pop();
		return result;
	}
	Stack<MyLayoutLine> lineStack = new Stack<MyLayoutLine>();
	void pushLine(MyLayoutLine newLine) {
		lineStack.push(nowLine);
		nowLine = newLine;
	}
	MyLayoutLine popLine() {
		MyLayoutLine result = nowLine;
		nowLine = lineStack.pop();
		return result;
	}
	void clearLineTag() { 
		nowLineTag = null;
	}
	void pushStyle(MyLayoutStyle style) {
		styleStack.push(nowStyle);
		nowStyle = style;
	}
	void pushStyle(Map<String, String> diff) {
		pushStyle(nowStyle.merge(diff));
	}
	MyLayoutStyle getNowStyle() {
		return nowStyle;
	}
	void popStyle() {
		nowStyle = styleStack.pop();
	}
	MyLayoutBlock getNowBlock() {
		if(nowBlock==null) {
			diveMargin = 1000000;
			divePass = 0;
			nowBlock = new MyLayoutPage(nowStyle, nowPageStyle, nowNest);
		}
		return nowBlock;
	}
	Stack<MyLayoutBlock> blockStack = new Stack<MyLayoutBlock>();
	Stack<MyPair<Double, Double>> diveStack = new Stack<MyPair<Double,Double>>();
	void pushBlock(MyLayoutBlock block) throws IOException {
		clearNowLine();
		pushNest(null);
		blockStack.push(getNowBlock());
		nowBlock = block;
		diveStack.push(new MyPair<Double, Double>(diveMargin, divePass));
		diveMargin = 0;
		divePass = 0;
		pushNewNest();
		applyDivePass();
	}
	MyLayoutBlock popBlock() throws IOException {
		clearNowLine();
		nowBlock.justify(nowNest);
		popNest();
		applyDivePass();
		MyPair<Double, Double> divePair = diveStack.pop();
		diveMargin = divePair.first;
		divePass = divePair.second;
		MyLayoutBlock result = nowBlock;
		nowBlock = blockStack.pop();
		popNest();
		return result;
	}
	private int lazyDrawLockCount = 0;
	private List<MyLayoutBlock> lazyDrawBlockList = new ArrayList<MyLayoutBlock>();
	private List<MyLayoutInlineText> lockedInlineTextList = new ArrayList<MyLayoutInlineText>();
	void lockLazyDraw() {
		++lazyDrawLockCount;
	}
	void unlockLazyDraw() throws IOException {
		assert(1<=lazyDrawLockCount);
		--lazyDrawLockCount;
		if(lazyDrawLockCount==0) {
			for(MyLayoutBlock block : lazyDrawBlockList) {
				((MyLayoutPage)block).drawPage(pdfFile);
			}
			lazyDrawBlockList.clear();
			lockedInlineTextList.clear();
		}
	}
	void clearLockedInlineTextList() {
		lockedInlineTextList.clear();
	}
	List<MyLayoutInlineText> getLockedInlineTextList() {
		return new ArrayList<MyLayoutInlineText>(lockedInlineTextList);
	}
	boolean isLockedLazyDraw() {
		return lazyDrawLockCount!=0;
	}
	void drawBlock(MyLayoutBlock block) throws IOException {
		if(isLockedLazyDraw()) {
			lazyDrawBlockList.add(block);
		}
		else {
			((MyLayoutPage)block).drawPage(pdfFile);
		}
	}
	void clearNowBlock() throws IOException {
		clearNowLine();
		if(nowBlock!=null) {
			assert(nowBlock.isPageRoot()) : "pageRootではないnowBlockをclearしてはならない。";
			((MyLayoutPage)nowBlock).finishPage(nowNest);
			drawBlock(nowBlock);
			nowBlock = null;
		}
	}
	MyLayoutLine getNowLine() {
		if(nowLine==null) {
			double lineWidth = getNowBlock().getLineWidth(nowNest);
			nowLine = new MyLayoutLine(lineWidth, getNowBlock().isVerticalWritingMode());
		}
		return nowLine;
	}
	boolean fourceBlockFlag = true;
	void applyDivePass() {
		MyLayoutBlock block = getNowBlock();
		if(divePass!=0) {
			block.addPass(divePass, nowNest);
			divePass = 0;
		}
		diveMargin = 0;
	}
	void clearNowLine() throws IOException {
		if(nowLine!=null) {
			while(true) {
				MyLayoutBlock block = getNowBlock();
				applyDivePass();
				boolean f = block.addLine(nowLine, fourceBlockFlag, nowNest);
				if(!f && block.isPageRoot()) {
					MyLayoutLine hold = nowLine;
					nowLine = null;
					clearNowBlock();
					nowLine = hold;
					fourceBlockFlag = true;
				}
				else {
					fourceBlockFlag = false;
					break;
				}
			}
			nowLine = null;
		}
	}
	public void writeText(String text) throws IOException {
		YiPdfFont font = nowStyle.getFont();
		YiPdfColor color = nowStyle.getFontColor();
		double fontSize = nowStyle.getFontSize();
		boolean tabooHangMode = nowStyle.getLineBreakHang();
		Double lineHeight = nowStyle.getLineHeight();
		int len = text.length();
		int pos = 0;
		while(pos<len) {
			MyLayoutLine nLine = getNowLine();
			double maxTravel = nLine.getRemainingTravel();
			MyQuartet<Integer, String, Double, Object> q = formattingText(font, fontSize, text, maxTravel, pos, nLine.isEmpty(), tabooHangMode);
			assert(!nLine.isEmpty() || pos!=q.first);
			if(pos!=q.first) {
				pos = q.first;
				String str = q.second;
				double totalTravel = q.third;
				MyLayoutInlineText inlineText = new MyLayoutInlineText(font, fontSize, color, str, totalTravel, getLineTag(), lineHeight);
				if(isLockedLazyDraw()) {
					lockedInlineTextList.add(inlineText);
				}
				nLine.addInline(inlineText);
			}
			if(pos<len) {
				writeNewLine();
			}
		}
	}
	public MyLayoutInlineText createNobrInlineText(String text) throws IOException {
		YiPdfFont font = nowStyle.getFont();
		YiPdfColor color = nowStyle.getFontColor();
		double fontSize = nowStyle.getFontSize();
		Double lineHeight = nowStyle.getLineHeight();
		int len = text.length();
		int travelSum = 0;
		for(int i=0; i<len; ++i) {
			travelSum += font.getTravel(text.charAt(i));
		}
		return new MyLayoutInlineText(font, fontSize, color, text, (fontSize * travelSum) / 1000, getLineTag(), lineHeight);
	}
	String tabooPrefix = "　、。」）・？！";
	String tabooSuffix = "「（";
	private MyQuartet<Integer, String, Double, Object> formattingText(YiPdfFont font, double fontSize, String text, double maxTravel, int stPos, boolean emptyLineFlag, boolean hangFlag) {
		int maxTravelInt = (int)((maxTravel * 1000) / fontSize);
		int len = text.length();
		int totalTravel = 0;
		int reservedTravel = -1;
		int reservedPos = -1;
		char beforeCh = 0;
		int pos;
		for(pos = stPos; pos < len; ++pos) {
			char ch = text.charAt(pos);
			if(ch=='\n') {
				pos += 1;
				break;
			}
			else if(ch==' ') {
				reservedPos = pos + 1;
				reservedTravel = totalTravel;
			}
			else if((ch<0 || 128<=ch) && tabooPrefix.indexOf(ch)<0 && tabooSuffix.indexOf(beforeCh)<0) {
				reservedPos = pos;
				reservedTravel = totalTravel;
			}
			int travel = font.getTravel(ch);
			if(maxTravelInt < totalTravel + travel) {
				if(hangFlag && 0<=tabooPrefix.indexOf(ch)) {
					++pos;
					while(pos < len && 0<=tabooPrefix.indexOf(text.charAt(pos))) {
						++pos;
					}
				}
				else if(reservedPos==-1) {
					if(emptyLineFlag && pos==stPos) {
						pos += 1;
						totalTravel += travel;
					}
				}
				else {
					pos = reservedPos;
					totalTravel = reservedTravel;
					if(emptyLineFlag && pos==stPos) {
						pos += 1;
						totalTravel += travel;
					}
				}
				break;
			}
			totalTravel += travel;
			beforeCh = ch;
		}
		String str = text.substring(stPos, pos);
		return new MyQuartet<Integer, String, Double, Object>(pos, str, (fontSize * totalTravel) / 1000, null);
	}
	public void writeBr() throws IOException {
		getNowLine().addBlankText(nowStyle.getFont(), nowStyle.getFontSize(), getLineTag(), nowStyle.getLineHeight());
		clearLineTag();
		writeNewLine();
	}
	public void writeClearLine() throws IOException {
		clearLineTag();
		writeNewLine();
	}
	public void writeNewLine() throws IOException {
		clearNowLine();
	}
	public void writeInline(MyLayoutInline item) {
		getNowLine().addInline(item);
	}
}
