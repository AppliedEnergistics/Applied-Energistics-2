package uristqwerty.gui_craftguide.texture;

import uristqwerty.gui_craftguide.rendering.RendererBase;

public class OffsetTexture implements Texture
{
	private final Texture base;
	private final int u, v;
	
	public OffsetTexture(Texture base, int u, int v)
	{
		this.base = base;
		this.u = u;
		this.v = v;
	}

	@Override
	public void renderRect(RendererBase renderer, int x, int y, int width, int height, int u, int v)
	{
		base.renderRect(renderer, x, y, width, height, u + this.u, v + this.v);
	}
}
