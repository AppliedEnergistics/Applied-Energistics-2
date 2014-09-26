package uristqwerty.CraftGuide.client.ui.Rendering;

import uristqwerty.gui_craftguide.minecraft.Text;
import uristqwerty.gui_craftguide.rendering.RendererBase;

public class CentredText extends Text
{
	private int width, height;

	public CentredText(int x, int y, int width, int height, String text)
	{
		this(x, y, width, height, text, 0xff000000);
	}
	
	public CentredText(int x, int y, int width, int height, String text, int color)
	{
		super(x, y, text, color);
		
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void render(RendererBase renderer, int xOffset, int yOffset)
	{
		renderer.setColor(color);
		renderer.drawText(text, x + xOffset + (width + 1 - textWidth()) / 2, y + yOffset + (height + 1 - textHeight()) / 2);
		renderer.setColor(0xffffffff);
	}

	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
}
