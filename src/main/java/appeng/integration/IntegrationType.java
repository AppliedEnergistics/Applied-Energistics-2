/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.integration;


import appeng.integration.modules.crafttweaker.CTModule;
import appeng.integration.modules.ic2.IC2Module;
import appeng.integration.modules.inventorytweaks.InventoryTweaksModule;
import appeng.integration.modules.jei.JEIModule;
import appeng.integration.modules.theoneprobe.TheOneProbeModule;
import appeng.integration.modules.waila.WailaModule;


public enum IntegrationType {
    IC2(IntegrationSide.BOTH, "Industrial Craft 2", "ic2") {
        @Override
        public IIntegrationModule createInstance() {
            return Integrations.setIc2(new IC2Module());
        }
    },

    GTCE(IntegrationSide.BOTH, "GregTech", "gregtech"),

    RC(IntegrationSide.BOTH, "Railcraft", "railcraft"),

    MFR(IntegrationSide.BOTH, "Mine Factory Reloaded", "minefactoryreloaded"),

    Waila(IntegrationSide.BOTH, "Waila", "waila") {
        @Override
        public IIntegrationModule createInstance() {
            return new WailaModule();
        }
    },

    InvTweaks(IntegrationSide.CLIENT, "Inventory Tweaks", "inventorytweaks") {
        @Override
        public IIntegrationModule createInstance() {
            return Integrations.setInvTweaks(new InventoryTweaksModule());
        }
    },

    JEI(IntegrationSide.CLIENT, "Just Enough Items", "jei") {
        @Override
        public IIntegrationModule createInstance() {
            return Integrations.setJei(new JEIModule());
        }
    },

    Mekanism(IntegrationSide.BOTH, "Mekanism", "mekanism"),

    OpenComputers(IntegrationSide.BOTH, "OpenComputers", "opencomputers"),

    THE_ONE_PROBE(IntegrationSide.BOTH, "TheOneProbe", "theoneprobe") {
        @Override
        public IIntegrationModule createInstance() {
            return new TheOneProbeModule();
        }
    },

    TESLA(IntegrationSide.BOTH, "Tesla", "tesla"),

    CRAFTTWEAKER(IntegrationSide.BOTH, "CraftTweaker", "crafttweaker") {
        @Override
        public IIntegrationModule createInstance() {
            return new CTModule();
        }
    };

    public final IntegrationSide side;
    public final String dspName;
    public final String modID;

    IntegrationType(final IntegrationSide side, final String name, final String modid) {
        this.side = side;
        this.dspName = name;
        this.modID = modid;
    }

    public IIntegrationModule createInstance() {
        return new IIntegrationModule() {
        };
    }

}
