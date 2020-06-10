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


import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional.Interface;

import cofh.api.item.IToolHammer;

import appeng.api.implementations.items.IAEWrench;
import appeng.api.util.DimensionalCoord;
import appeng.items.AEBaseItem;
import appeng.util.Platform;


// TODO BC Integration
//@Interface( iface = "buildcraft.api.tools.IToolWrench", iname = IntegrationType.BuildCraftCore )
@Interface( iface = "cofh.api.item.IToolHammer", modid = "cofhcore" )
public class ToolQuartzWrench extends AEBaseItem implements IAEWrench, IToolHammer /* , IToolWrench */
{

	public ToolQuartzWrench()
	{
		this.setMaxStackSize( 1 );
		this.setHarvestLevel( "wrench", 0 );
	}

	@Override
	public ActionResultType onItemUseFirst( final PlayerEntity player, final World world, final BlockPos pos, final Direction side, final float hitX, final float hitY, final float hitZ, final Hand hand )
	{
		final Block b = world.getBlockState( pos ).getBlock();
		if( b != null && !player.isSneaking() && Platform.hasPermissions( new DimensionalCoord( world, pos ), player ) )
		{
			if( Platform.isClient() )
			{
				// TODO 1.10-R - if we return FAIL on client, action will not be sent to server. Fix that in all
				// Block#onItemUseFirst overrides.
				return !world.isRemote ? ActionResultType.SUCCESS : ActionResultType.PASS;
			}

			if( b.rotateBlock( world, pos, side ) )
			{
				player.swingArm( hand );
				return !world.isRemote ? ActionResultType.SUCCESS : ActionResultType.FAIL;
			}
		}
		return ActionResultType.PASS;
	}

	@Override
	public boolean doesSneakBypassUse( final ItemStack itemstack, final IBlockReader world, final BlockPos pos, final PlayerEntity player )
	{
		return true;
	}

	@Override
	public boolean canWrench( final ItemStack wrench, final PlayerEntity player, final BlockPos pos )
	{
		return true;
	}

	// IToolHammer - start
	@Override
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
	}

	// IToolHammer - end

	// TODO: BC Wrench Integration
	/*
	 * @Override
	 * public boolean canWrench( PlayerEntity player, int x, int y, int z )
	 * {
	 * return true;
	 * }
	 * @Override
	 * public void wrenchUsed( PlayerEntity player, int x, int y, int z )
	 * {
	 * player.swingItem();
	 * }
	 */
}
