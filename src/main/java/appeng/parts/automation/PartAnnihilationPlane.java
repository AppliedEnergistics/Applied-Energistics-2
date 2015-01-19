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


import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.settings.TickRates;
import appeng.core.sync.packets.PacketTransitionEffect;
import appeng.me.GridAccessException;
import appeng.parts.PartBasicState;
import appeng.server.ServerHelper;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


public class PartAnnihilationPlane extends PartBasicState implements IGridTickable
{

	private boolean isAccepting = true;
	private final BaseActionSource mySrc = new MachineSource( this );

	public PartAnnihilationPlane( ItemStack is )
	{
		super( PartAnnihilationPlane.class, is );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper rh, RenderBlocks renderer )
	{
		rh.setTexture( CableBusTextures.PartPlaneSides.getIcon(), CableBusTextures.PartPlaneSides.getIcon(),
				CableBusTextures.PartTransitionPlaneBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartPlaneSides.getIcon(),
				CableBusTextures.PartPlaneSides.getIcon() );

		rh.setBounds( 1, 1, 15, 15, 15, 16 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer )
	{
		int minX = 1;
		int minY = 1;
		int maxX = 15;
		int maxY = 15;

		final ForgeDirection e = rh.getWorldX();
		final ForgeDirection u = rh.getWorldY();

		final TileEntity te = this.getHost().getTile();

		if ( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x - e.offsetX, y - e.offsetY, z - e.offsetZ ), this.side ) )
		{
			minX = 0;
		}

