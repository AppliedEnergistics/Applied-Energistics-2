package uristqwerty.gui_craftguide;

public class MutableRect
{
	private int x, y, width, height;
	private Rect rect = null;

	public MutableRect(int x, int y, int width, int height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public MutableRect(Rect rect)
	{
		x = rect.x;
		y = rect.y;
		width = rect.width;
		height = rect.height;
	}
	
	public Rect rect()
	{
		if(rect == null)
		{
			rect = new Rect(x, y, width, height);
		}
		
		return rect;
	}
	
	public void setPosition(int x, int y)
	{
		rect = null;
		this.x = x;
		this.y = y;
	}
	
	public void setSize(int width, int height)
	{
		rect = null;
		this.width = width;
		this.height = height;
	}
	
	public void setRect(int x, int y, int width, int height)
	{
		rect = null;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void setRect(Rect rect)
	{
		this.rect = null;
		x = rect.x;
		y = rect.y;
		width = rect.width;
		height = rect.height;
	}
	
	public int x()
	{
		return x;
	}
	
	public int y()
	{
		return y;
	}
	
	public int width()
	{
		return width;
	}
	
	public int height()
	{
		return height;
	}
	
	@Override
	public String toString()
	{
		return "(x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + ")";
	}
}
