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

package appeng.debug;


import appeng.worldgen.MeteoritePlacer;
import appeng.worldgen.MeteoriteSpawner;
import appeng.worldgen.PlacedMeteoriteSettings;
import appeng.worldgen.meteorite.StandardWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import appeng.items.AEBaseItem;


public class ToolMeteoritePlacer extends AEBaseItem
{

	public ToolMeteoritePlacer(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		if( context.getWorld().isRemote() )
		{
			return ActionResultType.PASS;
		}

		PlayerEntity player = context.getPlayer();
		World world = context.getWorld();
		BlockPos pos = context.getPos();

		if (player == null) {
			return ActionResultType.PASS;
		}

 		final MeteoriteSpawner ms = new MeteoriteSpawner();
		PlacedMeteoriteSettings spawned = ms.trySpawnMeteorite(world, pos);

 		if( spawned == null )
 		{
 			player.sendMessage( new StringTextComponent( "Un-suitable Location." ) );
 			return ActionResultType.FAIL;
 		}

		final MeteoritePlacer placer = new MeteoritePlacer(world, spawned);
		placer.place();

		return ActionResultType.SUCCESS;
	}
}
