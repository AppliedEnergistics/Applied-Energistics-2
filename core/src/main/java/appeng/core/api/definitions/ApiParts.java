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

package appeng.core.api.definitions;

import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IParts;
import appeng.api.util.AEColoredItemDefinition;
import appeng.bootstrap.FeatureFactory;
import appeng.core.features.registries.PartModels;

/**
 * Internal implementation for the API parts
 */
public final class ApiParts implements IParts {
    private AEColoredItemDefinition cableSmart;
    private AEColoredItemDefinition cableCovered;
    private AEColoredItemDefinition cableGlass;
    private AEColoredItemDefinition cableDenseCovered;
    private AEColoredItemDefinition cableDenseSmart;
    private IItemDefinition quartzFiber;
    private IItemDefinition toggleBus;
    private IItemDefinition invertedToggleBus;
    private IItemDefinition storageBus;
    private IItemDefinition importBus;
    private IItemDefinition exportBus;
    private IItemDefinition iface;
    private IItemDefinition fluidIface;
    private IItemDefinition levelEmitter;
    private IItemDefinition fluidLevelEmitter;
    private IItemDefinition annihilationPlane;
    private IItemDefinition identityAnnihilationPlane;
    private IItemDefinition fluidAnnihilationPlane;
    private IItemDefinition formationPlane;
    private IItemDefinition fluidFormationPlane;
    private IItemDefinition p2PTunnelME;
    private IItemDefinition p2PTunnelRedstone;
    private IItemDefinition p2PTunnelItems;
    private IItemDefinition p2PTunnelFluids;
    private IItemDefinition p2PTunnelEU;
    private IItemDefinition p2PTunnelFE;
    private IItemDefinition p2PTunnelLight;
    private IItemDefinition cableAnchor;
    private IItemDefinition monitor;
    private IItemDefinition semiDarkMonitor;
    private IItemDefinition darkMonitor;
    private IItemDefinition interfaceTerminal;
    private IItemDefinition patternTerminal;
    private IItemDefinition craftingTerminal;
    private IItemDefinition terminal;
    private IItemDefinition storageMonitor;
    private IItemDefinition conversionMonitor;
    private IItemDefinition fluidImportBus;
    private IItemDefinition fluidExportBus;
    private IItemDefinition fluidTerminal;
    private IItemDefinition fluidStorageBus;

