/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.tile.crafting;


import appeng.api.AEApi;
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
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.util.Platform;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;


public class TileCraftingTile extends AENetworkTile implements IAEMultiBlock, IPowerChannelState
{
	private final CraftingCPUCalculator calc = new CraftingCPUCalculator( this );

	private ISimplifiedBundle lightCache;

	private NBTTagCompound previousState = null;
	private boolean isCoreBlock = false;
	private CraftingCPUCluster cluster;

	public TileCraftingTile()
	{
		this.getProxy().setFlags( GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL );
		this.getProxy().setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
	}

	@Override
	protected AENetworkProxy createProxy()
	{
		return new AENetworkProxyMultiblock( this, "proxy", this.getItemFromTile( this ), true );
	}

	@Override
	protected ItemStack getItemFromTile( final Object obj )
	{
		if( ( (TileCraftingTile) obj ).isAccelerator() )
		{
			for( final ItemStack accelerator : AEApi.instance().definitions().blocks().craftingAccelerator().maybeStack( 1 ).asSet() )
			{
				return accelerator;
			}
		}

		return super.getItemFromTile( obj );
	}

	@Override
	public boolean canBeRotated()
	{
		return true;// return BlockCraftingUnit.checkType( worldObj.getBlockMetadata( xCoord, yCoord, zCoord ),
		// BlockCraftingUnit.BASE_MONITOR );
	}

	@Override
	public void setName( final String name )
	{
		super.setName( name );
		if( this.cluster != null )
		{
			this.cluster.updateName();
		}
	}

	public boolean isAccelerator()
	{
		if( this.worldObj == null )
		{
			return false;
		}
		return ( this.worldObj.getBlockMetadata( this.xCoord, this.yCoord, this.zCoord ) & 3 ) == 1;
	}

	@Override
	public void onReady()
	{
		super.onReady();
		this.getProxy().setVisualRepresentation( this.getItemFromTile( this ) );
		this.updateMultiBlock();
	}

	public void updateMultiBlock()
	{
		this.calc.calculateMultiblock( this.worldObj, this.getLocation() );
	}

	public void updateStatus( final CraftingCPUCluster c )
	{
		if( this.cluster != null && this.cluster != c )
		{
			this.cluster.breakCluster();
		}

		this.cluster = c;
		this.updateMeta( true );
	}

	public void updateMeta( final boolean updateFormed )
	{
		if( this.worldObj == null || this.notLoaded() )
		{
			return;
		}

		final boolean formed = this.isFormed();
		boolean power = false;

		if( this.getProxy().isReady() )
		{
			power = this.getProxy().isActive();
		}

		final int current = this.worldObj.getBlockMetadata( this.xCoord, this.yCoord, this.zCoord );
		final int newMeta = ( current & 3 ) | ( formed ? 8 : 0 ) | ( power ? 4 : 0 );

		if( current != newMeta )
		{
			this.worldObj.setBlockMetadataWithNotify( this.xCoord, this.yCoord, this.zCoord, newMeta, 2 );
		}

		if( updateFormed )
		{
			if( formed )
			{
				this.getProxy().setValidSides( EnumSet.allOf( ForgeDirection.class ) );
			}
			else
			{
				this.getProxy().setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
			}
		}
	}

	public boolean isFormed()
	{
		if( Platform.isClient() )
		{
			return ( this.worldObj.getBlockMetadata( this.xCoord, this.yCoord, this.zCoord ) & 8 ) == 8;
		}
		return this.cluster != null;
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileCraftingTile( final NBTTagCompound data )
	{
		data.setBoolean( "core", this.isCoreBlock() );
		if( this.isCoreBlock() && this.cluster != null )
		{
			this.cluster.writeToNBT( data );
		}
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileCraftingTile( final NBTTagCompound data )
	{
		this.setCoreBlock( data.getBoolean( "core" ) );
		if( this.isCoreBlock() )
		{
			if( this.cluster != null )
			{
				this.cluster.readFromNBT( data );
			}
			else
			{
				this.setPreviousState( (NBTTagCompound) data.copy() );
			}
		}
	}

	@Override
	public void disconnect( final boolean update )
	{
		if( this.cluster != null )
		{
			this.cluster.destroy();
			if( update )
			{
				this.updateMeta( true );
			}
		}
	}

	@Override
	public IAECluster getCluster()
	{
		return this.cluster;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@MENetworkEventSubscribe
	public void onPowerStateChange( final MENetworkChannelsChanged ev )
	{
		this.updateMeta( false );
	}

	@MENetworkEventSubscribe
	public void onPowerStateChange( final MENetworkPowerStatusChange ev )
	{
		this.updateMeta( false );
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

	public void breakCluster()
	{
		if( this.cluster != null )
		{
			this.cluster.cancel();
			final IMEInventory<IAEItemStack> inv = this.cluster.getInventory();

			final LinkedList<WorldCoord> places = new LinkedList<WorldCoord>();

			final Iterator<IGridHost> i = this.cluster.getTiles();
			while( i.hasNext() )
			{
				final IGridHost h = i.next();
				if( h == this )
				{
					places.add( new WorldCoord( this ) );
				}
				else
				{
					final TileEntity te = (TileEntity) h;

					for( final ForgeDirection d : ForgeDirection.VALID_DIRECTIONS )
					{
						final WorldCoord wc = new WorldCoord( te );
						wc.add( d, 1 );
						if( this.worldObj.isAirBlock( wc.x, wc.y, wc.z ) )
						{
							places.add( wc );
						}
					}
				}
			}

			Collections.shuffle( places );

			if( places.isEmpty() )
			{
				throw new IllegalStateException( this.cluster + " does not contain any kind of blocks, which were destroyed." );
			}

			for( IAEItemStack ais : inv.getAvailableItems( AEApi.instance().storage().createItemList() ) )
			{
				ais = ais.copy();
				ais.setStackSize( ais.getItemStack().getMaxStackSize() );
				while( true )
				{
					final IAEItemStack g = inv.extractItems( ais.copy(), Actionable.MODULATE, this.cluster.getActionSource() );
					if( g == null )
					{
						break;
					}

					final WorldCoord wc = places.poll();
					places.add( wc );

					Platform.spawnDrops( this.worldObj, wc.x, wc.y, wc.z, Collections.singletonList( g.getItemStack() ) );
				}
			}

			this.cluster.destroy();
		}
	}

	@Override
	public boolean isPowered()
	{
		if( Platform.isClient() )
		{
			return ( this.worldObj.getBlockMetadata( this.xCoord, this.yCoord, this.zCoord ) & 4 ) == 4;
		}
		return this.getProxy().isActive();
	}

	@Override
	public boolean isActive()
	{
		if( Platform.isServer() )
		{
			return this.getProxy().isActive();
		}
		return this.isPowered() && this.isFormed();
	}

	public boolean isCoreBlock()
	{
		return this.isCoreBlock;
	}

	public void setCoreBlock( final boolean isCoreBlock )
	{
		this.isCoreBlock = isCoreBlock;
	}

	public ISimplifiedBundle getLightCache()
	{
		return this.lightCache;
	}

	public void setLightCache( final ISimplifiedBundle lightCache )
	{
		this.lightCache = lightCache;
	}

	public NBTTagCompound getPreviousState()
	{
		return this.previousState;
	}

	public void setPreviousState( final NBTTagCompound previousState )
	{
		this.previousState = previousState;
	}
}
