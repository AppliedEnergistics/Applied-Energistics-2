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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import appeng.util.Platform;


final public class DispenserBlockTool extends BehaviorDefaultDispenseItem
{

	@Override
	protected ItemStack dispenseStack( IBlockSource dispenser, ItemStack dispensedItem )
	{
		Item i = dispensedItem.getItem();
		if( i instanceof IBlockTool )
		{
			EnumFacing enumfacing = BlockDispenser.func_149937_b( dispenser.getBlockMetadata() );
			IBlockTool tm = (IBlockTool) i;

			World w = dispenser.getWorld();
			if( w instanceof WorldServer )
			{
				int x = dispenser.getXInt() + enumfacing.getFrontOffsetX();
				int y = dispenser.getYInt() + enumfacing.getFrontOffsetY();
				int z = dispenser.getZInt() + enumfacing.getFrontOffsetZ();

				tm.onItemUse( dispensedItem, Platform.getPlayer( (WorldServer) w ), w, x, y, z, enumfacing.ordinal(), 0.5f, 0.5f, 0.5f );
			}
		}
		return dispensedItem;
	}
}
