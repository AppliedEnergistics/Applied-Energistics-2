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


import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.util.DimensionalCoord;
import appeng.core.features.AEFeature;
import appeng.integration.IntegrationType;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

import com.google.common.base.Optional;

// TODO BC Integration
//@Interface( iface = "buildcraft.api.tools.IToolWrench", iname = IntegrationType.BuildCraftCore )
public class ToolQuartzWrench extends AEBaseItem implements IAEWrench /*, IToolWrench */
{

	public ToolQuartzWrench( AEFeature type )
	{
		super( Optional.of( type.name() ) );

		this.setFeature( EnumSet.of( type, AEFeature.QuartzWrench ) );
		this.setMaxStackSize( 1 );
		this.setHarvestLevel( "wrench", 0 );
	}

	@Override
	public boolean onItemUseFirst(
			ItemStack stack,
			EntityPlayer player,
			World world,
			BlockPos pos,
			EnumFacing side,
			float hitX,
			float hitY,
			float hitZ )
	{
		Block b = world.getBlockState( pos ).getBlock();
		if( b != null && !player.isSneaking() && Platform.hasPermissions( new DimensionalCoord( world, pos ), player ) )
		{
			if( Platform.isClient() )
			{
				return !world.isRemote;
			}

			if( b.rotateBlock( world, pos, side ) )
			{
				b.onNeighborBlockChange( world, pos, Platform.AIR_BLOCK.getDefaultState(), Platform.AIR_BLOCK );
				player.swingItem();
				return !world.isRemote;
			}
		}
		return false;
	}

	@Override
	public boolean doesSneakBypassUse(
			World world,
			BlockPos pos,
			EntityPlayer player )
	{
		return true;
	}

	@Override
	public boolean canWrench(
			ItemStack wrench,
			EntityPlayer player,
			BlockPos pos )
	{
		return true;
	}

	// TODO: BC Wrench Integration
	/*
	@Override
	public boolean canWrench( EntityPlayer player, int x, int y, int z )
	{
		return true;
	}

	@Override
	public void wrenchUsed( EntityPlayer player, int x, int y, int z )
	{
		player.swingItem();
	}
	*/
}
