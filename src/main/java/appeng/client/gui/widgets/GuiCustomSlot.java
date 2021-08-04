
package appeng.client.gui.widgets;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;


public abstract class GuiCustomSlot extends Gui implements ITooltip
{
	protected final int x;
	protected final int y;
	protected final int id;

	public GuiCustomSlot( final int id, final int x, final int y )
	{
		this.x = x;
		this.y = y;
		this.id = id;
	}

	public int getId()
	{
		return this.id;
	}

	public boolean canClick( final EntityPlayer player )
	{
		return true;
	}

	public void slotClicked( final ItemStack clickStack, final int mouseButton )
	{
	}

	public abstract void drawContent( final Minecraft mc, final int mouseX, final int mouseY, final float partialTicks );

	public void drawBackground( int guileft, int guitop )
	{
	}

	@Override
	public String getMessage()
	{
		return null;
	}

	@Override
	public int xPos()
	{
		return this.x;
	}

	@Override
	public int yPos()
	{
		return this.y;
	}

	@Override
	public int getWidth()
	{
		return 16;
	}

	@Override
	public int getHeight()
	{
		return 16;
	}

	@Override
	public boolean isVisible()
	{
		return false;
	}

	public boolean isSlotEnabled()
	{
		return true;
	}

}
