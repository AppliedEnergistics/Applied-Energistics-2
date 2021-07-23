/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.init;

import net.minecraft.world.level.block.DispenserBlock;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.hooks.BlockToolDispenseItemBehavior;
import appeng.hooks.MatterCannonDispenseItemBehavior;
import appeng.hooks.TinyTNTDispenseItemBehavior;

/**
 * Registers custom {@link DispenserBlock} behaviors for our items.
 */
public final class InitDispenserBehavior {

    private InitDispenserBehavior() {
    }

    public static void init() {
        DispenserBlock.registerBehavior(AEBlocks.TINY_TNT, new TinyTNTDispenseItemBehavior());
        DispenserBlock.registerBehavior(AEItems.ENTROPY_MANIPULATOR, new BlockToolDispenseItemBehavior());
        DispenserBlock.registerBehavior(AEItems.MASS_CANNON, new MatterCannonDispenseItemBehavior());
        DispenserBlock.registerBehavior(AEItems.COLOR_APPLICATOR, new BlockToolDispenseItemBehavior());
    }

}
