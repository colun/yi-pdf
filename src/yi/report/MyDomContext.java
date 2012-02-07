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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yi.pdf.YiPdfFile;

class MyDomContext {
	enum TagType {
		TAG_HTML("html"),
		TAG_HEAD("head"),
		TAG_TITLE("title"),
		TAG_META("meta"),
		TAG_BODY("body"),
		TAG_DIV("div"),
		TAG_SPAN("span"),
		TAG_BR("br"),
		TAG_TABLE("table"),
		TAG_TR("tr"),
		TAG_TD("td"),
		TAG_TH("th"),
		TAG_RUBY("ruby"),
		TAG_RB("rb"),
		TAG_RT("rt"),
		TAG_RP("rp"),
		TAG_H1("h1"),
		NOT_TAG_TEXT("XXXXX");
		private final String name;
		private TagType(String name) {
			this.name = name;
		}
		static HashMap<String, TagType> enumDic = null;
		public static HashMap<String, TagType> getDic() {
			HashMap<String, TagType> eDic = enumDic;
			if(eDic==null) {
				eDic = new HashMap<String, MyDomContext.TagType>();
				for(TagType tag : values()) {
					eDic.put(tag.name, tag);
				}
				enumDic = eDic;
			}
			return eDic;
		}
		public static TagType fromString(String name) {
			TagType result = getDic().get(name);
			return result;
		}
	}
	MyDomContext() {
	}
	static Pattern stylePattern = Pattern.compile(" *([-a-zA-Z0-9_]+) *: *([-a-zA-Z0-9_\\.]+) *");
	Map<String, String> fromStyle(String style) {
		Map<String, String> dic = new HashMap<String, String>();
		Matcher m = stylePattern.matcher("");
		for(String str : style.split(";")) {
			m.reset(str);
			if(m.matches()) {
				dic.put(m.group(1), m.group(2));
			}
		}
		return dic;
	}
	MyLayoutContext layoutContext;
	void visitChildren(YiDomNode node, Set<TagType> enableChildTag) throws IOException {
		List<YiDomNode> children = node.getChildren();
		for(YiDomNode child : children) {
			if(child!=null) {
				if(child.getNodeType()==YiDomNode.TYPE_OF_TEXT) {
					if(enableChildTag==null || enableChildTag.contains(TagType.NOT_TAG_TEXT)) {
						visitText(child);
					}
					continue;
				}
				Map<String, String> diff = null;
				{
					Map<String, String> attr = child.getAttr();
					if(attr!=null) {
						String style = attr.get("style");
						if(style!=null) {
							diff = fromStyle(style);
						}
					}
					layoutContext.pushStyle(diff);
				}
				TagType tagType = TagType.fromString(child.getTagName());
				if(tagType==null || enableChildTag!=null && !enableChildTag.contains(tagType)) {
					continue;
				}
				switch(tagType) {
				case TAG_HTML: visitHtml(child); break;
				case TAG_BODY: visitBody(child); break;
				case TAG_H1: visitH1(child); break;
				case TAG_BR: visitBr(child); break;
				case TAG_SPAN: visitSpan(child); break;
				case TAG_RUBY: visitRuby(child); break;
				case TAG_RB: visitRb(child); break;
				case TAG_RT: visitRt(child); break;
				case TAG_RP: visitRp(child); break;
				}
				layoutContext.popStyle();
			}
		}
	}
	private void visitRuby(YiDomNode node) throws IOException {
		layoutContext.lockLazyDraw();
		visitChildren(node, rubyTagSet);
		layoutContext.unlockLazyDraw();
	}
	List<MyLayoutInlineText> rbInlineText;
	private void visitRb(YiDomNode node) throws IOException {
		layoutContext.unlockLazyDraw();
		layoutContext.lockLazyDraw();
		layoutContext.clearLockedInlineTextList();
		rbInlineText = null;
		visitChildren(node, rbTagSet);
		assert(rbInlineText==null);
		rbInlineText = layoutContext.getLockedInlineTextList();
		if(!rbInlineText.isEmpty()) {
			rbInlineText.get(rbInlineText.size() - 1).setRubyLastFlag(true);
		}
		afterRtFlag = false;
	}
	boolean afterRtFlag = false;
	void mergeRuby(List<MyLayoutInlineText> rbList, List<MyLayoutInlineText> rtList) {
		double rbTravelSum = 0;
		for(MyLayoutInlineText rb : rbList) {
			rbTravelSum += rb.getTravel();
		}
		double rtTravelSum = 0;
		for(MyLayoutInlineText rt : rtList) {
			rtTravelSum += rt.getTravel();
		}
		if(rbTravelSum==0 || rtTravelSum==0) {
			return;
		}
		double rbrtRate = rbTravelSum / rtTravelSum;
		double rbTravelPos = 0;
		int rtPos = 0;
		double rtTravelPos = 0;
		for(MyLayoutInlineText rb : rbList) {
			double rbNextTravelPos = rbTravelPos + rb.getTravel();
			int rtPos2 = rtPos;
			while(rtPos2<rtList.size()) {
				MyLayoutInlineText rt = rtList.get(rtPos2);
				double trvl = rt.getTravel();
				double trvl2 = trvl / 2;
				double rtt = rtTravelPos + trvl2;
				double rbt = rtt * rbrtRate;
				if(rbNextTravelPos<rbt) {
					break;
				}
				rt.setDx((rbt - trvl2) - rbTravelPos);
				rtTravelPos += trvl;
				++rtPos2;
			}
			if(rtPos!=rtPos2) {
				rb.setRuby(rtList.subList(rtPos, rtPos2));
				rtPos = rtPos2;
			}
			rbTravelPos = rbNextTravelPos;
		}
		assert(rtPos==rtList.size());
	}
	private void visitRt(YiDomNode node) throws IOException {
		assert(rbInlineText!=null) : "rtタグよりも前にrbタグが必要です。";
		Map<String, String> halfFontSizeMap = new HashMap<String, String>();
		halfFontSizeMap.put("font-size", String.format("%fpt", layoutContext.getNowFontSize() * 0.5));
		MyLayoutLine infLine = MyLayoutLine.createInfLine();
		layoutContext.pushLine(infLine);
		layoutContext.pushStyle(halfFontSizeMap);
		visitChildren(node, rbTagSet);
		layoutContext.popStyle();
		MyLayoutLine infLine2 = layoutContext.popLine();
		assert(infLine==infLine2);
		List<MyLayoutInlineText> rtList = new ArrayList<MyLayoutInlineText>();
		for(MyLayoutInline il : infLine.getInlineList()) {
			rtList.addAll(((MyLayoutInlineText)il).explode());
		}
		mergeRuby(rbInlineText, rtList);
		afterRtFlag = true;
	}
	private void visitRp(YiDomNode node) throws IOException {
		assert(rbInlineText!=null) : "rpタグよりも前にrbタグが必要です。";
		if(rbInlineText.isEmpty()) {
			return;
		}
		List<YiDomNode> children = node.getChildren();
		StringBuilder builder = new StringBuilder();
		for(YiDomNode child : children) {
			assert(child.getNodeType()==YiDomNode.TYPE_OF_TEXT) : "rpタグの要素はテキストのみが許される";
			builder.append(child.getText());
		}
		if(1<=builder.length()) {
			String text = builder.toString();
			Map<String, String> halfFontSizeMap = new HashMap<String, String>();
			halfFontSizeMap.put("font-size", String.format("%fpt", layoutContext.getNowFontSize() * 0.015625));
			layoutContext.pushStyle(halfFontSizeMap);
			MyLayoutInlineText rp = layoutContext.createNobrInlineText(text);
			rp.setTransparentFlag(true);
			layoutContext.popStyle();
			if(afterRtFlag==false) {
				rbInlineText.get(0).setBeforeRp(rp);
			}
			else {
				rbInlineText.get(rbInlineText.size()-1).setAfterRp(rp);
			}
		}
	}
	private void visitSpan(YiDomNode node) throws IOException {
		visitChildren(node, normalTagSet);
	}
	private void visitH1(YiDomNode node) throws IOException {
		layoutContext.pushPdfTag(layoutContext.getNowTag().makeChild("H1"));
		visitChildren(node, normalTagSet);
		layoutContext.writeBr();
		layoutContext.popPdfTag();
	}
	private void visitBr(YiDomNode child) throws IOException {
		layoutContext.writeBr();
	}
	void visitHtml(YiDomNode node) throws IOException {
		visitChildren(node, htmlTagSet);
	}
	void visitBody(YiDomNode node) throws IOException {
		visitChildren(node, normalTagSet);
	}
	void visitText(YiDomNode node) throws IOException {
		layoutContext.writeText(node.getText());
	}
	public void exec(YiDomNode dom, YiPdfFile pdfFile) throws IOException {
		layoutContext = new MyLayoutContext(pdfFile);
		visitChildren(dom, rootTagSet);
		layoutContext.clearNowBlock();
	}

