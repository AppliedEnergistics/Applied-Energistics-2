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

package appeng.items.tools;


import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

//import cofh.api.item.IToolHammer;

import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.networking.IGridHost;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.INetworkToolAgent;
import appeng.container.AEBaseContainer;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketClick;
import appeng.items.AEBaseItem;
import appeng.items.contents.NetworkToolViewer;
import appeng.util.Platform;


// TODO BC Integration
//@Interface( iface = "buildcraft.api.tools.IToolWrench", iname = IntegrationType.BuildCraftCore )
//@Interface( iface = "cofh.api.item.IToolHammer", modid = "cofhcore" )
public class ToolNetworkTool extends AEBaseItem implements IGuiItem, IAEWrench/*, IToolHammer *//* , IToolWrench */
{

	public ToolNetworkTool()
	{
		this.setMaxStackSize( 1 );
		this.setHarvestLevel( "wrench", 0 );
	}

	@Override
	public IGuiItemObject getGuiObject( final ItemStack is, final World world, final BlockPos pos )
	{
		final TileEntity te = world.getTileEntity( pos );
		return new NetworkToolViewer( is, (IGridHost) ( te instanceof IGridHost ? te : null ) );
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick( final World w, final PlayerEntity p, final Hand hand )
	{
		if( Platform.isClient() )
		{
			final RayTraceResult mop = AppEng.proxy.getRTR();

			if( mop == null || mop.getType() == RayTraceResult.Type.MISS )
			{
				NetworkHandler.instance().sendToServer( new PacketClick( BlockPos.ZERO, null, 0, 0, 0, hand ) );
			}
		}

		return new ActionResult<>( ActionResultType.SUCCESS, p.getHeldItem( hand ) );
	}

	@Override
	public ActionResultType onItemUseFirst( final PlayerEntity player, final World world, final BlockPos pos, final Direction side, final float hitX, final float hitY, final float hitZ, final Hand hand )
	{
		final RayTraceResult mop = new BlockRayTraceResult( new Vec3d( hitX, hitY, hitZ ), side, pos, false );
		final TileEntity te = world.getTileEntity( pos );

		if( te instanceof IPartHost )
		{
			final SelectedPart part = ( (IPartHost) te ).selectPart( mop.getHitVec() );

			if( part.part != null || part.facade != null )
			{
				if( part.part instanceof INetworkToolAgent && !( (INetworkToolAgent) part.part ).showNetworkInfo( mop ) )
				{
					return ActionResultType.FAIL;
				}
				else if( player.isSneaking() )
				{
					return ActionResultType.PASS;
				}
			}
		}
		else if( te instanceof INetworkToolAgent && !( (INetworkToolAgent) te ).showNetworkInfo( mop ) )
		{
			return ActionResultType.FAIL;
		}

		if( Platform.isClient() )
		{
			NetworkHandler.instance().sendToServer( new PacketClick( pos, side, hitX, hitY, hitZ, hand ) );
		}

		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean doesSneakBypassUse( final ItemStack itemstack, final IBlockReader world, final BlockPos pos, final PlayerEntity player )
	{
		return true;
	}

	public boolean serverSideToolLogic( final ItemStack is, final PlayerEntity p, final Hand hand, final World w, final BlockPos pos, final Direction side, final float hitX, final float hitY, final float hitZ )
	{
		if( side != null )
		{
			if( !Platform.hasPermissions( new DimensionalCoord( w, pos ), p ) )
			{
				return false;
			}

			final Block b = w.getBlockState( pos ).getBlock();
			if( !p.isSneaking() )
			{
				final TileEntity te = w.getTileEntity( pos );
				if( !( te instanceof IGridHost ) )
				{
					if( b.rotate( w, pos, side ) )
					{
						b.neighborChanged( Platform.AIR_BLOCK.getDefaultState(), w, pos, Platform.AIR_BLOCK, null );
						p.swingArm( hand );
						return !w.isRemote;
					}
				}
			}

			if( !p.isSneaking() )
			{
				if( p.openContainer instanceof AEBaseContainer )
				{
					return true;
				}

				final TileEntity te = w.getTileEntity( pos );

				if( te instanceof IGridHost )
				{
					Platform.openGUI( p, te, AEPartLocation.fromFacing( side ), GuiBridge.GUI_NETWORK_STATUS );
				}
				else
				{
					Platform.openGUI( p, null, AEPartLocation.INTERNAL, GuiBridge.GUI_NETWORK_TOOL );
				}

				return true;
			}
			else
			{
				b.onBlockActivated( w.getBlockState( pos ), w, pos, p, hand, rayTraceResult);
			}
		}
		else
		{
			Platform.openGUI( p, null, AEPartLocation.INTERNAL, GuiBridge.GUI_NETWORK_TOOL );
		}

		return false;
	}

	@Override
	public boolean canWrench( final ItemStack wrench, final PlayerEntity player, final BlockPos pos )
	{
		return true;
	}

	// IToolHammer - start
	/*@Override
	public boolean isUsable( ItemStack item, LivingEntity user, BlockPos pos )
	{
		return true;
	}

	@Override
	public boolean isUsable( ItemStack item, LivingEntity user, Entity entity )
	{
		return true;
	}

	@Override
	public void toolUsed( ItemStack item, LivingEntity user, BlockPos pos )
	{
	}

	@Override
	public void toolUsed( ItemStack item, LivingEntity user, Entity entity )
	{
	}*/
	// IToolHammer - end

	// TODO: BC WRENCH INTEGRATION

}
