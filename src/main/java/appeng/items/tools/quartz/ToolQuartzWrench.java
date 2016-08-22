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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import appeng.api.implementations.items.IAEWrench;
import appeng.api.util.DimensionalCoord;
import appeng.items.AEBaseItem;
import appeng.util.Platform;


// TODO BC Integration
//@Interface( iface = "buildcraft.api.tools.IToolWrench", iname = IntegrationType.BuildCraftCore )
public class ToolQuartzWrench extends AEBaseItem implements IAEWrench /* , IToolWrench */
{

	public ToolQuartzWrench()
	{
		this.setMaxStackSize( 1 );
		this.setHarvestLevel( "wrench", 0 );
	}

	@Override
	public EnumActionResult onItemUseFirst( final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand )
	{
		final Block b = world.getBlockState( pos ).getBlock();
		if( b != null && !player.isSneaking() && Platform.hasPermissions( new DimensionalCoord( world, pos ), player ) )
		{
			if( Platform.isClient() )
			{
				//TODO 1.10-R - if we return FAIL on client, action will not be sent to server. Fix that in all Block#onItemUseFirst overrides.
				return !world.isRemote ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
			}

			if( b.rotateBlock( world, pos, side ) )
			{
				b.neighborChanged( Platform.AIR_BLOCK.getDefaultState(), world, pos, Platform.AIR_BLOCK );
				player.swingArm( hand );
				return !world.isRemote ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
			}
		}
		return EnumActionResult.PASS;
	}

	@Override
	public boolean doesSneakBypassUse( final ItemStack itemstack, final IBlockAccess world, final BlockPos pos, final EntityPlayer player )
	{
		return true;
	}

	@Override
	public boolean canWrench( final ItemStack wrench, final EntityPlayer player, final BlockPos pos )
	{
		return true;
	}

	// TODO: BC Wrench Integration
	/*
	 * @Override
	 * public boolean canWrench( EntityPlayer player, int x, int y, int z )
	 * {
	 * return true;
	 * }
	 * @Override
	 * public void wrenchUsed( EntityPlayer player, int x, int y, int z )
	 * {
	 * player.swingItem();
	 * }
	 */
}
