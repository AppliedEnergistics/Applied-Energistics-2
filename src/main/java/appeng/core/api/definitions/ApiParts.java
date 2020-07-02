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
import net.minecraft.util.ResourceLocation;

import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IParts;
import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.bootstrap.FeatureFactory;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.core.features.ActivityState;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.ItemStackSrc;
import appeng.core.features.registries.PartModels;
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
import appeng.parts.p2p.FEP2PTunnelPart;
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

    public ApiParts(FeatureFactory registry, PartModels partModels) {
        registerPartModels(partModels);

        this.cableSmart = constructColoredDefinition(registry, "smart_cable", SmartCablePart::new);
        this.cableCovered = constructColoredDefinition(registry, "covered_cable", CoveredCablePart::new);
        this.cableGlass = constructColoredDefinition(registry, "glass_cable", GlassCablePart::new);
        this.cableDenseCovered = constructColoredDefinition(registry, "covered_dense_cable",
                CoveredDenseCablePart::new);
        this.cableDenseSmart = constructColoredDefinition(registry, "smart_dense_cable", SmartDenseCablePart::new);
        this.quartzFiber = createPart(registry, "quartz_fiber", QuartzFiberPart::new);
        this.toggleBus = createPart(registry, "toggle_bus", ToggleBusPart::new);
        this.invertedToggleBus = createPart(registry, "inverted_toggle_bus", InvertedToggleBusPart::new);
        this.cableAnchor = createPart(registry, "cable_anchor", CableAnchorPart::new);
        this.monitor = createPart(registry, "monitor", PanelPart::new);
        this.semiDarkMonitor = createPart(registry, "semi_dark_monitor", SemiDarkPanelPart::new);
        this.darkMonitor = createPart(registry, "dark_monitor", DarkPanelPart::new);
        this.storageBus = createPart(registry, "storage_bus", StorageBusPart::new);
        this.fluidStorageBus = createPart(registry, "fluid_storage_bus", FluidStorageBusPart::new);
        this.importBus = createPart(registry, "import_bus", ImportBusPart::new);
        this.fluidImportBus = createPart(registry, "fluid_import_bus", FluidImportBusPart::new);
        this.exportBus = createPart(registry, "export_bus", ExportBusPart::new);
        this.fluidExportBus = createPart(registry, "fluid_export_bus", FluidExportBusPart::new);
        this.levelEmitter = createPart(registry, "level_emitter", LevelEmitterPart::new);
        this.fluidLevelEmitter = createPart(registry, "fluid_level_emitter", FluidLevelEmitterPart::new);
        this.annihilationPlane = createPart(registry, "annihilation_plane", AnnihilationPlanePart::new);
        this.identityAnnihilationPlane = createPart(registry, "identity_annihilation_plane",
                IdentityAnnihilationPlanePart::new);
        this.fluidAnnihilationPlane = createPart(registry, "fluid_annihilation_plane", FluidAnnihilationPlanePart::new);
        this.formationPlane = createPart(registry, "formation_plane", FormationPlanePart::new);
        this.fluidFormationPlane = createPart(registry, "fluid_formation_plane", FluidFormationPlanePart::new);
        this.patternTerminal = createPart(registry, "pattern_terminal", PatternTerminalPart::new);
        this.craftingTerminal = createPart(registry, "crafting_terminal", CraftingTerminalPart::new);
        this.terminal = createPart(registry, "terminal", TerminalPart::new);
        this.storageMonitor = createPart(registry, "storage_monitor", StorageMonitorPart::new);
        this.conversionMonitor = createPart(registry, "conversion_monitor", ConversionMonitorPart::new);
        this.iface = createPart(registry, "cable_interface", InterfacePart::new);
        this.fluidIface = createPart(registry, "cable_fluid_interface", FluidInterfacePart::new);
        this.p2PTunnelME = createPart(registry, "me_p2p_tunnel", MEP2PTunnelPart::new);
        this.p2PTunnelRedstone = createPart(registry, "redstone_p2p_tunnel", RedstoneP2PTunnelPart::new);
        this.p2PTunnelItems = createPart(registry, "item_p2p_tunnel", ItemP2PTunnelPart::new);
        this.p2PTunnelFluids = createPart(registry, "fluid_p2p_tunnel", FluidP2PTunnelPart::new);
        this.p2PTunnelEU = null; // FIXME createPart( "ic2_p2p_tunnel", PartType.P2P_TUNNEL_IC2,
                                 // PartP2PIC2Power::new);
        this.p2PTunnelFE = createPart(registry, "fe_p2p_tunnel", FEP2PTunnelPart::new);
        this.p2PTunnelLight = createPart(registry, "light_p2p_tunnel", LightP2PTunnelPart::new);
        this.interfaceTerminal = createPart(registry, "interface_terminal", InterfaceTerminalPart::new);
        this.fluidTerminal = createPart(registry, "fluid_terminal", FluidTerminalPart::new);
    }

    private void registerPartModels(PartModels partModels) {

        // Register the built-in models for annihilation planes
        ResourceLocation fluidFormationPlaneTexture = new ResourceLocation(AppEng.MOD_ID,
                "item/part/fluid_formation_plane");
        ResourceLocation fluidFormationPlaneOnTexture = new ResourceLocation(AppEng.MOD_ID,
                "parts/fluid_formation_plane_on");

        // Register all part models
        for (PartType partType : PartType.values()) {
            partModels.registerModels(partType.getModels());
        }
    }

    private <T extends IPart> IItemDefinition createPart(FeatureFactory registry, String id,
            Function<ItemStack, T> factory) {
        return registry.item(id, props -> new PartItem<>(props, factory)).itemGroup(CreativeTab.INSTANCE)
                .rendering(new PartItemRendering()).build();
    }

    private <T extends IPart> AEColoredItemDefinition constructColoredDefinition(FeatureFactory registry,
            String idSuffix, Function<ItemStack, T> factory) {
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