    public ApiParts(FeatureFactory registry, PartModels partModels) {
        registerPartModels(partModels);

// FIXME       this.cableSmart = constructColoredDefinition(registry, "smart_cable", PartType.CABLE_SMART,
// FIXME               SmartCablePart::new);
// FIXME       this.cableCovered = constructColoredDefinition(registry, "covered_cable", PartType.CABLE_COVERED,
// FIXME               CoveredCablePart::new);
// FIXME       this.cableGlass = constructColoredDefinition(registry, "glass_cable", PartType.CABLE_GLASS,
// FIXME               GlassCablePart::new);
// FIXME       this.cableDenseCovered = constructColoredDefinition(registry, "covered_dense_cable",
// FIXME               PartType.CABLE_DENSE_COVERED, CoveredDenseCablePart::new);
// FIXME       this.cableDenseSmart = constructColoredDefinition(registry, "smart_dense_cable", PartType.CABLE_DENSE_SMART,
// FIXME               SmartDenseCablePart::new);
// FIXME       this.quartzFiber = createPart(registry, "quartz_fiber", PartType.QUARTZ_FIBER, QuartzFiberPart::new);
// FIXME       this.toggleBus = createPart(registry, "toggle_bus", PartType.TOGGLE_BUS, ToggleBusPart::new);
// FIXME       this.invertedToggleBus = createPart(registry, "inverted_toggle_bus", PartType.INVERTED_TOGGLE_BUS,
// FIXME               InvertedToggleBusPart::new);
// FIXME       this.cableAnchor = createPart(registry, "cable_anchor", PartType.CABLE_ANCHOR, CableAnchorPart::new);
// FIXME       this.monitor = createPart(registry, "monitor", PartType.MONITOR, PanelPart::new);
// FIXME       this.semiDarkMonitor = createPart(registry, "semi_dark_monitor", PartType.SEMI_DARK_MONITOR,
// FIXME               SemiDarkPanelPart::new);
// FIXME       this.darkMonitor = createPart(registry, "dark_monitor", PartType.DARK_MONITOR, DarkPanelPart::new);
// FIXME       this.storageBus = createPart(registry, "storage_bus", PartType.STORAGE_BUS, StorageBusPart::new);
// FIXME       this.fluidStorageBus = createPart(registry, "fluid_storage_bus", PartType.FLUID_STORAGE_BUS,
// FIXME               FluidStorageBusPart::new);
// FIXME       this.importBus = createPart(registry, "import_bus", PartType.IMPORT_BUS, ImportBusPart::new);
// FIXME       this.fluidImportBus = createPart(registry, "fluid_import_bus", PartType.FLUID_IMPORT_BUS,
// FIXME               FluidImportBusPart::new);
// FIXME       this.exportBus = createPart(registry, "export_bus", PartType.EXPORT_BUS, ExportBusPart::new);
// FIXME       this.fluidExportBus = createPart(registry, "fluid_export_bus", PartType.FLUID_EXPORT_BUS,
// FIXME               FluidExportBusPart::new);
// FIXME       this.levelEmitter = createPart(registry, "level_emitter", PartType.LEVEL_EMITTER, LevelEmitterPart::new);
// FIXME       this.fluidLevelEmitter = createPart(registry, "fluid_level_emitter", PartType.FLUID_LEVEL_EMITTER,
// FIXME               FluidLevelEmitterPart::new);
// FIXME       this.annihilationPlane = createPart(registry, "annihilation_plane", PartType.ANNIHILATION_PLANE,
// FIXME               AnnihilationPlanePart::new);
// FIXME       this.identityAnnihilationPlane = createPart(registry, "identity_annihilation_plane",
// FIXME               PartType.IDENTITY_ANNIHILATION_PLANE, IdentityAnnihilationPlanePart::new);
// FIXME       this.fluidAnnihilationPlane = createPart(registry, "fluid_annihilation_plane",
// FIXME               PartType.FLUID_ANNIHILATION_PLANE, FluidAnnihilationPlanePart::new);
// FIXME       this.formationPlane = createPart(registry, "formation_plane", PartType.FORMATION_PLANE,
// FIXME               FormationPlanePart::new);
// FIXME       this.fluidFormationPlane = createPart(registry, "fluid_formation_plane", PartType.FLUID_FORMATION_PLANE,
// FIXME               FluidFormationPlanePart::new);
// FIXME       this.patternTerminal = createPart(registry, "pattern_terminal", PartType.PATTERN_TERMINAL,
// FIXME               PatternTerminalPart::new);
// FIXME       this.craftingTerminal = createPart(registry, "crafting_terminal", PartType.CRAFTING_TERMINAL,
// FIXME               CraftingTerminalPart::new);
// FIXME       this.terminal = createPart(registry, "terminal", PartType.TERMINAL, TerminalPart::new);
// FIXME       this.storageMonitor = createPart(registry, "storage_monitor", PartType.STORAGE_MONITOR,
// FIXME               StorageMonitorPart::new);
// FIXME       this.conversionMonitor = createPart(registry, "conversion_monitor", PartType.CONVERSION_MONITOR,
// FIXME               ConversionMonitorPart::new);
// FIXME       this.iface = createPart(registry, "cable_interface", PartType.INTERFACE, InterfacePart::new);
// FIXME       this.fluidIface = createPart(registry, "cable_fluid_interface", PartType.FLUID_INTERFACE,
// FIXME               FluidInterfacePart::new);
// FIXME       this.p2PTunnelME = createPart(registry, "me_p2p_tunnel", PartType.P2P_TUNNEL_ME, MEP2PTunnelPart::new);
// FIXME       this.p2PTunnelRedstone = createPart(registry, "redstone_p2p_tunnel", PartType.P2P_TUNNEL_REDSTONE,
// FIXME               RedstoneP2PTunnelPart::new);
// FIXME       this.p2PTunnelItems = createPart(registry, "item_p2p_tunnel", PartType.P2P_TUNNEL_ITEM, ItemP2PTunnelPart::new);
// FIXME       this.p2PTunnelFluids = createPart(registry, "fluid_p2p_tunnel", PartType.P2P_TUNNEL_FLUID,
// FIXME               FluidP2PTunnelPart::new);
// FIXME       this.p2PTunnelEU = null; // FIXME createPart( "ic2_p2p_tunnel", PartType.P2P_TUNNEL_IC2,
// FIXME                                // PartP2PIC2Power::new);
// FIXME       this.p2PTunnelFE = createPart(registry, "fe_p2p_tunnel", PartType.P2P_TUNNEL_FE, FEP2PTunnelPart::new);
// FIXME       this.p2PTunnelLight = createPart(registry, "light_p2p_tunnel", PartType.P2P_TUNNEL_LIGHT,
// FIXME               LightP2PTunnelPart::new);
// FIXME       this.interfaceTerminal = createPart(registry, "interface_terminal", PartType.INTERFACE_TERMINAL,
// FIXME               InterfaceTerminalPart::new);
// FIXME       this.fluidTerminal = createPart(registry, "fluid_terminal", PartType.FLUID_TERMINAL, FluidTerminalPart::new);
    }

