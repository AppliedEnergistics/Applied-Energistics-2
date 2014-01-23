package appeng.client.gui.implementations;

import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.me.ItemRepo;
import appeng.client.me.SlotME;
import appeng.container.implementations.ContainerNetworkStatus;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class GuiNetworkStatus extends AEBaseGui
{

	ItemRepo repo;

	int rows = 4;

	public GuiNetworkStatus(InventoryPlayer inventoryPlayer, INetworkTool te) {
		super( new ContainerNetworkStatus( inventoryPlayer, te ) );
		this.ySize = 125;
		this.xSize = 195;
		myScrollBar = new GuiScrollbar();
		repo = new ItemRepo( myScrollBar );
	}

	public void postUpdate(List<IAEItemStack> list)
	{
		repo.clear();

		for (IAEItemStack is : list)
			repo.postUpdate( is );

		repo.updateView();
		setScrollBar();
	}

	private void setScrollBar()
	{
		myScrollBar.setTop( 39 ).setLeft( 175 ).setHeight( 78 );
		myScrollBar.setRange( 0, (repo.size() + 8) / 5 - rows, 2 );
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/networkstatus.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	int tooltip = -1;

	@Override
	public void drawScreen(int mouse_x, int mouse_y, float btn)
	{
		int x = 0;
		int y = 0;

		int gx = (width - xSize) / 2;
		int gy = (height - ySize) / 2;

		tooltip = -1;

		for (int z = 0; z <= 4 * 5; z++)
		{
			int minX = gx + 14 + x * 31;
			int minY = gy + 41 + y * 22;

			if ( minX < mouse_x && minX + 28 > mouse_x )
			{
				if ( minY < mouse_y && minY + 20 > mouse_y )
				{
					tooltip = z;
					break;
				}

			}

			x++;

			if ( x > 4 )
			{
				y++;
				x = 0;
			}
		}

		super.drawScreen( mouse_x, mouse_y, btn );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		ContainerNetworkStatus ns = (ContainerNetworkStatus) inventorySlots;

		fontRenderer.drawString( GuiText.NetworkDetails.getLocal(), 8, 6, 4210752 );

		fontRenderer.drawString( GuiText.PowerInputRate.getLocal() + ": " + formatPowerLong( ns.avgAddition ), 8, 16, 4210752 );
		fontRenderer.drawString( GuiText.PowerUsageRate.getLocal() + ": " + formatPowerLong( ns.powerUsage ), 8, 26, 4210752 );

		int sectionLength = 30;

		int x = 0;
		int y = 0;
		int xo = 0 + 12;
		int yo = 0 + 42;
		int viewStart = myScrollBar.getCurrentScroll() * 5;
		int viewEnd = viewStart + 5 * 4;

		int pn = repo.size();
		int rows = pn / 5 + ((pn % 5) > 0 ? 1 : 0);
		// updateScrollRegion( rows - 4 > 0 ? rows - 4 : 0 );

		for (int z = viewStart; z < Math.min( viewEnd, repo.size() ); z++)
		{
			GL11.glPushMatrix();
			GL11.glScaled( 0.5, 0.5, 0.5 );

			IAEItemStack refStack = repo.getRefrenceItem( z );

			String str = Long.toString( refStack.getStackSize() );
			if ( repo.getRefrenceItem( z ).getStackSize() >= 10000 )
				str = Long.toString( refStack.getStackSize() / 1000 ) + StatCollector.translateToLocal( "AppEng.Sizes.1000" );

			int w = fontRenderer.getStringWidth( str );
			fontRenderer
					.drawString( str, (int) ((x * sectionLength + xo + sectionLength - 19 - ((float) w * 0.5)) * 2), (int) ((y * 18 + yo + 6) * 2), 4210752 );
			GL11.glPopMatrix();

			int posX = x * sectionLength + xo + sectionLength - 18;
			int posY = y * 18 + yo;

			drawItem( posX, posY, repo.getItem( z ) );

			x++;

			if ( x > 4 )
			{
				y++;
				x = 0;
			}
		}

		x = 0;
		y = 0;
		for (int z = viewStart; z < Math.min( viewEnd, repo.size() ); z++)
		{
			if ( tooltip == z - viewStart )
			{
				IAEItemStack refStack = repo.getRefrenceItem( z );
				GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
				String out = Platform.getItemDisplayName( repo.getItem( z ) );

				out = out + ("\n" + GuiText.Installed.getLocal() + ": " + (refStack.getStackSize()));
				out = out + ("\n" + GuiText.EnergyDrain.getLocal() + ": " + formatPowerLong( refStack.getCountRequestable() ));

				int posX = x * sectionLength + xo + sectionLength - 8;
				int posY = y * 18 + yo;

				drawTooltip( posX, posY + 10, 0, out );
				GL11.glPopAttrib();
			}
			x++;

			if ( x > 4 )
			{
				y++;
				x = 0;
			}
		}
	}

	private String formatPowerLong(long n)
	{
		double p = ((double) n) / 100;
		return Double.toString( p ) + " ae/t";
	}

	// @Override - NEI
	public List<String> handleItemTooltip(ItemStack stack, int mousex, int mousey, List<String> currenttip)
	{
		if ( stack != null )
		{
			Slot s = getSlot( mousex, mousey );
			if ( s instanceof SlotME )
			{
				IAEItemStack myStack = null;

				try
				{
					SlotME theSlotField = (SlotME) s;
					myStack = theSlotField.getAEStack();
				}
				catch (Throwable _)
				{
				}

				if ( myStack != null )
				{
					while (currenttip.size() > 1)
						currenttip.remove( 1 );

				}
			}
		}
		return currenttip;
	}

	// Vanillia version...
	@Override
	protected void drawItemStackTooltip(ItemStack stack, int x, int y)
	{
		Slot s = getSlot( x, y );
		if ( s instanceof SlotME && stack != null )
		{
			IAEItemStack myStack = null;

			try
			{
				SlotME theSlotField = (SlotME) s;
				myStack = theSlotField.getAEStack();
			}
			catch (Throwable _)
			{
			}

			if ( myStack != null )
			{
				List currenttip = stack.getTooltip( this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips );

				while (currenttip.size() > 1)
					currenttip.remove( 1 );

				currenttip.add( GuiText.Installed.getLocal() + ": " + (myStack.getStackSize()) );
				currenttip.add( GuiText.EnergyDrain.getLocal() + ": " + formatPowerLong( myStack.getCountRequestable() ) );

				drawTooltip( x, y, 0, join( currenttip, "\n" ) );
			}
		}
		super.drawItemStackTooltip( stack, x, y );
	}
}
