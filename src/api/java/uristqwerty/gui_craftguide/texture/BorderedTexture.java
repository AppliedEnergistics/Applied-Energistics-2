package uristqwerty.gui_craftguide.texture;

import uristqwerty.gui_craftguide.editor.TextureMeta;
import uristqwerty.gui_craftguide.editor.TextureMeta.ListSize;
import uristqwerty.gui_craftguide.editor.TextureMeta.TextureParameter;
import uristqwerty.gui_craftguide.rendering.RendererBase;

@TextureMeta(name = "borderedtexture")
public class BorderedTexture implements Texture
{
	@ListSize(9)
	@TextureParameter
	public Texture[] textures = new Texture[9];

	@TextureParameter
	public int borderWidth;

	@TextureParameter
	public int borderHeight;

	public BorderedTexture()
	{
	}

	public BorderedTexture(Texture[] textures, int borderWidth)
	{
		this(textures, borderWidth, borderWidth);
	}

	public BorderedTexture(Texture[] textures, int borderWidth, int borderHeight)
	{
		for(int i = 0; i < Math.min(textures.length, 9); i++)
		{
			this.textures[i] = textures[i];
		}

		this.borderWidth = borderWidth;
		this.borderHeight = borderHeight;
	}

	public BorderedTexture(Texture source, int u, int v, int spacing, int centerSize, int borderSize)
	{
		this(source, u, v, spacing, centerSize, centerSize, borderSize, borderSize);
	}

	public BorderedTexture(Texture source, int u, int v, int spacing, int centerWidth, int centerHeight, int borderWidth, int borderHeight)
	{
		int centerU = u + spacing + borderWidth;
		int rightU = u + spacing * 2 + centerWidth + borderWidth;
		int centerV = v + spacing + borderHeight;
		int bottomV = v + spacing * 2 + centerHeight + borderHeight;

		textures[0] = new TextureClip(source, u, v, borderWidth, borderHeight);
		textures[1] = new SubTexture(source, centerU, v, centerWidth, borderHeight);
		textures[2] = new TextureClip(source, rightU, v, borderWidth, borderHeight);
		textures[3] = new TextureClip(source, u, centerV, borderWidth, centerHeight);
		textures[4] = new SubTexture(source, centerU, centerV, centerWidth, centerHeight);
		textures[5] = new TextureClip(source, rightU, centerV, borderWidth, centerHeight);
		textures[6] = new TextureClip(source, u, bottomV, borderWidth, borderHeight);
		textures[7] = new SubTexture(source, centerU, bottomV, centerWidth, borderHeight);
		textures[8] = new TextureClip(source, rightU, bottomV, borderWidth, borderHeight);

		this.borderWidth = borderWidth;
		this.borderHeight = borderHeight;
	}

	@Override
	public void renderRect(RendererBase renderer, int x, int y, int width, int height, int u, int v)
	{
		textures[0].renderRect(renderer, x, y, borderWidth, borderHeight, 0, 0);
		textures[1].renderRect(renderer, x + borderWidth, y, width - borderWidth * 2, borderHeight, 0, 0);
		textures[2].renderRect(renderer, x + width - borderWidth, y, borderWidth, borderHeight, 0, 0);
		textures[3].renderRect(renderer, x, y + borderHeight, borderWidth, height - borderHeight * 2, 0, 0);
		textures[4].renderRect(renderer, x + borderWidth, y + borderHeight, width - borderWidth * 2, height - borderHeight * 2, 0, 0);
		textures[5].renderRect(renderer, x + width - borderWidth, y + borderHeight, borderWidth, height - borderHeight * 2, 0, 0);
		textures[6].renderRect(renderer, x, y + height - borderHeight, borderWidth, borderHeight, 0, 0);
		textures[7].renderRect(renderer, x + borderWidth, y + height - borderHeight, width - borderWidth * 2, borderHeight, 0, 0);
		textures[8].renderRect(renderer, x + width - borderWidth, y + height - borderHeight, borderWidth, borderHeight, 0, 0);
	}
}
