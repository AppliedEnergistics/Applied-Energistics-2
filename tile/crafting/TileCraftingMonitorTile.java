package appeng.tile.crafting;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import appeng.api.storage.data.IAEItemStack;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileCraftingMonitorTile extends TileCraftingTile
{

	@SideOnly(Side.CLIENT)
	public Integer dspList;

	@SideOnly(Side.CLIENT)
	public boolean updateList;

	IAEItemStack dspPlay;

	class CraftingMonitorHandler extends AETileEventHandler
	{

		public CraftingMonitorHandler() {
			super( TileEventType.NETWORK );
		}

		@Override
		public boolean readFromStream(ByteBuf data) throws IOException
		{
			boolean hasItem = data.readBoolean();

			if ( hasItem )
				dspPlay = AEItemStack.loadItemStackFromPacket( data );
			else
				dspPlay = null;

			updateList = true;
			return false; // tesr!
		}

		@Override
		public void writeToStream(ByteBuf data) throws IOException
		{
			if ( dspPlay == null )
				data.writeBoolean( false );
			else
			{
				data.writeBoolean( true );
				dspPlay.writeToPacket( data );
			}
		}
	};

	public TileCraftingMonitorTile() {
		addNewHandler( new CraftingMonitorHandler() );
	}

	public boolean isAccelerator()
	{
		return false;
	}

	public boolean isStatus()
	{
		return true;
	}

	public void setJob(IAEItemStack is)
	{
		if ( (is == null) != (dspPlay == null) )
		{
			dspPlay = is == null ? null : is.copy();
			markForUpdate();
		}
		else if ( is != null && dspPlay != null )
		{
			if ( is.getStackSize() != dspPlay.getStackSize() )
			{
				dspPlay = is == null ? null : is.copy();
				markForUpdate();
			}
		}
	}

	public IAEItemStack getJobProgress()
	{
		return dspPlay;// AEItemStack.create( new ItemStack( Items.diamond, 64 ) );
	}

	@Override
	public boolean requiresTESR()
	{
		return getJobProgress() != null;
	}

}
