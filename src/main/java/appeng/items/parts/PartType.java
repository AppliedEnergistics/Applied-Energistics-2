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

package appeng.items.parts;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.features.AEFeature;
import appeng.api.parts.IPart;
import appeng.fluids.parts.PartFluidAnnihilationPlane;
import appeng.fluids.parts.PartFluidExportBus;
import appeng.fluids.parts.PartFluidFormationPlane;
import appeng.fluids.parts.PartFluidImportBus;
import appeng.fluids.parts.PartFluidInterface;
import appeng.fluids.parts.PartFluidLevelEmitter;
import appeng.fluids.parts.PartFluidStorageBus;
import appeng.fluids.parts.PartFluidTerminal;
import appeng.parts.automation.PartAnnihilationPlane;
import appeng.parts.automation.PartExportBus;
import appeng.parts.automation.PartFormationPlane;
import appeng.parts.automation.PartIdentityAnnihilationPlane;
import appeng.parts.automation.PartImportBus;
import appeng.parts.automation.PartLevelEmitter;
import appeng.parts.misc.PartCableAnchor;
import appeng.parts.misc.PartInterface;
import appeng.parts.misc.PartInvertedToggleBus;
import appeng.parts.misc.PartStorageBus;
import appeng.parts.misc.PartToggleBus;
import appeng.parts.networking.PartCableCovered;
import appeng.parts.networking.PartCableGlass;
import appeng.parts.networking.PartCableSmart;
import appeng.parts.networking.PartDenseCableCovered;
import appeng.parts.networking.PartDenseCableSmart;
import appeng.parts.networking.PartQuartzFiber;
import appeng.parts.p2p.PartP2PFEPower;
import appeng.parts.p2p.PartP2PFluids;
import appeng.parts.p2p.PartP2PItems;
import appeng.parts.p2p.PartP2PLight;
import appeng.parts.p2p.PartP2PRedstone;
import appeng.parts.p2p.PartP2PTunnelME;
import appeng.parts.reporting.PartConversionMonitor;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartDarkPanel;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.parts.reporting.PartPanel;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartSemiDarkPanel;
import appeng.parts.reporting.PartStorageMonitor;
import appeng.parts.reporting.PartTerminal;

public enum PartType {
    INVALID_TYPE(EnumSet.of(AEFeature.CORE), EnumSet.noneOf(IntegrationType.class), null),

