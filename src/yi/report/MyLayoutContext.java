package yi.report;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import yi.pdf.YiPdfFile;

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

}
