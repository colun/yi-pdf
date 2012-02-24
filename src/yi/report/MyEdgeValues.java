package yi.report;

class MyEdgeValues<T> {
	public MyEdgeValues(T left, T top, T right, T bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	final T left;
	final T top;
	final T right;
	final T bottom;
}
