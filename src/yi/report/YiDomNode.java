package yi.report;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class YiDomNode {
	public final static int TYPE_OF_ROOT = 0;
	public final static int TYPE_OF_TAG = 1;
	public final static int TYPE_OF_EXT_TAG = 2;
	public final static int TYPE_OF_TEXT = 3;
	private int nodeType;
	private String tagName;
	private String text;
	private Map<String, String> attr;
	private List<YiDomNode> children;
	public static YiDomNode parse(String html) throws ParseException {
		return MyHtmlParser.parse(html);
	}
	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
	}
	public int getNodeType() {
		return nodeType;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public String getTagName() {
		return tagName;
	}
	public void setAttr(Map<String, String> attr) {
		this.attr = attr;
	}
	public Map<String, String> getAttr() {
		return attr;
	}
	public void setChildren(List<YiDomNode> children) {
		this.children = children;
	}
	public List<YiDomNode> getChildren() {
		return children;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getText() {
		return text;
	}
	public static YiDomNode createTextNode(String text) {
		YiDomNode node = new YiDomNode();
		node.nodeType = TYPE_OF_TEXT;
		node.text = text;
		return node;
	}
	public static YiDomNode createRootNode() {
		YiDomNode node = new YiDomNode();
		node.nodeType = TYPE_OF_ROOT;
		return node;
	}
	public static YiDomNode createTagNode(String tagName, Map<String, String> attr) {
		YiDomNode node = new YiDomNode();
		node.nodeType = TYPE_OF_TAG;
		node.tagName = tagName;
		node.attr = attr;
		return node;
	}
	public static YiDomNode createExtTagNode(String tagName, Map<String, String> attr) {
		YiDomNode node = new YiDomNode();
		node.nodeType = TYPE_OF_EXT_TAG;
		node.tagName = tagName;
		node.attr = attr;
		return node;
	}
}
