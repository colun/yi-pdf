package yi.pdf;

import java.util.ArrayList;
import java.util.List;

public class YiPdfTag {
	private final static Object staticLockObj = new Object();
	private static int mcIdSequence = 0;

	private List<YiPdfTag> childrenList = new ArrayList<YiPdfTag>();
	private String tagName;
	private YiPdfFile pdfFile;
	private List<Integer> mcIdList = new ArrayList<Integer>();
	protected YiPdfTag(YiPdfFile pdfFile, String tagName) {
		this.pdfFile = pdfFile;
		this.tagName = tagName;
	}
	public String getTagName() {
		return tagName;
	}
	public int publishMcId() {
		int mcId;
		synchronized(staticLockObj) {
			mcId = ++mcIdSequence;
		}
		mcIdList.add(mcId);
		return mcId;
	}
	public YiPdfTag makeChild(String tagName) {
		YiPdfTag result = new YiPdfTag(pdfFile, tagName);
		childrenList.add(result);
		mcIdList.add(-childrenList.size());
		return result;
	}
}
