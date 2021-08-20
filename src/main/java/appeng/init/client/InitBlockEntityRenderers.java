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

package appeng.init.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import appeng.blockentity.networking.CableBusTESR;
import appeng.client.render.crafting.CraftingMonitorTESR;
import appeng.client.render.crafting.MolecularAssemblerRenderer;
import appeng.client.render.tesr.ChargerTESR;
import appeng.client.render.tesr.ChestBlockEntityRenderer;
import appeng.client.render.tesr.CrankTESR;
import appeng.client.render.tesr.DriveLedBlockEntityRenderer;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.client.render.tesr.SkyCompassTESR;
import appeng.core.definitions.AEBlockEntities;

@Environment(EnvType.CLIENT)
public final class InitBlockEntityRenderers {

    private InitBlockEntityRenderers() {
    }

    public static void init() {

        register(AEBlockEntities.INSCRIBER, InscriberTESR::new);
        register(AEBlockEntities.SKY_CHEST, SkyChestTESR::new);
        register(AEBlockEntities.CRANK, CrankTESR::new);
        register(AEBlockEntities.CHARGER, ChargerTESR.FACTORY);
        register(AEBlockEntities.DRIVE, DriveLedBlockEntityRenderer::new);
        register(AEBlockEntities.CHEST, ChestBlockEntityRenderer::new);
        register(AEBlockEntities.CRAFTING_MONITOR, CraftingMonitorTESR::new);
        register(AEBlockEntities.MOLECULAR_ASSEMBLER, MolecularAssemblerRenderer::new);
        register(AEBlockEntities.CABLE_BUS, CableBusTESR::new);
        register(AEBlockEntities.SKY_COMPASS, SkyCompassTESR::new);

    }

    private static <T extends BlockEntity> void register(BlockEntityType<T> type,
            BlockEntityRendererProvider<T> factory) {
        BlockEntityRendererRegistry.INSTANCE.register(type, factory);
    }

}
