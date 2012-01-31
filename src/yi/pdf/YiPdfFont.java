package yi.pdf;

public abstract class YiPdfFont extends YiPdfResource {
	public abstract byte[] encode(String text);
	public abstract String getEncoding();
	public abstract String getFontName();
	public abstract int getTravel(char c);
	public abstract int getLowerPerpend(char c);
	public abstract int getUpperPerpend(char c);
	public abstract int getAscent();
	public abstract int getDescent();
	public abstract int getXHeight();
}
