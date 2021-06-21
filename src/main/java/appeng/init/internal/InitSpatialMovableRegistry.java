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

import net.minecraft.block.Blocks;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.ComparatorTileEntity;
import net.minecraft.tileentity.DaylightDetectorTileEntity;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.DropperTileEntity;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.tileentity.EndPortalTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.SkullTileEntity;

import appeng.api.movable.IMovableRegistry;
import appeng.core.Api;
import appeng.tile.AEBaseTileEntity;

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
        mr.whiteListTileEntity(BannerTileEntity.class);
        mr.whiteListTileEntity(BeaconTileEntity.class);
        mr.whiteListTileEntity(BrewingStandTileEntity.class);
        mr.whiteListTileEntity(ChestTileEntity.class);
        mr.whiteListTileEntity(CommandBlockTileEntity.class);
        mr.whiteListTileEntity(ComparatorTileEntity.class);
        mr.whiteListTileEntity(DaylightDetectorTileEntity.class);
        mr.whiteListTileEntity(DispenserTileEntity.class);
        mr.whiteListTileEntity(DropperTileEntity.class);
        mr.whiteListTileEntity(EnchantingTableTileEntity.class);
        mr.whiteListTileEntity(EnderChestTileEntity.class);
        mr.whiteListTileEntity(EndPortalTileEntity.class);
        mr.whiteListTileEntity(FurnaceTileEntity.class);
        mr.whiteListTileEntity(HopperTileEntity.class);
        mr.whiteListTileEntity(MobSpawnerTileEntity.class);
        mr.whiteListTileEntity(PistonTileEntity.class);
        mr.whiteListTileEntity(ShulkerBoxTileEntity.class);
        mr.whiteListTileEntity(SignTileEntity.class);
        mr.whiteListTileEntity(SkullTileEntity.class);

        /*
         * Whitelist AE2
         */
        mr.whiteListTileEntity(AEBaseTileEntity.class);
    }

}
