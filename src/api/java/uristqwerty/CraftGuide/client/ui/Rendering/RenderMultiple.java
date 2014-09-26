package uristqwerty.CraftGuide.client.ui.Rendering;

import uristqwerty.gui_craftguide.rendering.Renderable;
import uristqwerty.gui_craftguide.rendering.RendererBase;

public class RenderMultiple implements Renderable
{
	Renderable render[];
	int x, y;
	
	public RenderMultiple(Renderable render[])
	{
		this(0, 0, render);
	}
	
	public RenderMultiple(int x, int y, Renderable render[])
	{
		this.x = x;
		this.y = y;
		this.render = render;
	}

	@Override
	public void render(RendererBase renderer, int x, int y)
	{
		for(Renderable renderable: render)
		{
			renderable.render(renderer, x + this.x, y + this.y);
		}
	}
}
