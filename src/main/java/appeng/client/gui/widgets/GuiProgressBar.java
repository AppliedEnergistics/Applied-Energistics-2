package appeng.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import appeng.container.interfaces.IProgressProvider;
import appeng.core.localization.GuiText;

public class GuiProgressBar extends GuiButton implements ITooltip
{

	public enum Direction
	{
		HORIZONTAL, VERTICAL
	}

	private IProgressProvider source;
	private ResourceLocation texture;
	private int fill_u;
	private int fill_v;
	private Direction layout;

	private String fullMsg;
	private final String titleName;

	public GuiProgressBar(IProgressProvider source, String texture, int posX, int posY, int u, int y, int _width, int _height, Direction dir)
	{
		this( source, texture, posX, posY, u, y, _width, _height, dir, null );
	}

	public GuiProgressBar(IProgressProvider source, String texture, int posX, int posY, int u, int y, int _width, int _height, Direction dir, String title)
	{
		super( posX, posY, _width, "" );
		this.source = source;
		this.xPosition = posX;
		this.yPosition = posY;
		this.texture = new ResourceLocation( "appliedenergistics2", "textures/" + texture );
		width = _width;
		height = _height;
		fill_u = u;
		fill_v = y;
		layout = dir;
		titleName = title;
	}

	@Override
	public void drawButton(Minecraft par1Minecraft, int par2, int par3)
	{
		if ( this.visible )
		{
			par1Minecraft.getTextureManager().bindTexture( texture );
			int max = source.getMaxProgress();
			int current = source.getCurrentProgress();

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

	public void setFullMsg(String msg)
	{
		this.fullMsg = msg;
	}

	@Override
	public String getMsg()
	{
		if ( fullMsg != null )
			return fullMsg;

		return (titleName != null ? titleName : "") + "\n" + source.getCurrentProgress() + " " + GuiText.Of.getLocal() + " " + source.getMaxProgress();
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
