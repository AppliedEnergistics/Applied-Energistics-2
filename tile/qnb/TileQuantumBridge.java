package appeng.tile.qnb;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.QuantumCluster;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class TileQuantumBridge extends AENetworkPowerTile implements IAEMultiBlock
{

	final int sidesRing[] = new int[] {};
	final int sidesLink[] = new int[] { 0 };

	AppEngInternalInventory inv = new AppEngInternalInventory( this, 1 );

	final byte start = 8 + 7;
	public final byte corner = 16;
	final byte hasSingularity = 32;
	final byte powered = 64;

	// private QuantumCalculator calc = new QuantumCalculator( this );
	int oldxdex = -1;
	byte xdex = -1;

	QuantumCluster clust;
	public boolean bridgePowered;

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{

	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if ( isCenter() )
			return sidesLink;
		return sidesRing;
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

	public void updateStatus(QuantumCluster c, byte fish)
	{
		// TODO Auto-generated method stub

	}

	public long getQEDest()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isCenter()
	{
		return getBlockType() == AEApi.instance().blocks().blockQuantumLink.block();
	}

	public boolean isCorner()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPowered()
	{
		if ( Platform.isClient() )
			return (xdex & powered) == powered;

		return false;
	}

	public boolean isFormed()
	{
		return xdex != -1;
	}

}
