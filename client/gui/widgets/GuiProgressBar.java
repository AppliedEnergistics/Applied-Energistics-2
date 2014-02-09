package appeng.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import appeng.core.localization.GuiText;

public class GuiProgressBar extends GuiButton implements ITooltip
{

	public enum Direction
	{
		HORIZONTAL, VERTICAL
	};

	private ResourceLocation texture;
	private int fill_u;
	private int fill_v;
	private int width;
	private int height;
	private Direction layout;

	public String FullMsg;

	public String TitleName;
	public int current;
	public int max;

	public GuiProgressBar(String string, int posX, int posY, int u, int y, int _width, int _height, Direction dir) {
		super( posX, posY, _width, "" );
		this.xPosition = posX;
		this.yPosition = posY;
		texture = new ResourceLocation( "appliedenergistics2", "textures/" + string );
		width = _width;
		height = _height;
		fill_u = u;
		fill_v = y;
		current = 0;
		max = 100;
		layout = dir;
	}

	@Override
	public void drawButton(Minecraft par1Minecraft, int par2, int par3)
	{
		if ( this.visible )
		{
			par1Minecraft.getTextureManager().bindTexture( texture );

			if ( layout == Direction.VERTICAL )
			{
				int diff = height - (max > 0 ? (height * current) / max : 0);
				this.drawTexturedModalRect( this.xPosition, this.yPosition + diff, fill_u, fill_v + diff, width, height - diff );
			}
			else
			{
				int diff = width - (max > 0 ? (width * current) / max : 0);
				this.drawTexturedModalRect( this.xPosition, this.yPosition, fill_u + diff, fill_v, width - diff, height );
			}

			this.mouseDragged( par1Minecraft, par2, par3 );
		}
	}

	@Override
	public String getMsg()
	{
		if ( FullMsg != null )
			return FullMsg;

		return (TitleName != null ? TitleName : "") + "\n" + current + " " + GuiText.Of.getLocal() + " " + max;
	}

	@Override
	public int xPos()
	{
		return xPosition - 2;
	}

	@Override
	public int yPos()
	{
		return yPosition - 2;
	}

	@Override
	public int getWidth()
	{
		return width + 4;
	}

	@Override
	public int getHeight()
	{
		return height + 4;
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}

}
