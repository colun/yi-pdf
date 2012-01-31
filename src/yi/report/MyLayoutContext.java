package yi.report;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import yi.pdf.YiPdfColor;
import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfFont;

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
			nowBlock = MyLayoutBlock.createPageRoot();
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
	void clearNowBlock() {
		if(nowBlock!=null) {
			assert(nowBlock.isPageRoot()) : "pageRootではないnowBlockをclearしてはならない。";
			assert(false) : "TODO: MyLayoutContext.clearNowBlock()";
			nowBlock = null;
		}
	}
	MyLayoutLine getNowLine() {
		if(nowLine==null) {
			nowLine = new MyLayoutLine();
		}
		return nowLine;
	}
	void clearNowLine() {
		if(nowLine!=null) {
			assert(false) : "TODO: MyLayoutContext.clearNowLine()";
			nowLine = null;
		}
	}
	YiPdfFont getNowFont() {
		assert(false) : "TODO: MyLayoutContext.getNowFont()";
		return null;
	}
	double getNowFontSize() {
		assert(false) : "TODO: MyLayoutContext.getNowFontSize()";
		return 0;
	}
	private YiPdfColor getNowFontColor() {
		assert(false) : "TODO: MyLayoutContext.getNowFontColor()";
		return new YiPdfColor(0, 0, 0);
	}
	public void writeText(String text) {
		YiPdfFont font = getNowFont();
		YiPdfColor color = getNowFontColor();
		double fontSize = getNowFontSize();
		int len = text.length();
		int pos = 0;
		while(pos<len) {
			MyLayoutLine nLine = getNowLine();
			double maxTravel = nLine.getRemainingWidth();
			MyQuartet<Integer, String, Boolean, Double> q = formattingText(font, fontSize, text, maxTravel, pos);
			pos = q.first;
			String str = q.second;
			boolean brFlag = q.third;
			double totalTravel = q.fourth;
			nLine.addInline(new MyLayoutInlineText(font, fontSize, color, str, totalTravel));
			if(brFlag) {
				clearNowLine();
			}
		}
		assert(false) : "TODO: MyLayoutContext.writeText()";
	}
	String tabooPrefix = "、。」）・？";
	String tabooSuffix = "「（";
	public MyQuartet<Integer, String, Boolean, Double> formattingText(YiPdfFont font, double fontSize, String text, double maxTravel, int stPos) {
		int maxTravelInt = (int)((maxTravel * 1000) / fontSize);
		int len = text.length();
		int totalTravel = 0;
		int reservedTravel = -1;
		int endPos = -1;
		int reservedPos = -1;
		int reservedEndPos = -1;
		int pos;
		char beforeCh = 0;
		boolean brFlag = false;
		for(pos = stPos; pos < len; ++pos) {
			char ch = text.charAt(pos);
			if(ch=='\n') {
				endPos = pos;
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
			if(maxTravelInt < totalTravel + travel) {
				if(reservedPos==-1) {
					if(pos==stPos) {
						pos += 1;
						totalTravel += travel;
					}
					endPos = pos;
				}
				else {
					pos = reservedPos;
					endPos = reservedEndPos;
					totalTravel = reservedTravel;
				}
				break;
			}
			totalTravel += travel;
			beforeCh = ch;
		}
		if(pos==len) {
			endPos = pos;
		}
		String str = text.substring(stPos, endPos);
		return new MyQuartet<Integer, String, Boolean, Double>(pos, str, brFlag, (fontSize * totalTravel) / 1000);
	}

}
