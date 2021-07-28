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

package appeng.init.internal;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

import appeng.api.movable.IMovableRegistry;
import appeng.core.Api;
import appeng.core.definitions.AEBlockEntities;

public final class InitSpatialMovableRegistry {

    private InitSpatialMovableRegistry() {
    }

    public static void init() {
        final IMovableRegistry mr = Api.instance().registries().movable();

        /*
         * You can't move bed rock.
         */
        mr.blacklistBlock(Blocks.BEDROCK);

        /*
         * White List Vanilla...
         */
        mr.whitelistBlockEntity(BlockEntityType.BANNER);
        mr.whitelistBlockEntity(BlockEntityType.BEACON);
        mr.whitelistBlockEntity(BlockEntityType.BREWING_STAND);
        mr.whitelistBlockEntity(BlockEntityType.CHEST);
        mr.whitelistBlockEntity(BlockEntityType.COMMAND_BLOCK);
        mr.whitelistBlockEntity(BlockEntityType.COMPARATOR);
        mr.whitelistBlockEntity(BlockEntityType.DAYLIGHT_DETECTOR);
        mr.whitelistBlockEntity(BlockEntityType.DISPENSER);
        mr.whitelistBlockEntity(BlockEntityType.DROPPER);
        mr.whitelistBlockEntity(BlockEntityType.ENCHANTING_TABLE);
        mr.whitelistBlockEntity(BlockEntityType.ENDER_CHEST);
        mr.whitelistBlockEntity(BlockEntityType.END_PORTAL);
        mr.whitelistBlockEntity(BlockEntityType.FURNACE);
        mr.whitelistBlockEntity(BlockEntityType.HOPPER);
        mr.whitelistBlockEntity(BlockEntityType.MOB_SPAWNER);
        mr.whitelistBlockEntity(BlockEntityType.PISTON);
        mr.whitelistBlockEntity(BlockEntityType.SHULKER_BOX);
        mr.whitelistBlockEntity(BlockEntityType.SIGN);
        mr.whitelistBlockEntity(BlockEntityType.SKULL);

        /*
         * Whitelist all of AE2
         */
        AEBlockEntities.getBlockEntityTypes().values().forEach(mr::whitelistBlockEntity);
    }

}
