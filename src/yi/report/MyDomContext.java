/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.report;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yi.pdf.YiPdfFile;
import yi.pdf.YiPdfTag;
import yi.report.MyLayoutTable.ModeType;

class MyDomContext {
	public static enum TagType {
		TAG_HTML("_html"),
		TAG_HEAD("_head"),
		TAG_TITLE("_title"),
		TAG_META("_meta"),
		TAG_STYLE("_style"),
		TAG_BODY("_body"),
		TAG_BR("_br"),
		TAG_RUBY("_ruby"),
		TAG_RB("_rb"),
		TAG_RT("_rt"),
		TAG_RP("_rp"),
		DISPLAY_BLOCK("block"),
		DISPLAY_INLINE("inline"),
		DISPLAY_TABLE("table"),
		DISPLAY_TABLE_ROW("table-row"),
		DISPLAY_TABLE_CELL("table-cell"),
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
		for(String key : new String[] { "html", "head", "title", "meta", "style", "body", "ruby", "rb", "rt", "rp", "br" }) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("display", "_" + key);
			regStyle(key, m);
		}
		{
			Map<String, String> m = new HashMap<String, String>();
			m.put("display", "block");
			regStyle("div", m);
			regStyle("h1", m);
			regStyle("h2", m);
			regStyle("h3", m);
			regStyle("h4", m);
			regStyle("h5", m);
			regStyle("h6", m);
		}
		{
			Map<String, String> m = new HashMap<String, String>();
			regStyle("@page", m);
		}
		{
			Map<String, String> m = new HashMap<String, String>();
			m.put("display", "table");
			regStyle("table", m);
		}
		{
			Map<String, String> m = new HashMap<String, String>();
			m.put("display", "table-row");
			regStyle("tr", m);
		}
		{
			Map<String, String> m = new HashMap<String, String>();
			m.put("display", "table-cell");
			regStyle("td", m);
			regStyle("th", m);
		}
	}
	static Pattern stylePattern = Pattern.compile(" *([-a-zA-Z0-9_]+) *: *([-#a-zA-Z0-9_\\.]+) *");
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
	void visitNode(YiDomNode node, Set<TagType> enableChildTag) throws IOException {
		if(node.getNodeType()!=YiDomNode.TYPE_OF_TAG) {
			if(node.getNodeType()==YiDomNode.TYPE_OF_TEXT) {
				if(enableChildTag==null || enableChildTag.contains(TagType.NOT_TAG_TEXT)) {
					visitText(node);
				}
			}
			return;
		}
		Map<String, String> diff = new HashMap<String, String>();
		diff.put("display", "inline");
		{
			{
				Map<String, String> st = styleDic.get(node.getTagName());
				if(st!=null) {
					diff.putAll(st);
				}
			}
			Map<String, String> attr = node.getAttr();
			if(attr!=null) {
				String cls = attr.get("class");
				if(cls!=null && !cls.isEmpty()) {
					for(String cc : cls.split(" ")) {
						Map<String, String> st = styleDic.get("." + cc);
						if(st!=null) {
							diff.putAll(st);
						}
					}
				}
				String id = attr.get("id");
				if(id!=null && !id.isEmpty()) {
					Map<String, String> st = styleDic.get("#" + id);
					if(st!=null) {
						diff.putAll(st);
					}
				}
				String style = attr.get("style");
				if(style!=null) {
					diff.putAll(fromStyle(style));
				}
			}
		}
		String display = diff.get("display");
		TagType tagType = TagType.fromString(display);
		if(tagType==null || enableChildTag!=null && !enableChildTag.contains(tagType)) {
			return;
		}
		YiPdfTag tag = null;
		if(node.getTagName().startsWith("h")) {
			if("h1".equals(node.getTagName())) {
				tag = layoutContext.pushPdfTag("H1");
			}
			else if("h2".equals(node.getTagName())) {
				tag = layoutContext.pushPdfTag("H2");
			}
			else if("h3".equals(node.getTagName())) {
				tag = layoutContext.pushPdfTag("H3");
			}
			else if("h4".equals(node.getTagName())) {
				tag = layoutContext.pushPdfTag("H4");
			}
			else if("h5".equals(node.getTagName())) {
				tag = layoutContext.pushPdfTag("H5");
			}
			else if("h6".equals(node.getTagName())) {
				tag = layoutContext.pushPdfTag("H6");
			}
		}
		layoutContext.pushStyle(diff);
		switch(tagType) {
		case TAG_HTML: visitHtmlTag(node); break;
		case TAG_HEAD: visitHeadTag(node); break;
		case TAG_TITLE: visitTitleTag(node); break;
		case TAG_META: visitMetaTag(node); break;
		case TAG_STYLE: visitStyleTag(node); break;
		case TAG_BODY: visitBodyTag(node); break;
		case TAG_BR: visitBrTag(node); break;
		case DISPLAY_INLINE: visitInline(node); break;
		case TAG_RUBY: visitRubyTag(node); break;
		case TAG_RB: visitRbTag(node); break;
		case TAG_RT: visitRtTag(node); break;
		case TAG_RP: visitRpTag(node); break;
		case DISPLAY_BLOCK: visitBlock(node); break;
		case DISPLAY_TABLE: visitTable(node); break;
		case DISPLAY_TABLE_ROW: visitTableRow(node); break;
		case DISPLAY_TABLE_CELL: visitTableCell(node); break;
		}
		layoutContext.popStyle();
		if(tag!=null) {
			layoutContext.popPdfTag();
		}
	}
	void visitChildren(YiDomNode node, Set<TagType> enableChildTag) throws IOException {
		List<YiDomNode> children = node.getChildren();
		for(YiDomNode child : children) {
			if(child!=null) {
				visitNode(child, enableChildTag);
			}
		}
	}
	private void visitTable(YiDomNode node) throws IOException {
		MyLayoutTable table = layoutContext.pushTable();

		table.beginMode(MyLayoutTable.ModeType.MODE_SCAN1);
		visitChildren(node, tableTagSet);
		table.endMode();

		table.beginMode(MyLayoutTable.ModeType.MODE_SCAN2);
		visitChildren(node, tableTagSet);
		table.endMode();

		table.beginMode(MyLayoutTable.ModeType.MODE_VISIT);
		visitChildren(node, tableTagSet);
		table.endMode();
		System.out.printf("colCount: %d, rowCount: %d\n", table.getColCount(), table.getRowCount());

		layoutContext.popTable();
	}
	private void visitTableRow(YiDomNode node) throws IOException {
		MyLayoutTable table = layoutContext.getNowTable();
		table.beginRow();
		visitChildren(node, trTagSet);
		table.endRow();
	}
	private void visitTableCell(YiDomNode node) throws IOException {
		MyLayoutTable table = layoutContext.getNowTable();
		ModeType mode = table.getMode();
		if(mode==ModeType.MODE_SCAN1 || mode==ModeType.MODE_SCAN2) {
			table.scan(node.getAttr(), mode);
		}
		else if(mode==ModeType.MODE_VISIT) {
			table.beginCell(node.getAttr());
			visitChildren(node, normalTagSet);
			table.endCell(node.getAttr());
		}
		else assert(false) : "未知のテーブル文脈モード";
	}
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private void visitMetaTag(YiDomNode node) {
		Map<String, String> attr = node.getAttr();
		if(attr!=null) {
			String name = attr.get("name");
			String content = attr.get("content");
			if("CreationDate".equalsIgnoreCase(name)) {
				try {
					Date date = dateFormat.parse(content);
					layoutContext.getPdfFile().setCreationDate(date);
				} catch (ParseException e) {
				}
			}
			else if("TaggedPDF".equalsIgnoreCase(name)) {
				layoutContext.setTaggedMode(!"none".equalsIgnoreCase(content));
			}
			else if(name!=null){
				layoutContext.getPdfFile().setInfo(name, content);
			}
		}
	}
	private void visitTitleTag(YiDomNode node) {
		List<YiDomNode> children = node.getChildren();
		if(children!=null) {
			StringBuilder builder = new StringBuilder();
			for(YiDomNode child : children) {
				if(child.getNodeType()==YiDomNode.TYPE_OF_TEXT) {
					builder.append(child.getText());
				}
			}
			String text = builder.toString();
			if(text!=null && !text.isEmpty()) {
				layoutContext.getPdfFile().setInfo("Title", text);
			}
		}
	}
	Map<String, Map<String, String>> styleDic = new HashMap<String, Map<String,String>>();
	public void regStyle(String key, Map<String, String> style) {
		styleDic.put(key, style);
	}
	private void visitStyleTag(YiDomNode node) {
		List<YiDomNode> children = node.getChildren();
		if(children!=null) {
			StringBuilder builder = new StringBuilder();
			for(YiDomNode child : children) {
				if(child.getNodeType()==YiDomNode.TYPE_OF_TEXT) {
					builder.append(child.getText());
				}
			}
			String text = builder.toString();
			if(text!=null && !text.isEmpty()) {
				Pattern pattern = Pattern.compile("[ ]*(#[-0-9A-Za-z_]+|\\.[-0-9A-Za-z_]+|@page|@page [-0-9A-Za-z_]+)[ ]*\\{([^}]+)\\}");
				Matcher m = pattern.matcher(text);
				while(m.find()) {
					String key = m.group(1);
					Map<String, String> style = fromStyle(m.group(2));
					regStyle(key, style);
				}
			}
		}
	}
	private void visitHeadTag(YiDomNode node) throws IOException {
		visitChildren(node, headTagSet);
	}
	private void visitBlock(YiDomNode node) throws IOException {
		MyLayoutStyle style = layoutContext.getNowStyle();
		boolean pageStyleFlag = style.hasPage();
		boolean floatFlag = style.hasFloat();
		assert(!(pageStyleFlag && floatFlag)) : "ページスタイル変更とfloat指定は、同時には行えません。";
		if(style.hasWritingMode()) {
			assert(pageStyleFlag || floatFlag) : "文字方向を変更する時、ページスタイル変更またはfloat指定が行われている必要があります。（ただし、この制限は将来的に解除される可能性があります。）";
		}
		layoutContext.writeClearLine();
		if(style.hasPageBreakBefore()) {
			layoutContext.clearNowBlock();
		}
		if(pageStyleFlag) {
			Map<String, String> pageStyleDic = styleDic.get("@page " + style.getPage());
			Map<String, String> vv;
			if(pageStyleDic!=null) {
				vv = new LinkedHashMap<String, String>(styleDic.get("@page"));
				vv.putAll(pageStyleDic);
			}
			else {
				vv = styleDic.get("@page");
			}
			layoutContext.clearNowBlock();
			layoutContext.pushPageStyle(vv);
		}
		if(floatFlag) {
			MyLayoutBlock block = layoutContext.getNowBlock().makeChildFloatBlock(style, layoutContext.getNowNest());
			layoutContext.pushBlock(block);
		}
		boolean nestFlag = false;
		if(!floatFlag && style.hasBackgroundColor()) {
			nestFlag = true;
			layoutContext.pushChildNest();
		}

		visitChildren(node, normalTagSet);
		layoutContext.writeClearLine();
		if(nestFlag) {
			layoutContext.popNest();
		}
		if(floatFlag) {
			MyLayoutBlock block = layoutContext.popBlock();
			layoutContext.getNowBlock().addFloatBlock(block, style.getFloat(), layoutContext.getNowNest());
		}
		if(pageStyleFlag) {
			layoutContext.popPageStyle();
			layoutContext.clearNowBlock();
		}
		if(style.hasPageBreakAfter()) {
			layoutContext.clearNowBlock();
		}
	}
	private void visitBodyTag(YiDomNode node) throws IOException {
		Map<String, String> pageStyle = styleDic.get("@page");
		if(pageStyle!=null) {
			layoutContext.pushPageStyle(pageStyle);
		}
		visitBlock(node);
		if(pageStyle!=null) {
			layoutContext.popPageStyle();
		}
	}
	private void visitRubyTag(YiDomNode node) throws IOException {
		layoutContext.lockLazyDraw();
		visitChildren(node, rubyTagSet);
		layoutContext.unlockLazyDraw();
	}
	List<MyLayoutInlineText> rbInlineText;
	private void visitRbTag(YiDomNode node) throws IOException {
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
		if(rbTravelSum < rtTravelSum) {
			double rate = rbTravelSum / rtTravelSum;
			rtTravelSum = 0;
			for(MyLayoutInlineText rt : rtList) {
				rt.changeScale(rate);
				rtTravelSum += rt.getTravel();
			}
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
				rt.setRubyTravelDiff((rbt - trvl2) - rbTravelPos);
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
	private void visitRtTag(YiDomNode node) throws IOException {
		assert(rbInlineText!=null) : "rtタグよりも前にrbタグが必要です。";
		Map<String, String> wrapStyle = new HashMap<String, String>();
		wrapStyle.put("font-size", String.format("%fpt", layoutContext.getNowStyle().getFontSize() * 0.5));
		wrapStyle.put("line-height", null);
		MyLayoutLine infLine = MyLayoutLine.createInfLine(layoutContext.getNowLine().isVerticalWritingMode());
		layoutContext.pushLine(infLine);
		layoutContext.pushStyle(wrapStyle);
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
	private void visitRpTag(YiDomNode node) throws IOException {
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
			Map<String, String> wrapStyle = new HashMap<String, String>();
			wrapStyle.put("font-size", String.format("%fpt", layoutContext.getNowStyle().getFontSize() * 0.015625));
			wrapStyle.put("line-height", null);
			layoutContext.pushStyle(wrapStyle);
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
	private void visitInline(YiDomNode node) throws IOException {
		MyLayoutStyle style = layoutContext.getNowStyle();
		boolean writingModeFlag = style.hasWritingMode();
		if(writingModeFlag) {
			MyLayoutLine infLine = MyLayoutLine.createInfLine(style.isVerticalWritingMode());
			layoutContext.pushLine(infLine);
			visitChildren(node, normalTagSet);
			MyLayoutLine infLine2 = layoutContext.popLine();
			assert(infLine==infLine2);
			layoutContext.writeInline(new MyLayoutInlineLine(layoutContext.getNowBlock().isVerticalWritingMode(), infLine));
		}
		else {
			visitChildren(node, normalTagSet);
		}
	}
	private void visitBrTag(YiDomNode child) throws IOException {
		layoutContext.writeBr();
	}
	private void visitHtmlTag(YiDomNode node) throws IOException {
		visitChildren(node, htmlTagSet);
	}
	private void visitText(YiDomNode node) throws IOException {
		layoutContext.writeText(node.getText());
	}
	public void exec(YiDomNode dom, YiPdfFile pdfFile) throws IOException {
		pdfFile.setInfo("Producer", "yi-report & yi-pdf");
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
		headTagSet.add(TagType.TAG_STYLE);
		normalTagSet.addAll(TagType.getDic().values());
		normalTagSet.remove(TagType.TAG_HTML);
		normalTagSet.remove(TagType.TAG_HEAD);
		normalTagSet.remove(TagType.TAG_BODY);
		normalTagSet.remove(TagType.TAG_TITLE);
		normalTagSet.remove(TagType.TAG_META);
		normalTagSet.remove(TagType.DISPLAY_TABLE_ROW);
		normalTagSet.remove(TagType.DISPLAY_TABLE_CELL);
		normalTagSet.remove(TagType.TAG_RB);
		normalTagSet.remove(TagType.TAG_RT);
		normalTagSet.remove(TagType.TAG_RP);
		tableTagSet.add(TagType.DISPLAY_TABLE_ROW);
		trTagSet.add(TagType.DISPLAY_TABLE_CELL);
		rubyTagSet.add(TagType.TAG_RB);
		rubyTagSet.add(TagType.TAG_RT);
		rubyTagSet.add(TagType.TAG_RP);
		rbTagSet.add(TagType.NOT_TAG_TEXT);
		rbTagSet.add(TagType.DISPLAY_INLINE);
	}
}