    private void registerPartModels(PartModels partModels) {

// FIXME        // Register the built-in models for annihilation planes
// FIXME        Identifier fluidFormationPlaneTexture = new Identifier(AppEng.MOD_ID,
// FIXME                "item/part/fluid_formation_plane");
// FIXME        Identifier fluidFormationPlaneOnTexture = new Identifier(AppEng.MOD_ID,
// FIXME                "parts/fluid_formation_plane_on");
// FIXME
// FIXME        // Register all part models
// FIXME        for (PartType partType : PartType.values()) {
// FIXME            partModels.registerModels(partType.getModels());
// FIXME        }
    }

// FIXME    private <T extends IPart> IItemDefinition createPart(FeatureFactory registry, String id, PartType type,
// FIXME            Function<ItemStack, T> factory) {
// FIXME        return registry.item(id, props -> new PartItem<>(props, type, factory)).itemGroup(CreativeTab.INSTANCE)
// FIXME                .rendering(new PartItemRendering()).build();
// FIXME    }
// FIXME
// FIXME    private <T extends IPart> AEColoredItemDefinition constructColoredDefinition(FeatureFactory registry,
// FIXME            String idSuffix, PartType type, Function<ItemStack, T> factory) {
// FIXME        final ColoredItemDefinition definition = new ColoredItemDefinition();
// FIXME
// FIXME        for (final AEColor color : AEColor.values()) {
// FIXME            String id = color.registryPrefix + '_' + idSuffix;
// FIXME
// FIXME            IItemDefinition itemDef = registry.item(id, props -> new ColoredPartItem<>(props, type, factory, color))
// FIXME                    .itemGroup(CreativeTab.INSTANCE).rendering(new PartItemRendering(color)).build();
// FIXME
// FIXME            definition.add(color, new ItemStackSrc(itemDef.item(), ActivityState.Enabled));
// FIXME        }
// FIXME
// FIXME        return definition;
// FIXME    }

    @Override
    public AEColoredItemDefinition cableSmart() {
        return this.cableSmart;
    }

    @Override
    public AEColoredItemDefinition cableCovered() {
        return this.cableCovered;
    }

    @Override
    public AEColoredItemDefinition cableGlass() {
        return this.cableGlass;
    }

