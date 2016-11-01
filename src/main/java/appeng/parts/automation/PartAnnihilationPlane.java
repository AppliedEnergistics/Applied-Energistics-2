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


import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.YesNo;
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
import appeng.hooks.TickHandler;
import appeng.me.GridAccessException;
import appeng.parts.PartBasicState;
import appeng.server.ServerHelper;
import appeng.util.IWorldCallable;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent;

import java.util.List;


public class PartAnnihilationPlane extends PartBasicState implements IGridTickable, IWorldCallable<TickRateModulation>
{
	private static final IIcon SIDE_ICON = CableBusTextures.PartPlaneSides.getIcon();
	private static final IIcon BACK_ICON = CableBusTextures.PartTransitionPlaneBack.getIcon();
	private static final IIcon STATUS_ICON = CableBusTextures.PartMonitorSidesStatus.getIcon();
	private static final IIcon ACTIVE_ICON = CableBusTextures.BlockAnnihilationPlaneOn.getIcon();
	private static final int MAX_CACHE_TIME = 60;

	private final BaseActionSource mySrc = new MachineSource( this );
	private EntityPlayer owner = null;
	private boolean isAccepting = true;
	private boolean breaking = false;
	private YesNo permissionCache = YesNo.UNDECIDED;
	private int cacheTime = 0;

	public PartAnnihilationPlane( final ItemStack is )
	{
		super( is );
	}

