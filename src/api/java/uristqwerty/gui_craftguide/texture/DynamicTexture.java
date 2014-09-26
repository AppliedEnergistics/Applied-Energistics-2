package uristqwerty.gui_craftguide.texture;

import java.util.HashMap;
import java.util.Map;

import uristqwerty.gui_craftguide.rendering.RendererBase;

public class DynamicTexture implements Texture
{
	public Texture mapped;
	public final String id;

	private DynamicTexture(String id)
	{
		this.id = id;
	}

	@Override
	public void renderRect(RendererBase renderer, int x, int y, int width, int height, int u, int v)
	{
		if(mapped != null)
		{
			mapped.renderRect(renderer, x, y, width, height, u, v);
		}
	}

	private static Map<String, DynamicTexture> instances = new HashMap<String, DynamicTexture>();

	public static Texture instance(String id, Texture mappedTo)
	{
		DynamicTexture texture = (DynamicTexture)instance(id);

		if(texture != null)
		{
			texture.mapped = mappedTo;
		}

		return texture;
	}

	public static Texture instance(String id)
	{
		DynamicTexture texture = instances.get(id);

		if(texture == null)
		{
			texture = new DynamicTexture(id);
			instances.put(id, texture);
		}

		return texture;
	}
}
