package yi.report;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import yi.pdf.YiPdfColor;
import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfFont;
import yi.pdf.font.YiPdfJGothicFont;

public class MyLayoutContext {
	YiPdfFile pdfFile;
	Map<String, String> nowStyle = new HashMap<String, String>();
	Map<String, String> nowStyleDiff;
	Stack<Map<String, String>> styleStack = new Stack<Map<String,String>>();
	MyLayoutBlock nowBlock;
	MyLayoutLine nowLine;

	MyLayoutContext(YiPdfFile pdfFile) {
		this.pdfFile = pdfFile;
	}
	void pushStyle(Map<String, String> style) {
		nowStyleDiff = style;
		styleStack.push(nowStyle);
		if(style!=null) {
			nowStyle = new HashMap<String, String>(nowStyle);
			nowStyle.putAll(style);
		}
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
	void clearNowBlock() throws IOException {
		clearNowLine();
		if(nowBlock!=null) {
			assert(nowBlock.isPageRoot()) : "pageRootではないnowBlockをclearしてはならない。";
			nowBlock.drawPage(pdfFile);
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
		//assert(false) : "TODO: MyLayoutContext.getNowFont()";
	}
	double getNowFontSize() {
		return 10.5;
		//assert(false) : "TODO: MyLayoutContext.getNowFontSize()";
	}
	private YiPdfColor getNowFontColor() {
		return new YiPdfColor(0, 0, 0);
		//assert(false) : "TODO: MyLayoutContext.getNowFontColor()";
	}
	public void writeText(String text) throws IOException {
		YiPdfFont font = getNowFont();
		YiPdfColor color = getNowFontColor();
		double fontSize = getNowFontSize();
		int len = text.length();
		int pos = 0;
		while(pos<len) {
			MyLayoutLine nLine = getNowLine();
			double maxTravel = nLine.getRemainingTravel();
			MyQuartet<Integer, String, Boolean, Double> q = formattingText(font, fontSize, text, maxTravel, pos);
			assert(pos!=q.first);
			pos = q.first;
			String str = q.second;
			boolean brFlag = q.third;
			double totalTravel = q.fourth;
			nLine.addInline(new MyLayoutInlineText(font, fontSize, color, str, totalTravel));
			if(brFlag) {
				writeNewLine();
			}
		}
	}
	String tabooPrefix = "、。」）・？";
	String tabooSuffix = "「（";
	public MyQuartet<Integer, String, Boolean, Double> formattingText(YiPdfFont font, double fontSize, String text, double maxTravel, int stPos) {
		int maxTravelInt = (int)((maxTravel * 1000) / fontSize);
		int len = text.length();
		int totalTravel = 0;
		int reservedTravel = -1;
		int reservedPos = -1;
		int reservedEndPos = -1;
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
				reservedEndPos = pos + 1;
			}
			else if(tabooPrefix.indexOf(ch)<0 && tabooSuffix.indexOf(beforeCh)<0) {
				reservedPos = pos;
				reservedTravel = totalTravel;
				reservedEndPos = pos;
			}
			int travel = font.getTravel(ch);
			if(128<=ch && maxTravelInt < totalTravel + travel) {
				if(reservedPos==-1) {
					if(pos==stPos) {
						pos += 1;
						totalTravel += travel;
					}
				}
				else {
					pos = reservedPos;
					totalTravel = reservedTravel;
					if(pos==stPos) {
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
	public void writeNewLine() throws IOException {
		clearNowLine();
	}

}
