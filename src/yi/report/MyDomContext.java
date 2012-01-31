package yi.report;

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
		TAG_TABLE("table"),
		TAG_TR("tr"),
		TAG_TD("td"),
		TAG_TH("th"),
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
	static Pattern stylePattern = Pattern.compile(" *([-a-zA-Z0-9_]+) *: *([-a-zA-Z0-9_]+) *");
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
	void visitChildren(YiDomNode node, Set<TagType> enableChildTag) {
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
				}
				layoutContext.popStyle();
			}
		}
	}
	void visitHtml(YiDomNode node) {
		visitChildren(node, htmlTagSet);
	}
	void visitBody(YiDomNode node) {
		visitChildren(node, normalTagSet);
	}
	void visitText(YiDomNode node) {
		layoutContext.writeText(node.getText());
	}
	public void exec(YiDomNode dom, YiPdfFile pdfFile) {
		layoutContext = new MyLayoutContext(pdfFile);
		visitChildren(dom, rootTagSet);
	}

	static final HashSet<TagType> rootTagSet = new HashSet<MyDomContext.TagType>();
	static final HashSet<TagType> htmlTagSet = new HashSet<MyDomContext.TagType>();
	static final HashSet<TagType> headTagSet = new HashSet<MyDomContext.TagType>();
	static final HashSet<TagType> normalTagSet = new HashSet<MyDomContext.TagType>();
	static final HashSet<TagType> tableTagSet = new HashSet<MyDomContext.TagType>();
	static final HashSet<TagType> trTagSet = new HashSet<MyDomContext.TagType>();
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
		tableTagSet.add(TagType.TAG_TR);
		trTagSet.add(TagType.TAG_TD);
		trTagSet.add(TagType.TAG_TH);
	}
}
