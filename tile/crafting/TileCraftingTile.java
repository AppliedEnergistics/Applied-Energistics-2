package appeng.tile.crafting;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.Actionable;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridHost;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.ISimplifiedBundle;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.CraftingCPUCalculator;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.AENetworkProxyMultiblock;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.util.Platform;
import appeng.util.item.ItemList;

public class TileCraftingTile extends AENetworkTile implements IAEMultiBlock, IPowerChannelState
{

	CraftingCPUCluster clust;
	final CraftingCPUCalculator calc = new CraftingCPUCalculator( this );
	public ISimplifiedBundle lightCache;

	public NBTTagCompound previousState = null;
	public boolean isCoreBlock = false;

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

	private class CraftingHandler extends AETileEventHandler
	{

		public CraftingHandler() {
			super( TileEventType.WORLD_NBT );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			data.setBoolean( "core", isCoreBlock );
			if ( isCoreBlock && clust != null )
				clust.writeToNBT( data );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			isCoreBlock = data.getBoolean( "core" );
			if ( isCoreBlock )
			{
				if ( clust != null )
					clust.readFromNBT( data );
				else
					previousState = (NBTTagCompound) data.copy();
			}
		}

	};

	public TileCraftingTile() {
		addNewHandler( new CraftingHandler() );
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
	public void disconnect(boolean update)
	{
		if ( clust != null )
		{
			clust.destroy();
			if ( update )
				updateMeta();
		}
	}

	@MENetworkEventSubscribe
	public void onPowerStateChage(MENetworkPowerStatusChange ev)
	{
		updateMeta();
	}

	@MENetworkEventSubscribe
	public void ChannelChangesd(MENetworkChannelsChanged ev)
	{
		updateMeta();
	}

	public void updateMeta()
	{
		if ( !gridProxy.isReady() )
			return;

		boolean formed = clust != null;
		boolean power = false;
		power = gridProxy.isActive();

		int current = worldObj.getBlockMetadata( xCoord, yCoord, zCoord );
		int newmeta = (current & 3) | (formed ? 8 : 0) | (power ? 4 : 0);

		if ( current != newmeta )
		{
			worldObj.setBlockMetadataWithNotify( xCoord, yCoord, zCoord, newmeta, 2 );

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

	@Override
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
		return (worldObj.getBlockMetadata( xCoord, yCoord, zCoord ) & 3) == 1;
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

	@Override
	public boolean isActive()
	{
		if ( Platform.isServer() )
			return gridProxy.isActive();
		return isPowered() && isFormed();
	}

	public void breakCluster()
	{
		if ( clust != null )
		{
			clust.cancel();
			IMEInventory<IAEItemStack> inv = clust.getInventory();

			LinkedList<WorldCoord> places = new LinkedList<WorldCoord>();

			Iterator<IGridHost> i = clust.getTiles();
			while (i.hasNext())
			{
				IGridHost h = i.next();
				if ( h == this )
					places.add( new WorldCoord( this ) );
				else
				{
					TileEntity te = (TileEntity) h;

					for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
					{
						WorldCoord wc = new WorldCoord( te );
						wc.add( d, 1 );
						if ( worldObj.isAirBlock( wc.x, wc.y, wc.z ) )
							places.add( wc );
					}

				}
			}

			Collections.shuffle( places );

			if ( places.isEmpty() )
				throw new RuntimeException( "No air or even the tile hat was destroyed?!?!" );

			for (IAEItemStack ais : inv.getAvailableItems( new ItemList<IAEItemStack>( IAEItemStack.class ) ))
			{
				ais = ais.copy();
				ais.setStackSize( ais.getItemStack().getMaxStackSize() );
				while (true)
				{
					IAEItemStack g = inv.extractItems( ais.copy(), Actionable.MODULATE, clust.getActionSource() );
					if ( g == null )
						break;

					WorldCoord wc = places.poll();
					places.add( wc );

					Platform.spawnDrops( worldObj, wc.x, wc.y, wc.z, Arrays.asList( g.getItemStack() ) );
				}

			}

			clust.destroy();
		}
	}
}
