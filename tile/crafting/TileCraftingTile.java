package appeng.tile.crafting;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.networking.GridFlags;
import appeng.block.crafting.BlockCraftingUnit;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.CraftingCPUCalculator;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.AENetworkProxyMultiblock;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;

public class TileCraftingTile extends AENetworkTile implements IAEMultiBlock
{

	private long storageBytes = 0;
	CraftingCPUCluster clust;
	final CraftingCPUCalculator calc = new CraftingCPUCalculator( this );

	@Override
	protected AENetworkProxy createProxy()
	{
		return new AENetworkProxyMultiblock( this, "proxy", getItemFromTile( this ), true );
	}

	public void updateStatus(CraftingCPUCluster c)
	{
		clust = c;
		updateMeta();
	}

	public void updateMultiBlock()
	{
		calc.calculateMultiblock( worldObj, getLocation() );
	}

	public TileCraftingTile() {
		gridProxy.setFlags( GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL );
		gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
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
	public void onReady()
	{
		super.onReady();
		updateMultiBlock();
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
		if ( clust != null )
		{
			clust.destroy();
			updateMeta();
		}
	}

	public void updateMeta()
	{
		boolean formed = clust != null;
		int current = worldObj.getBlockMetadata( xCoord, yCoord, zCoord );
		int newmeta = (current & 7) | (formed ? 8 : 0);

		if ( current != newmeta )
			worldObj.setBlockMetadataWithNotify( xCoord, yCoord, zCoord, newmeta, 3 );

		if ( isFormed() )
			gridProxy.setValidSides( EnumSet.allOf( ForgeDirection.class ) );
		else
			gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
	}

	private void dropAndBreak()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public IAECluster getCluster()
	{
		return clust;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	public long getStorageBytes()
	{
		return storageBytes;
	}

	public boolean isFormed()
	{
		return (worldObj.getBlockMetadata( xCoord, yCoord, zCoord ) & 8) == 8;
	}

	public boolean isStorage()
	{
		return BlockCraftingUnit.checkType( worldObj.getBlockMetadata( xCoord, yCoord, zCoord ), BlockCraftingUnit.BASE_STORAGE );
	}

	public boolean isStatus()
	{
		return BlockCraftingUnit.checkType( worldObj.getBlockMetadata( xCoord, yCoord, zCoord ), BlockCraftingUnit.BASE_MONITOR );
	}

	public boolean isAccelerator()
	{
		return BlockCraftingUnit.checkType( worldObj.getBlockMetadata( xCoord, yCoord, zCoord ), BlockCraftingUnit.BASE_ACCELERATOR );
	}

}
