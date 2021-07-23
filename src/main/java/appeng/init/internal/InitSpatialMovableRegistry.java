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
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

import appeng.api.movable.IMovableRegistry;
import appeng.core.Api;
import appeng.tile.AEBaseTileEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;

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
        mr.whiteListTileEntity(BannerBlockEntity.class);
        mr.whiteListTileEntity(BeaconBlockEntity.class);
        mr.whiteListTileEntity(BrewingStandBlockEntity.class);
        mr.whiteListTileEntity(ChestBlockEntity.class);
        mr.whiteListTileEntity(CommandBlockEntity.class);
        mr.whiteListTileEntity(ComparatorBlockEntity.class);
        mr.whiteListTileEntity(DaylightDetectorBlockEntity.class);
        mr.whiteListTileEntity(DispenserBlockEntity.class);
        mr.whiteListTileEntity(DropperBlockEntity.class);
        mr.whiteListTileEntity(EnchantmentTableBlockEntity.class);
        mr.whiteListTileEntity(EnderChestBlockEntity.class);
        mr.whiteListTileEntity(TheEndPortalBlockEntity.class);
        mr.whiteListTileEntity(FurnaceBlockEntity.class);
        mr.whiteListTileEntity(HopperBlockEntity.class);
        mr.whiteListTileEntity(SpawnerBlockEntity.class);
        mr.whiteListTileEntity(PistonMovingBlockEntity.class);
        mr.whiteListTileEntity(ShulkerBoxBlockEntity.class);
        mr.whiteListTileEntity(SignBlockEntity.class);
        mr.whiteListTileEntity(SkullBlockEntity.class);

        /*
         * Whitelist AE2
         */
        mr.whiteListTileEntity(AEBaseTileEntity.class);
    }

}
