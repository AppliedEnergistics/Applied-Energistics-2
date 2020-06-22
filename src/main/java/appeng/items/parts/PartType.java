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

import appeng.api.features.AEFeature;
import appeng.api.parts.IPart;
import appeng.fluids.parts.FluidAnnihilationPlanePart;
import appeng.fluids.parts.FluidExportBusPart;
import appeng.fluids.parts.FluidFormationPlanePart;
import appeng.fluids.parts.FluidImportBusPart;
import appeng.fluids.parts.FluidInterfacePart;
import appeng.fluids.parts.FluidLevelEmitterPart;
import appeng.fluids.parts.FluidStorageBusPart;
import appeng.fluids.parts.FluidTerminalPart;
import appeng.parts.automation.AnnihilationPlanePart;
import appeng.parts.automation.ExportBusPart;
import appeng.parts.automation.FormationPlanePart;
import appeng.parts.automation.IdentityAnnihilationPlanePart;
import appeng.parts.automation.ImportBusPart;
import appeng.parts.automation.LevelEmitterPart;
import appeng.parts.misc.CableAnchorPart;
import appeng.parts.misc.InterfacePart;
import appeng.parts.misc.InvertedToggleBusPart;
import appeng.parts.misc.StorageBusPart;
import appeng.parts.misc.ToggleBusPart;
import appeng.parts.networking.CoveredCablePart;
import appeng.parts.networking.GlassCablePart;
import appeng.parts.networking.SmartCablePart;
import appeng.parts.networking.CoveredDenseCablePart;
import appeng.parts.networking.SmartDenseCablePart;
import appeng.parts.networking.QuartzFiberPart;
import appeng.parts.p2p.FEP2PTunnelPart;
import appeng.parts.p2p.FluidP2PTunnelPart;
import appeng.parts.p2p.ItemP2PTunnelPart;
import appeng.parts.p2p.LightP2PTunnelPart;
import appeng.parts.p2p.RedstoneP2PTunnelPart;
import appeng.parts.p2p.MEP2PTunnelPart;
import appeng.parts.reporting.ConversionMonitorPart;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.parts.reporting.DarkPanelPart;
import appeng.parts.reporting.InterfaceTerminalPart;
import appeng.parts.reporting.PanelPart;
import appeng.parts.reporting.PatternTerminalPart;
import appeng.parts.reporting.SemiDarkPanelPart;
import appeng.parts.reporting.StorageMonitorPart;
import appeng.parts.reporting.TerminalPart;

public enum PartType {
    INVALID_TYPE(EnumSet.of(AEFeature.CORE), EnumSet.noneOf(IntegrationType.class), null),

