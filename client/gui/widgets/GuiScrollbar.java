package appeng.client.gui.widgets;

import org.lwjgl.opengl.GL11;

import appeng.client.gui.AEBaseGui;

public class GuiScrollbar implements IScrollSource
{

	private int displayX = 0;
	private int displayY = 0;
	private int width = 12;
	private int height = 16;
	private int pageSize = 1;

	private int maxScroll = 0;
	private int minScroll = 0;
	private int currentScroll = 0;

	private void applyRange()
	{
		currentScroll = Math.max( Math.min( currentScroll, maxScroll ), minScroll );
	}

	public void draw(AEBaseGui g)
	{
		g.bindTexture( "minecraft", "gui/container/creative_inventory/tabs.png" );
		GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );

		if ( getRange() == 0 )
		{
			g.drawTexturedModalRect( displayX, displayY, 232 + width, 0, width, 15 );
		}
		else
		{
			int offset = (currentScroll - minScroll) * (height - 15) / getRange();
			g.drawTexturedModalRect( displayX, offset + displayY, 232, 0, width, 15 );
		}
	}

	public int getRange()
	{
		return maxScroll - minScroll;
	}

	public int getLeft()
	{
		return displayX;
	}

	public int getTop()
	{
		return displayY;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public GuiScrollbar setLeft(int v)
	{
		displayX = v;
		return this;
	}

	public GuiScrollbar setTop(int v)
	{
		displayY = v;
		return this;
	}

	public GuiScrollbar setWidth(int v)
	{
		width = v;
		return this;
	}

	public GuiScrollbar setHeight(int v)
	{
		height = v;
		return this;
	}

	public void setRange(int min, int max, int pageSize)
	{
		minScroll = min;
		maxScroll = max;
		this.pageSize = pageSize;

		if ( minScroll > maxScroll )
			maxScroll = minScroll;

		applyRange();
	}

	@Override
	public int getCurrentScroll()
	{
		return currentScroll;
	}

	public void click(AEBaseGui aeBaseGui, int x, int y)
	{
		if ( getRange() == 0 )
			return;

		if ( x > displayX && x <= displayX + width )
		{
			if ( y > displayY && y <= displayY + height )
			{
				currentScroll = (y - displayY);
				currentScroll = minScroll + ((currentScroll * 2 * getRange() / height));
				currentScroll = (currentScroll + 1) >> 1;
				applyRange();
			}
		}
	}

	public void wheel(int delta)
	{
		delta = Math.max( Math.min( -delta, 1 ), -1 );
		currentScroll += delta * pageSize;
		applyRange();
	}

}
