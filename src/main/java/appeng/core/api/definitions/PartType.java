package appeng.core.api.definitions;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.ResourceLocation;

import appeng.api.features.AEFeature;
import appeng.api.parts.IPart;
import appeng.fluids.parts.*;
import appeng.items.parts.PartModelsHelper;
import appeng.parts.automation.*;
import appeng.parts.misc.*;
import appeng.parts.networking.*;
import appeng.parts.p2p.*;
import appeng.parts.reporting.*;

enum PartType {
    INVALID_TYPE(EnumSet.of(AEFeature.CORE), null),

    CABLE_GLASS(EnumSet.of(AEFeature.GLASS_CABLES), GlassCablePart.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    CABLE_COVERED(EnumSet.of(AEFeature.COVERED_CABLES), CoveredCablePart.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    CABLE_SMART(EnumSet.of(AEFeature.CHANNELS, AEFeature.SMART_CABLES), SmartCablePart.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    CABLE_DENSE_SMART(EnumSet.of(AEFeature.CHANNELS, AEFeature.DENSE_CABLES), SmartDenseCablePart.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    CABLE_DENSE_COVERED(EnumSet.of(AEFeature.CHANNELS, AEFeature.DENSE_CABLES), CoveredDenseCablePart.class) {
        @Override
        public boolean isCable() {
            return true;
        }
    },

    TOGGLE_BUS(EnumSet.of(AEFeature.TOGGLE_BUS), ToggleBusPart.class),

    INVERTED_TOGGLE_BUS(EnumSet.of(AEFeature.TOGGLE_BUS), InvertedToggleBusPart.class),

    CABLE_ANCHOR(EnumSet.of(AEFeature.CABLE_ANCHOR), CableAnchorPart.class),

    QUARTZ_FIBER(EnumSet.of(AEFeature.QUARTZ_FIBER), QuartzFiberPart.class),

    MONITOR(EnumSet.of(AEFeature.PANELS), PanelPart.class),

    SEMI_DARK_MONITOR(EnumSet.of(AEFeature.PANELS), SemiDarkPanelPart.class),

    DARK_MONITOR(EnumSet.of(AEFeature.PANELS), DarkPanelPart.class),

    STORAGE_BUS(EnumSet.of(AEFeature.STORAGE_BUS), StorageBusPart.class),
    FLUID_STORAGE_BUS(EnumSet.of(AEFeature.FLUID_STORAGE_BUS), FluidStorageBusPart.class),

    IMPORT_BUS(EnumSet.of(AEFeature.IMPORT_BUS), ImportBusPart.class),

    FLUID_IMPORT_BUS(EnumSet.of(AEFeature.FLUID_IMPORT_BUS), FluidImportBusPart.class),

    EXPORT_BUS(EnumSet.of(AEFeature.EXPORT_BUS), ExportBusPart.class),

    FLUID_EXPORT_BUS(EnumSet.of(AEFeature.FLUID_EXPORT_BUS), FluidExportBusPart.class),

    LEVEL_EMITTER(EnumSet.of(AEFeature.LEVEL_EMITTER), LevelEmitterPart.class),
    FLUID_LEVEL_EMITTER(EnumSet.of(AEFeature.FLUID_LEVEL_EMITTER), FluidLevelEmitterPart.class),

    ANNIHILATION_PLANE(EnumSet.of(AEFeature.ANNIHILATION_PLANE), AnnihilationPlanePart.class),

    IDENTITY_ANNIHILATION_PLANE(EnumSet.of(AEFeature.ANNIHILATION_PLANE, AEFeature.IDENTITY_ANNIHILATION_PLANE),
            IdentityAnnihilationPlanePart.class),

    FLUID_ANNIHILATION_PLANE(EnumSet.of(AEFeature.FLUID_ANNIHILATION_PLANE), FluidAnnihilationPlanePart.class),

    FORMATION_PLANE(EnumSet.of(AEFeature.FORMATION_PLANE), FormationPlanePart.class),

    FLUID_FORMATION_PLANE(EnumSet.of(AEFeature.FLUID_FORMATION_PLANE), FluidFormationPlanePart.class),

    PATTERN_TERMINAL(EnumSet.of(AEFeature.PATTERNS), PatternTerminalPart.class),

    CRAFTING_TERMINAL(EnumSet.of(AEFeature.CRAFTING_TERMINAL), CraftingTerminalPart.class),

    TERMINAL(EnumSet.of(AEFeature.TERMINAL), TerminalPart.class),

    STORAGE_MONITOR(EnumSet.of(AEFeature.STORAGE_MONITOR), StorageMonitorPart.class),

    CONVERSION_MONITOR(EnumSet.of(AEFeature.PART_CONVERSION_MONITOR), ConversionMonitorPart.class),

    INTERFACE(EnumSet.of(AEFeature.INTERFACE), InterfacePart.class),
    FLUID_INTERFACE(EnumSet.of(AEFeature.FLUID_INTERFACE), FluidInterfacePart.class),

    P2P_TUNNEL_ME(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_ME), MEP2PTunnelPart.class),

    P2P_TUNNEL_REDSTONE(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_REDSTONE), RedstoneP2PTunnelPart.class),

    P2P_TUNNEL_ITEM(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_ITEMS), ItemP2PTunnelPart.class),

    P2P_TUNNEL_FLUID(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_FLUIDS), FluidP2PTunnelPart.class),

//FIXME	P2P_TUNNEL_IC2( 465, "p2p_tunnel_ic2", EnumSet.of( AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_EU ), EnumSet
//FIXME			.of( IntegrationType.IC2 ), PartP2PIC2Power.class ),

    P2P_TUNNEL_LIGHT(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_LIGHT), LightP2PTunnelPart.class),

    P2P_TUNNEL_FE(EnumSet.of(AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_FE), FEP2PTunnelPart.class),

    // P2PTunnelOpenComputers( 468, EnumSet.of( AEFeature.P2PTunnel,
    // AEFeature.P2PTunnelOpenComputers ), EnumSet.of(
    // IntegrationType.OpenComputers ), PartP2POpenComputers.class, GuiText.OCTunnel
    // ),

    INTERFACE_TERMINAL(EnumSet.of(AEFeature.INTERFACE_TERMINAL), InterfaceTerminalPart.class),

    FLUID_TERMINAL(EnumSet.of(AEFeature.FLUID_TERMINAL), FluidTerminalPart.class);

    private final Set<AEFeature> features;
    private final Set<ResourceLocation> models;

    PartType(final Set<AEFeature> features, final Class<? extends IPart> c) {
        this.features = Collections.unmodifiableSet(features);

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

    public Set<ResourceLocation> getModels() {
        return this.models;
    }

}
