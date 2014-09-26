package uristqwerty.gui_craftguide.texture;

import uristqwerty.gui_craftguide.Rect;
import uristqwerty.gui_craftguide.editor.TextureMeta;
import uristqwerty.gui_craftguide.editor.TextureMeta.TextureParameter;
import uristqwerty.gui_craftguide.rendering.RendererBase;

/**
 * Represents a subsection of a larger texture, shifted so that
 * the sections's top left corner is at (0, 0). When drawn, loops
 * the subsection to form an arbitrarily large virtual texture, to
 * fully cover the drawn rectangle.
 */
@TextureMeta(name = "subtexture")
public class SubTexture implements Texture
{
	@TextureParameter
	public Texture source;

	@TextureParameter
	public Rect rect;

	public SubTexture(Texture source, int u, int v, int width, int height)
	{
		this.source = source;
		this.rect = new Rect(u, v, width, height);
	}

	public SubTexture()
	{
	}

	@Override
	public void renderRect(RendererBase renderer, int x, int y, int width, int height, int u, int v)
	{
		if(rect.width < 1 || rect.height < 1)
		{
			return;
		}

		u = ((u % rect.width) + rect.width) % rect.width; //Properly handle negatives

		if(u + width <= rect.width)
		{
			renderRectColumn(renderer, x, y, width, height, rect.x + u, v);
		}
		else
		{
			if(u != 0)
			{
				renderRectColumn(renderer, x, y, rect.width - (u % rect.width), height, rect.x + (u % rect.width), v);
			}

			int segment_start;
			for(segment_start = u; segment_start + rect.width < width; segment_start += rect.width)
			{
				renderRectColumn(renderer, x + segment_start, y, rect.width, height, rect.x, v);
			}

			renderRectColumn(renderer, x + segment_start, y, width - segment_start, height, rect.x, v);
		}
	}

	private void renderRectColumn(RendererBase renderer, int x, int y, int width, int height, int u, int v)
	{
		int v1 = (((v % rect.height) + rect.height) % rect.height) + rect.y;
		int v2 = v1 + height;

		if(v2 < rect.y + rect.height)
		{
			source.renderRect(renderer, x, y, width, height, u, v1);
		}
		else
		{
			if(v != 0)
			{
				source.renderRect(renderer, x, y, width, rect.height - (v % rect.height), u, rect.y + (v % rect.height));
			}

			int segment_start;
			for(segment_start = v; segment_start + rect.height < height; segment_start += rect.height)
			{
				source.renderRect(renderer, x, y + segment_start, width, rect.height, u, rect.y);
			}

			source.renderRect(renderer, x, y + segment_start, width, height - segment_start, u, rect.y);
		}
	}
}