    CABLE_GLASS(EnumSet.of(AEFeature.GLASS_CABLES), EnumSet.noneOf(IntegrationType.class), PartCableGlass.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    CABLE_COVERED(EnumSet.of(AEFeature.COVERED_CABLES), EnumSet.noneOf(IntegrationType.class), PartCableCovered.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    CABLE_SMART(EnumSet.of(AEFeature.CHANNELS, AEFeature.SMART_CABLES), EnumSet.noneOf(IntegrationType.class),
            PartCableSmart.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    CABLE_DENSE_SMART(EnumSet.of(AEFeature.CHANNELS, AEFeature.DENSE_CABLES), EnumSet.noneOf(IntegrationType.class),
            PartDenseCableSmart.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    CABLE_DENSE_COVERED(EnumSet.of(AEFeature.CHANNELS, AEFeature.DENSE_CABLES), EnumSet.noneOf(IntegrationType.class),
            PartDenseCableCovered.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    TOGGLE_BUS(EnumSet.of(AEFeature.TOGGLE_BUS), EnumSet.noneOf(IntegrationType.class), PartToggleBus.class),

    INVERTED_TOGGLE_BUS(EnumSet.of(AEFeature.TOGGLE_BUS), EnumSet.noneOf(IntegrationType.class),
            PartInvertedToggleBus.class),

    CABLE_ANCHOR(EnumSet.of(AEFeature.CABLE_ANCHOR), EnumSet.noneOf(IntegrationType.class), PartCableAnchor.class),

    QUARTZ_FIBER(EnumSet.of(AEFeature.QUARTZ_FIBER), EnumSet.noneOf(IntegrationType.class), PartQuartzFiber.class),

    MONITOR(EnumSet.of(AEFeature.PANELS), EnumSet.noneOf(IntegrationType.class), PartPanel.class),

    SEMI_DARK_MONITOR(EnumSet.of(AEFeature.PANELS), EnumSet.noneOf(IntegrationType.class), PartSemiDarkPanel.class),

    DARK_MONITOR(EnumSet.of(AEFeature.PANELS), EnumSet.noneOf(IntegrationType.class), PartDarkPanel.class),

    STORAGE_BUS(EnumSet.of(AEFeature.STORAGE_BUS), EnumSet.noneOf(IntegrationType.class), PartStorageBus.class),
    FLUID_STORAGE_BUS(EnumSet.of(AEFeature.FLUID_STORAGE_BUS), EnumSet.noneOf(IntegrationType.class),
            PartFluidStorageBus.class),

    IMPORT_BUS(EnumSet.of(AEFeature.IMPORT_BUS), EnumSet.noneOf(IntegrationType.class), PartImportBus.class),

    FLUID_IMPORT_BUS(EnumSet.of(AEFeature.FLUID_IMPORT_BUS), EnumSet.noneOf(IntegrationType.class),
            PartFluidImportBus.class),

    EXPORT_BUS(EnumSet.of(AEFeature.EXPORT_BUS), EnumSet.noneOf(IntegrationType.class), PartExportBus.class),

    FLUID_EXPORT_BUS(EnumSet.of(AEFeature.FLUID_EXPORT_BUS), EnumSet.noneOf(IntegrationType.class),
            PartFluidExportBus.class),

    LEVEL_EMITTER(EnumSet.of(AEFeature.LEVEL_EMITTER), EnumSet.noneOf(IntegrationType.class), PartLevelEmitter.class),
    FLUID_LEVEL_EMITTER(EnumSet.of(AEFeature.FLUID_LEVEL_EMITTER), EnumSet.noneOf(IntegrationType.class),
            PartFluidLevelEmitter.class),

    ANNIHILATION_PLANE(EnumSet.of(AEFeature.ANNIHILATION_PLANE), EnumSet.noneOf(IntegrationType.class),
            PartAnnihilationPlane.class),

    IDENTITY_ANNIHILATION_PLANE(EnumSet.of(AEFeature.ANNIHILATION_PLANE, AEFeature.IDENTITY_ANNIHILATION_PLANE),
            EnumSet.noneOf(IntegrationType.class), PartIdentityAnnihilationPlane.class),

    FLUID_ANNIHILATION_PLANE(EnumSet.of(AEFeature.FLUID_ANNIHILATION_PLANE), EnumSet.noneOf(IntegrationType.class),
            PartFluidAnnihilationPlane.class),

    FORMATION_PLANE(EnumSet.of(AEFeature.FORMATION_PLANE), EnumSet.noneOf(IntegrationType.class),
            PartFormationPlane.class),

    FLUID_FORMATION_PLANE(EnumSet.of(AEFeature.FLUID_FORMATION_PLANE), EnumSet.noneOf(IntegrationType.class),
            PartFluidFormationPlane.class),

    PATTERN_TERMINAL(EnumSet.of(AEFeature.PATTERNS), EnumSet.noneOf(IntegrationType.class), PartPatternTerminal.class),

    CRAFTING_TERMINAL(EnumSet.of(AEFeature.CRAFTING_TERMINAL), EnumSet.noneOf(IntegrationType.class),
            PartCraftingTerminal.class),

    TERMINAL(EnumSet.of(AEFeature.TERMINAL), EnumSet.noneOf(IntegrationType.class), PartTerminal.class),

    STORAGE_MONITOR(EnumSet.of(AEFeature.STORAGE_MONITOR), EnumSet.noneOf(IntegrationType.class),
            PartStorageMonitor.class),

    CONVERSION_MONITOR(EnumSet.of(AEFeature.PART_CONVERSION_MONITOR), EnumSet.noneOf(IntegrationType.class),
            PartConversionMonitor.class),

    INTERFACE(EnumSet.of(AEFeature.INTERFACE), EnumSet.noneOf(IntegrationType.class), PartInterface.class),
    FLUID_INTERFACE(EnumSet.of(AEFeature.FLUID_INTERFACE), EnumSet.noneOf(IntegrationType.class),
            PartFluidInterface.class),

    P2P_TUNNEL_ME(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_ME), EnumSet.noneOf(IntegrationType.class),
            PartP2PTunnelME.class),

    P2P_TUNNEL_REDSTONE(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_REDSTONE),
            EnumSet.noneOf(IntegrationType.class), PartP2PRedstone.class),

    P2P_TUNNEL_ITEM(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_ITEMS), EnumSet.noneOf(IntegrationType.class),
            PartP2PItems.class),

    P2P_TUNNEL_FLUID(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_FLUIDS),
            EnumSet.noneOf(IntegrationType.class), PartP2PFluids.class),

//FIXME	P2P_TUNNEL_IC2( 465, "p2p_tunnel_ic2", EnumSet.of( AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_EU ), EnumSet
//FIXME			.of( IntegrationType.IC2 ), PartP2PIC2Power.class ),

    P2P_TUNNEL_LIGHT(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_LIGHT),
            EnumSet.noneOf(IntegrationType.class), PartP2PLight.class),

    P2P_TUNNEL_FE(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_FE), EnumSet.noneOf(IntegrationType.class),
            PartP2PFEPower.class),

    // P2PTunnelOpenComputers( 468, EnumSet.of( AEFeature.P2PTunnel,
    // AEFeature.P2PTunnelOpenComputers ), EnumSet.of(
    // IntegrationType.OpenComputers ), PartP2POpenComputers.class, GuiText.OCTunnel
    // ),

    INTERFACE_TERMINAL(EnumSet.of(AEFeature.INTERFACE_TERMINAL), EnumSet.noneOf(IntegrationType.class),
            PartInterfaceTerminal.class),

    FLUID_TERMINAL(EnumSet.of(AEFeature.FLUID_TERMINAL), EnumSet.noneOf(IntegrationType.class),
            PartFluidTerminal.class);

    private final Set<AEFeature> features;
    private final Set<IntegrationType> integrations;
    @OnlyIn(Dist.CLIENT)
    private final Set<ResourceLocation> models;

    PartType(final Set<AEFeature> features, final Set<IntegrationType> integrations, final Class<? extends IPart> c) {
        this.features = Collections.unmodifiableSet(features);
        this.integrations = Collections.unmodifiableSet(integrations);

        if (c != null) {
            this.models = new HashSet<>(PartModelsHelper.createModels(c));
        } else {
            this.models = Collections.emptySet();
        }
    }

    public boolean isCable() {
        return false;
    }

    public Set<AEFeature> getFeature() {
        return this.features;
    }

    Set<IntegrationType> getIntegrations() {
        return this.integrations;
    }

    public Set<ResourceLocation> getModels() {
        return this.models;
    }

}

enum IntegrationType {
}