    CABLE_GLASS(EnumSet.of(AEFeature.GLASS_CABLES), EnumSet.noneOf(IntegrationType.class), GlassCablePart.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    CABLE_COVERED(EnumSet.of(AEFeature.COVERED_CABLES), EnumSet.noneOf(IntegrationType.class), CoveredCablePart.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    CABLE_SMART(EnumSet.of(AEFeature.CHANNELS, AEFeature.SMART_CABLES), EnumSet.noneOf(IntegrationType.class),
            SmartCablePart.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    CABLE_DENSE_SMART(EnumSet.of(AEFeature.CHANNELS, AEFeature.DENSE_CABLES), EnumSet.noneOf(IntegrationType.class),
            SmartDenseCablePart.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    CABLE_DENSE_COVERED(EnumSet.of(AEFeature.CHANNELS, AEFeature.DENSE_CABLES), EnumSet.noneOf(IntegrationType.class),
            CoveredDenseCablePart.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    TOGGLE_BUS(EnumSet.of(AEFeature.TOGGLE_BUS), EnumSet.noneOf(IntegrationType.class), ToggleBusPart.class),

    INVERTED_TOGGLE_BUS(EnumSet.of(AEFeature.TOGGLE_BUS), EnumSet.noneOf(IntegrationType.class),
            InvertedToggleBusPart.class),

    CABLE_ANCHOR(EnumSet.of(AEFeature.CABLE_ANCHOR), EnumSet.noneOf(IntegrationType.class), CableAnchorPart.class),

    QUARTZ_FIBER(EnumSet.of(AEFeature.QUARTZ_FIBER), EnumSet.noneOf(IntegrationType.class), QuartzFiberPart.class),

    MONITOR(EnumSet.of(AEFeature.PANELS), EnumSet.noneOf(IntegrationType.class), PanelPart.class),

    SEMI_DARK_MONITOR(EnumSet.of(AEFeature.PANELS), EnumSet.noneOf(IntegrationType.class), SemiDarkPanelPart.class),

    DARK_MONITOR(EnumSet.of(AEFeature.PANELS), EnumSet.noneOf(IntegrationType.class), DarkPanelPart.class),

    STORAGE_BUS(EnumSet.of(AEFeature.STORAGE_BUS), EnumSet.noneOf(IntegrationType.class), StorageBusPart.class),
    FLUID_STORAGE_BUS(EnumSet.of(AEFeature.FLUID_STORAGE_BUS), EnumSet.noneOf(IntegrationType.class),
            FluidStorageBusPart.class),

    IMPORT_BUS(EnumSet.of(AEFeature.IMPORT_BUS), EnumSet.noneOf(IntegrationType.class), ImportBusPart.class),

    FLUID_IMPORT_BUS(EnumSet.of(AEFeature.FLUID_IMPORT_BUS), EnumSet.noneOf(IntegrationType.class),
            FluidImportBusPart.class),

    EXPORT_BUS(EnumSet.of(AEFeature.EXPORT_BUS), EnumSet.noneOf(IntegrationType.class), ExportBusPart.class),

    FLUID_EXPORT_BUS(EnumSet.of(AEFeature.FLUID_EXPORT_BUS), EnumSet.noneOf(IntegrationType.class),
            FluidExportBusPart.class),

    LEVEL_EMITTER(EnumSet.of(AEFeature.LEVEL_EMITTER), EnumSet.noneOf(IntegrationType.class), LevelEmitterPart.class),
    FLUID_LEVEL_EMITTER(EnumSet.of(AEFeature.FLUID_LEVEL_EMITTER), EnumSet.noneOf(IntegrationType.class),
            FluidLevelEmitterPart.class),

    ANNIHILATION_PLANE(EnumSet.of(AEFeature.ANNIHILATION_PLANE), EnumSet.noneOf(IntegrationType.class),
            AnnihilationPlanePart.class),

    IDENTITY_ANNIHILATION_PLANE(EnumSet.of(AEFeature.ANNIHILATION_PLANE, AEFeature.IDENTITY_ANNIHILATION_PLANE),
            EnumSet.noneOf(IntegrationType.class), IdentityAnnihilationPlanePart.class),

    FLUID_ANNIHILATION_PLANE(EnumSet.of(AEFeature.FLUID_ANNIHILATION_PLANE), EnumSet.noneOf(IntegrationType.class),
            FluidAnnihilationPlanePart.class),

    FORMATION_PLANE(EnumSet.of(AEFeature.FORMATION_PLANE), EnumSet.noneOf(IntegrationType.class),
            FormationPlanePart.class),

    FLUID_FORMATION_PLANE(EnumSet.of(AEFeature.FLUID_FORMATION_PLANE), EnumSet.noneOf(IntegrationType.class),
            FluidFormationPlanePart.class),

    PATTERN_TERMINAL(EnumSet.of(AEFeature.PATTERNS), EnumSet.noneOf(IntegrationType.class), PatternTerminalPart.class),

    CRAFTING_TERMINAL(EnumSet.of(AEFeature.CRAFTING_TERMINAL), EnumSet.noneOf(IntegrationType.class),
            CraftingTerminalPart.class),

    TERMINAL(EnumSet.of(AEFeature.TERMINAL), EnumSet.noneOf(IntegrationType.class), TerminalPart.class),

    STORAGE_MONITOR(EnumSet.of(AEFeature.STORAGE_MONITOR), EnumSet.noneOf(IntegrationType.class),
            StorageMonitorPart.class),

    CONVERSION_MONITOR(EnumSet.of(AEFeature.PART_CONVERSION_MONITOR), EnumSet.noneOf(IntegrationType.class),
            ConversionMonitorPart.class),

    INTERFACE(EnumSet.of(AEFeature.INTERFACE), EnumSet.noneOf(IntegrationType.class), InterfacePart.class),
    FLUID_INTERFACE(EnumSet.of(AEFeature.FLUID_INTERFACE), EnumSet.noneOf(IntegrationType.class),
            FluidInterfacePart.class),

    P2P_TUNNEL_ME(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_ME), EnumSet.noneOf(IntegrationType.class),
            MEP2PTunnelPart.class),

    P2P_TUNNEL_REDSTONE(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_REDSTONE),
            EnumSet.noneOf(IntegrationType.class), RedstoneP2PTunnelPart.class),

    P2P_TUNNEL_ITEM(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_ITEMS), EnumSet.noneOf(IntegrationType.class),
            ItemP2PTunnelPart.class),

    P2P_TUNNEL_FLUID(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_FLUIDS),
            EnumSet.noneOf(IntegrationType.class), FluidP2PTunnelPart.class),

//FIXME	P2P_TUNNEL_IC2( 465, "p2p_tunnel_ic2", EnumSet.of( AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_EU ), EnumSet
//FIXME			.of( IntegrationType.IC2 ), PartP2PIC2Power.class ),

    P2P_TUNNEL_LIGHT(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_LIGHT),
            EnumSet.noneOf(IntegrationType.class), LightP2PTunnelPart.class),

    P2P_TUNNEL_FE(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_FE), EnumSet.noneOf(IntegrationType.class),
            FEP2PTunnelPart.class),

    // P2PTunnelOpenComputers( 468, EnumSet.of( AEFeature.P2PTunnel,
    // AEFeature.P2PTunnelOpenComputers ), EnumSet.of(
    // IntegrationType.OpenComputers ), PartP2POpenComputers.class, GuiText.OCTunnel
    // ),

    INTERFACE_TERMINAL(EnumSet.of(AEFeature.INTERFACE_TERMINAL), EnumSet.noneOf(IntegrationType.class),
            InterfaceTerminalPart.class),

    FLUID_TERMINAL(EnumSet.of(AEFeature.FLUID_TERMINAL), EnumSet.noneOf(IntegrationType.class),
            FluidTerminalPart.class);

    private final Set<AEFeature> features;
    private final Set<IntegrationType> integrations;
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