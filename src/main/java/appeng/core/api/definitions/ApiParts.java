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

import java.util.function.Function;

import net.minecraft.item.ItemStack;

import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IParts;
import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.bootstrap.FeatureFactory;
import appeng.core.CreativeTab;
import appeng.core.features.ActivityState;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.ItemStackSrc;
import appeng.core.features.registries.PartModels;
import appeng.fluids.parts.*;
import appeng.fluids.parts.FluidAnnihilationPlanePart;
import appeng.fluids.parts.FluidExportBusPart;
import appeng.fluids.parts.FluidFormationPlanePart;
import appeng.fluids.parts.FluidImportBusPart;
import appeng.fluids.parts.FluidInterfacePart;
import appeng.fluids.parts.FluidLevelEmitterPart;
import appeng.fluids.parts.FluidStorageBusPart;
import appeng.fluids.parts.FluidTerminalPart;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartItemRendering;
import appeng.items.parts.PartModelsHelper;
import appeng.parts.automation.*;
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
import appeng.parts.networking.CoveredDenseCablePart;
import appeng.parts.networking.GlassCablePart;
import appeng.parts.networking.QuartzFiberPart;
import appeng.parts.networking.SmartCablePart;
import appeng.parts.networking.SmartDenseCablePart;
import appeng.parts.p2p.*;
import appeng.parts.p2p.FluidP2PTunnelPart;
import appeng.parts.p2p.ItemP2PTunnelPart;
import appeng.parts.p2p.LightP2PTunnelPart;
import appeng.parts.p2p.MEP2PTunnelPart;
import appeng.parts.p2p.RedstoneP2PTunnelPart;
import appeng.parts.reporting.ConversionMonitorPart;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.parts.reporting.DarkPanelPart;
import appeng.parts.reporting.InterfaceTerminalPart;
import appeng.parts.reporting.PanelPart;
import appeng.parts.reporting.PatternTerminalPart;
import appeng.parts.reporting.SemiDarkPanelPart;
import appeng.parts.reporting.StorageMonitorPart;
import appeng.parts.reporting.TerminalPart;

/**
 * Internal implementation for the API parts
 */
public final class ApiParts implements IParts {
    private final AEColoredItemDefinition cableSmart;
    private final AEColoredItemDefinition cableCovered;
    private final AEColoredItemDefinition cableGlass;
    private final AEColoredItemDefinition cableDenseCovered;
    private final AEColoredItemDefinition cableDenseSmart;
    private final IItemDefinition quartzFiber;
    private final IItemDefinition toggleBus;
    private final IItemDefinition invertedToggleBus;
    private final IItemDefinition storageBus;
    private final IItemDefinition importBus;
    private final IItemDefinition exportBus;
    private final IItemDefinition iface;
    private final IItemDefinition fluidIface;
    private final IItemDefinition levelEmitter;
    private final IItemDefinition fluidLevelEmitter;
    private final IItemDefinition annihilationPlane;
    private final IItemDefinition identityAnnihilationPlane;
    private final IItemDefinition fluidAnnihilationPlane;
    private final IItemDefinition formationPlane;
    private final IItemDefinition fluidFormationPlane;
    private final IItemDefinition p2PTunnelME;
    private final IItemDefinition p2PTunnelRedstone;
    private final IItemDefinition p2PTunnelItems;
    private final IItemDefinition p2PTunnelFluids;
    private final IItemDefinition p2PTunnelEU;
    private final IItemDefinition p2PTunnelFE;
    private final IItemDefinition p2PTunnelLight;
    private final IItemDefinition cableAnchor;
    private final IItemDefinition monitor;
    private final IItemDefinition semiDarkMonitor;
    private final IItemDefinition darkMonitor;
    private final IItemDefinition interfaceTerminal;
    private final IItemDefinition patternTerminal;
    private final IItemDefinition craftingTerminal;
    private final IItemDefinition terminal;
    private final IItemDefinition storageMonitor;
    private final IItemDefinition conversionMonitor;
    private final IItemDefinition fluidImportBus;
    private final IItemDefinition fluidExportBus;
    private final IItemDefinition fluidTerminal;
    private final IItemDefinition fluidStorageBus;

    private FeatureFactory registry;
    private PartModels partModels;

