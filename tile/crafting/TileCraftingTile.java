package appeng.tile.crafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.AEApi;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.tile.AEBaseTile;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;

public class TileCraftingTile extends AEBaseTile implements IAEMultiBlock
{

	private long storageBytes = 0;

	public TileCraftingTile() {
		addNewHandler( new AETileEventHandler( TileEventType.NETWORK, TileEventType.WORLD_NBT ) {

			public void writeToNBT(NBTTagCompound data)
			{
				if ( storageBytes > 0 )
					data.setLong( "bytes", storageBytes );
			}

			public void readFromNBT(NBTTagCompound data)
			{
				storageBytes = data.getLong( "bytes" );
			}

			public boolean readFromStream(io.netty.buffer.ByteBuf data) throws java.io.IOException
			{
				storageBytes = data.readLong();
				return false;
			}

			public void writeToStream(io.netty.buffer.ByteBuf data) throws java.io.IOException
			{
				data.writeLong( storageBytes );
			}

		} );
	}

	@Override
	public void onPlacement(ItemStack stack, EntityPlayer player, int side)
	{
		if ( AEApi.instance().blocks().blockCraftingStorage.sameAsStack( stack ) && stack.hasTagCompound() )
		{
			NBTTagCompound data = stack.getTagCompound();
			storageBytes = data.getLong( "bytes" );
		}
	}

	@Override
	public void disconnect()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public IAECluster getCluster()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public long getStorageBytes()
	{
		return storageBytes;
	}

}
