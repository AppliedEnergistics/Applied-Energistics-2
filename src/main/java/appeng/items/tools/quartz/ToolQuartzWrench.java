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

package appeng.items.tools.quartz;


import appeng.api.implementations.items.IAEWrench;
import appeng.api.util.DimensionalCoord;
import appeng.core.features.AEFeature;
import appeng.integration.IntegrationType;
import appeng.items.AEBaseItem;
import appeng.transformer.annotations.Integration.Interface;
import appeng.util.Platform;
import buildcraft.api.tools.IToolWrench;
import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.EnumSet;


@Interface( iface = "buildcraft.api.tools.IToolWrench", iname = IntegrationType.BuildCraftCore )
public class ToolQuartzWrench extends AEBaseItem implements IAEWrench, IToolWrench
{

	public ToolQuartzWrench( final AEFeature type )
	{
		super( Optional.of( type.name() ) );

		this.setFeature( EnumSet.of( type, AEFeature.QuartzWrench ) );
		this.setMaxStackSize( 1 );
		this.setHarvestLevel( "wrench", 0 );
	}

	@Override
	public boolean onItemUseFirst( final ItemStack is, final EntityPlayer player, final World world, final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( ForgeEventFactory.onItemUseStart( player, is, 1 ) <= 0 )
			return true;

		final Block b = world.getBlock( x, y, z );

		if( b != null && ForgeEventFactory.onPlayerInteract( player,
				b.isAir( world, x, y, z ) ? PlayerInteractEvent.Action.RIGHT_CLICK_AIR : PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,
				x, y, z, side, world ).isCanceled() )
			return true;

		if( b != null && !player.isSneaking() && Platform.hasPermissions( new DimensionalCoord( world, x, y, z ), player ) )
		{
			if( Platform.isClient() )
			{
				return !world.isRemote;
			}

			final ForgeDirection mySide = ForgeDirection.getOrientation( side );
			if( b.rotateBlock( world, x, y, z, mySide ) )
			{
				b.onNeighborBlockChange( world, x, y, z, Platform.AIR_BLOCK );
				player.swingItem();
				return !world.isRemote;
			}
		}
		return false;
	}

	@Override
	// public boolean shouldPassSneakingClickToBlock(World w, int x, int y, int z)
	public boolean doesSneakBypassUse( final World world, final int x, final int y, final int z, final EntityPlayer player )
	{
		return true;
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
