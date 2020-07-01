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
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartItemRendering;
import appeng.parts.misc.CableAnchorPart;
import appeng.parts.networking.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.function.Function;

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

       this.cableSmart = constructColoredDefinition(registry, "smart_cable", SmartCablePart::new);
       this.cableCovered = constructColoredDefinition(registry, "covered_cable", CoveredCablePart::new);
       this.cableGlass = constructColoredDefinition(registry, "glass_cable", 
               GlassCablePart::new);
       this.cableDenseCovered = constructColoredDefinition(registry, "covered_dense_cable",
                CoveredDenseCablePart::new);
       this.cableDenseSmart = constructColoredDefinition(registry, "smart_dense_cable", 
               SmartDenseCablePart::new);
      this.quartzFiber = createPart(registry, "quartz_fiber",  QuartzFiberPart::new);
// FIXME       this.toggleBus = createPart(registry, "toggle_bus",  ToggleBusPart::new);
// FIXME       this.invertedToggleBus = createPart(registry, "inverted_toggle_bus", 
// FIXME               InvertedToggleBusPart::new);
       this.cableAnchor = createPart(registry, "cable_anchor",  CableAnchorPart::new);
// FIXME       this.monitor = createPart(registry, "monitor",  PanelPart::new);
// FIXME       this.semiDarkMonitor = createPart(registry, "semi_dark_monitor", 
// FIXME               SemiDarkPanelPart::new);
// FIXME       this.darkMonitor = createPart(registry, "dark_monitor",  DarkPanelPart::new);
// FIXME       this.storageBus = createPart(registry, "storage_bus",  StorageBusPart::new);
// FIXME       this.fluidStorageBus = createPart(registry, "fluid_storage_bus", 
// FIXME               FluidStorageBusPart::new);
// FIXME       this.importBus = createPart(registry, "import_bus",  ImportBusPart::new);
// FIXME       this.fluidImportBus = createPart(registry, "fluid_import_bus", 
// FIXME               FluidImportBusPart::new);
// FIXME       this.exportBus = createPart(registry, "export_bus",  ExportBusPart::new);
// FIXME       this.fluidExportBus = createPart(registry, "fluid_export_bus", 
// FIXME               FluidExportBusPart::new);
// FIXME       this.levelEmitter = createPart(registry, "level_emitter",  LevelEmitterPart::new);
// FIXME       this.fluidLevelEmitter = createPart(registry, "fluid_level_emitter", 
// FIXME               FluidLevelEmitterPart::new);
// FIXME       this.annihilationPlane = createPart(registry, "annihilation_plane", 
// FIXME               AnnihilationPlanePart::new);
// FIXME       this.identityAnnihilationPlane = createPart(registry, "identity_annihilation_plane",
// FIXME                IdentityAnnihilationPlanePart::new);
// FIXME       this.fluidAnnihilationPlane = createPart(registry, "fluid_annihilation_plane",
// FIXME                FluidAnnihilationPlanePart::new);
// FIXME       this.formationPlane = createPart(registry, "formation_plane", 
// FIXME               FormationPlanePart::new);
// FIXME       this.fluidFormationPlane = createPart(registry, "fluid_formation_plane", 
// FIXME               FluidFormationPlanePart::new);
// FIXME       this.patternTerminal = createPart(registry, "pattern_terminal", 
// FIXME               PatternTerminalPart::new);
// FIXME       this.craftingTerminal = createPart(registry, "crafting_terminal", 
// FIXME               CraftingTerminalPart::new);
// FIXME       this.terminal = createPart(registry, "terminal",  TerminalPart::new);
// FIXME       this.storageMonitor = createPart(registry, "storage_monitor", 
// FIXME               StorageMonitorPart::new);
// FIXME       this.conversionMonitor = createPart(registry, "conversion_monitor", 
// FIXME               ConversionMonitorPart::new);
// FIXME       this.iface = createPart(registry, "cable_interface",  InterfacePart::new);
// FIXME       this.fluidIface = createPart(registry, "cable_fluid_interface", 
// FIXME               FluidInterfacePart::new);
// FIXME       this.p2PTunnelME = createPart(registry, "me_p2p_tunnel",  MEP2PTunnelPart::new);
// FIXME       this.p2PTunnelRedstone = createPart(registry, "redstone_p2p_tunnel", 
// FIXME               RedstoneP2PTunnelPart::new);
// FIXME       this.p2PTunnelItems = createPart(registry, "item_p2p_tunnel",  ItemP2PTunnelPart::new);
// FIXME       this.p2PTunnelFluids = createPart(registry, "fluid_p2p_tunnel", 
// FIXME               FluidP2PTunnelPart::new);
// FIXME       this.p2PTunnelEU = null; // FIXME createPart( "ic2_p2p_tunnel", 
// FIXME                                // PartP2PIC2Power::new);
// FIXME       this.p2PTunnelFE = createPart(registry, "fe_p2p_tunnel",  FEP2PTunnelPart::new);
// FIXME       this.p2PTunnelLight = createPart(registry, "light_p2p_tunnel", 
// FIXME               LightP2PTunnelPart::new);
// FIXME       this.interfaceTerminal = createPart(registry, "interface_terminal", 
// FIXME               InterfaceTerminalPart::new);
// FIXME       this.fluidTerminal = createPart(registry, "fluid_terminal",  FluidTerminalPart::new);
    }

    private void registerPartModels(PartModels partModels) {

        // Register the built-in models for annihilation planes
        Identifier fluidFormationPlaneTexture = new Identifier(AppEng.MOD_ID,
                "item/part/fluid_formation_plane");
        Identifier fluidFormationPlaneOnTexture = new Identifier(AppEng.MOD_ID,
                "parts/fluid_formation_plane_on");

        // Register all part models
// FIXME FABRIC        for (PartType partType : PartType.values()) {
// FIXME FABRIC            partModels.registerModels(partType.getModels());
// FIXME FABRIC        }
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
