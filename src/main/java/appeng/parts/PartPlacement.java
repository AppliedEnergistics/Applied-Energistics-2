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


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import appeng.api.AEApi;
import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IItems;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketClick;
import appeng.core.sync.packets.PacketPartPlacement;
import appeng.facade.IFacadeItem;
import appeng.util.LookDirection;
import appeng.util.Platform;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PartPlacement
{

	private static float eyeHeight = 0.0f;
	private final ThreadLocal<Object> placing = new ThreadLocal<>();
	private boolean wasCanceled = false;

	// TODO refactor
	public static ActionResultType place( final ItemUseContext context, PlaceType pass, final int depth )
	{
		if( depth > 3 )
		{
			return ActionResultType.FAIL;
		}

		if( !context.getItem().isEmpty() && Platform.isWrench( context.getPlayer(), context.getItem(), context.getPos() ) && context.getPlayer().isSneaking() )
		{
			if( !Platform.hasPermissions( new DimensionalCoord( context.getWorld(), context.getPos() ), context.getPlayer() ) )
			{
				return ActionResultType.FAIL;
			}

			final Block block = context.getWorld().getBlockState( context.getPos() ).getBlock();
			final TileEntity tile = context.getWorld().getTileEntity( context.getPos() );
			IPartHost host = null;

			if( tile instanceof IPartHost )
			{
				host = (IPartHost) tile;
			}

			if( host != null )
			{
				if( !context.getWorld().isRemote )
				{
					final LookDirection dir = Platform.getPlayerRay( context.getPlayer(), getEyeOffset( context.getPlayer() ) );
					final RayTraceResult mop = block.collisionRayTrace( context.getWorld().getBlockState( context.getPos() ), context.getWorld(), context.getPos(), dir.getA(), dir.getB() );

					if( mop != null )
					{
						final List<ItemStack> is = new ArrayList<>();
						final SelectedPart sp = selectPart( context.getPlayer(), host,
								mop.getHitVec().add( -mop.getBlockPos().getX(), -mop.getBlockPos().getY(), -mop.getBlockPos().getZ() ) );

						if( sp.part != null )
						{
							is.add( sp.part.getItemStack( PartItemStack.WRENCH ) );
							sp.part.getDrops( is, true );
							host.removePart( sp.side, false );
						}

						if( sp.facade != null )
						{
							is.add( sp.facade.getItemStack() );
							host.getFacadeContainer().removeFacade( host, sp.side );
							Platform.notifyBlocksOfNeighbors( context.getWorld(), context.getPos() );
						}

						if( host.isEmpty() )
						{
							host.cleanup();
						}

						if( !is.isEmpty() )
						{
							Platform.spawnDrops( context.getWorld(), context.getPos(), is );
						}
					}
				}
				else
				{
					context.getPlayer().swingArm( context.getHand() );
					NetworkHandler.instance().sendToServer( new PacketPartPlacement( context.getPos(), side, getEyeOffset( context.getPlayer() ), context.getHand() ) );
				}
				return ActionResultType.SUCCESS;
			}

			return ActionResultType.PASS;
		}

		TileEntity tile = context.getWorld().getTileEntity( context.getPos() );
		IPartHost host = null;

		if( tile instanceof IPartHost )
		{
			host = (IPartHost) tile;
		}

		if( !context.getItem().isEmpty() )
		{
			final IFacadePart fp = isFacade( context.getItem(), AEPartLocation.fromFacing( context.getFace() ) );
			if( fp != null )
			{
				if( host != null )
				{
					if( !context.getWorld().isRemote )
					{
						if( host.getPart( AEPartLocation.INTERNAL ) == null )
						{
							return ActionResultType.FAIL;
						}

						if( host.canAddPart( context.getItem(), AEPartLocation.fromFacing( context.getFace() ) ) )
						{
							if( host.getFacadeContainer().addFacade( fp ) )
							{
								host.markForSave();
								host.markForUpdate();
								if( !context.getPlayer().capabilities.isCreativeMode )
								{
									context.getItem().grow( -1 );
									;
									if( context.getItem().getCount() == 0 )
									{
										context.getPlayer().inventory.mainInventory.set( context.getPlayer().inventory.currentItem, ItemStack.EMPTY );
										MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( context.getPlayer(), context.getItem(), context.getHand() ) );
									}
								}
								return ActionResultType.SUCCESS;
							}
						}
					}
					else
					{
						context.getPlayer().swingArm( context.getHand() );
						NetworkHandler.instance().sendToServer( new PacketPartPlacement( context.getPos(), context.getFace(), getEyeOffset( context.getPlayer() ), context.getHand() ) );
						return ActionResultType.SUCCESS;
					}
				}
				return ActionResultType.FAIL;
			}
		}

		if( context.getItem().isEmpty() )
		{
			final Block block = context.getWorld().getBlockState( context.getPos() ).getBlock();
			if( host != null && context.getPlayer().isSneaking() && block != null )
			{
				final LookDirection dir = Platform.getPlayerRay( context.getPlayer(), getEyeOffset( context.getPlayer() ) );
				final RayTraceResult mop = block.collisionRayTrace( context.getWorld().getBlockState( context.getPos() ), context, context.getPos(), dir.getA(), dir.getB() );

				if( mop != null )
				{
					mop.hitVec = mop.hitVec.addVector( -mop.getBlockPos().getX(), -mop.getBlockPos().getY(), -mop.getBlockPos().getZ() );
					final SelectedPart sPart = selectPart( context.getPlayer(), host, mop.getHitVec() );
					if( sPart != null && sPart.part != null )
					{
						if( sPart.part.onShiftActivate( context.getPlayer(), context.getHand(), mop.hitVec ) )
						{
							if( context.getWorld().isRemote )
							{
								NetworkHandler.instance().sendToServer( new PacketPartPlacement( context.getPos(), context.getFace(), getEyeOffset( context.getPlayer() ), context.getHand() ) );
							}
							return ActionResultType.SUCCESS;
						}
					}
				}
			}
		}

		if( context.getItem().isEmpty() || !( context.getItem().getItem() instanceof IPartItem ) )
		{
			return ActionResultType.PASS;
		}

		BlockPos te_pos = context.getPos();

		final IBlockDefinition multiPart = AEApi.instance().definitions().blocks().multiPart();
		if( host == null && pass == PlaceType.PLACE_ITEM )
		{
			Direction offset = null;

			final Block blkID = context.getWorld().getBlockState( context.getPos() ).getBlock();
			if( blkID != null && !blkID.isReplaceable( context.getWorld(), context.getPos() ) )
			{
				offset = context.getFace();
				if( Platform.isServer() )
				{
					side = context.getFace().getOpposite();
				}
			}

			te_pos = offset == null ? context.getPos() : context.getPos().offset( offset );

			tile = context.getWorld().getTileEntity( te_pos );
			if( tile instanceof IPartHost )
			{
				host = (IPartHost) tile;
			}

			final Optional<ItemStack> maybeMultiPartStack = multiPart.maybeStack( 1 );
			final Optional<Block> maybeMultiPartBlock = multiPart.maybeBlock();
			final Optional<BlockItem> maybeMultiPartItemBlock = multiPart.maybeItemBlock();

			final boolean hostIsNotPresent = host == null;
			final boolean multiPartPresent = maybeMultiPartBlock.isPresent() && maybeMultiPartStack.isPresent() && maybeMultiPartItemBlock.isPresent();
			final boolean canMultiPartBePlaced = maybeMultiPartBlock.get().canPlaceBlockAt( context.getWorld(), te_pos );

			if( hostIsNotPresent && multiPartPresent && canMultiPartBePlaced && maybeMultiPartItemBlock.get()
					.placeBlockAt( maybeMultiPartStack.get(), context.getPlayer(),
							context, te_pos, side, 0.5f, 0.5f, 0.5f, maybeMultiPartBlock.get().getDefaultState() ) )
			{
				if( !context.getWorld().isRemote )
				{
					tile = context.getWorld().getTileEntity( te_pos );

					if( tile instanceof IPartHost )
					{
						host = (IPartHost) tile;
					}

					pass = PlaceType.INTERACT_SECOND_PASS;
				}
				else
				{
					context.getPlayer().swingArm( context.getHand() );
					NetworkHandler.instance().sendToServer( new PacketPartPlacement( context.getPos(), side, getEyeOffset( context.getPlayer() ), context.getHand() ) );
					return ActionResultType.SUCCESS;
				}
			}
			else if( host != null && !host.canAddPart( context.getItem(), AEPartLocation.fromFacing( side ) ) )
			{
				return ActionResultType.FAIL;
			}
		}

		if( host == null )
		{
			return ActionResultType.PASS;
		}

		if( !host.canAddPart( context.getItem(), AEPartLocation.fromFacing( side ) ) )
		{
			if( pass == PlaceType.INTERACT_FIRST_PASS || pass == PlaceType.PLACE_ITEM )
			{
				te_pos = context.getPos().offset( side );

				final Block blkID = context.getWorld().getBlockState( te_pos ).getBlock();

				if( blkID == null || blkID.isReplaceable( context.getWorld(), te_pos ) || host != null )
				{
					return place( context,
							pass == PlaceType.INTERACT_FIRST_PASS ? PlaceType.INTERACT_SECOND_PASS : PlaceType.PLACE_ITEM, depth + 1 );
				}
			}
			return ActionResultType.PASS;
		}

		if( !context.getWorld().isRemote )
		{
			final BlockState state = context.getWorld().getBlockState( context.getPos() );
			final LookDirection dir = Platform.getPlayerRay( context.getPlayer(), getEyeOffset( context.getPlayer() ) );
			final RayTraceResult mop = state.getBlock().collisionRayTrace( state, context.getWorld(), context.getPos(), dir.getA(), dir.getB() );

			if( mop != null )
			{
				final SelectedPart sp = selectPart( context.getPlayer(), host,
						mop.hitVec.addVector( -mop.getBlockPos().getX(), -mop.getBlockPos().getY(), -mop.getBlockPos().getZ() ) );

				if( sp.part != null )
				{
					if( !context.getPlayer().isSneaking() && sp.part.onActivate( context.getPlayer(), context.getHand(), mop.getHitVec() ) )
					{
						return ActionResultType.FAIL;
					}
				}
			}

			final DimensionalCoord dc = host.getLocation();
			if( !Platform.hasPermissions( dc, context.getPlayer() ) )
			{
				return ActionResultType.FAIL;
			}

			final AEPartLocation mySide = host.addPart( context.getItem(), AEPartLocation.fromFacing( side ), context.getPlayer(), context.getHand() );
			if( mySide != null )
			{
				multiPart.maybeBlock().ifPresent( multiPartBlock ->
				{
					final SoundType ss = multiPartBlock.getSoundType( state, context.getWorld(), context.getPos(), context.getPlayer() );

					context.getWorld().playSound( null, context.getPos(), ss.getPlaceSound(), SoundCategory.BLOCKS, ( ss.getVolume() + 1.0F ) / 2.0F, ss.getPitch() * 0.8F );
				} );

				if( !context.getPlayer().getCapability( PlayerCapa ) )
				{
					context.getItem().grow( -1 );
					if( context.getItem().getCount() == 0 )
					{
						context.getPlayer().setHeldItem( context.getHand(), ItemStack.EMPTY );
						MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( context.getPlayer(), context.getItem(), context.getHand() ) );
					}
				}
			}
		}
		else
		{
			context.getPlayer().swingArm( context.getHand() );
		}
		return ActionResultType.SUCCESS;
	}

	private static float getEyeOffset( final PlayerEntity p )
	{
		if( p.world.isRemote )
		{
			return Platform.getEyeOffset( p );
		}

		return getEyeHeight();
	}

	private static SelectedPart selectPart( final PlayerEntity player, final IPartHost host, final Vec3d pos )
	{
		AppEng.proxy.updateRenderMode( player );
		final SelectedPart sp = host.selectPart( pos );
		AppEng.proxy.updateRenderMode( null );

		return sp;
	}

	public static IFacadePart isFacade( final ItemStack held, final AEPartLocation side )
	{
		if( held.getItem() instanceof IFacadeItem )
		{
			return ( (IFacadeItem) held.getItem() ).createPartFromItemStack( held, side );
		}

		return null;
	}

	@SubscribeEvent
	public void playerInteract( final TickEvent.ClientTickEvent event )
	{
		this.wasCanceled = false;
	}

	@SubscribeEvent
	public void playerInteract( final PlayerInteractEvent event )
	{
		// Only handle the main hand event
		if( event.getHand() != Hand.MAIN_HAND )
		{
			return;
		}

		if( event instanceof PlayerInteractEvent.RightClickEmpty && event.getPlayerEntity().world.isRemote )
		{
			// re-check to see if this event was already channeled, cause these two events are really stupid...
			final RayTraceResult mop = Platform.rayTrace( event.getPlayerEntity(), true, false );
			final Minecraft mc = Minecraft.getMinecraft();

			final float f = 1.0F;
			final double d0 = mc.playerController.getBlockReachDistance();
			final Vec3d vec3 = mc.getRenderViewEntity().getPositionEyes( f );

			if( mop != null && mop.hitVec.distanceTo( vec3 ) < d0 )
			{
				final World w = event.getEntity().world;
				final TileEntity te = w.getTileEntity( mop.getBlockPos() );
				if( te instanceof IPartHost && this.wasCanceled )
				{
					event.setCanceled( true );
				}
			}
			else
			{
				final ItemStack held = event.getPlayerEntity().getHeldItem( event.getHand() );
				final IItems items = AEApi.instance().definitions().items();

				boolean supportedItem = items.memoryCard().isSameAs( held );
				supportedItem |= items.colorApplicator().isSameAs( held );

				if( event.getPlayerEntity().isSneaking() && !held.isEmpty() && supportedItem )
				{
					NetworkHandler.instance().sendToServer( new PacketClick( event.getPos(), event.getFace(), 0, 0, 0, event.getHand() ) );
				}
			}
		}
		else if( event instanceof PlayerInteractEvent.RightClickBlock && !event.getPlayerEntity().world.isRemote )
		{
			if( this.placing.get() != null )
			{
				return;
			}

			this.placing.set( event );

			final ItemStack held = event.getPlayerEntity().getHeldItem( event.getHand() );
			if( place( event.getPlayerEntity().world,
					PlaceType.INTERACT_FIRST_PASS, 0 ) == ActionResultType.SUCCESS )
			{
				event.setCanceled( true );
				this.wasCanceled = true;
			}

			this.placing.set( null );
		}
	}

	private static float getEyeHeight()
	{
		return eyeHeight;
	}

	public static void setEyeHeight( final float eyeHeight )
	{
		PartPlacement.eyeHeight = eyeHeight;
	}

	public enum PlaceType
	{
		PLACE_ITEM, INTERACT_FIRST_PASS, INTERACT_SECOND_PASS
	}
}