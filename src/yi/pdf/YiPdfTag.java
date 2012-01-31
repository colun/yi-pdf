/**
 * Copyright (c) 2011-2012 Yasunobu Imamura ( TwitterID: @colun )
 */
package yi.pdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class YiPdfTag {
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
	protected Collection<YiPdfTag> getChildrenList() {
		return childrenList;
	}
	protected Collection<Integer> getMcIdList() {
		return mcIdList;
	}
	public int publishMcId() {
		int mcId = pdfFile.publishMcId();
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
