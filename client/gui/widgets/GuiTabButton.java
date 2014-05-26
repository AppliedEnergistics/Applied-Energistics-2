package appeng.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import appeng.client.texture.ExtraBlockTextures;

public class GuiTabButton extends GuiButton implements ITooltip
{

	RenderItem itemRenderer;

	int myIcon = -1;
	ItemStack myItem;

	String Msg;

	public void setVisibility(boolean vis)
	{
		visible = vis;
		enabled = vis;
	}

	public GuiTabButton(int x, int y, int ico, String Msg, RenderItem ir) {
		super( 0, 0, 16, "" );
		xPosition = x;
		yPosition = y;
		width = 22;
		height = 22;
		myIcon = ico;
		this.Msg = Msg;
		this.itemRenderer = ir;
	}

	public GuiTabButton(int x, int y, ItemStack ico, String Msg, RenderItem ir) {
		super( 0, 0, 16, "" );
		xPosition = x;
		yPosition = y;
		width = 22;
		height = 22;
		myItem = ico;
		this.Msg = Msg;
		this.itemRenderer = ir;
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	@Override
	public void drawButton(Minecraft par1Minecraft, int par2, int par3)
	{
		if ( this.visible )
		{
			GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
			par1Minecraft.renderEngine.bindTexture( ExtraBlockTextures.GuiTexture( "guis/states.png" ) );
			this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;

			int uv_y = (int) Math.floor( 13 / 16 );
			int uv_x = 13 - uv_y * 16;

			this.drawTexturedModalRect( this.xPosition, this.yPosition, uv_x * 16, uv_y * 16, 22, 22 );

			if ( myIcon >= 0 )
			{
				uv_y = (int) Math.floor( myIcon / 16 );
				uv_x = myIcon - uv_y * 16;

				this.drawTexturedModalRect( this.xPosition + 3, this.yPosition + 3, uv_x * 16, uv_y * 16, 16, 16 );
			}

			this.mouseDragged( par1Minecraft, par2, par3 );

			if ( myItem != null )
			{
				this.zLevel = 100.0F;
				itemRenderer.zLevel = 100.0F;

				GL11.glEnable( GL11.GL_LIGHTING );
				GL11.glEnable( GL12.GL_RESCALE_NORMAL );
				RenderHelper.enableGUIStandardItemLighting();
				FontRenderer fontrenderer = par1Minecraft.fontRenderer;
				itemRenderer.renderItemAndEffectIntoGUI( fontrenderer, par1Minecraft.renderEngine, myItem, this.xPosition + 3, this.yPosition + 3 );
				GL11.glDisable( GL11.GL_LIGHTING );

				itemRenderer.zLevel = 0.0F;
				this.zLevel = 0.0F;
			}
		}
	}

	@Override
	public String getMsg()
	{
		return Msg;
	}

	@Override
	public int xPos()
	{
		return xPosition;
	}

	@Override
	public int yPos()
	{
		return yPosition;
	}

	@Override
	public int getWidth()
	{
		return 22;
	}

	@Override
	public int getHeight()
	{
		return 22;
	}

}