	@Override
	public void onPlacement( EntityPlayer player, ItemStack held, ForgeDirection side )
	{
		super.onPlacement( player, held, side );
		this.owner = player;
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

			final int x = te.xCoord;
			final int y = te.yCoord;
			final int z = te.zCoord;

			final ForgeDirection e = bch.getWorldX();
			final ForgeDirection u = bch.getWorldY();

			if( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x - e.offsetX, y - e.offsetY, z - e.offsetZ ), this.getSide() ) )
			{
				minX = 0;
			}

			if( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x + e.offsetX, y + e.offsetY, z + e.offsetZ ), this.getSide() ) )
			{
				maxX = 16;
			}

			if( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x - u.offsetX, y - u.offsetY, z - u.offsetZ ), this.getSide() ) )
			{
				minY = 0;
			}

			if( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x + u.offsetX, y + u.offsetY, z + u.offsetZ ), this.getSide() ) )
			{
				maxY = 16;
			}
		}

		bch.addBox( 5, 5, 14, 11, 11, 15 );
		// The smaller collision hitbox here is needed to allow for the entity collision event
		bch.addBox( minX, minY, 15, maxX, maxY, bch.isBBCollision() ? 15 : 16 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		rh.setTexture( SIDE_ICON, SIDE_ICON, BACK_ICON, this.getItemStack().getIconIndex(), SIDE_ICON, SIDE_ICON );

		rh.setBounds( 1, 1, 15, 15, 15, 16 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		this.renderStaticWithIcon( x, y, z, rh, renderer, ACTIVE_ICON );
	}

	protected void renderStaticWithIcon( final int x, final int y, final int z, final IPartRenderHelper rh, final RenderBlocks renderer, final IIcon activeIcon )
	{
		int minX = 1;

		final ForgeDirection e = rh.getWorldX();
		final ForgeDirection u = rh.getWorldY();

		final TileEntity te = this.getHost().getTile();

		if( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x - e.offsetX, y - e.offsetY, z - e.offsetZ ), this.getSide() ) )
		{
			minX = 0;
		}

		int maxX = 15;
		if( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x + e.offsetX, y + e.offsetY, z + e.offsetZ ), this.getSide() ) )
		{
			maxX = 16;
		}

		int minY = 1;
		if( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x - u.offsetX, y - u.offsetY, z - u.offsetZ ), this.getSide() ) )
		{
			minY = 0;
		}

		int maxY = 15;
		if( this.isAnnihilationPlane( te.getWorldObj().getTileEntity( x + u.offsetX, y + u.offsetY, z + u.offsetZ ), this.getSide() ) )
		{
			maxY = 16;
		}

		final boolean isActive = ( this.getClientFlags() & ( PartBasicState.POWERED_FLAG | PartBasicState.CHANNEL_FLAG ) ) == ( PartBasicState.POWERED_FLAG | PartBasicState.CHANNEL_FLAG );

		this.setRenderCache( rh.useSimplifiedRendering( x, y, z, this, this.getRenderCache() ) );
		rh.setTexture( SIDE_ICON, SIDE_ICON, BACK_ICON, isActive ? activeIcon : this.getItemStack().getIconIndex(), SIDE_ICON, SIDE_ICON );

		rh.setBounds( minX, minY, 15, maxX, maxY, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( STATUS_ICON, STATUS_ICON, BACK_ICON, isActive ? activeIcon : this.getItemStack().getIconIndex(), STATUS_ICON, STATUS_ICON );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderBlock( x, y, z, renderer );

		this.renderLights( x, y, z, rh, renderer );
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

			// This is the middle point of the entities BB, which is better suited for comparisons that don't rely on it "touching" the plane
			double posYMiddle = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0D;

			switch( this.getSide() )
			{
				case DOWN:
				case UP:
					if( entity.posX > this.getTile().xCoord && entity.posX < this.getTile().xCoord + 1 )
					{
						if( entity.posZ > this.getTile().zCoord && entity.posZ < this.getTile().zCoord + 1 )
						{
							if( ( entity.posY > this.getTile().yCoord + 0.9 && this.getSide() == ForgeDirection.UP ) || ( entity.posY < this.getTile().yCoord + 0.1 && this.getSide() == ForgeDirection.DOWN ) )
							{
								capture = true;
							}
						}
					}
					break;
				case SOUTH:
				case NORTH:
					if( entity.posX > this.getTile().xCoord && entity.posX < this.getTile().xCoord + 1 )
					{
						if( posYMiddle > this.getTile().yCoord && posYMiddle < this.getTile().yCoord + 1 )
						{
							if( ( entity.posZ > this.getTile().zCoord + 0.9 && this.getSide() == ForgeDirection.SOUTH ) || ( entity.posZ < this.getTile().zCoord + 0.1 && this.getSide() == ForgeDirection.NORTH ) )
							{
								capture = true;
							}
						}
					}
					break;
				case EAST:
				case WEST:
					if( entity.posZ > this.getTile().zCoord && entity.posZ < this.getTile().zCoord + 1 )
					{
						if( posYMiddle > this.getTile().yCoord && posYMiddle < this.getTile().yCoord + 1 )
						{
							if( ( entity.posX > this.getTile().xCoord + 0.9 && this.getSide() == ForgeDirection.EAST ) || ( entity.posX < this.getTile().xCoord + 0.1 && this.getSide() == ForgeDirection.WEST ) )
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
					ServerHelper.proxy.sendToAllNearExcept( null, this.getTile().xCoord, this.getTile().yCoord, this.getTile().zCoord, 64, this.getTile().getWorldObj(), new PacketTransitionEffect( entity.posX, entity.posY, entity.posZ, this.getSide(), false ) );
				}
			}
		}
	}

	@Override
	public int cableConnectionRenderTo()
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
	 * @param overflow   the leftover {@link IAEItemStack}
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
		final WorldServer w = (WorldServer) te.getWorldObj();
		final double x = te.xCoord + this.getSide().offsetX + .5d;
		final double y = te.yCoord + this.getSide().offsetY + .5d;
		final double z = te.zCoord + this.getSide().offsetZ + .5d;

		final EntityItem overflowEntity = new EntityItem( w, x, y, z, overflow.getItemStack() );
		overflowEntity.motionX = 0;
		overflowEntity.motionY = 0;
		overflowEntity.motionZ = 0;

		w.spawnEntityInWorld( overflowEntity );
	}

	protected boolean isAnnihilationPlane( final TileEntity blockTileEntity, final ForgeDirection side )
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
				final WorldServer w = (WorldServer) te.getWorldObj();

				final int x = te.xCoord + this.getSide().offsetX;
				final int y = te.yCoord + this.getSide().offsetY;
				final int z = te.zCoord + this.getSide().offsetZ;

				final IEnergyGrid energy = this.getProxy().getEnergy();

				if( this.canHandleBlock( w, x, y, z ) )
				{
					final List<ItemStack> items = this.obtainBlockDrops( w, x, y, z );
					final float requiredPower = this.calculateEnergyUsage( w, x, y, z, items );

					final boolean hasPower = energy.extractAEPower( requiredPower, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > requiredPower - 0.1;
					final boolean canStore = this.canStoreItemStacks( items );

					if( hasPower && canStore )
					{
						if( modulate )
						{
							energy.extractAEPower( requiredPower, Actionable.MODULATE, PowerMultiplier.CONFIG );
							this.breakBlockAndStoreItems( w, x, y, z, items );
							ServerHelper.proxy.sendToAllNearExcept( null, x, y, z, 64, w, new PacketTransitionEffect( x, y, z, this.getSide(), true ) );
						}
						else
						{
							this.breaking = true;
							TickHandler.INSTANCE.addCallable( this.getTile().getWorldObj(), this );
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

		if( ticksSinceLastCall == 120 || cacheTime >= MAX_CACHE_TIME )
		{
			cacheTime = 0;
			permissionCache = YesNo.UNDECIDED;
		}
		cacheTime += ticksSinceLastCall - 120;
		this.isAccepting = true;
		return this.breakBlock( false );
	}

	/**
	 * Checks if this plane can handle the block at the specific coordinates.
	 */
	private boolean canHandleBlock( final WorldServer w, final int x, final int y, final int z )
	{
		final Block block = w.getBlock( x, y, z );
		final Material material = block.getMaterial();
		final float hardness = block.getBlockHardness( w, x, y, z );
		final boolean ignoreMaterials = material == Material.air || material == Material.lava || material == Material.water || material.isLiquid();
		final boolean ignoreBlocks = block == Blocks.bedrock || block == Blocks.end_portal || block == Blocks.end_portal_frame || block == Blocks.command_block;
		final EntityPlayer player = owner == null ? Platform.getPlayer( w ) : owner;
		if( permissionCache == YesNo.UNDECIDED )
		{
			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent( x, y, z, w, block, w.getBlockMetadata( x, y, z ), player );
			MinecraftForge.EVENT_BUS.post( event );
			permissionCache = ( event.isCanceled() ) ? YesNo.NO : YesNo.YES;
		}
		return permissionCache == YesNo.YES && !ignoreMaterials && !ignoreBlocks && !w.isAirBlock( x, y, z ) && w.blockExists( x, y, z ) && w.canMineBlock( player , x, y, z ) && hardness >= 0f;
	}

	protected List<ItemStack> obtainBlockDrops( final WorldServer w, final int x, final int y, final int z )
	{
		final ItemStack[] out = Platform.getBlockDrops( w, x, y, z );
		return Lists.newArrayList( out );
	}

	/**
	 * Checks if this plane can handle the block at the specific coordinates.
	 */
	protected float calculateEnergyUsage( final WorldServer w, final int x, final int y, final int z, final List<ItemStack> items )
	{
		final Block block = w.getBlock( x, y, z );
		final float hardness = block.getBlockHardness( w, x, y, z );

		float requiredEnergy = 1 + hardness;
		for( final ItemStack is : items )
		{
			requiredEnergy += is.stackSize;
		}

		return requiredEnergy;
	}

	/**
	 * Checks if the network can store the possible drops.
	 * <p>
	 * It also sets isAccepting to false, if the item can not be stored.
	 *
	 * @param itemStacks an array of {@link ItemStack} to test
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

	private void breakBlockAndStoreItems( final WorldServer w, final int x, final int y, final int z, final List<ItemStack> items )
	{
		w.setBlock( x, y, z, Platform.AIR_BLOCK, 0, 3 );

		final AxisAlignedBB box = AxisAlignedBB.getBoundingBox( x - 0.2, y - 0.2, z - 0.2, x + 1.2, y + 1.2, z + 1.2 );
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
