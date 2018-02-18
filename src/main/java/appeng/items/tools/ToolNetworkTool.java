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


import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.networking.IGridHost;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.INetworkToolAgent;
import appeng.client.ClientHelper;
import appeng.container.AEBaseContainer;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketClick;
import appeng.integration.IntegrationType;
import appeng.items.AEBaseItem;
import appeng.items.contents.NetworkToolViewer;
import appeng.transformer.annotations.Integration.Interface;
import appeng.util.Platform;
import buildcraft.api.tools.IToolWrench;
import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.EnumSet;


@Interface( iface = "buildcraft.api.tools.IToolWrench", iname = IntegrationType.BuildCraftCore )
public class ToolNetworkTool extends AEBaseItem implements IGuiItem, IAEWrench, IToolWrench
{

	public ToolNetworkTool()
	{
		super( Optional.<String>absent() );

		this.setFeature( EnumSet.of( AEFeature.NetworkTool ) );
		this.setMaxStackSize( 1 );
		this.setHarvestLevel( "wrench", 0 );
	}

	@Override
	public IGuiItemObject getGuiObject( final ItemStack is, final World world, final int x, final int y, final int z )
	{
		final TileEntity te = world.getTileEntity( x, y, z );
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
				this.onItemUseFirst( it, p, w, 0, 0, 0, -1, 0, 0, 0 );
			}
			else
			{
				final int i = mop.blockX;
				final int j = mop.blockY;
				final int k = mop.blockZ;

				if( w.getBlock( i, j, k ).isAir( w, i, j, k ) )
				{
					this.onItemUseFirst( it, p, w, 0, 0, 0, -1, 0, 0, 0 );
				}
			}
		}

		return it;
	}

	@Override
	public boolean onItemUseFirst( final ItemStack is, final EntityPlayer player, final World world, final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( ForgeEventFactory.onItemUseStart( player, is, 1 ) <= 0 )
			return true;

		Block blk = world.getBlock( x, y, z );
		if( blk != null )
			if( ForgeEventFactory.onPlayerInteract( player,
					blk.isAir( world, x, y, z ) ? PlayerInteractEvent.Action.RIGHT_CLICK_AIR : PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,
					x, y, z, side, world ).isCanceled() )
				return true;

		final MovingObjectPosition mop = new MovingObjectPosition( x, y, z, side, Vec3.createVectorHelper( hitX, hitY, hitZ ) );
		final TileEntity te = world.getTileEntity( x, y, z );
		if( te instanceof IPartHost )
		{
			final SelectedPart part = ( (IPartHost) te ).selectPart( mop.hitVec );

			if( part.part != null || part.facade != null )
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
			NetworkHandler.instance.sendToServer( new PacketClick( x, y, z, side, hitX, hitY, hitZ ) );
		}
		return true;
	}

	@Override
	public boolean doesSneakBypassUse( final World world, final int x, final int y, final int z, final EntityPlayer player )
	{
		return true;
	}

	public boolean serverSideToolLogic( final ItemStack is, final EntityPlayer p, final World w, final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( side >= 0 )
		{
			if( !Platform.hasPermissions( new DimensionalCoord( w, x, y, z ), p ) )
			{
				return false;
			}

			final Block b = w.getBlock( x, y, z );

			if( b != null )
				if( ForgeEventFactory.onPlayerInteract( p,
						b.isAir( w, x, y, z ) ? PlayerInteractEvent.Action.RIGHT_CLICK_AIR : PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,
						x, y, z, side, w ).isCanceled() )
					return false;

			if( b != null && !p.isSneaking() )
			{
				final TileEntity te = w.getTileEntity( x, y, z );
				if( !( te instanceof IGridHost ) )
				{
					if( b.rotateBlock( w, x, y, z, ForgeDirection.getOrientation( side ) ) )
					{
						b.onNeighborBlockChange( w, x, y, z, Platform.AIR_BLOCK );
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

				final TileEntity te = w.getTileEntity( x, y, z );

				if( te instanceof IGridHost )
				{
					Platform.openGUI( p, te, ForgeDirection.getOrientation( side ), GuiBridge.GUI_NETWORK_STATUS );
				}
				else
				{
					Platform.openGUI( p, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_NETWORK_TOOL );
				}

				return true;
			}
			else
			{
				b.onBlockActivated( w, x, y, z, p, side, hitX, hitY, hitZ );
			}
		}
		else
		{
			Platform.openGUI( p, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_NETWORK_TOOL );
		}

		return false;
	}

	@Override
	public boolean canWrench( final ItemStack is, final EntityPlayer player, final int x, final int y, final int z )
	{
		return true;
	}

	@Override
	public boolean canWrench( final EntityPlayer player, final int x, final int y, final int z )
	{
		return true;
	}

	@Override
	public void wrenchUsed( final EntityPlayer player, final int x, final int y, final int z )
	{
		player.swingItem();
	}
}
