package yi.report;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class MyHtmlParser {

	private final static boolean[] nameCharset;
	private final static boolean[] spaceCharset;
	private final static boolean[] alphaCharset;
	private final static Map<String, Character> symbolMap;
	static {
		nameCharset = new boolean[128];
		for(char i='A'; i<='Z'; ++i) {
			nameCharset[i] = true;
		}
		for(char i='a'; i<='z'; ++i) {
			nameCharset[i] = true;
		}
		for(char i='0'; i<='9'; ++i) {
			nameCharset[i] = true;
		}
		nameCharset['_'] = true;
		nameCharset['-'] = true;
		nameCharset[':'] = true;

		spaceCharset = new boolean[128];
		spaceCharset[' '] = true;
		spaceCharset['\t'] = true;
		spaceCharset['\r'] = true;
		spaceCharset['\n'] = true;

		alphaCharset = new boolean[128];
		for(char i='A'; i<='Z'; ++i) {
			alphaCharset[i] = true;
		}
		for(char i='a'; i<='z'; ++i) {
			alphaCharset[i] = true;
		}
		symbolMap = new LinkedHashMap<String, Character>();
		symbolMap.put("quot", '"');
		symbolMap.put("amp", '&');
		symbolMap.put("lt", '<');
		symbolMap.put("gt", '>');
		symbolMap.put("nbsp", ' ');
		symbolMap.put("iexcl", (char)161);
		symbolMap.put("cent", (char)162);
		symbolMap.put("pound", (char)163);
		symbolMap.put("curren", (char)164);
		symbolMap.put("yen", (char)165);
	}
	private static boolean isNameCharset(char c) {
		return 0<=c && c<128 && nameCharset[c];
	}
	private static boolean isSpaceCharset(char c) {
		return 0<=c && c<128 && spaceCharset[c];
	}
	private static boolean isNumCharset(char c) {
		return '0'<=c && c<='9';
	}
	private static boolean isAlphaNumCharset(char c) {
		return 0<=c && c<128 && (isNumCharset(c) || alphaCharset[c]);
	}
	private static String decodeHtmlText(String html, char[] text, int spos, int epos, boolean spFlag) {
		if(spFlag) {
			while(spos<epos && isSpaceCharset(text[spos])) {
				++spos;
			}
			while(spos<epos && isSpaceCharset(text[epos-1])) {
				--epos;
			}
		}
		StringBuilder builder = new StringBuilder();
		int pos = spos;
		while(pos<epos) {
			if(spFlag && isSpaceCharset(text[pos])) {
				++pos;
				builder.append(' ');
				while(pos<epos && isSpaceCharset(text[pos])) {
					++pos;
				}
				continue;
			}
			else if(pos+2<epos && text[pos]=='&') {
				if(text[pos+1]=='#') {
					int pos2 = pos + 2;
					int numStartPos = pos2;
					while(pos2<epos && isNumCharset(text[pos2])) {
						++pos2;
					}
					if(pos2!=numStartPos && pos2<epos && text[pos2]==';') {
						char c = (char)Integer.parseInt(html.substring(numStartPos, pos2));
						builder.append(c);
						pos = pos2 + 1;
						continue;
					}
				}
				else {
					int pos2 = pos + 1;
					int numStartPos = pos2;
					while(pos2<epos && isAlphaNumCharset(text[pos2])) {
						++pos2;
					}
					if(pos2!=numStartPos && pos2<epos && text[pos2]==';') {
						String key = html.substring(numStartPos, pos2);
						if(symbolMap.containsKey(key)) {
							char c = (char)symbolMap.get(key);
							builder.append(c);
							pos = pos2 + 1;
							continue;
						}
					}
				}
			}
			builder.append(text[pos]);
			++pos;
		}
		if(builder.length()==0) {
			return null;
		}
		return builder.toString();
	}
	public static YiDomNode parse(String html) throws ParseException {
		YiDomNode root = YiDomNode.createRootNode();
		Stack<YiDomNode> nodeStack = new Stack<YiDomNode>();

		root.setChildren(new ArrayList<YiDomNode>());
		nodeStack.push(root);

		char[] text = html.toCharArray();
		int pos = 0;
		int startPos = pos;
		try {
			while(true) {
				while(text[pos]!='<') {
					++pos;
				}
				int tagStartPos = pos;
				++pos;
				if(text[pos]=='/') {
					++pos;
					int tagNameStartPos = pos;
					while(isNameCharset(text[pos])) {
						++pos;
					}
					if(pos==tagNameStartPos || text[pos]!='>') {
						continue;
					}
					String tagName = html.substring(tagNameStartPos, pos);
					++pos;
					if(startPos!=tagStartPos) {
						String t = decodeHtmlText(html, text, startPos, tagStartPos, true);
						if(t!=null) {
							YiDomNode textNode = YiDomNode.createTextNode(t);
							nodeStack.peek().getChildren().add(textNode);
						}
					}
					while(!tagName.equalsIgnoreCase(nodeStack.peek().getTagName())) {
						YiDomNode node = nodeStack.peek();
						List<YiDomNode> nodeList = node.getChildren();
						node.setChildren(null);
						nodeStack.pop();
						if(nodeStack.empty()) {
							throwError(text, tagStartPos, String.format("</%s> : cant't found pair tag beginning.", tagName));
						}
						nodeStack.peek().getChildren().addAll(nodeList);
					}
					nodeStack.pop();
					startPos = pos;
				}
				else {
					boolean extTagFlag = false;
					if(text[pos]=='!') {
						++pos;
						if(text[pos]=='-' && text[pos+1]=='-') {
							pos += 2;
							if(startPos!=tagStartPos) {
								String t = decodeHtmlText(html, text, startPos, tagStartPos, true);
								if(t!=null) {
									YiDomNode textNode = YiDomNode.createTextNode(t);
									nodeStack.peek().getChildren().add(textNode);
								}
							}
							while(true) {
								while(text[pos]!='-') {
									++pos;
								}
								++pos;
								if(text[pos]=='-') {
									if(text[pos+1]=='>') {
										pos += 2;
										break;
									}
								}
							}
							startPos = pos;
							continue;
						}
						extTagFlag = true;
					}
					int tagNameStartPos = pos;
					while(isNameCharset(text[pos])) {
						++pos;
					}
					if(pos==tagNameStartPos) {
						continue;
					}
					String tagName = html.substring(tagNameStartPos, pos);
					boolean selfFlag = extTagFlag;
					boolean endTagFlag = false;
					Map<String, String> attr = new LinkedHashMap<String, String>();
					while(true) {
						while(isSpaceCharset(text[pos])) {
							++pos;
						}
						if(text[pos]=='>') {
							++pos;
							endTagFlag = true;
							break;
						}
						if(text[pos]=='/') {
							++pos;
							while(isSpaceCharset(text[pos])) {
								++pos;
							}
							if(text[pos]=='>') {
								++pos;
								endTagFlag = true;
							}
							break;
						}
						int attrNameStartPos = pos;
						while(isNameCharset(text[pos])) {
							++pos;
						}
						if(pos==attrNameStartPos) {
							break;
						}
						String attrName = html.substring(attrNameStartPos, pos);
						String value = null;
						if(text[pos]=='=') {
							++pos;
							if(text[pos]=='"') {
								++pos;
								int valueStartPos = pos;
								while(text[pos]!='"') {
									++pos;
								}
								value = decodeHtmlText(html, text, valueStartPos, pos, false);
								++pos;
							}
							else if(text[pos]=='\'') {
								++pos;
								int valueStartPos = pos;
								while(text[pos]!='\'') {
									++pos;
								}
								value = decodeHtmlText(html, text, valueStartPos, pos, false);
								++pos;
							}
							else {
								int valueStartPos = pos;
								while(isNameCharset(text[pos])) {
									++pos;
								}
								value = html.substring(valueStartPos, pos);
							}
						}
						attr.put(attrName, value);
					}
					if(!endTagFlag) {
						continue;
					}
					if(startPos!=tagStartPos) {
						String t = decodeHtmlText(html, text, startPos, tagStartPos, true);
						if(t!=null) {
							YiDomNode textNode = YiDomNode.createTextNode(t);
							nodeStack.peek().getChildren().add(textNode);
						}
					}
					YiDomNode node = extTagFlag
							? YiDomNode.createExtTagNode(tagName, attr)
							: YiDomNode.createTagNode(tagName, attr)
							;
					nodeStack.peek().getChildren().add(node);
					if(!selfFlag) {
						node.setChildren(new ArrayList<YiDomNode>());
						nodeStack.push(node);
					}
					startPos = pos;
				}
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
		}
		return root;
	}
	private static void throwError(char[] text, int pos, String message) throws ParseException {
		int lineCount = 0;
		int linePos = 0;
		for(int i=0; i<pos; ++i) {
			if(text[i]=='\n') {
				linePos = i+1;
				++lineCount;
			}
		}
		String err = String.format("%s, line %d, char %d", message, lineCount+1, pos-linePos+1);
		throw new ParseException(err, pos);
	}
}
