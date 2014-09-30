package appeng.client.gui.implementations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.lwjgl.opengl.GL11;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.me.ClientDCInternalInv;
import appeng.client.me.SlotDisconnected;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.core.localization.GuiText;
import appeng.parts.reporting.PartMonitor;

import com.google.common.collect.HashMultimap;

public class GuiInterfaceTerminal extends AEBaseGui
{

	private static final int LINES_ON_PAGE = 6;

	// TODO: copied from GuiMEMonitorable. It looks not changed, maybe unneeded?
	int offsetX = 9;

	final HashMap<Long, ClientDCInternalInv> byId = new HashMap<Long, ClientDCInternalInv>();
	final HashMultimap<String, ClientDCInternalInv> byName = HashMultimap.create();
	final ArrayList<String> names = new ArrayList<String>();

	ArrayList<Object> lines = new ArrayList<Object>();
	LinkedList<SlotDisconnected> dcSlots = new LinkedList<SlotDisconnected>();
	private MEGuiTextField searchField;

	private int getMaxRows()
	{
		return names.size() + byId.size();// unique names, and each inv row.
	}

	public GuiInterfaceTerminal(InventoryPlayer inventoryPlayer, PartMonitor te)
	{
		super( new ContainerInterfaceTerminal( inventoryPlayer, te ) );
		myScrollBar = new GuiScrollbar();
		xSize = 195;
		ySize = 222;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		myScrollBar.setLeft( 175 );
		myScrollBar.setHeight( 106 );
		myScrollBar.setTop( 18 );

		searchField = new MEGuiTextField( fontRendererObj, this.guiLeft + Math.max( 107, offsetX ), this.guiTop + 6, 64, fontRendererObj.FONT_HEIGHT );
		searchField.setEnableBackgroundDrawing( false );
		searchField.setMaxStringLength( 25 );
		searchField.setTextColor( 0xFFFFFF );
		searchField.setVisible( true );
	}

	@Override
	protected void mouseClicked(int xCoord, int yCoord, int btn)
	{
		searchField.mouseClicked( xCoord, yCoord, btn );

		if ( btn == 1 && searchField.isMouseIn( xCoord, yCoord ) )
		{
			searchField.setText( "" );
			refreshList();
		}

		super.mouseClicked( xCoord, yCoord, btn );
	}

	@Override
	protected void keyTyped(char character, int key)
	{
		if ( !this.checkHotbarKeys( key ) )
		{
			if ( character == ' ' && this.searchField.getText().length() == 0 )
				return;

			if ( searchField.textboxKeyTyped( character, key ) )
			{
				refreshList();
			}
			else
			{
				super.keyTyped( character, key );
			}
		}
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/interfaceterminal.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );

		int offset = 17;
		int ex = myScrollBar.getCurrentScroll();

		for (int x = 0; x < LINES_ON_PAGE && ex + x < lines.size(); x++)
		{
			Object lineObj = lines.get( ex + x );
			if ( lineObj instanceof ClientDCInternalInv )
			{
				ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;

				GL11.glColor4f( 1, 1, 1, 1 );
				for (int z = 0; z < inv.inv.getSizeInventory(); z++)
					this.drawTexturedModalRect( offsetX + z * 18 + 7, offsetY + offset, 7, 139, 18, 18 );
			}
			offset += 18;
		}

		if ( searchField != null )
			searchField.drawTextBox();
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( getGuiDisplayName( GuiText.InterfaceTerminal.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		int offset = 17;
		int ex = myScrollBar.getCurrentScroll();

		Iterator<Object> o = inventorySlots.inventorySlots.iterator();
		while (o.hasNext())
		{
			if ( o.next() instanceof SlotDisconnected )
				o.remove();
		}

		for (int x = 0; x < LINES_ON_PAGE && ex + x < lines.size(); x++)
		{
			Object lineObj = lines.get( ex + x );
			if ( lineObj instanceof ClientDCInternalInv )
			{
				ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;
				for (int z = 0; z < inv.inv.getSizeInventory(); z++)
				{
					inventorySlots.inventorySlots.add( new SlotDisconnected( inv, z, z * 18 + 8, 1 + offset ) );
				}
			}
			else if ( lineObj instanceof String )
			{
				String name = (String) lineObj;
				int rows = byName.get( name ).size();
				if ( rows > 1 )
					name = name + " (" + rows + ")";

				while (name.length() > 2 && fontRendererObj.getStringWidth( name ) > 155)
					name = name.substring( 0, name.length() - 1 );

				fontRendererObj.drawString( name, 10, 6 + offset, 4210752 );
			}
			offset += 18;
		}
	}

	boolean refreshList = false;

	public void postUpdate(NBTTagCompound in)
	{
		if ( in.getBoolean( "clear" ) )
		{
			byId.clear();
			refreshList = true;
		}

		for (Object oKey : in.func_150296_c())
		{
			String key = (String) oKey;
			if ( key.startsWith( "=" ) )
			{
				try
				{
					long id = Long.parseLong( key.substring( 1 ), Character.MAX_RADIX );
					NBTTagCompound invData = in.getCompoundTag( key );
					ClientDCInternalInv current = getById( id, invData.getLong( "sortBy" ), invData.getString( "un" ) );

					for (int x = 0; x < current.inv.getSizeInventory(); x++)
					{
						String which = Integer.toString( x );
						if ( invData.hasKey( which ) )
							current.inv.setInventorySlotContents( x, ItemStack.loadItemStackFromNBT( invData.getCompoundTag( which ) ) );
					}
				}
				catch (NumberFormatException ex)
				{
				}
			}
		}

		if ( refreshList )
		{
			refreshList = false;
			refreshList();
		}
	}

	private void refreshList()
	{
		byName.clear();
		String searchFilterLowerCase = searchField.getText().toLowerCase();
		String itemNameLowerCase;

		for (ClientDCInternalInv entry : byId.values())
		{
			itemNameLowerCase = entry.getName().toLowerCase();
			if ( !searchFilterLowerCase.equals( "" ) && !itemNameLowerCase.contains( searchFilterLowerCase ) )
				continue;
			byName.put( entry.getName(), entry );
		}

		names.clear();
		names.addAll( byName.keySet() );

		Collections.sort( names );

		lines = new ArrayList<Object>( getMaxRows() );
		for (String n : names)
		{
			lines.add( n );

			ArrayList<ClientDCInternalInv> clientInventories = new ArrayList<ClientDCInternalInv>();
			clientInventories.addAll( byName.get( n ) );

			Collections.sort( clientInventories );
			lines.addAll( clientInventories );
		}

		myScrollBar.setRange( 0, lines.size() - LINES_ON_PAGE, 2 );
	}

	private ClientDCInternalInv getById(long id, long sortBy, String string)
	{
		ClientDCInternalInv o = byId.get( id );

		if ( o == null )
		{
			byId.put( id, o = new ClientDCInternalInv( 9, id, sortBy, string ) );
			refreshList = true;
		}

		return o;
	}
}
