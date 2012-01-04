package yi.pdf;

public abstract class YiPdfResource {
	private final static Object staticLockObj = new Object();
	private static int resourceIdSequence = 0;
	private final int resourceId;
	public int getResourceId() {
		return resourceId;
	}
	public YiPdfResource() {
		synchronized(staticLockObj) {
			resourceId = ++resourceIdSequence;
		}
	}
}
