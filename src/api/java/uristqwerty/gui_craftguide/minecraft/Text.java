package uristqwerty.gui_craftguide.minecraft;

import net.minecraft.client.Minecraft;
import uristqwerty.gui_craftguide.rendering.Renderable;
import uristqwerty.gui_craftguide.rendering.RendererBase;

public class Text implements Renderable
{
	protected int x, y;
	protected String text;
	protected int color;

	public Text(int x, int y, String text, int color)
	{
		this.x = x;
		this.y = y;
		this.text = text;
		this.color = color;
	}
	public Text(int x, int y, String text)
	{
		this(x, y, text, 0xff000000);
	}

	@Override
	public void render(RendererBase renderer, int x, int y)
	{
		renderer.setColor(color);
		renderer.drawText(text, x + this.x, y + this.y);
		renderer.setColor(0xffffffff);
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public int textHeight()
	{
		return 8;
	}

	public int textWidth()
	{
		return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
	}

	public static int textWidth(String text)
	{
		return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
	}
}
