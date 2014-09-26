package uristqwerty.gui_craftguide.texture;

import uristqwerty.gui_craftguide.rendering.RendererBase;

public interface Texture
{
	public void renderRect(RendererBase renderer, int x, int y, int width, int height, int u, int v);
}
