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


import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

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

public class TileCraftingTile extends AENetworkTile implements IAEMultiBlock, IPowerChannelState
{

	CraftingCPUCluster cluster;
	final CraftingCPUCalculator calc = new CraftingCPUCalculator( this );

	public ISimplifiedBundle lightCache;

	public NBTTagCompound previousState = null;
	public boolean isCoreBlock = false;

	static final ItemStack coProcessorStack = AEApi.instance().blocks().blockCraftingAccelerator.stack( 1 );

	@Override
	protected AENetworkProxy createProxy()
	{
		return new AENetworkProxyMultiblock( this, "proxy", getItemFromTile( this ), true );
	}

	@Override
	protected ItemStack getItemFromTile(Object obj)
	{
		if ( ((TileCraftingTile) obj).isAccelerator() )
			return coProcessorStack;
		return super.getItemFromTile( obj );
	}

	public void updateStatus(CraftingCPUCluster c)
	{
		if ( cluster != null && cluster != c )
			cluster.breakCluster();

		cluster = c;
		updateMeta( true );
	}

	public void updateMultiBlock()
	{
		calc.calculateMultiblock( worldObj, getLocation() );
	}

	@Override
	public void setName(String name)
	{
		super.setName( name );
		if ( cluster != null )
			cluster.updateName();
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileCraftingTile(NBTTagCompound data)
	{
		data.setBoolean( "core", isCoreBlock );
		if ( isCoreBlock && cluster != null )
			cluster.writeToNBT( data );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileCraftingTile(NBTTagCompound data)
	{
		isCoreBlock = data.getBoolean( "core" );
		if ( isCoreBlock )
		{
			if ( cluster != null )
				cluster.readFromNBT( data );
			else
				previousState = (NBTTagCompound) data.copy();
		}
	}

	public TileCraftingTile() {
		gridProxy.setFlags( GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL );
		gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
	}

	@Override
	public void onReady()
	{
		super.onReady();
		gridProxy.setVisualRepresentation( getItemFromTile( this ) );
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
		if ( cluster != null )
		{
			cluster.destroy();
			if ( update )
				updateMeta( true );
		}
	}

	@MENetworkEventSubscribe
	public void onPowerStateChange(MENetworkChannelsChanged ev)
	{
		updateMeta( false );
	}

	@MENetworkEventSubscribe
	public void onPowerStateChange(MENetworkPowerStatusChange ev)
	{
		updateMeta( false );
	}

	public void updateMeta(boolean updateFormed)
	{
		if ( worldObj == null || notLoaded() )
			return;

		boolean formed = isFormed();
		boolean power = false;

		if ( gridProxy.isReady() )
			power = gridProxy.isActive();

		int current = worldObj.getBlockMetadata( xCoord, yCoord, zCoord );
		int newMeta = (current & 3) | (formed ? 8 : 0) | (power ? 4 : 0);

		if ( current != newMeta )
			worldObj.setBlockMetadataWithNotify( xCoord, yCoord, zCoord, newMeta, 2 );

		if ( updateFormed )
		{
			if ( formed )
				gridProxy.setValidSides( EnumSet.allOf( ForgeDirection.class ) );
			else
				gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		}
	}

	@Override
	public IAECluster getCluster()
	{
		return cluster;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public boolean isPowered()
	{
		if ( Platform.isClient() )
			return (worldObj.getBlockMetadata( xCoord, yCoord, zCoord ) & 4) == 4;
		return gridProxy.isActive();
	}

	public boolean isFormed()
	{
		if ( Platform.isClient() )
			return (worldObj.getBlockMetadata( xCoord, yCoord, zCoord ) & 8) == 8;
		return cluster != null;
	}

	public boolean isAccelerator()
	{
		if ( worldObj == null )
			return false;
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
		if ( cluster != null )
		{
			cluster.cancel();
			IMEInventory<IAEItemStack> inv = cluster.getInventory();

			LinkedList<WorldCoord> places = new LinkedList<WorldCoord>();

			Iterator<IGridHost> i = cluster.getTiles();
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

			for (IAEItemStack ais : inv.getAvailableItems( AEApi.instance().storage().createItemList() ))
			{
				ais = ais.copy();
				ais.setStackSize( ais.getItemStack().getMaxStackSize() );
				while (true)
				{
					IAEItemStack g = inv.extractItems( ais.copy(), Actionable.MODULATE, cluster.getActionSource() );
					if ( g == null )
						break;

					WorldCoord wc = places.poll();
					places.add( wc );

					Platform.spawnDrops( worldObj, wc.x, wc.y, wc.z, Collections.singletonList( g.getItemStack() ) );
				}

			}

			cluster.destroy();
		}
	}
}
