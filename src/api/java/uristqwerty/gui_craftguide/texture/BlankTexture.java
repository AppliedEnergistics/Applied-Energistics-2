package uristqwerty.gui_craftguide.texture;

import uristqwerty.gui_craftguide.editor.TextureMeta;
import uristqwerty.gui_craftguide.rendering.RendererBase;

@TextureMeta(name = "blank")
public class BlankTexture implements Texture
{
	@Override
	public void renderRect(RendererBase renderer, int x, int y, int width, int height, int u, int v)
	{
	}
}
