package appeng.client.gui.implementations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.core.localization.GuiText;
import appeng.parts.reporting.PartMonitor;
import appeng.tile.inventory.AppEngInternalInventory;

import com.google.common.collect.HashMultimap;

public class GuiInterfaceTerminal extends AEBaseGui
{

	class ClientFakeInv
	{

		String unlocalizedName;
		long id;

		AppEngInternalInventory inv = new AppEngInternalInventory( null, 9 );

		public String getName()
		{
			return StatCollector.translateToLocal( unlocalizedName + ".name" );
		}

	};

	HashMap<Long, ClientFakeInv> byId = new HashMap();
	HashMultimap<String, ClientFakeInv> byName = HashMultimap.create();
	ArrayList<String> names = new ArrayList();

	public GuiInterfaceTerminal(InventoryPlayer inventoryPlayer, PartMonitor te) {
		super( new ContainerInterfaceTerminal( inventoryPlayer, te ) );
		xSize = 195;
		ySize = 222;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/interfaceterminal.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( getGuiDisplayName( GuiText.InterfaceTerminal.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		int offset = 0;

		for (String name : names)
		{
			fontRendererObj.drawString( name, 8, 30 + offset, 4210752 );
			offset += 18;
		}
	}

	boolean refreshList = false;

	public void postUpdate(NBTTagCompound in)
	{
		if ( in.getBoolean( "clear" ) )
			byId.clear();

		for (Object oKey : in.func_150296_c())
		{
			String key = (String) oKey;
			if ( key.startsWith( "=" ) )
			{
				try
				{
					long id = Long.parseLong( key.substring( 1 ), Character.MAX_RADIX );
					NBTTagCompound invData = in.getCompoundTag( key );
					ClientFakeInv current = getById( id );
					current.unlocalizedName = invData.getString( "un" );

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
			byName.clear();
			for (ClientFakeInv o : byId.values())
				byName.put( o.getName(), o );

			names.clear();
			names.addAll( byName.keySet() );

			Collections.sort( names );
		}
	}

	private ClientFakeInv getById(long id)
	{
		ClientFakeInv o = byId.get( id );

		if ( o == null )
		{
			byId.put( id, o = new ClientFakeInv() );
			refreshList = true;
		}

		return o;
	}
}
