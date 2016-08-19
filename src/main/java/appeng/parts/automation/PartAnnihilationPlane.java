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

package appeng.parts.automation;


import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import appeng.core.settings.TickRates;
import appeng.core.sync.packets.PacketTransitionEffect;
import appeng.hooks.TickHandler;
import appeng.me.GridAccessException;
import appeng.parts.PartBasicState;
import appeng.server.ServerHelper;
import appeng.util.IWorldCallable;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


public class PartAnnihilationPlane extends PartBasicState implements IGridTickable, IWorldCallable<TickRateModulation>
{

	private final BaseActionSource mySrc = new MachineSource( this );
	private boolean isAccepting = true;
	private boolean breaking = false;

	public PartAnnihilationPlane( final ItemStack is )
	{
		super( is );
	}

	@Override
	public TickRateModulation call( final World world ) throws Exception
	{
		this.breaking = false;
		return this.breakBlock( true );
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		int minX = 1;
		int minY = 1;
		int maxX = 15;
		int maxY = 15;

		final IPartHost host = this.getHost();
		if( host != null )
		{
			final TileEntity te = host.getTile();

			final BlockPos pos = te.getPos();

			final EnumFacing e = bch.getWorldX();
			final EnumFacing u = bch.getWorldY();

			if( this.isAnnihilationPlane( te.getWorld().getTileEntity( pos.offset( e.getOpposite() ) ), this.getSide() ) )
			{
				minX = 0;
			}

			if( this.isAnnihilationPlane( te.getWorld().getTileEntity( pos.offset( e ) ), this.getSide() ) )
			{
				maxX = 16;
			}

			if( this.isAnnihilationPlane( te.getWorld().getTileEntity( pos.offset( u.getOpposite() ) ), this.getSide() ) )
			{
				minY = 0;
			}

			if( this.isAnnihilationPlane( te.getWorld().getTileEntity( pos.offset( e ) ), this.getSide() ) )
			{
				maxY = 16;
			}
		}

		bch.addBox( 5, 5, 14, 11, 11, 15 );
		bch.addBox( minX, minY, 15, maxX, maxY, bch.isBBCollision() ? 15 : 16 );
	}

