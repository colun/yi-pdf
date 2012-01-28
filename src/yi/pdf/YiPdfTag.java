package yi.pdf;

import java.util.ArrayList;
import java.util.List;

public class YiPdfTag {
	List<YiPdfTag> childrenList = new ArrayList<YiPdfTag>();
	String tagName;
	protected YiPdfTag(String tagName) {
		this.tagName = tagName;
	}
	public YiPdfTag makeChild(String tagName) {
		YiPdfTag result = new YiPdfTag(tagName);
		childrenList.add(result);
		return result;
	}
}
