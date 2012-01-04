package yi.pdf;

public abstract class YiPdfFont extends YiPdfResource {
	public abstract byte[] encode(String text);
	public abstract String getEncoding();
	public abstract String getFontName();
}
