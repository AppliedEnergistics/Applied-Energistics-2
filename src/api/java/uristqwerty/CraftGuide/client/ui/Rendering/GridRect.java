package uristqwerty.CraftGuide.client.ui.Rendering;

import uristqwerty.CraftGuide.client.ui.GuiRenderer;
import uristqwerty.CraftGuide.client.ui.GuiScrollableGrid;
import uristqwerty.gui_craftguide.rendering.Renderable;
import uristqwerty.gui_craftguide.rendering.RendererBase;

public class GridRect implements Renderable
{
	private int x, y, width, height;
	private GuiScrollableGrid gridElement;

	public GridRect(int x, int y, int width, int height, GuiScrollableGrid displayElement)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gridElement = displayElement;
	}

	//@Override
	public void render(GuiRenderer renderer, int xOffset, int yOffset)
	{
		renderer.setClippingRegion(x + xOffset, y + yOffset, width, height);

		try
		{
			gridElement.renderGridRows(renderer, x + xOffset, y + yOffset);
		}
		finally
		{
			renderer.clearClippingRegion();
		}
	}

	@Override
	public void render(RendererBase renderer, int x, int y)
	{
		render((GuiRenderer)renderer, x, y);
	}

	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
}
