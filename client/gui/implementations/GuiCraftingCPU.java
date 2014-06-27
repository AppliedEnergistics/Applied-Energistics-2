package appeng.client.gui.implementations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import appeng.api.AEApi;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ISortSource;
import appeng.container.implementations.ContainerCraftingCPU;
import appeng.core.localization.GuiText;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;

public class GuiCraftingCPU extends AEBaseGui implements ISortSource
{

	int rows = 6;

	IItemList<IAEItemStack> storage = AEApi.instance().storage().createItemList();
	IItemList<IAEItemStack> active = AEApi.instance().storage().createItemList();
	IItemList<IAEItemStack> pending = AEApi.instance().storage().createItemList();

	List<IAEItemStack> visual = new ArrayList();

	public GuiCraftingCPU(InventoryPlayer inventoryPlayer, TileCraftingTile te) {
		super( new ContainerCraftingCPU( inventoryPlayer, te ) );
		this.ySize = 153;
		this.xSize = 195;
		myScrollBar = new GuiScrollbar();
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );
	}

	@Override
	public void initGui()
	{
		super.initGui();
	}

	private long getTotal(IAEItemStack is)
	{
		IAEItemStack a = storage.findPrecise( is );
		IAEItemStack b = active.findPrecise( is );
		IAEItemStack c = pending.findPrecise( is );

		long total = 0;

		if ( a != null )
			total += a.getStackSize();

		if ( b != null )
			total += b.getStackSize();

		if ( c != null )
			total += c.getStackSize();

		return total;
	}

	public void postUpdate(List<IAEItemStack> list, byte ref)
	{
		switch (ref)
		{
		case 0:
			for (IAEItemStack l : list)
				handleInput( storage, l );
			break;

		case 1:
			for (IAEItemStack l : list)
				handleInput( active, l );
			break;

		case 2:
			for (IAEItemStack l : list)
				handleInput( pending, l );
			break;
		}

		for (IAEItemStack l : list)
		{
			long amt = getTotal( l );

			if ( amt <= 0 )
				deleteVisualStack( l );
			else
			{
				IAEItemStack is = findVisualStack( l );
				is.setStackSize( amt );
			}
		}

		setScrollBar();
	}

	private void handleInput(IItemList<IAEItemStack> s, IAEItemStack l)
	{
		IAEItemStack a = s.findPrecise( l );

		if ( l.getStackSize() <= 0 )
		{
			if ( a != null )
				a.reset();
		}
		else
		{
			if ( a == null )
			{
				s.add( l.copy() );
				a = s.findPrecise( l );
			}

			if ( a != null )
				a.setStackSize( l.getStackSize() );
		}
	}

	private IAEItemStack findVisualStack(IAEItemStack l)
	{
		Iterator<IAEItemStack> i = visual.iterator();
		while (i.hasNext())
		{
			IAEItemStack o = i.next();
			if ( o.equals( l ) )
				return o;
		}

		IAEItemStack stack = l.copy();
		visual.add( stack );
		return stack;
	}

	private void deleteVisualStack(IAEItemStack l)
	{
		Iterator<IAEItemStack> i = visual.iterator();
		while (i.hasNext())
		{
			IAEItemStack o = i.next();
			if ( o.equals( l ) )
			{
				i.remove();
				return;
			}
		}
	}

	private void setScrollBar()
	{
		int size = 0;
		for (IAEItemStack l : visual)
			size++;

		myScrollBar.setTop( 39 ).setLeft( 175 ).setHeight( 78 );
		myScrollBar.setRange( 0, (size + 4) / 5 - rows, 1 );
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/craftingcpu.png" );
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
		fontRendererObj.drawString( GuiText.NetworkDetails.getLocal(), 8, 7, 4210752 );

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

		for (int z = viewStart; z < Math.min( viewEnd, visual.size() ); z++)
		{
			IAEItemStack refStack = visual.get( z );// repo.getRefrenceItem( z );
			if ( refStack != null )
			{
				GL11.glPushMatrix();
				GL11.glScaled( 0.5, 0.5, 0.5 );

				String str = Long.toString( refStack.getStackSize() );
				if ( refStack.getStackSize() >= 10000 )
					str = Long.toString( refStack.getStackSize() / 1000 ) + "k";

				int w = fontRendererObj.getStringWidth( str );
				fontRendererObj.drawString( str, (int) ((x * sectionLength + xo + sectionLength - 19 - ((float) w * 0.5)) * 2), (int) ((y * 18 + yo + 6) * 2),
						4210752 );

				GL11.glPopMatrix();
				int posX = x * sectionLength + xo + sectionLength - 18;
				int posY = y * 18 + yo;

				ItemStack is = refStack.copy().getItemStack();

				if ( tooltip == z - viewStart )
				{
					ToolTip = Platform.getItemDisplayName( is );

					ToolTip = ToolTip + ("\n" + GuiText.Installed.getLocal() + ": " + (refStack.getStackSize()));
					if ( refStack.getCountRequestable() > 0 )
						ToolTip = ToolTip + ("\n" + GuiText.EnergyDrain.getLocal() + ": " + Platform.formatPowerLong( refStack.getCountRequestable(), true ));

					toolPosX = x * sectionLength + xo + sectionLength - 8;
					toolPosY = y * 18 + yo;
				}

				drawItem( posX, posY, is );

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

	@Override
	public Enum getSortDisplay()
	{
		return ViewItems.ALL;
	}
}
