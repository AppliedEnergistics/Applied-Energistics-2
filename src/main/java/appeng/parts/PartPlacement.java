/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.parts;


import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import appeng.api.AEApi;
import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IItems;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.api.util.DimensionalCoord;
import appeng.core.CommonHelper;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketClick;
import appeng.core.sync.packets.PacketPartPlacement;
import appeng.facade.IFacadeItem;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBC;
import appeng.integration.abstraction.IFMP;
import appeng.integration.abstraction.IImmibisMicroblocks;
import appeng.util.LookDirection;
import appeng.util.Platform;


public final class PartPlacement
{

	public static float eyeHeight = 0.0f;
	private final ThreadLocal<Object> placing = new ThreadLocal<Object>();
	private boolean wasCanceled = false;

	public static boolean place( ItemStack held, int x, int y, int z, int face, EntityPlayer player, World world, PlaceType pass, int depth )
	{
		if( depth > 3 )
		{
			return false;
		}

		ForgeDirection side = ForgeDirection.getOrientation( face );

		if( held != null && Platform.isWrench( player, held, x, y, z ) && player.isSneaking() )
		{
			if( !Platform.hasPermissions( new DimensionalCoord( world, x, y, z ), player ) )
			{
				return false;
			}

			Block block = world.getBlock( x, y, z );
			TileEntity tile = world.getTileEntity( x, y, z );
			IPartHost host = null;

			if( tile instanceof IPartHost )
			{
				host = (IPartHost) tile;
			}

			if( host != null )
			{
				if( !world.isRemote )
				{
					LookDirection dir = Platform.getPlayerRay( player, getEyeOffset( player ) );
					MovingObjectPosition mop = block.collisionRayTrace( world, x, y, z, dir.a, dir.b );
					if( mop != null )
					{
						List<ItemStack> is = new LinkedList<ItemStack>();
						SelectedPart sp = selectPart( player, host, mop.hitVec.addVector( -mop.blockX, -mop.blockY, -mop.blockZ ) );

						if( sp.part != null )
						{
							is.add( sp.part.getItemStack( PartItemStack.Wrench ) );
							sp.part.getDrops( is, true );
							host.removePart( sp.side, false );
						}

						if( sp.facade != null )
						{
							is.add( sp.facade.getItemStack() );
							host.getFacadeContainer().removeFacade( host, sp.side );
							Platform.notifyBlocksOfNeighbors( world, x, y, z );
						}

						if( host.isEmpty() )
						{
							host.cleanup();
						}

						if( !is.isEmpty() )
						{
							Platform.spawnDrops( world, x, y, z, is );
						}
					}
				}
				else
				{
					player.swingItem();
					NetworkHandler.instance.sendToServer( new PacketPartPlacement( x, y, z, face, getEyeOffset( player ) ) );
				}
				return true;
			}

			return false;
		}

		TileEntity tile = world.getTileEntity( x, y, z );
		IPartHost host = null;

		if( tile instanceof IPartHost )
		{
			host = (IPartHost) tile;
		}

		if( held != null )
		{
			IFacadePart fp = isFacade( held, side );
			if( fp != null )
			{
				if( host != null )
				{
					if( !world.isRemote )
					{
						if( host.getPart( ForgeDirection.UNKNOWN ) == null )
						{
							return false;
						}

						if( host.canAddPart( held, side ) )
						{
							if( host.getFacadeContainer().addFacade( fp ) )
							{
								host.markForUpdate();
								if( !player.capabilities.isCreativeMode )
								{
									held.stackSize--;
									if( held.stackSize == 0 )
									{
										player.inventory.mainInventory[player.inventory.currentItem] = null;
										MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( player, held ) );
									}
								}
								return true;
							}
						}
					}
					else
					{
						player.swingItem();
						NetworkHandler.instance.sendToServer( new PacketPartPlacement( x, y, z, face, getEyeOffset( player ) ) );
						return true;
					}
				}
				return false;
			}
		}