    @Override
    public AEColoredItemDefinition cableDenseCovered() {
        return this.cableDenseCovered;
    }

    @Override
    public AEColoredItemDefinition cableDenseSmart() {
        return this.cableDenseSmart;
    }

    @Override
    public IItemDefinition quartzFiber() {
        return this.quartzFiber;
    }

    @Override
    public IItemDefinition toggleBus() {
        return this.toggleBus;
    }

    @Override
    public IItemDefinition invertedToggleBus() {
        return this.invertedToggleBus;
    }

    @Override
    public IItemDefinition storageBus() {
        return this.storageBus;
    }

    @Override
    public IItemDefinition importBus() {
        return this.importBus;
    }

    @Override
    public IItemDefinition exportBus() {
        return this.exportBus;
    }

    @Override
    public IItemDefinition iface() {
        return this.iface;
    }

    @Override
    public IItemDefinition fluidIface() {
        return this.fluidIface;
    }

    @Override
    public IItemDefinition levelEmitter() {
        return this.levelEmitter;
    }

    @Override
    public IItemDefinition annihilationPlane() {
        return this.annihilationPlane;
    }

    @Override
    public IItemDefinition identityAnnihilationPlane() {
        return this.identityAnnihilationPlane;
    }

    @Override
    public IItemDefinition formationPlane() {
        return this.formationPlane;
    }

    @Override
    public IItemDefinition p2PTunnelME() {
        return this.p2PTunnelME;
    }

    @Override
    public IItemDefinition p2PTunnelRedstone() {
        return this.p2PTunnelRedstone;
    }

    @Override
    public IItemDefinition p2PTunnelItems() {
        return this.p2PTunnelItems;
    }

    @Override
    public IItemDefinition p2PTunnelFluids() {
        return this.p2PTunnelFluids;
    }

    @Override
    public IItemDefinition p2PTunnelEU() {
        return this.p2PTunnelEU;
    }

    @Override
    public IItemDefinition p2PTunnelFE() {
        return this.p2PTunnelFE;
    }

    @Override
    public IItemDefinition p2PTunnelLight() {
        return this.p2PTunnelLight;
    }

    @Override
    public IItemDefinition cableAnchor() {
        return this.cableAnchor;
    }

    @Override
    public IItemDefinition monitor() {
        return this.monitor;
    }

    @Override
    public IItemDefinition semiDarkMonitor() {
        return this.semiDarkMonitor;
    }

    @Override
    public IItemDefinition darkMonitor() {
        return this.darkMonitor;
    }

    @Override
    public IItemDefinition interfaceTerminal() {
        return this.interfaceTerminal;
    }

    @Override
    public IItemDefinition patternTerminal() {
        return this.patternTerminal;
    }

    @Override
    public IItemDefinition craftingTerminal() {
        return this.craftingTerminal;
    }

    @Override
    public IItemDefinition terminal() {
        return this.terminal;
    }

    @Override
    public IItemDefinition storageMonitor() {
        return this.storageMonitor;
    }

    @Override
    public IItemDefinition conversionMonitor() {
        return this.conversionMonitor;
    }

    @Override
    public IItemDefinition fluidTerminal() {
        return this.fluidTerminal;
    }

    @Override
    public IItemDefinition fluidImportBus() {
        return this.fluidImportBus;
    }

    @Override
    public IItemDefinition fluidExportBus() {
        return this.fluidExportBus;
    }

    @Override
    public IItemDefinition fluidStorageBus() {
        return this.fluidStorageBus;
    }

    @Override
    public IItemDefinition fluidLevelEmitter() {
        return this.fluidLevelEmitter;
    }

    @Override
    public IItemDefinition fluidAnnihilationPlane() {
        return this.fluidAnnihilationPlane;
    }

    @Override
    public IItemDefinition fluidFormationnPlane() {
        return this.fluidFormationPlane;
    }
}
