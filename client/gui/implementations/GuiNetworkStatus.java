package appeng.client.gui.implementations;

import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.ItemRepo;
import appeng.client.me.SlotME;
import appeng.container.implementations.ContainerNetworkStatus;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class GuiNetworkStatus extends AEBaseGui implements ISortSource
{

	ItemRepo repo;

	int rows = 4;

	public GuiNetworkStatus(InventoryPlayer inventoryPlayer, INetworkTool te) {
		super( new ContainerNetworkStatus( inventoryPlayer, te ) );
		this.ySize = 125;
		this.xSize = 195;
		myScrollBar = new GuiScrollbar();
		repo = new ItemRepo( myScrollBar, this );
		repo.rowSize = 5;
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
		int size = repo.size();
		myScrollBar.setTop( 39 ).setLeft( 175 ).setHeight( 78 );
		myScrollBar.setRange( 0, (size + 4) / 5 - rows, 1 );
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
			int minY = gy + 41 + y * 18;

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

		fontRendererObj.drawString( GuiText.NetworkDetails.getLocal(), 8, 6, 4210752 );

		fontRendererObj.drawString( GuiText.PowerInputRate.getLocal() + ": " + formatPowerLong( ns.avgAddition ), 8, 16, 4210752 );
		fontRendererObj.drawString( GuiText.PowerUsageRate.getLocal() + ": " + formatPowerLong( ns.powerUsage ), 8, 26, 4210752 );

		int sectionLength = 30;

		int x = 0;
		int y = 0;
		int xo = 0 + 12;
		int yo = 0 + 42;
		int viewStart = 0;// myScrollBar.getCurrentScroll() * 5;
		int viewEnd = viewStart + 5 * 4;

		String ToolTip = "";
		int toolPosX = 0;
		int toolPosY = 0;

		for (int z = viewStart; z < Math.min( viewEnd, repo.size() ); z++)
		{
			IAEItemStack refStack = repo.getRefrenceItem( z );
			if ( refStack != null )
			{
				GL11.glPushMatrix();
				GL11.glScaled( 0.5, 0.5, 0.5 );

				String str = Long.toString( refStack.getStackSize() );
				if ( refStack.getStackSize() >= 10000 )
					str = Long.toString( refStack.getStackSize() / 1000 ) + StatCollector.translateToLocal( "AppEng.Sizes.1000" );

				int w = fontRendererObj.getStringWidth( str );
				fontRendererObj.drawString( str, (int) ((x * sectionLength + xo + sectionLength - 19 - ((float) w * 0.5)) * 2), (int) ((y * 18 + yo + 6) * 2),
						4210752 );

				GL11.glPopMatrix();
				int posX = x * sectionLength + xo + sectionLength - 18;
				int posY = y * 18 + yo;

				if ( tooltip == z - viewStart )
				{
					ToolTip = Platform.getItemDisplayName( repo.getItem( z ) );

					ToolTip = ToolTip + ("\n" + GuiText.Installed.getLocal() + ": " + (refStack.getStackSize()));
					ToolTip = ToolTip + ("\n" + GuiText.EnergyDrain.getLocal() + ": " + formatPowerLong( refStack.getCountRequestable() ));

					toolPosX = x * sectionLength + xo + sectionLength - 8;
					toolPosY = y * 18 + yo;
				}

				drawItem( posX, posY, repo.getItem( z ) );

				x++;

				if ( x > 4 )
				{
					y++;
					x = 0;
				}
			}

		}

		if ( tooltip >= 0 && ToolTip.length() > 0 )
		{
			GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
			drawTooltip( toolPosX, toolPosY + 10, 0, ToolTip );
			GL11.glPopAttrib();
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
		// super.drawItemStackTooltip( stack, x, y );
	}

	@Override
	public Enum getSortBy()
	{
		return SortOrder.NAME;
	}

	@Override
	public Enum getSortDir()
	{
		return SortDir.ASCENDING;
	}
}