	@Override
	public void onNeighborChanged()
	{
		this.isAccepting = true;
		try
		{
			this.getProxy().getTick().alertDevice( this.getProxy().getNode() );
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	@Override
	public void onEntityCollision( final Entity entity )
	{
		if( this.isAccepting && entity instanceof EntityItem && !entity.isDead && Platform.isServer() && this.getProxy().isActive() )
		{
			boolean capture = false;
			final BlockPos pos = this.getTile().getPos();

			switch( this.getSide() )
			{
				case DOWN:
				case UP:
					if( entity.posX > pos.getX() && entity.posX < pos.getX() + 1 )
					{
						if( entity.posZ > pos.getZ() && entity.posZ < pos.getZ() + 1 )
						{
							if( ( entity.posY > pos.getY() + 0.9 && this.getSide() == AEPartLocation.UP ) || ( entity.posY < pos.getY() + 0.1 && this.getSide() == AEPartLocation.DOWN ) )
							{
								capture = true;
							}
						}
					}
					break;
				case SOUTH:
				case NORTH:
					if( entity.posX > pos.getX() && entity.posX < pos.getX() + 1 )
					{
						if( entity.posY > pos.getY() && entity.posY < pos.getY() + 1 )
						{
							if( ( entity.posZ > pos.getZ() + 0.9 && this.getSide() == AEPartLocation.SOUTH ) || ( entity.posZ < pos.getZ() + 0.1 && this.getSide() == AEPartLocation.NORTH ) )
							{
								capture = true;
							}
						}
					}
					break;
				case EAST:
				case WEST:
					if( entity.posZ > pos.getZ() && entity.posZ < pos.getZ() + 1 )
					{
						if( entity.posY > pos.getY() && entity.posY < pos.getY() + 1 )
						{
							if( ( entity.posX > pos.getX() + 0.9 && this.getSide() == AEPartLocation.EAST ) || ( entity.posX < pos.getX() + 0.1 && this.getSide() == AEPartLocation.WEST ) )
							{
								capture = true;
							}
						}
					}
					break;
				default:
					// umm?
					break;
			}

			if( capture )
			{
				final boolean changed = this.storeEntityItem( (EntityItem) entity );

				if( changed )
				{
					ServerHelper.proxy.sendToAllNearExcept( null, pos.getX(), pos.getY(), pos.getZ(), 64, this.getTile().getWorld(), new PacketTransitionEffect( entity.posX, entity.posY, entity.posZ, this.getSide(), false ) );
				}
			}
		}
	}

	@Override
	public int getCableConnectionLength()
	{
		return 1;
	}

	/**
	 * Stores an {@link EntityItem} inside the network and either marks it as dead or sets it to the leftover stackSize.
	 *
	 * @param entityItem {@link EntityItem} to store
	 */
	private boolean storeEntityItem( final EntityItem entityItem )
	{
		if( !entityItem.isDead )
		{
			final IAEItemStack overflow = this.storeItemStack( entityItem.getEntityItem() );

			return this.handleOverflow( entityItem, overflow );
		}

		return false;
	}

	/**
	 * Stores an {@link ItemStack} inside the network.
	 *
	 * @param item {@link ItemStack} to store
	 * @return the leftover items, which could not be stored inside the network
	 */
	private IAEItemStack storeItemStack( final ItemStack item )
	{
		final IAEItemStack itemToStore = AEItemStack.create( item );
		try
		{
			final IStorageGrid storage = this.getProxy().getStorage();
			final IEnergyGrid energy = this.getProxy().getEnergy();
			final IAEItemStack overflow = Platform.poweredInsert( energy, storage.getItemInventory(), itemToStore, this.mySrc );

			this.isAccepting = overflow == null;

			return overflow;
		}
		catch( final GridAccessException e1 )
		{
			// :P
		}

		return null;
	}

	/**
	 * Handles a possible overflow or none at all.
	 * It will update the entity to match the leftover stack size as well as mark it as dead without any leftover
	 * amount.
	 *
	 * @param entityItem the entity to update or destroy
	 * @param overflow the leftover {@link IAEItemStack}
	 * @return true, if the entity was changed otherwise false.
	 */
	private boolean handleOverflow( final EntityItem entityItem, final IAEItemStack overflow )
	{
		if( overflow == null || overflow.getStackSize() == 0 )
		{
			entityItem.setDead();
			return true;
		}

		final int oldStackSize = entityItem.getEntityItem().stackSize;
		final int newStackSize = (int) overflow.getStackSize();
		final boolean changed = oldStackSize != newStackSize;

		entityItem.getEntityItem().stackSize = newStackSize;

		return changed;
	}

	/**
	 * Spawns an overflow item as new {@link EntityItem} into the {@link World}
	 *
	 * @param overflow the item to spawn
	 */
	private void spawnOverflow( final IAEItemStack overflow )
	{
		if( overflow == null )
		{
			return;
		}

		final TileEntity te = this.getTile();
		final WorldServer w = (WorldServer) te.getWorld();
		final BlockPos offset = te.getPos().offset( this.getSide().getFacing() );
		final BlockPos add = offset.add( .5, .5, .5 );
		final double x = add.getX();
		final double y = add.getY();
		final double z = add.getZ();

		final EntityItem overflowEntity = new EntityItem( w, x, y, z, overflow.getItemStack() );
		overflowEntity.motionX = 0;
		overflowEntity.motionY = 0;
		overflowEntity.motionZ = 0;

		w.spawnEntityInWorld( overflowEntity );
	}

	protected boolean isAnnihilationPlane( final TileEntity blockTileEntity, final AEPartLocation side )
	{
		if( blockTileEntity instanceof IPartHost )
		{
			final IPart p = ( (IPartHost) blockTileEntity ).getPart( side );
			return p != null && p.getClass() == this.getClass();
		}
		return false;
	}

	@Override
	@MENetworkEventSubscribe
	public void chanRender( final MENetworkChannelsChanged c )
	{
		this.onNeighborChanged();
		this.getHost().markForUpdate();
	}

	@Override
	@MENetworkEventSubscribe
	public void powerRender( final MENetworkPowerStatusChange c )
	{
		this.onNeighborChanged();
		this.getHost().markForUpdate();
	}

	private TickRateModulation breakBlock( final boolean modulate )
	{
		if( this.isAccepting && this.getProxy().isActive() )
		{
			try
			{
				final TileEntity te = this.getTile();
				final WorldServer w = (WorldServer) te.getWorld();

				final BlockPos pos = te.getPos().offset( this.getSide().getFacing() );
				final IEnergyGrid energy = this.getProxy().getEnergy();

				if( this.canHandleBlock( w, pos ) )
				{
					final List<ItemStack> items = this.obtainBlockDrops( w, pos );
					final float requiredPower = this.calculateEnergyUsage( w, pos, items );

					final boolean hasPower = energy.extractAEPower( requiredPower, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > requiredPower - 0.1;
					final boolean canStore = this.canStoreItemStacks( items );

					if( hasPower && canStore )
					{
						if( modulate )
						{
							energy.extractAEPower( requiredPower, Actionable.MODULATE, PowerMultiplier.CONFIG );
							this.breakBlockAndStoreItems( w, pos, items );
							ServerHelper.proxy.sendToAllNearExcept( null, pos.getX(), pos.getY(), pos.getZ(), 64, w, new PacketTransitionEffect( pos.getX(), pos.getY(), pos.getZ(), this.getSide(), true ) );
						}
						else
						{
							this.breaking = true;
							TickHandler.INSTANCE.addCallable( this.getTile().getWorld(), this );
						}
						return TickRateModulation.URGENT;
					}
				}
			}
			catch( final GridAccessException e1 )
			{
				// :P
			}
		}

		// nothing to do here :)
		return TickRateModulation.IDLE;
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return new TickingRequest( TickRates.AnnihilationPlane.getMin(), TickRates.AnnihilationPlane.getMax(), false, true );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		if( this.breaking )
		{
			return TickRateModulation.URGENT;
		}

		this.isAccepting = true;
		return this.breakBlock( false );
	}

	/**
	 * Checks if this plane can handle the block at the specific coordinates.
	 */
	private boolean canHandleBlock( final WorldServer w, final BlockPos pos )
	{
		final IBlockState state = w.getBlockState( pos );
		final Material material = state.getMaterial();
		final float hardness = state.getBlockHardness( w, pos );
		final boolean ignoreMaterials = material == Material.AIR || material == Material.LAVA || material == Material.WATER || material.isLiquid();
		final boolean ignoreBlocks = state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.END_PORTAL || state.getBlock() == Blocks.END_PORTAL_FRAME || state.getBlock() == Blocks.COMMAND_BLOCK;

		return !ignoreMaterials && !ignoreBlocks && !w.isAirBlock( pos ) && w.isBlockLoaded( pos ) && w.canMineBlockBody( Platform.getPlayer( w ), pos ) && hardness >= 0f;
	}

	protected List<ItemStack> obtainBlockDrops( final WorldServer w, final BlockPos pos )
	{
		final ItemStack[] out = Platform.getBlockDrops( w, pos );
		return Lists.newArrayList( out );
	}

	/**
	 * Checks if this plane can handle the block at the specific coordinates.
	 */
	protected float calculateEnergyUsage( final WorldServer w, final BlockPos pos, final List<ItemStack> items )
	{
		final IBlockState state = w.getBlockState( pos );
		final float hardness = state.getBlockHardness( w, pos );

		float requiredEnergy = 1 + hardness;
		for( final ItemStack is : items )
		{
			requiredEnergy += is.stackSize;
		}

		return requiredEnergy;
	}

	/**
	 * Checks if the network can store the possible drops.
	 *
	 * It also sets isAccepting to false, if the item can not be stored.
	 *
	 * @param itemStacks an array of {@link ItemStack} to test
	 *
	 * @return true, if the network can store at least a single item of all drops or no drops are reported
	 */
	private boolean canStoreItemStacks( final List<ItemStack> itemStacks )
	{
		boolean canStore = itemStacks.isEmpty();

		try
		{
			final IStorageGrid storage = this.getProxy().getStorage();

			for( final ItemStack itemStack : itemStacks )
			{
				final IAEItemStack itemToTest = AEItemStack.create( itemStack );
				final IAEItemStack overflow = storage.getItemInventory().injectItems( itemToTest, Actionable.SIMULATE, this.mySrc );
				if( overflow == null || itemToTest.getStackSize() > overflow.getStackSize() )
				{
					canStore = true;
				}
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		this.isAccepting = canStore;
		return canStore;
	}

	private void breakBlockAndStoreItems( final WorldServer w, final BlockPos pos, final List<ItemStack> items )
	{
		w.setBlockToAir( pos );

		final AxisAlignedBB box = new AxisAlignedBB( pos.getX() - 0.2, pos.getY() - 0.2, pos.getZ() - 0.2, pos.getX() + 1.2, pos.getY() + 1.2, pos.getZ() + 1.2 );
		for( final Object ei : w.getEntitiesWithinAABB( EntityItem.class, box ) )
		{
			if( ei instanceof EntityItem )
			{
				final EntityItem entityItem = (EntityItem) ei;
				this.storeEntityItem( entityItem );
			}
		}

		for( final ItemStack snaggedItem : items )
		{
			final IAEItemStack overflow = this.storeItemStack( snaggedItem );
			this.spawnOverflow( overflow );
		}
	}
}