	static final HashSet<TagType> rootTagSet = new HashSet<MyDomContext.TagType>();
	static final HashSet<TagType> htmlTagSet = new HashSet<MyDomContext.TagType>();
	static final HashSet<TagType> headTagSet = new HashSet<MyDomContext.TagType>();
	static final HashSet<TagType> normalTagSet = new HashSet<MyDomContext.TagType>();
	static final HashSet<TagType> tableTagSet = new HashSet<MyDomContext.TagType>();
	static final HashSet<TagType> trTagSet = new HashSet<MyDomContext.TagType>();
	static final HashSet<TagType> rubyTagSet = new HashSet<MyDomContext.TagType>();
	static final HashSet<TagType> rbTagSet = new HashSet<MyDomContext.TagType>();
	static {
		rootTagSet.add(TagType.TAG_HTML);
		htmlTagSet.add(TagType.TAG_HEAD);
		htmlTagSet.add(TagType.TAG_BODY);
		headTagSet.add(TagType.TAG_TITLE);
		headTagSet.add(TagType.TAG_META);
		normalTagSet.addAll(TagType.getDic().values());
		normalTagSet.remove(TagType.TAG_HTML);
		normalTagSet.remove(TagType.TAG_HEAD);
		normalTagSet.remove(TagType.TAG_BODY);
		normalTagSet.remove(TagType.TAG_TITLE);
		normalTagSet.remove(TagType.TAG_META);
		normalTagSet.remove(TagType.TAG_TR);
		normalTagSet.remove(TagType.TAG_TD);
		normalTagSet.remove(TagType.TAG_TH);
		normalTagSet.remove(TagType.TAG_RB);
		normalTagSet.remove(TagType.TAG_RT);
		normalTagSet.remove(TagType.TAG_RP);
		tableTagSet.add(TagType.TAG_TR);
		trTagSet.add(TagType.TAG_TD);
		trTagSet.add(TagType.TAG_TH);
		rubyTagSet.add(TagType.TAG_RB);
		rubyTagSet.add(TagType.TAG_RT);
		rubyTagSet.add(TagType.TAG_RP);
		rbTagSet.add(TagType.NOT_TAG_TEXT);
		rbTagSet.add(TagType.TAG_SPAN);
	}
}
