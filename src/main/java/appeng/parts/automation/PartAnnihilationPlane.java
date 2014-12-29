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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
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
import appeng.hooks.TickHandler;
import appeng.me.GridAccessException;
import appeng.parts.PartBasicState;
import appeng.server.ServerHelper;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartAnnihilationPlane extends PartBasicState implements IGridTickable, Callable
{

	public PartAnnihilationPlane(ItemStack is) {
		super( PartAnnihilationPlane.class, is );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
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
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		int minX = 1;
		int minY = 1;
		int maxX = 15;
		int maxY = 15;

		ForgeDirection e = rh.getWorldX();
		ForgeDirection u = rh.getWorldY();

		TileEntity te = this.getHost().getTile();

		if ( this.isTransitionPlane( te.getWorldObj().getTileEntity( x - e.offsetX, y - e.offsetY, z - e.offsetZ ), this.side ) )
			minX = 0;

		if ( this.isTransitionPlane( te.getWorldObj().getTileEntity( x + e.offsetX, y + e.offsetY, z + e.offsetZ ), this.side ) )
			maxX = 16;

		if ( this.isTransitionPlane( te.getWorldObj().getTileEntity( x - u.offsetX, y - u.offsetY, z - u.offsetZ ), this.side ) )
			minY = 0;

		if ( this.isTransitionPlane( te.getWorldObj().getTileEntity( x + u.offsetX, y + u.offsetY, z + u.offsetZ ), this.side ) )
			maxY = 16;

		boolean isActive = (this.clientFlags & (this.POWERED_FLAG | this.CHANNEL_FLAG)) == (this.POWERED_FLAG | this.CHANNEL_FLAG);

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

	private boolean isTransitionPlane(TileEntity blockTileEntity, ForgeDirection side)
	{
		if ( blockTileEntity instanceof IPartHost )
		{
			IPart p = ((IPartHost) blockTileEntity).getPart( side );
			return p instanceof PartAnnihilationPlane;
		}
		return false;
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch)
	{
		int minX = 1;
		int minY = 1;
		int maxX = 15;
		int maxY = 15;

		IPartHost host = this.getHost();
		if ( host != null )
		{
			TileEntity te = host.getTile();

			int x = te.xCoord;
			int y = te.yCoord;
			int z = te.zCoord;

			ForgeDirection e = bch.getWorldX();
			ForgeDirection u = bch.getWorldY();

			if ( this.isTransitionPlane( te.getWorldObj().getTileEntity( x - e.offsetX, y - e.offsetY, z - e.offsetZ ), this.side ) )
				minX = 0;

			if ( this.isTransitionPlane( te.getWorldObj().getTileEntity( x + e.offsetX, y + e.offsetY, z + e.offsetZ ), this.side ) )
				maxX = 16;

			if ( this.isTransitionPlane( te.getWorldObj().getTileEntity( x - u.offsetX, y - u.offsetY, z - u.offsetZ ), this.side ) )
				minY = 0;

			if ( this.isTransitionPlane( te.getWorldObj().getTileEntity( x + u.offsetX, y + u.offsetY, z + u.offsetZ ), this.side ) )
				maxY = 16;
		}

		bch.addBox( 5, 5, 14, 11, 11, 15 );
		bch.addBox( minX, minY, 15, maxX, maxY, bch.isBBCollision() ? 15 : 16 );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 1;
	}

	boolean breaking = false;
	final LinkedList<IAEItemStack> Buffer = new LinkedList<IAEItemStack>();
	final BaseActionSource mySrc = new MachineSource( this );

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );

		data.setInteger( "bufferSize", this.Buffer.size() );
		for (int x = 0; x < this.Buffer.size(); x++)
		{
			NBTTagCompound pack = new NBTTagCompound();
			this.Buffer.get( x ).writeToNBT( pack );
			data.setTag( "buffer" + x, pack );
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );

		int size = data.getInteger( "bufferSize" );
		this.Buffer.clear();
		for (int x = 0; x < size; x++)
		{
			NBTTagCompound pack = (NBTTagCompound) data.getTag( "buffer" + x );
			IAEItemStack ais = AEItemStack.loadItemStackFromNBT( pack );
			if ( ais != null )
				this.Buffer.add( ais );
		}
	}

	private boolean isAccepting()
	{
		return this.Buffer.isEmpty();
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched)
	{
		for (IAEItemStack is : this.Buffer)
			if ( is != null )
				drops.add( is.getItemStack() );
	}

	@Override
	@MENetworkEventSubscribe
	public void chanRender(MENetworkChannelsChanged c)
	{
		this.onNeighborChanged();
		this.getHost().markForUpdate();
	}

	@Override
	@MENetworkEventSubscribe
	public void powerRender(MENetworkPowerStatusChange c)
	{
		this.onNeighborChanged();
		this.getHost().markForUpdate();
	}

	public TickRateModulation EatBlock(boolean eatForReal)
	{
		if ( this.isAccepting() && this.proxy.isActive() )
		{
			try
			{
				TileEntity te = this.getTile();
				WorldServer w = (WorldServer) te.getWorldObj();

				int x = te.xCoord + this.side.offsetX;
				int y = te.yCoord + this.side.offsetY;
				int z = te.zCoord + this.side.offsetZ;

				Block blk = w.getBlock( x, y, z );

				IStorageGrid storage = this.proxy.getStorage();
				IEnergyGrid energy = this.proxy.getEnergy();

				Material mat = blk.getMaterial();
				boolean ignore = mat == Material.air || mat == Material.lava || mat == Material.water || mat.isLiquid() || blk == Blocks.bedrock
						|| blk == Blocks.end_portal || blk == Blocks.end_portal_frame || blk == Blocks.command_block;

				if ( !ignore )
				{
					if ( !w.isAirBlock( x, y, z ) && w.blockExists( x, y, z ) && w.canMineBlock( Platform.getPlayer( w ), x, y, z ) )
					{
						float hardness = blk.getBlockHardness( w, x, y, z );
						if ( hardness >= 0.0 )
						{
							ItemStack[] out = Platform.getBlockDrops( w, x, y, z );
							float total = 1 + hardness;
							for (ItemStack is : out)
								total += is.stackSize;

							boolean hasPower = energy.extractAEPower( total, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > total - 0.1;
							if ( hasPower )
							{
								if ( eatForReal )
								{
									energy.extractAEPower( total, Actionable.MODULATE, PowerMultiplier.CONFIG );
									w.setBlock( x, y, z, Platform.air, 0, 3 );

									AxisAlignedBB box = AxisAlignedBB.getBoundingBox( x - 0.2, y - 0.2, z - 0.2, x + 1.2, y + 1.2, z + 1.2 );
									for (Object ei : w.getEntitiesWithinAABB( EntityItem.class, box ))
									{
										if ( ei instanceof EntityItem )
										{
											EntityItem item = (EntityItem) ei;
											if ( !item.isDead )
											{
												IAEItemStack storedItem = AEItemStack.create( item.getEntityItem() );
												storedItem = Platform.poweredInsert( energy, storage.getItemInventory(), storedItem, this.mySrc );
												if ( storedItem != null )
													this.Buffer.add( storedItem );

												item.setDead();
											}
										}
									}

									ServerHelper.proxy.sendToAllNearExcept( null, x, y, z, 64, w, new PacketTransitionEffect( x, y, z, this.side, true ) );

									for (ItemStack snaggedItem : out)
									{
										IAEItemStack storedItem = AEItemStack.create( snaggedItem );
										storedItem = Platform.poweredInsert( energy, storage.getItemInventory(), storedItem, this.mySrc );
										if ( storedItem != null )
											this.Buffer.add( storedItem );
									}

									return TickRateModulation.URGENT;
								}
								else
								{
									this.breaking = true;
									TickHandler.instance.addCallable( this.tile.getWorldObj(), this );
									return TickRateModulation.URGENT;
								}
							}
						}
					}
				}
			}
			catch (GridAccessException e1)
			{
				// :P
			}
		}

		// nothing to do here :)
		return TickRateModulation.SLEEP;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( TickRates.AnnihilationPlane.min, TickRates.AnnihilationPlane.max, false, true );
	}

	@Override
	public void onNeighborChanged()
	{
		try
		{
			this.proxy.getTick().alertDevice( this.proxy.getNode() );
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

	@Override
	public void onEntityCollision(Entity entity)
	{
		if ( entity instanceof EntityItem && !entity.isDead && this.isAccepting() )
		{
			boolean capture = false;

			switch (this.side)
			{
			case DOWN:
			case UP:
				if ( entity.posX > this.tile.xCoord && entity.posX < this.tile.xCoord + 1 )
					if ( entity.posZ > this.tile.zCoord && entity.posZ < this.tile.zCoord + 1 )
						if ( (entity.posY > this.tile.yCoord + 0.9 && this.side == ForgeDirection.UP) || (entity.posY < this.tile.yCoord + 0.1 && this.side == ForgeDirection.DOWN) )
							capture = true;
				break;
			case SOUTH:
			case NORTH:
				if ( entity.posX > this.tile.xCoord && entity.posX < this.tile.xCoord + 1 )
					if ( entity.posY > this.tile.yCoord && entity.posY < this.tile.yCoord + 1 )
						if ( (entity.posZ > this.tile.zCoord + 0.9 && this.side == ForgeDirection.SOUTH)
								|| (entity.posZ < this.tile.zCoord + 0.1 && this.side == ForgeDirection.NORTH) )
							capture = true;
				break;
			case EAST:
			case WEST:
				if ( entity.posZ > this.tile.zCoord && entity.posZ < this.tile.zCoord + 1 )
					if ( entity.posY > this.tile.yCoord && entity.posY < this.tile.yCoord + 1 )
						if ( (entity.posX > this.tile.xCoord + 0.9 && this.side == ForgeDirection.EAST)
								|| (entity.posX < this.tile.xCoord + 0.1 && this.side == ForgeDirection.WEST) )
							capture = true;
				break;
			default:
				// umm?
				break;
			}

			if ( capture && Platform.isServer() && this.proxy.isActive() )
			{
				IAEItemStack stack = AEItemStack.create( ((EntityItem) entity).getEntityItem() );
				if ( stack != null )
				{
					ServerHelper.proxy.sendToAllNearExcept( null, this.tile.xCoord, this.tile.yCoord, this.tile.zCoord, 64, this.tile.getWorldObj(),
							new PacketTransitionEffect( entity.posX, entity.posY, entity.posZ, this.side, false ) );

					this.Buffer.add( stack );
					this.storeBuffer();
					entity.setDead();
				}
			}
		}
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		if ( this.breaking )
			return TickRateModulation.URGENT;

		if ( this.isAccepting() )
			return this.EatBlock( false );
		else
		{
			this.storeBuffer();
			return TickRateModulation.IDLE;
		}
	}

	private void storeBuffer()
	{
		try
		{
			IStorageGrid storage = this.proxy.getStorage();
			IEnergyGrid energy = this.proxy.getEnergy();

			while (!this.Buffer.isEmpty())
			{
				IAEItemStack storedItem = this.Buffer.pop();
				storedItem = Platform.poweredInsert( energy, storage.getItemInventory(), storedItem, this.mySrc );
				if ( storedItem != null )
				{
					this.Buffer.add( storedItem );
					break;
				}
			}
		}
		catch (GridAccessException e1)
		{
			// :P
		}
	}

	@Override
	public Object call() throws Exception
	{
		this.breaking = false;
		return this.EatBlock( true );
	}

}