    public ApiParts(FeatureFactory registry, PartModels partModels) {
        this.registry = registry;
        this.partModels = partModels;

        this.cableSmart = constructColoredDefinition("smart_cable", SmartCablePart.class, SmartCablePart::new);
        this.cableCovered = constructColoredDefinition("covered_cable", CoveredCablePart.class, CoveredCablePart::new);
        this.cableGlass = constructColoredDefinition("glass_cable", GlassCablePart.class, GlassCablePart::new);
        this.cableDenseCovered = constructColoredDefinition("covered_dense_cable", CoveredDenseCablePart.class,
                CoveredDenseCablePart::new);
        this.cableDenseSmart = constructColoredDefinition("smart_dense_cable", SmartDenseCablePart.class,
                SmartDenseCablePart::new);
        this.quartzFiber = createPart("quartz_fiber", QuartzFiberPart.class, QuartzFiberPart::new);
        this.toggleBus = createPart("toggle_bus", ToggleBusPart.class, ToggleBusPart::new);
        this.invertedToggleBus = createPart("inverted_toggle_bus", InvertedToggleBusPart.class,
                InvertedToggleBusPart::new);
        this.cableAnchor = createPart("cable_anchor", CableAnchorPart.class, CableAnchorPart::new);
        this.monitor = createPart("monitor", PanelPart.class, PanelPart::new);
        this.semiDarkMonitor = createPart("semi_dark_monitor", SemiDarkPanelPart.class, SemiDarkPanelPart::new);
        this.darkMonitor = createPart("dark_monitor", DarkPanelPart.class, DarkPanelPart::new);
        this.storageBus = createPart("storage_bus", StorageBusPart.class, StorageBusPart::new);
        this.fluidStorageBus = createPart("fluid_storage_bus", FluidStorageBusPart.class, FluidStorageBusPart::new);
        this.importBus = createPart("import_bus", ImportBusPart.class, ImportBusPart::new);
        this.fluidImportBus = createPart("fluid_import_bus", FluidImportBusPart.class, FluidImportBusPart::new);
        this.exportBus = createPart("export_bus", ExportBusPart.class, ExportBusPart::new);
        this.fluidExportBus = createPart("fluid_export_bus", FluidExportBusPart.class, FluidExportBusPart::new);
        this.levelEmitter = createPart("level_emitter", LevelEmitterPart.class, LevelEmitterPart::new);
        this.fluidLevelEmitter = createPart("fluid_level_emitter", FluidLevelEmitterPart.class,
                FluidLevelEmitterPart::new);
        this.annihilationPlane = createPart("annihilation_plane", AnnihilationPlanePart.class,
                AnnihilationPlanePart::new);
        this.identityAnnihilationPlane = createPart("identity_annihilation_plane", IdentityAnnihilationPlanePart.class,
                IdentityAnnihilationPlanePart::new);
        this.fluidAnnihilationPlane = createPart("fluid_annihilation_plane", FluidAnnihilationPlanePart.class,
                FluidAnnihilationPlanePart::new);
        this.formationPlane = createPart("formation_plane", FormationPlanePart.class, FormationPlanePart::new);
        this.fluidFormationPlane = createPart("fluid_formation_plane", FluidFormationPlanePart.class,
                FluidFormationPlanePart::new);
        this.patternTerminal = createPart("pattern_terminal", PatternTerminalPart.class, PatternTerminalPart::new);
        this.craftingTerminal = createPart("crafting_terminal", CraftingTerminalPart.class, CraftingTerminalPart::new);
        this.terminal = createPart("terminal", TerminalPart.class, TerminalPart::new);
        this.storageMonitor = createPart("storage_monitor", StorageMonitorPart.class, StorageMonitorPart::new);
        this.conversionMonitor = createPart("conversion_monitor", ConversionMonitorPart.class,
                ConversionMonitorPart::new);
        this.iface = createPart("cable_interface", InterfacePart.class, InterfacePart::new);
        this.fluidIface = createPart("cable_fluid_interface", FluidInterfacePart.class, FluidInterfacePart::new);
        this.p2PTunnelME = createPart("me_p2p_tunnel", MEP2PTunnelPart.class, MEP2PTunnelPart::new);
        this.p2PTunnelRedstone = createPart("redstone_p2p_tunnel", RedstoneP2PTunnelPart.class,
                RedstoneP2PTunnelPart::new);
        this.p2PTunnelItems = createPart("item_p2p_tunnel", ItemP2PTunnelPart.class, ItemP2PTunnelPart::new);
        this.p2PTunnelFluids = createPart("fluid_p2p_tunnel", FluidP2PTunnelPart.class, FluidP2PTunnelPart::new);
// FIXME FABRIC         this.p2PTunnelEU = null; // FIXME createPart( "ic2_p2p_tunnel", PartType.P2P_TUNNEL_IC2,
// FIXME FABRIC                                  // PartP2PIC2Power.class, PartP2PIC2Power::new);
// FIXME FABRIC         this.p2PTunnelFE = createPart("fe_p2p_tunnel", FEP2PTunnelPart.class, FEP2PTunnelPart::new);
        this.p2PTunnelLight = createPart("light_p2p_tunnel", LightP2PTunnelPart.class, LightP2PTunnelPart::new);
        this.interfaceTerminal = createPart("interface_terminal", InterfaceTerminalPart.class,
                InterfaceTerminalPart::new);
        this.fluidTerminal = createPart("fluid_terminal", FluidTerminalPart.class, FluidTerminalPart::new);

        this.p2PTunnelEU = null;
        this.p2PTunnelFE = null;

        this.registry = null;
        this.partModels = null;
    }

    private <T extends IPart> IItemDefinition createPart(String id, Class<T> partClass,
            Function<ItemStack, T> factory) {

        partModels.registerModels(PartModelsHelper.createModels(partClass));

        return registry.item(id, props -> new PartItem<>(props, factory)).itemGroup(CreativeTab.INSTANCE)
                .rendering(new PartItemRendering()).build();
    }

    private <T extends IPart> AEColoredItemDefinition constructColoredDefinition(String idSuffix, Class<T> partClass,
            Function<ItemStack, T> factory) {

        partModels.registerModels(PartModelsHelper.createModels(partClass));

        final ColoredItemDefinition definition = new ColoredItemDefinition();

        for (final AEColor color : AEColor.values()) {
            String id = color.registryPrefix + '_' + idSuffix;

            IItemDefinition itemDef = registry.item(id, props -> new ColoredPartItem<>(props, factory, color))
                    .itemGroup(CreativeTab.INSTANCE).rendering(new PartItemRendering(color)).build();

            definition.add(color, new ItemStackSrc(itemDef.item(), ActivityState.Enabled));
        }

        return definition;
    }

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
