package appeng.recipes;

public enum RecipeType
{
	// Shapeless
	Shape0x0,
	// Shaped 1x1
	Shape1x1(1, 1),
	// Shaped 2x2
	Shape1x2(1, 2), Shape2x1(2, 1), Shape2x2(2, 2),
	// Shaped 3x3
	Shape2x3(2, 3), Shape3x2(3, 2), Shape3x3(3, 3);

	boolean shapeless;
	final int width;
	final int height;
	final int size;

	private RecipeType(int w, int h) {
		width = w;
		height = h;
		size = w * h;
		shapeless = false;
	}

	private RecipeType() {
		this( 3, 3 );
		shapeless = true;
	}

	public boolean isShaped()
	{
		return shapeless;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public int getSize()
	{
		return size;
	}

}
