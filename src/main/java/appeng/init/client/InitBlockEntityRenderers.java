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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.render.crafting.CraftingMonitorTESR;
import appeng.client.render.crafting.MolecularAssemblerRenderer;
import appeng.client.render.tesr.ChargerTESR;
import appeng.client.render.tesr.ChestTileEntityRenderer;
import appeng.client.render.tesr.CrankTESR;
import appeng.client.render.tesr.DriveLedTileEntityRenderer;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.client.render.tesr.SkyCompassTESR;
import appeng.core.definitions.AEBlockEntities;
import appeng.tile.networking.CableBusTESR;
import net.minecraftforge.fmlclient.registry.ClientRegistry;

@OnlyIn(Dist.CLIENT)
public final class InitBlockEntityRenderers {

    private InitBlockEntityRenderers() {
    }

    public static void init() {

        ClientRegistry.bindTileEntityRenderer(AEBlockEntities.INSCRIBER, InscriberTESR::new);
        ClientRegistry.bindTileEntityRenderer(AEBlockEntities.SKY_CHEST, SkyChestTESR::new);
        ClientRegistry.bindTileEntityRenderer(AEBlockEntities.CRANK, CrankTESR::new);
        ClientRegistry.bindTileEntityRenderer(AEBlockEntities.CHARGER, ChargerTESR.FACTORY);
        ClientRegistry.bindTileEntityRenderer(AEBlockEntities.DRIVE, DriveLedTileEntityRenderer::new);
        ClientRegistry.bindTileEntityRenderer(AEBlockEntities.CHEST, ChestTileEntityRenderer::new);
        ClientRegistry.bindTileEntityRenderer(AEBlockEntities.CRAFTING_MONITOR, CraftingMonitorTESR::new);
        ClientRegistry.bindTileEntityRenderer(AEBlockEntities.MOLECULAR_ASSEMBLER, MolecularAssemblerRenderer::new);
        ClientRegistry.bindTileEntityRenderer(AEBlockEntities.CABLE_BUS, CableBusTESR::new);
        ClientRegistry.bindTileEntityRenderer(AEBlockEntities.SKY_COMPASS, SkyCompassTESR::new);

    }

}
