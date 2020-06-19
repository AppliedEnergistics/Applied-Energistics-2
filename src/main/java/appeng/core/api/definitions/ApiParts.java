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
import appeng.fluids.parts.PartFluidAnnihilationPlane;
import appeng.fluids.parts.PartFluidExportBus;
import appeng.fluids.parts.PartFluidFormationPlane;
import appeng.fluids.parts.PartFluidImportBus;
import appeng.fluids.parts.PartFluidInterface;
import appeng.fluids.parts.PartFluidLevelEmitter;
import appeng.fluids.parts.PartFluidStorageBus;
import appeng.fluids.parts.PartFluidTerminal;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.ItemPart;
import appeng.items.parts.ItemPartRendering;
import appeng.items.parts.PartType;
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

        this.cableSmart = constructColoredDefinition(registry, "smart_cable", PartType.CABLE_SMART,
                PartCableSmart::new);
        this.cableCovered = constructColoredDefinition(registry, "covered_cable", PartType.CABLE_COVERED,
                PartCableCovered::new);
        this.cableGlass = constructColoredDefinition(registry, "glass_cable", PartType.CABLE_GLASS,
                PartCableGlass::new);
        this.cableDenseCovered = constructColoredDefinition(registry, "covered_dense_cable",
                PartType.CABLE_DENSE_COVERED, PartDenseCableCovered::new);
        this.cableDenseSmart = constructColoredDefinition(registry, "smart_dense_cable", PartType.CABLE_DENSE_SMART,
                PartDenseCableSmart::new);
        this.quartzFiber = createPart(registry, "quartz_fiber", PartType.QUARTZ_FIBER, PartQuartzFiber::new);
        this.toggleBus = createPart(registry, "toggle_bus", PartType.TOGGLE_BUS, PartToggleBus::new);
        this.invertedToggleBus = createPart(registry, "inverted_toggle_bus", PartType.INVERTED_TOGGLE_BUS,
                PartInvertedToggleBus::new);
        this.cableAnchor = createPart(registry, "cable_anchor", PartType.CABLE_ANCHOR, PartCableAnchor::new);
        this.monitor = createPart(registry, "monitor", PartType.MONITOR, PartPanel::new);
        this.semiDarkMonitor = createPart(registry, "semi_dark_monitor", PartType.SEMI_DARK_MONITOR,
                PartSemiDarkPanel::new);
        this.darkMonitor = createPart(registry, "dark_monitor", PartType.DARK_MONITOR, PartDarkPanel::new);
        this.storageBus = createPart(registry, "storage_bus", PartType.STORAGE_BUS, PartStorageBus::new);
        this.fluidStorageBus = createPart(registry, "fluid_storage_bus", PartType.FLUID_STORAGE_BUS,
                PartFluidStorageBus::new);
        this.importBus = createPart(registry, "import_bus", PartType.IMPORT_BUS, PartImportBus::new);
        this.fluidImportBus = createPart(registry, "fluid_import_bus", PartType.FLUID_IMPORT_BUS,
                PartFluidImportBus::new);
        this.exportBus = createPart(registry, "export_bus", PartType.EXPORT_BUS, PartExportBus::new);
        this.fluidExportBus = createPart(registry, "fluid_export_bus", PartType.FLUID_EXPORT_BUS,
                PartFluidExportBus::new);
        this.levelEmitter = createPart(registry, "level_emitter", PartType.LEVEL_EMITTER, PartLevelEmitter::new);
        this.fluidLevelEmitter = createPart(registry, "fluid_level_emitter", PartType.FLUID_LEVEL_EMITTER,
                PartFluidLevelEmitter::new);
        this.annihilationPlane = createPart(registry, "annihilation_plane", PartType.ANNIHILATION_PLANE,
                PartAnnihilationPlane::new);
        this.identityAnnihilationPlane = createPart(registry, "identity_annihilation_plane",
                PartType.IDENTITY_ANNIHILATION_PLANE, PartIdentityAnnihilationPlane::new);
        this.fluidAnnihilationPlane = createPart(registry, "fluid_annihilation_plane",
                PartType.FLUID_ANNIHILATION_PLANE, PartFluidAnnihilationPlane::new);
        this.formationPlane = createPart(registry, "formation_plane", PartType.FORMATION_PLANE,
                PartFormationPlane::new);
        this.fluidFormationPlane = createPart(registry, "fluid_formation_plane", PartType.FLUID_FORMATION_PLANE,
                PartFluidFormationPlane::new);
        this.patternTerminal = createPart(registry, "pattern_terminal", PartType.PATTERN_TERMINAL,
                PartPatternTerminal::new);
        this.craftingTerminal = createPart(registry, "crafting_terminal", PartType.CRAFTING_TERMINAL,
                PartCraftingTerminal::new);
        this.terminal = createPart(registry, "terminal", PartType.TERMINAL, PartTerminal::new);
        this.storageMonitor = createPart(registry, "storage_monitor", PartType.STORAGE_MONITOR,
                PartStorageMonitor::new);
        this.conversionMonitor = createPart(registry, "conversion_monitor", PartType.CONVERSION_MONITOR,
                PartConversionMonitor::new);
        this.iface = createPart(registry, "cable_interface", PartType.INTERFACE, PartInterface::new);
        this.fluidIface = createPart(registry, "cable_fluid_interface", PartType.FLUID_INTERFACE,
                PartFluidInterface::new);
        this.p2PTunnelME = createPart(registry, "me_p2p_tunnel", PartType.P2P_TUNNEL_ME, PartP2PTunnelME::new);
        this.p2PTunnelRedstone = createPart(registry, "redstone_p2p_tunnel", PartType.P2P_TUNNEL_REDSTONE,
                PartP2PRedstone::new);
        this.p2PTunnelItems = createPart(registry, "item_p2p_tunnel", PartType.P2P_TUNNEL_ITEM, PartP2PItems::new);
        this.p2PTunnelFluids = createPart(registry, "fluid_p2p_tunnel", PartType.P2P_TUNNEL_FLUID, PartP2PFluids::new);
        this.p2PTunnelEU = null; // FIXME createPart( "ic2_p2p_tunnel", PartType.P2P_TUNNEL_IC2,
                                 // PartP2PIC2Power::new);
        this.p2PTunnelFE = createPart(registry, "fe_p2p_tunnel", PartType.P2P_TUNNEL_FE, PartP2PFEPower::new);
        this.p2PTunnelLight = createPart(registry, "light_p2p_tunnel", PartType.P2P_TUNNEL_LIGHT, PartP2PLight::new);
        this.interfaceTerminal = createPart(registry, "interface_terminal", PartType.INTERFACE_TERMINAL,
                PartInterfaceTerminal::new);
        this.fluidTerminal = createPart(registry, "fluid_terminal", PartType.FLUID_TERMINAL, PartFluidTerminal::new);
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

    private <T extends IPart> IItemDefinition createPart(FeatureFactory registry, String id, PartType type,
            Function<ItemStack, T> factory) {
        return registry.item(id, props -> new ItemPart<>(props, type, factory)).itemGroup(CreativeTab.INSTANCE)
                .rendering(new ItemPartRendering()).build();
    }

    private <T extends IPart> AEColoredItemDefinition constructColoredDefinition(FeatureFactory registry,
            String idSuffix, PartType type, Function<ItemStack, T> factory) {
        final ColoredItemDefinition definition = new ColoredItemDefinition();

        for (final AEColor color : AEColor.values()) {
            String id = color.registryPrefix + '_' + idSuffix;

            IItemDefinition itemDef = registry.item(id, props -> new ColoredPartItem<>(props, type, factory, color))
                    .itemGroup(CreativeTab.INSTANCE).rendering(new ItemPartRendering(color)).build();

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
