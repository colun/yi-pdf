/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import yi.pdf.YiPdfColor;
import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfFont;
import yi.pdf.YiPdfTag;
import yi.pdf.font.YiPdfJGothicFont;

public class MyLayoutContext {
	YiPdfFile pdfFile;
	Map<String, String> nowStyle = new HashMap<String, String>();
	Map<String, String> nowStyleDiff;
	Stack<Map<String, String>> styleStack = new Stack<Map<String,String>>();
	MyLayoutBlock nowBlock;
	MyLayoutLine nowLine;
	YiPdfTag nowTag;
	YiPdfTag nowLineTag;

	MyLayoutContext(YiPdfFile pdfFile) {
		this.pdfFile = pdfFile;
		nowTag = pdfFile.getDocument();
	}
	Stack<YiPdfTag> tagStack = new Stack<YiPdfTag>();
	void pushPdfTag(YiPdfTag tag) {
		tagStack.push(nowTag);
		nowTag = tag;
		clearLineTag();
	}
	void popPdfTag() {
		nowTag = tagStack.pop();
		clearLineTag();
	}
	YiPdfTag getNowTag() {
		return nowTag;
	}
	YiPdfTag getLineTag() {
		if(nowLineTag==null) {
			nowLineTag = nowTag.makeChild("P");
		}
		return nowLineTag;
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
	void pushStyle(Map<String, String> style) {
		nowStyleDiff = style;
		styleStack.push(nowStyle);
		if(style!=null) {
			nowStyle = new HashMap<String, String>(nowStyle);
			nowStyle.putAll(style);
		}
	}
	Map<String, String> getNowStyle() {
		return nowStyle;
	}
	void popStyle() {
		nowStyle = styleStack.pop();
	}
	MyLayoutBlock getNowBlock() {
		if(nowBlock==null) {
			nowBlock = MyLayoutBlock.createPageRoot(nowStyle);
		}
		return nowBlock;
	}
	Stack<MyLayoutBlock> blockStack;
	void pushNewBlock() {
		blockStack.push(getNowBlock());
		nowBlock = MyLayoutBlock.createChildBlock();
	}
	void popBlock() {
		MyLayoutBlock childBlock = nowBlock;
		nowBlock = blockStack.pop();
		nowBlock.addBlock(childBlock);
		assert(false) : "TODO: MyLayoutContext.popBlock()";
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
				block.drawPage(pdfFile);
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
			block.drawPage(pdfFile);
		}
	}
	void clearNowBlock() throws IOException {
		clearNowLine();
		if(nowBlock!=null) {
			assert(nowBlock.isPageRoot()) : "pageRootではないnowBlockをclearしてはならない。";
			drawBlock(nowBlock);
			nowBlock = null;
		}
	}
	MyLayoutLine getNowLine() {
		if(nowLine==null) {
			double lineWidth = getNowBlock().getLineWidth();
			nowLine = new MyLayoutLine(lineWidth);
		}
		return nowLine;
	}
	boolean fourceBlockFlag = true;
	void clearNowLine() throws IOException {
		if(nowLine!=null) {
			while(true) {
				boolean f = getNowBlock().addLine(nowLine, fourceBlockFlag);
				if(!f) {
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
	YiPdfFont dummyFont = new YiPdfJGothicFont();
	YiPdfFont getNowFont() {
		return dummyFont;
	}
	double getNowFontSize() {
		String str = nowStyle.get("font-size");
		if(str==null) {
			return 10.5;
		}
		return MyUtil.evalUnit(str);
	}
	private YiPdfColor getNowFontColor() {
		String str = nowStyle.get("color");
		if(str==null) {
			return new YiPdfColor(0, 0, 0);
		}
		return MyUtil.evalColor(str);
	}
	private boolean getTabooHangMode() {
		String str = nowStyle.get("line-break-hang");
		if(str==null || str.isEmpty() || "0".equals(str) || "false".equals(str)) {
			return false;
		}
		return true;
	}
	public void writeText(String text) throws IOException {
		YiPdfFont font = getNowFont();
		YiPdfColor color = getNowFontColor();
		double fontSize = getNowFontSize();
		boolean tabooHangMode = getTabooHangMode();
		int len = text.length();
		int pos = 0;
		while(pos<len) {
			MyLayoutLine nLine = getNowLine();
			double maxTravel = nLine.getRemainingTravel();
			MyQuartet<Integer, String, Boolean, Double> q = formattingText(font, fontSize, text, maxTravel, pos, nLine.isEmpty(), tabooHangMode);
			assert(!nLine.isEmpty() || pos!=q.first);
			if(pos!=q.first) {
				pos = q.first;
				String str = q.second;
				double totalTravel = q.fourth;
				MyLayoutInlineText inlineText = new MyLayoutInlineText(font, fontSize, color, str, totalTravel, getLineTag());
				if(isLockedLazyDraw()) {
					lockedInlineTextList.add(inlineText);
				}
				nLine.addInline(inlineText);
			}
			boolean brFlag = q.third;
			if(brFlag) {
				writeNewLine();
			}
		}
	}
	public MyLayoutInlineText createNobrInlineText(String text) throws IOException {
		YiPdfFont font = getNowFont();
		YiPdfColor color = getNowFontColor();
		double fontSize = getNowFontSize();
		int len = text.length();
		int travelSum = 0;
		for(int i=0; i<len; ++i) {
			travelSum += font.getTravel(text.charAt(i));
		}
		return new MyLayoutInlineText(font, fontSize, color, text, (fontSize * travelSum) / 1000, getLineTag());
	}
	String tabooPrefix = "　、。」）・？！";
	String tabooSuffix = "「（";
	private MyQuartet<Integer, String, Boolean, Double> formattingText(YiPdfFont font, double fontSize, String text, double maxTravel, int stPos, boolean emptyLineFlag, boolean hangFlag) {
		int maxTravelInt = (int)((maxTravel * 1000) / fontSize);
		int len = text.length();
		int totalTravel = 0;
		int reservedTravel = -1;
		int reservedPos = -1;
		char beforeCh = 0;
		boolean brFlag = false;
		int pos;
		for(pos = stPos; pos < len; ++pos) {
			char ch = text.charAt(pos);
			if(ch=='\n') {
				pos += 1;
				brFlag = true;
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
				brFlag = true;
				break;
			}
			totalTravel += travel;
			beforeCh = ch;
		}
		String str = text.substring(stPos, pos);
		return new MyQuartet<Integer, String, Boolean, Double>(pos, str, brFlag, (fontSize * totalTravel) / 1000);
	}
	public void writeBr() throws IOException {
		getNowLine().addBlankText(getNowFont(), getNowFontSize(), getLineTag());
		clearLineTag();
		writeNewLine();
	}
	public void writeNewLine() throws IOException {
		clearNowLine();
	}
}
