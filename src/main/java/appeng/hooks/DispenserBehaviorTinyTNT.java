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

package appeng.hooks;


import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import appeng.entity.EntityTinyTNTPrimed;


public final class DispenserBehaviorTinyTNT extends BehaviorDefaultDispenseItem
{

	@Override
	protected ItemStack dispenseStack( IBlockSource dispenser, ItemStack dispensedItem )
	{
		EnumFacing enumfacing = BlockDispenser.func_149937_b( dispenser.getBlockMetadata() );
		World world = dispenser.getWorld();
		int i = dispenser.getXInt() + enumfacing.getFrontOffsetX();
		int j = dispenser.getYInt() + enumfacing.getFrontOffsetY();
		int k = dispenser.getZInt() + enumfacing.getFrontOffsetZ();
		EntityTinyTNTPrimed primedTinyTNTEntity = new EntityTinyTNTPrimed( world, i + 0.5F, j + 0.5F, k + 0.5F, null );
		world.spawnEntityInWorld( primedTinyTNTEntity );
		--dispensedItem.stackSize;
		return dispensedItem;
	}
}
