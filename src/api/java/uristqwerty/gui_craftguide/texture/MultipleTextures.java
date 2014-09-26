package uristqwerty.gui_craftguide.texture;

import uristqwerty.gui_craftguide.editor.TextureMeta;
import uristqwerty.gui_craftguide.editor.TextureMeta.TextureParameter;
import uristqwerty.gui_craftguide.rendering.RendererBase;

@TextureMeta(name = "multipletextures")
public class MultipleTextures implements Texture
{
	@TextureParameter
	public Texture[] textures;

	public MultipleTextures(Texture[] textures)
	{
		this.textures = textures;
	}

	@Override
	public void renderRect(RendererBase renderer, int x, int y, int width, int height, int u, int v)
	{
		for(Texture texture: textures)
		{
			texture.renderRect(renderer, x, y, width, height, u, v);
		}
	}
}