		if( host == null && tile != null && IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.FMP ) )
		{
			host = ( (IFMP) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.FMP ) ).getOrCreateHost( tile );
		}

		if( host == null && tile != null && IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.ImmibisMicroblocks ) )
		{
			host = ( (IImmibisMicroblocks) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.ImmibisMicroblocks ) ).getOrCreateHost( player, face, tile );
		}

		// if ( held == null )
		{
			Block block = world.getBlock( x, y, z );
			if( host != null && player.isSneaking() && block != null )
			{
				LookDirection dir = Platform.getPlayerRay( player, getEyeOffset( player ) );
				MovingObjectPosition mop = block.collisionRayTrace( world, x, y, z, dir.a, dir.b );
				if( mop != null )
				{
					mop.hitVec = mop.hitVec.addVector( -mop.blockX, -mop.blockY, -mop.blockZ );
					SelectedPart sPart = selectPart( player, host, mop.hitVec );
					if( sPart != null && sPart.part != null )
					{
						if( sPart.part.onShiftActivate( player, mop.hitVec ) )
						{
							if( world.isRemote )
							{
								NetworkHandler.instance.sendToServer( new PacketPartPlacement( x, y, z, face, getEyeOffset( player ) ) );
							}
							return true;
						}
					}
				}
			}
		}

		if( held == null || !( held.getItem() instanceof IPartItem ) )
		{
			return false;
		}

		int te_x = x;
		int te_y = y;
		int te_z = z;

		final IBlockDefinition multiPart = AEApi.instance().definitions().blocks().multiPart();
		if( host == null && pass == PlaceType.PLACE_ITEM )
		{
			ForgeDirection offset = ForgeDirection.UNKNOWN;

			Block blkID = world.getBlock( x, y, z );
			if( blkID != null && !blkID.isReplaceable( world, x, y, z ) )
			{
				offset = side;
				if( Platform.isServer() )
				{
					side = side.getOpposite();
				}
			}

			te_x = x + offset.offsetX;
			te_y = y + offset.offsetY;
			te_z = z + offset.offsetZ;

			tile = world.getTileEntity( te_x, te_y, te_z );
			if( tile instanceof IPartHost )
			{
				host = (IPartHost) tile;
			}

			if( host == null && tile != null && IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.FMP ) )
			{
				host = ( (IFMP) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.FMP ) ).getOrCreateHost( tile );
			}

			if( host == null && tile != null && IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.ImmibisMicroblocks ) )
			{
				host = ( (IImmibisMicroblocks) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.ImmibisMicroblocks ) ).getOrCreateHost( player, face, tile );
			}

			final Optional<ItemStack> maybeMultiPartStack = multiPart.maybeStack( 1 );
			final Optional<Block> maybeMultiPartBlock = multiPart.maybeBlock();
			final Optional<ItemBlock> maybeMultiPartItemBlock = multiPart.maybeItemBlock();

			final boolean hostIsNotPresent = host == null;
			final boolean multiPartPresent = maybeMultiPartBlock.isPresent() && maybeMultiPartStack.isPresent() && maybeMultiPartItemBlock.isPresent();
			final boolean canMultiPartBePlaced = maybeMultiPartBlock.get().canPlaceBlockAt( world, te_x, te_y, te_z );

			if( hostIsNotPresent && multiPartPresent && canMultiPartBePlaced && maybeMultiPartItemBlock.get().placeBlockAt( maybeMultiPartStack.get(), player, world, te_x, te_y, te_z, side.ordinal(), 0.5f, 0.5f, 0.5f, 0 ) )
			{
				if( !world.isRemote )
				{
					tile = world.getTileEntity( te_x, te_y, te_z );

					if( tile instanceof IPartHost )
					{
						host = (IPartHost) tile;
					}

					pass = PlaceType.INTERACT_SECOND_PASS;
				}
				else
				{
					player.swingItem();
					NetworkHandler.instance.sendToServer( new PacketPartPlacement( x, y, z, face, getEyeOffset( player ) ) );
					return true;
				}
			}
			else if( host != null && !host.canAddPart( held, side ) )
			{
				return false;
			}
		}

		if( host == null )
		{
			return false;
		}

		if( !host.canAddPart( held, side ) )
		{
			if( pass == PlaceType.INTERACT_FIRST_PASS || pass == PlaceType.PLACE_ITEM )
			{
				te_x = x + side.offsetX;
				te_y = y + side.offsetY;
				te_z = z + side.offsetZ;

				Block blkID = world.getBlock( te_x, te_y, te_z );
				tile = world.getTileEntity( te_x, te_y, te_z );

				if( tile != null && IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.FMP ) )
				{
					host = ( (IFMP) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.FMP ) ).getOrCreateHost( tile );
				}

				if( ( blkID == null || blkID.isReplaceable( world, te_x, te_y, te_z ) || host != null ) && side != ForgeDirection.UNKNOWN )
				{
					return place( held, te_x, te_y, te_z, side.getOpposite().ordinal(), player, world, pass == PlaceType.INTERACT_FIRST_PASS ? PlaceType.INTERACT_SECOND_PASS : PlaceType.PLACE_ITEM, depth + 1 );
				}
			}
			return false;
		}

		if( !world.isRemote )
		{
			Block block = world.getBlock( x, y, z );
			LookDirection dir = Platform.getPlayerRay( player, getEyeOffset( player ) );
			MovingObjectPosition mop = block.collisionRayTrace( world, x, y, z, dir.a, dir.b );
			if( mop != null )
			{
				SelectedPart sp = selectPart( player, host, mop.hitVec.addVector( -mop.blockX, -mop.blockY, -mop.blockZ ) );

				if( sp.part != null )
				{
					if( !player.isSneaking() && sp.part.onActivate( player, mop.hitVec ) )
					{
						return false;
					}
				}
			}

			DimensionalCoord dc = host.getLocation();
			if( !Platform.hasPermissions( dc, player ) )
			{
				return false;
			}

			ForgeDirection mySide = host.addPart( held, side, player );
			if( mySide != null )
			{
				for( Block multiPartBlock : multiPart.maybeBlock().asSet() )
				{
					final SoundType ss = multiPartBlock.stepSound;

					world.playSoundEffect( 0.5 + x, 0.5 + y, 0.5 + z, ss.func_150496_b(), ( ss.getVolume() + 1.0F ) / 2.0F, ss.getPitch() * 0.8F );
				}

				if( !player.capabilities.isCreativeMode )
				{
					held.stackSize--;
					if( held.stackSize == 0 )
					{
						player.inventory.mainInventory[player.inventory.currentItem] = null;
						MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( player, held ) );
					}
				}
			}
		}
		else
		{
			player.swingItem();
			NetworkHandler.instance.sendToServer( new PacketPartPlacement( x, y, z, face, getEyeOffset( player ) ) );
		}
		return true;
	}

	private static float getEyeOffset( EntityPlayer p )
	{
		if( p.worldObj.isRemote )
		{
			return Platform.getEyeOffset( p );
		}

		return eyeHeight;
	}

	private static SelectedPart selectPart( EntityPlayer player, IPartHost host, Vec3 pos )
	{
		CommonHelper.proxy.updateRenderMode( player );
		SelectedPart sp = host.selectPart( pos );
		CommonHelper.proxy.updateRenderMode( null );

		return sp;
	}

	public static IFacadePart isFacade( ItemStack held, ForgeDirection side )
	{
		if( held.getItem() instanceof IFacadeItem )
		{
			return ( (IFacadeItem) held.getItem() ).createPartFromItemStack( held, side );
		}

		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BC ) )
		{
			IBC bc = (IBC) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BC );
			if( bc.isFacade( held ) )
			{
				return bc.createFacadePart( held, side );
			}
		}

		return null;
	}

	@SubscribeEvent
	public void playerInteract( TickEvent.ClientTickEvent event )
	{
		this.wasCanceled = false;
	}

	@SubscribeEvent
	public void playerInteract( PlayerInteractEvent event )
	{
		if( event.action == Action.RIGHT_CLICK_AIR && event.entityPlayer.worldObj.isRemote )
		{
			// re-check to see if this event was already channeled, cause these two events are really stupid...
			MovingObjectPosition mop = Platform.rayTrace( event.entityPlayer, true, false );
			Minecraft mc = Minecraft.getMinecraft();

			float f = 1.0F;
			double d0 = mc.playerController.getBlockReachDistance();
			Vec3 vec3 = mc.renderViewEntity.getPosition( f );

			if( mop != null && mop.hitVec.distanceTo( vec3 ) < d0 )
			{
				World w = event.entity.worldObj;
				TileEntity te = w.getTileEntity( mop.blockX, mop.blockY, mop.blockZ );
				if( te instanceof IPartHost && this.wasCanceled )
				{
					event.setCanceled( true );
				}
			}
			else
			{
				ItemStack held = event.entityPlayer.getHeldItem();
				final IItems items = AEApi.instance().definitions().items();

				boolean supportedItem = items.memoryCard().isSameAs( held );
				supportedItem |= items.colorApplicator().isSameAs( held );

				if( event.entityPlayer.isSneaking() && held != null && supportedItem )
				{
					NetworkHandler.instance.sendToServer( new PacketClick( event.x, event.y, event.z, event.face, 0, 0, 0 ) );
				}
			}
		}
		else if( event.action == Action.RIGHT_CLICK_BLOCK && event.entityPlayer.worldObj.isRemote )
		{
			if( this.placing.get() != null )
			{
				return;
			}

			this.placing.set( event );

			ItemStack held = event.entityPlayer.getHeldItem();
			if( place( held, event.x, event.y, event.z, event.face, event.entityPlayer, event.entityPlayer.worldObj, PlaceType.INTERACT_FIRST_PASS, 0 ) )
			{
				event.setCanceled( true );
				this.wasCanceled = true;
			}

			this.placing.set( null );
		}
	}

	public enum PlaceType
	{
		PLACE_ITEM, INTERACT_FIRST_PASS, INTERACT_SECOND_PASS
	}
}