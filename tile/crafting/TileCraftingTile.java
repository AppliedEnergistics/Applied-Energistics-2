package appeng.tile.crafting;

import java.util.EnumSet;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.ISimplifiedBundle;
import appeng.me.GridAccessException;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.CraftingCPUCalculator;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.AENetworkProxyMultiblock;
import appeng.tile.grid.AENetworkTile;

public class TileCraftingTile extends AENetworkTile implements IAEMultiBlock
{

	CraftingCPUCluster clust;
	final CraftingCPUCalculator calc = new CraftingCPUCalculator( this );
	public ISimplifiedBundle lightCache;

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
	}

	@Override
	public void onReady()
	{
		super.onReady();
		updateMultiBlock();
	}

	@Override
	public boolean canBeRotated()
	{
		return true;// return BlockCraftingUnit.checkType( worldObj.getBlockMetadata( xCoord, yCoord, zCoord ),
					// BlockCraftingUnit.BASE_MONITOR );
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

	@MENetworkEventSubscribe
	public void onPowerStateChage(MENetworkPowerStatusChange ev)
	{
		updateMeta();
	}

	public void updateMeta()
	{
		boolean formed = clust != null;
		boolean power = false;
		try
		{
			power = gridProxy.getEnergy().isNetworkPowered();
		}
		catch (GridAccessException e)
		{
			// ;P
		}

		int current = worldObj.getBlockMetadata( xCoord, yCoord, zCoord );
		int newmeta = (current & 3) | (formed ? 8 : 0) | (power ? 4 : 0);

		if ( current != newmeta )
		{
			worldObj.setBlockMetadataWithNotify( xCoord, yCoord, zCoord, newmeta, 3 );

			if ( isFormed() )
				gridProxy.setValidSides( EnumSet.allOf( ForgeDirection.class ) );
			else
				gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		}
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

	public boolean isPowered()
	{
		return (worldObj.getBlockMetadata( xCoord, yCoord, zCoord ) & 4) == 4;
	}

	public boolean isFormed()
	{
		return (worldObj.getBlockMetadata( xCoord, yCoord, zCoord ) & 8) == 8;
	}

	public boolean isAccelerator()
	{
		return worldObj.getBlockMetadata( xCoord, yCoord, zCoord ) == 1;
	}

	public boolean isStatus()
	{
		return false;
	}

	public boolean isStorage()
	{
		return false;
	}

	public int getStorageBytes()
	{
		return 0;
	}

}
