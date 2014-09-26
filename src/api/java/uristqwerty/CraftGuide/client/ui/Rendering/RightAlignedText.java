package uristqwerty.CraftGuide.client.ui.Rendering;

import uristqwerty.gui_craftguide.minecraft.Text;
import uristqwerty.gui_craftguide.rendering.RendererBase;

public class RightAlignedText extends Text
{
	public RightAlignedText(int x, int y, String text)
	{
		super(x, y, text);
	}
	
	public RightAlignedText(int x, int y, String text, int color)
	{
		super(x, y, text, color);
	}
	
	@Override
	public void render(RendererBase renderer, int xOffset, int yOffset)
	{
		renderer.setColor(color);
		renderer.drawText(text, x + xOffset - textWidth(), y + yOffset);
		renderer.setColor(0xffffffff);
	}
}