		if ( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x + e.offsetX, y + e.offsetY, z + e.offsetZ ), this.side ) )
		{
			maxX = 16;
		}

		if ( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x - u.offsetX, y - u.offsetY, z - u.offsetZ ), this.side ) )
		{
			minY = 0;
		}

		if ( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x + u.offsetX, y + u.offsetY, z + u.offsetZ ), this.side ) )
		{
			maxY = 16;
		}

		final boolean isActive = ( this.clientFlags & ( this.POWERED_FLAG | this.CHANNEL_FLAG ) ) == ( this.POWERED_FLAG | this.CHANNEL_FLAG );

		this.renderCache = rh.useSimplifiedRendering( x, y, z, this, this.renderCache );
		rh.setTexture( CableBusTextures.PartPlaneSides.getIcon(), CableBusTextures.PartPlaneSides.getIcon(),
				CableBusTextures.PartTransitionPlaneBack.getIcon(), isActive ? CableBusTextures.BlockAnnihilationPlaneOn.getIcon() : this.is.getIconIndex(),
				CableBusTextures.PartPlaneSides.getIcon(), CableBusTextures.PartPlaneSides.getIcon() );

		rh.setBounds( minX, minY, 15, maxX, maxY, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartTransitionPlaneBack.getIcon(), isActive ? CableBusTextures.BlockAnnihilationPlaneOn.getIcon() : this.is.getIconIndex(),
				CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderBlock( x, y, z, renderer );

		this.renderLights( x, y, z, rh, renderer );
	}

	private boolean isAnnihilationPlane( TileEntity blockTileEntity, ForgeDirection side )
	{
		if ( blockTileEntity instanceof IPartHost )
		{
			final IPart p = ( ( IPartHost ) blockTileEntity ).getPart( side );
			return p instanceof PartAnnihilationPlane;
		}
		return false;
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
	{
		int minX = 1;
		int minY = 1;
		int maxX = 15;
		int maxY = 15;

		final IPartHost host = this.getHost();
		if ( host != null )
		{
			final TileEntity te = host.getTile();

			final int x = te.xCoord;
			final int y = te.yCoord;
			final int z = te.zCoord;

			final ForgeDirection e = bch.getWorldX();
			final ForgeDirection u = bch.getWorldY();

			if ( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x - e.offsetX, y - e.offsetY, z - e.offsetZ ), this.side ) )
			{
				minX = 0;
			}

			if ( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x + e.offsetX, y + e.offsetY, z + e.offsetZ ), this.side ) )
			{
				maxX = 16;
			}

			if ( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x - u.offsetX, y - u.offsetY, z - u.offsetZ ), this.side ) )
			{
				minY = 0;
			}

			if ( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x + u.offsetX, y + u.offsetY, z + u.offsetZ ), this.side ) )
			{
				maxY = 16;
			}
		}

		bch.addBox( 5, 5, 14, 11, 11, 15 );
		bch.addBox( minX, minY, 15, maxX, maxY, bch.isBBCollision() ? 15 : 16 );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 1;
	}

	/**
	 * If the plane is accepting items.
	 * 
	 * This might be improved if a performance problem shows up.
	 * 
	 * @return true if planes accepts items.
	 */
	private boolean isAccepting()
	{
		return this.isAccepting;
	}

	@Override
	@MENetworkEventSubscribe
	public void chanRender( MENetworkChannelsChanged c )
	{
		this.onNeighborChanged();
		this.getHost().markForUpdate();
	}

	@Override
	@MENetworkEventSubscribe
	public void powerRender( MENetworkPowerStatusChange c )
	{
		this.onNeighborChanged();
		this.getHost().markForUpdate();
	}

	public TickRateModulation breakblock()
	{
		if ( this.isAccepting() && this.proxy.isActive() )
		{
			try
			{
				final TileEntity te = this.getTile();
				final WorldServer w = ( WorldServer ) te.getWorldObj();

				final int x = te.xCoord + this.side.offsetX;
				final int y = te.yCoord + this.side.offsetY;
				final int z = te.zCoord + this.side.offsetZ;

				final Block blk = w.getBlock( x, y, z );

				final IEnergyGrid energy = this.proxy.getEnergy();

				final Material mat = blk.getMaterial();
				final boolean ignore = mat == Material.air || mat == Material.lava || mat == Material.water || mat.isLiquid() || blk == Blocks.bedrock
						|| blk == Blocks.end_portal || blk == Blocks.end_portal_frame || blk == Blocks.command_block;

				if ( !ignore && !w.isAirBlock( x, y, z ) && w.blockExists( x, y, z ) && w.canMineBlock( Platform.getPlayer( w ), x, y, z ) )
				{
					final float hardness = blk.getBlockHardness( w, x, y, z );

					if ( hardness >= 0.0 )
					{
						final ItemStack[] out = Platform.getBlockDrops( w, x, y, z );

						float total = 1 + hardness;
						for ( final ItemStack is : out )
						{
							total += is.stackSize;
						}

						final boolean hasPower = energy.extractAEPower( total, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > total - 0.1;
						final boolean canStore = this.canStoreItemStacks( out );

						if ( hasPower && canStore )
						{
							w.setBlock( x, y, z, Platform.AIR, 0, 3 );
							energy.extractAEPower( total, Actionable.MODULATE, PowerMultiplier.CONFIG );

							final AxisAlignedBB box = AxisAlignedBB.getBoundingBox( x - 0.2, y - 0.2, z - 0.2, x + 1.2, y + 1.2, z + 1.2 );
							for ( final Object ei : w.getEntitiesWithinAABB( EntityItem.class, box ) )
							{
								if ( ei instanceof EntityItem )
								{
									final EntityItem entityItem = ( EntityItem ) ei;
									this.storeEntityItem( entityItem );
								}
							}

							for ( final ItemStack snaggedItem : out )
							{
								if ( this.storeItemStack( snaggedItem ) )
								{
									ServerHelper.proxy.sendToAllNearExcept( null, x, y, z, 64, w, new PacketTransitionEffect( x, y, z, this.side, true ) );
								}
							}

							return TickRateModulation.URGENT;

						}
					}
				}
			}
			catch ( final GridAccessException e1 )
			{
				// :P
			}
		}

		// nothing to do here :)
		return TickRateModulation.IDLE;
	}

	@Override
	public TickingRequest getTickingRequest( IGridNode node )
	{
		return new TickingRequest( TickRates.AnnihilationPlane.min, TickRates.AnnihilationPlane.max, false, true );
	}

	@Override
	public void onNeighborChanged()
	{
		this.isAccepting = true;
		try
		{
			this.proxy.getTick().alertDevice( this.proxy.getNode() );
		}
		catch ( final GridAccessException e )
		{
			// :P
		}
	}

	@Override
	public void onEntityCollision( Entity entity )
	{
		if ( this.isAccepting() && entity instanceof EntityItem && !entity.isDead && Platform.isServer() && this.proxy.isActive() )
		{
			boolean capture = false;

			switch ( this.side )
			{
				case DOWN:
				case UP:
					if ( entity.posX > this.tile.xCoord && entity.posX < this.tile.xCoord + 1 )
					{
						if ( entity.posZ > this.tile.zCoord && entity.posZ < this.tile.zCoord + 1 )
						{
							if ( ( entity.posY > this.tile.yCoord + 0.9 && this.side == ForgeDirection.UP ) || ( entity.posY < this.tile.yCoord + 0.1 && this.side == ForgeDirection.DOWN ) )
							{
								capture = true;
							}
						}
					}
					break;
				case SOUTH:
				case NORTH:
					if ( entity.posX > this.tile.xCoord && entity.posX < this.tile.xCoord + 1 )
					{
						if ( entity.posY > this.tile.yCoord && entity.posY < this.tile.yCoord + 1 )
						{
							if ( ( entity.posZ > this.tile.zCoord + 0.9 && this.side == ForgeDirection.SOUTH )
									|| ( entity.posZ < this.tile.zCoord + 0.1 && this.side == ForgeDirection.NORTH ) )
							{
								capture = true;
							}
						}
					}
					break;
				case EAST:
				case WEST:
					if ( entity.posZ > this.tile.zCoord && entity.posZ < this.tile.zCoord + 1 )
					{
						if ( entity.posY > this.tile.yCoord && entity.posY < this.tile.yCoord + 1 )
						{
							if ( ( entity.posX > this.tile.xCoord + 0.9 && this.side == ForgeDirection.EAST )
									|| ( entity.posX < this.tile.xCoord + 0.1 && this.side == ForgeDirection.WEST ) )
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

			if ( capture )
			{
				ServerHelper.proxy.sendToAllNearExcept( null, this.tile.xCoord, this.tile.yCoord, this.tile.zCoord, 64, this.tile.getWorldObj(),
						new PacketTransitionEffect( entity.posX, entity.posY, entity.posZ, this.side, false ) );
				this.storeEntityItem( ( EntityItem ) entity );
			}
		}
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int TicksSinceLastCall )
	{
		this.isAccepting = true;
		return this.breakblock();
	}

	/**
	 * Checks if the network can store the possible drops.
	 * 
	 * It also sets isAccepting to false, if the item can not be stored.
	 * 
	 * @param itemStacks an array of {@link ItemStack} to test
	 * @return true, if the network can store at least a single item of all drops or no drops are reported
	 */
	private boolean canStoreItemStacks( ItemStack[] itemStacks )
	{
		boolean canStore = itemStacks.length == 0;

		try
		{
			final IStorageGrid storage = this.proxy.getStorage();

			for ( final ItemStack itemStack : itemStacks )
			{
				final IAEItemStack itemToTest = AEItemStack.create( itemStack );
				final IAEItemStack overflow = storage.getItemInventory().injectItems( itemToTest, Actionable.SIMULATE, this.mySrc );
				if ( overflow == null || itemToTest.getStackSize() > overflow.getStackSize() )
				{
					canStore = true;
				}
			}
		}
		catch ( final GridAccessException e )
		{
			// :P
		}

		this.isAccepting = canStore;
		return canStore;
	}

	/**
	 * Stores an {@link ItemStack} inside the network.
	 * 
	 * @param item {@link ItemStack} to store
	 * @return null or leftover {@link IAEItemStack}
	 */
	private boolean storeItemStack( ItemStack item )
	{
		final IAEItemStack itemToStore = AEItemStack.create( item );
		try
		{
			final IStorageGrid storage = this.proxy.getStorage();
			final IEnergyGrid energy = this.proxy.getEnergy();
			final IAEItemStack overflow = Platform.poweredInsert( energy, storage.getItemInventory(), itemToStore, this.mySrc );

			this.spawnOverflowItemStack( overflow );

			this.isAccepting = overflow == null;
			return overflow == null || item.stackSize < overflow.getStackSize();
		}
		catch ( final GridAccessException e1 )
		{
			// :P
		}
		return false;
	}

	/**
	 * Stores an {@link EntityItem} inside the network and either marks it as dead or sets it to the leftover stackSize.
	 * 
	 * @param entityItem {@link EntityItem} to store
	 */
	private void storeEntityItem( EntityItem entityItem )
	{
		if ( !entityItem.isDead )
		{
			this.storeItemStack( entityItem.getEntityItem() );
			entityItem.setDead();
		}
	}

	private void spawnOverflowItemStack( IAEItemStack overflow )
	{
		if ( overflow == null )
		{
			return;
		}

		final TileEntity tileEntity = this.getTile();
		final WorldServer world = ( WorldServer ) tileEntity.getWorldObj();

		final int x = tileEntity.xCoord + this.side.offsetX;
		final int y = tileEntity.yCoord + this.side.offsetY;
		final int z = tileEntity.zCoord + this.side.offsetZ;

		Platform.spawnDrops( world, x, y, z, Lists.newArrayList( overflow.getItemStack() ) );
	}

}
