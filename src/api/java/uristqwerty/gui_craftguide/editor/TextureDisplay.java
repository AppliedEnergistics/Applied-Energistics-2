package uristqwerty.gui_craftguide.editor;

import uristqwerty.gui_craftguide.Rect;
import uristqwerty.gui_craftguide.components.GuiElement;
import uristqwerty.gui_craftguide.rendering.Renderable;
import uristqwerty.gui_craftguide.rendering.RendererBase;
import uristqwerty.gui_craftguide.texture.Texture;

public class TextureDisplay extends GuiElement implements Renderable
{
	private Texture currentTexture;
	private Rect textureSize;
	
	public TextureDisplay(int x, int y, int width, int height)
	{
		super(x, y, width, height);
		textureSize = bounds.rect();
	}

	public TextureDisplay(Rect rect)
	{
		super(rect);
		textureSize = rect;
	}
	
	@Override
	public void draw()
	{
		render(this);
		super.draw();
	}
	
	public void setTexture(Texture texture, Rect size)
	{
		currentTexture = texture;
		textureSize = size;
	}

	@Override
	public void render(RendererBase renderer, int x, int y)
	{
		if(currentTexture != null && textureSize != null)
		{
			renderer.setAlpha(127);
			renderer.drawTexturedRect(currentTexture,
					x, y, bounds.width(), bounds.height(),
					(textureSize.width - bounds.width()) / 2, (textureSize.height - bounds.height()) / 2);
			
			renderer.setAlpha(256);
			renderer.drawTexturedRect(currentTexture,
					x + (bounds.width() - textureSize.width) / 2, y + (bounds.height() - textureSize.height) / 2,
					textureSize.width, textureSize.height, 0, 0);
		}
	}
}
