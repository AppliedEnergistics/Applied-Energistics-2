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

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;

import team.reborn.energy.api.EnergyStorage;

import appeng.api.features.P2PTunnelAttunement;
import appeng.core.definitions.AEParts;
import appeng.core.localization.InGameTooltip;

public final class InitP2PAttunements {

    private InitP2PAttunements() {
    }

    public static void init() {
        P2PTunnelAttunement.registerAttunementTag(AEParts.ME_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.FE_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.REDSTONE_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.FLUID_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.ITEM_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.LIGHT_P2P_TUNNEL);

        P2PTunnelAttunement.registerAttunementApi(P2PTunnelAttunement.ENERGY_TUNNEL, EnergyStorage.ITEM,
                InGameTooltip.P2PAttunementEnergy.text());
        P2PTunnelAttunement.registerAttunementApi(P2PTunnelAttunement.FLUID_TUNNEL, FluidStorage.ITEM,
                InGameTooltip.P2PAttunementFluid.text());
    }
}
