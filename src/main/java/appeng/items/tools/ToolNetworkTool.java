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


import java.util.EnumSet;

import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.networking.IGridHost;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.INetworkToolAgent;
import appeng.client.ClientHelper;
import appeng.container.AEBaseContainer;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketClick;
import appeng.items.AEBaseItem;
import appeng.items.contents.NetworkToolViewer;
import appeng.util.Platform;


// TODO BC Integration
//@Interface( iface = "buildcraft.api.tools.IToolWrench", iname = IntegrationType.BuildCraftCore )
public class ToolNetworkTool extends AEBaseItem implements IGuiItem, IAEWrench /* , IToolWrench */
{

	public ToolNetworkTool()
	{
		super( Optional.<String>absent() );

		this.setFeature( EnumSet.of( AEFeature.NetworkTool ) );
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
	public ItemStack onItemRightClick( final ItemStack it, final World w, final EntityPlayer p )
	{
		if( Platform.isClient() )
		{
			final MovingObjectPosition mop = ClientHelper.proxy.getMOP();

			if( mop == null )
			{
				this.onItemUseFirst( it, p, w, new BlockPos( 0, 0, 0 ), null, 0, 0, 0 ); // eh?
			}
			else
			{
				if( w.getBlockState( mop.getBlockPos() ).getBlock().isAir( w, mop.getBlockPos() ) )
				{
					this.onItemUseFirst( it, p, w, new BlockPos( 0, 0, 0 ), null, 0, 0, 0 ); // eh?
				}
			}
		}

		return it;
	}

	@Override
	public boolean onItemUseFirst( final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		final MovingObjectPosition mop = new MovingObjectPosition( new Vec3( hitX, hitY, hitZ ), side, pos );
		final TileEntity te = world.getTileEntity( pos );
		if( te instanceof IPartHost )
		{
			final SelectedPart part = ( (IPartHost) te ).selectPart( mop.hitVec );
			if( part.part != null )
			{
				if( part.part instanceof INetworkToolAgent && !( (INetworkToolAgent) part.part ).showNetworkInfo( mop ) )
				{
					return false;
				}
			}
		}
		else if( te instanceof INetworkToolAgent && !( (INetworkToolAgent) te ).showNetworkInfo( mop ) )
		{
			return false;
		}

		if( Platform.isClient() )
		{
			NetworkHandler.instance.sendToServer( new PacketClick( pos, side, hitX, hitY, hitZ ) );
		}
		return true;
	}

	@Override
	public boolean doesSneakBypassUse( final World world, final BlockPos pos, final EntityPlayer player )
	{
		return true;
	}

	public boolean serverSideToolLogic( final ItemStack is, final EntityPlayer p, final World w, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		if( side != null )
		{
			if( !Platform.hasPermissions( new DimensionalCoord( w, pos ), p ) )
			{
				return false;
			}

			final Block b = w.getBlockState( pos ).getBlock();
			if( b != null && !p.isSneaking() )
			{
				final TileEntity te = w.getTileEntity( pos );
				if( !( te instanceof IGridHost ) )
				{
					if( b.rotateBlock( w, pos, side ) )
					{
						b.onNeighborBlockChange( w, pos, Platform.AIR_BLOCK.getDefaultState(), Platform.AIR_BLOCK );
						p.swingItem();
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
				b.onBlockActivated( w, pos, w.getBlockState( pos ), p, side, hitX, hitY, hitZ );
			}
		}
		else
		{
			Platform.openGUI( p, null, AEPartLocation.INTERNAL, GuiBridge.GUI_NETWORK_TOOL );
		}

		return false;
	}

	@Override
	public boolean canWrench( final ItemStack wrench, final EntityPlayer player, final BlockPos pos )
	{
		return true;
	}

	// TODO: BC WRENCH INTEGRATION

}
