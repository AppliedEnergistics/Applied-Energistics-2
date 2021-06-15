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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import net.minecraft.item.ItemStack;

import appeng.api.definitions.IItemDefinition;
import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.core.Api;
import appeng.core.features.ActivityState;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.ItemDefinition;
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
import appeng.items.parts.PartModelsHelper;
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
import appeng.parts.networking.EnergyAcceptorPart;
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
public final class ApiParts {
    private static final List<IItemDefinition> UNCOLORED_PARTS = new ArrayList<>();
    private static final List<AEColoredItemDefinition> COLORED_PARTS = new ArrayList<>();
    private static final AEColoredItemDefinition cableSmart;
    private static final AEColoredItemDefinition cableCovered;
    private static final AEColoredItemDefinition cableGlass;
    private static final AEColoredItemDefinition cableDenseCovered;
    private static final AEColoredItemDefinition cableDenseSmart;
    private static final IItemDefinition quartzFiber;
    private static final IItemDefinition toggleBus;
    private static final IItemDefinition invertedToggleBus;
    private static final IItemDefinition storageBus;
    private static final IItemDefinition importBus;
    private static final IItemDefinition exportBus;
    private static final IItemDefinition iface;
    private static final IItemDefinition fluidIface;
    private static final IItemDefinition levelEmitter;
    private static final IItemDefinition fluidLevelEmitter;
    private static final IItemDefinition annihilationPlane;
    private static final IItemDefinition identityAnnihilationPlane;
    private static final IItemDefinition fluidAnnihilationPlane;
    private static final IItemDefinition formationPlane;
    private static final IItemDefinition fluidFormationPlane;
    private static final IItemDefinition p2PTunnelME;
    private static final IItemDefinition p2PTunnelRedstone;
    private static final IItemDefinition p2PTunnelItems;
    private static final IItemDefinition p2PTunnelFluids;
    private static final IItemDefinition p2PTunnelEU;
    private static final IItemDefinition p2PTunnelFE;
    private static final IItemDefinition p2PTunnelLight;
    private static final IItemDefinition cableAnchor;
    private static final IItemDefinition monitor;
    private static final IItemDefinition semiDarkMonitor;
    private static final IItemDefinition darkMonitor;
    private static final IItemDefinition interfaceTerminal;
    private static final IItemDefinition patternTerminal;
    private static final IItemDefinition craftingTerminal;
    private static final IItemDefinition terminal;
    private static final IItemDefinition storageMonitor;
    private static final IItemDefinition conversionMonitor;
    private static final IItemDefinition fluidImportBus;
    private static final IItemDefinition fluidExportBus;
    private static final IItemDefinition fluidTerminal;
    private static final IItemDefinition fluidStorageBus;
    private static final IItemDefinition energyAcceptor;

    static {
        cableSmart = constructColoredDefinition("smart_cable", SmartCablePart.class, SmartCablePart::new);
        cableCovered = constructColoredDefinition("covered_cable", CoveredCablePart.class, CoveredCablePart::new);
        cableGlass = constructColoredDefinition("glass_cable", GlassCablePart.class, GlassCablePart::new);
        cableDenseCovered = constructColoredDefinition("covered_dense_cable", CoveredDenseCablePart.class,
                CoveredDenseCablePart::new);
        cableDenseSmart = constructColoredDefinition("smart_dense_cable", SmartDenseCablePart.class,
                SmartDenseCablePart::new);
        quartzFiber = createPart("quartz_fiber", QuartzFiberPart.class, QuartzFiberPart::new);
        toggleBus = createPart("toggle_bus", ToggleBusPart.class, ToggleBusPart::new);
        invertedToggleBus = createPart("inverted_toggle_bus", InvertedToggleBusPart.class,
                InvertedToggleBusPart::new);
        cableAnchor = createPart("cable_anchor", CableAnchorPart.class, CableAnchorPart::new);
        monitor = createPart("monitor", PanelPart.class, PanelPart::new);
        semiDarkMonitor = createPart("semi_dark_monitor", SemiDarkPanelPart.class, SemiDarkPanelPart::new);
        darkMonitor = createPart("dark_monitor", DarkPanelPart.class, DarkPanelPart::new);
        storageBus = createPart("storage_bus", StorageBusPart.class, StorageBusPart::new);
        fluidStorageBus = createPart("fluid_storage_bus", FluidStorageBusPart.class, FluidStorageBusPart::new);
        importBus = createPart("import_bus", ImportBusPart.class, ImportBusPart::new);
        fluidImportBus = createPart("fluid_import_bus", FluidImportBusPart.class, FluidImportBusPart::new);
        exportBus = createPart("export_bus", ExportBusPart.class, ExportBusPart::new);
        fluidExportBus = createPart("fluid_export_bus", FluidExportBusPart.class, FluidExportBusPart::new);
        levelEmitter = createPart("level_emitter", LevelEmitterPart.class, LevelEmitterPart::new);
        fluidLevelEmitter = createPart("fluid_level_emitter", FluidLevelEmitterPart.class,
                FluidLevelEmitterPart::new);
        annihilationPlane = createPart("annihilation_plane", AnnihilationPlanePart.class,
                AnnihilationPlanePart::new);
        identityAnnihilationPlane = createPart("identity_annihilation_plane", IdentityAnnihilationPlanePart.class,
                IdentityAnnihilationPlanePart::new);
        fluidAnnihilationPlane = createPart("fluid_annihilation_plane", FluidAnnihilationPlanePart.class,
                FluidAnnihilationPlanePart::new);
        formationPlane = createPart("formation_plane", FormationPlanePart.class, FormationPlanePart::new);
        fluidFormationPlane = createPart("fluid_formation_plane", FluidFormationPlanePart.class,
                FluidFormationPlanePart::new);
        patternTerminal = createPart("pattern_terminal", PatternTerminalPart.class, PatternTerminalPart::new);
        craftingTerminal = createPart("crafting_terminal", CraftingTerminalPart.class, CraftingTerminalPart::new);
        terminal = createPart("terminal", TerminalPart.class, TerminalPart::new);
        storageMonitor = createPart("storage_monitor", StorageMonitorPart.class, StorageMonitorPart::new);
        conversionMonitor = createPart("conversion_monitor", ConversionMonitorPart.class,
                ConversionMonitorPart::new);
        iface = createPart("cable_interface", InterfacePart.class, InterfacePart::new);
        fluidIface = createPart("cable_fluid_interface", FluidInterfacePart.class, FluidInterfacePart::new);
        p2PTunnelME = createPart("me_p2p_tunnel", MEP2PTunnelPart.class, MEP2PTunnelPart::new);
        p2PTunnelRedstone = createPart("redstone_p2p_tunnel", RedstoneP2PTunnelPart.class,
                RedstoneP2PTunnelPart::new);
        p2PTunnelItems = createPart("item_p2p_tunnel", ItemP2PTunnelPart.class, ItemP2PTunnelPart::new);
        p2PTunnelFluids = createPart("fluid_p2p_tunnel", FluidP2PTunnelPart.class, FluidP2PTunnelPart::new);
        p2PTunnelEU = null; // FIXME createPart( "ic2_p2p_tunnel", PartType.P2P_TUNNEL_IC2,
        // PartP2PIC2Power.class, PartP2PIC2Power::new);
        p2PTunnelFE = createPart("fe_p2p_tunnel", FEP2PTunnelPart.class, FEP2PTunnelPart::new);
        p2PTunnelLight = createPart("light_p2p_tunnel", LightP2PTunnelPart.class, LightP2PTunnelPart::new);
        interfaceTerminal = createPart("interface_terminal", InterfaceTerminalPart.class,
                InterfaceTerminalPart::new);
        fluidTerminal = createPart("fluid_terminal", FluidTerminalPart.class, FluidTerminalPart::new);

        energyAcceptor = createPart("cable_energy_acceptor", EnergyAcceptorPart.class, EnergyAcceptorPart::new);
    }

    public static List<IItemDefinition> getUncoloredParts() {
        return Collections.unmodifiableList(UNCOLORED_PARTS);
    }

    public static List<AEColoredItemDefinition> getColoredParts() {
        return Collections.unmodifiableList(COLORED_PARTS);
    }

    private static <T extends IPart> IItemDefinition createPart(String id, Class<T> partClass,
            Function<ItemStack, T> factory) {

        // TODO
        PartModels partModels = (PartModels) Api.instance().registries().partModels();
        partModels.registerModels(PartModelsHelper.createModels(partClass));
        ItemDefinition result = ApiItems.item(id, props -> new PartItem<>(props, factory)).build();
        UNCOLORED_PARTS.add(result);
        return result;
    }

    private static <T extends IPart> AEColoredItemDefinition constructColoredDefinition(String idSuffix,
            Class<T> partClass,
            Function<ItemStack, T> factory) {

        // TODO
        PartModels partModels = (PartModels) Api.instance().registries().partModels();
        partModels.registerModels(PartModelsHelper.createModels(partClass));

        final ColoredItemDefinition definition = new ColoredItemDefinition();
        for (final AEColor color : AEColor.values()) {
            String id = color.registryPrefix + '_' + idSuffix;

            IItemDefinition itemDef = ApiItems.item(id, props -> new ColoredPartItem<>(props, factory, color)).build();

            definition.add(color, new ItemStackSrc(itemDef.item(), ActivityState.Enabled));
        }

        COLORED_PARTS.add(definition);

        return definition;
    }

    public static AEColoredItemDefinition cableSmart() {
        return cableSmart;
    }

    public static AEColoredItemDefinition cableCovered() {
        return cableCovered;
    }

    public static AEColoredItemDefinition cableGlass() {
        return cableGlass;
    }

    public static AEColoredItemDefinition cableDenseCovered() {
        return cableDenseCovered;
    }

    public static AEColoredItemDefinition cableDenseSmart() {
        return cableDenseSmart;
    }

    public static IItemDefinition quartzFiber() {
        return quartzFiber;
    }

    public static IItemDefinition toggleBus() {
        return toggleBus;
    }

    public static IItemDefinition invertedToggleBus() {
        return invertedToggleBus;
    }

    public static IItemDefinition storageBus() {
        return storageBus;
    }

    public static IItemDefinition importBus() {
        return importBus;
    }

    public static IItemDefinition exportBus() {
        return exportBus;
    }

    public static IItemDefinition iface() {
        return iface;
    }

    public static IItemDefinition fluidIface() {
        return fluidIface;
    }

    public static IItemDefinition levelEmitter() {
        return levelEmitter;
    }

    public static IItemDefinition annihilationPlane() {
        return annihilationPlane;
    }

    public static IItemDefinition identityAnnihilationPlane() {
        return identityAnnihilationPlane;
    }

    public static IItemDefinition formationPlane() {
        return formationPlane;
    }

    public static IItemDefinition p2PTunnelME() {
        return p2PTunnelME;
    }

    public static IItemDefinition p2PTunnelRedstone() {
        return p2PTunnelRedstone;
    }

    public static IItemDefinition p2PTunnelItems() {
        return p2PTunnelItems;
    }

    public static IItemDefinition p2PTunnelFluids() {
        return p2PTunnelFluids;
    }

    public static IItemDefinition p2PTunnelEU() {
        return p2PTunnelEU;
    }

    public static IItemDefinition p2PTunnelFE() {
        return p2PTunnelFE;
    }

    public static IItemDefinition p2PTunnelLight() {
        return p2PTunnelLight;
    }

    public static IItemDefinition cableAnchor() {
        return cableAnchor;
    }

    public static IItemDefinition monitor() {
        return monitor;
    }

    public static IItemDefinition semiDarkMonitor() {
        return semiDarkMonitor;
    }

    public static IItemDefinition darkMonitor() {
        return darkMonitor;
    }

    public static IItemDefinition interfaceTerminal() {
        return interfaceTerminal;
    }

    public static IItemDefinition patternTerminal() {
        return patternTerminal;
    }

    public static IItemDefinition craftingTerminal() {
        return craftingTerminal;
    }

    public static IItemDefinition terminal() {
        return terminal;
    }

    public static IItemDefinition storageMonitor() {
        return storageMonitor;
    }

    public static IItemDefinition conversionMonitor() {
        return conversionMonitor;
    }

    public static IItemDefinition fluidTerminal() {
        return fluidTerminal;
    }

    public static IItemDefinition fluidImportBus() {
        return fluidImportBus;
    }

    public static IItemDefinition fluidExportBus() {
        return fluidExportBus;
    }

    public static IItemDefinition fluidStorageBus() {
        return fluidStorageBus;
    }

    public static IItemDefinition fluidLevelEmitter() {
        return fluidLevelEmitter;
    }

    public static IItemDefinition fluidAnnihilationPlane() {
        return fluidAnnihilationPlane;
    }

    public static IItemDefinition fluidFormationnPlane() {
        return fluidFormationPlane;
    }

    public static IItemDefinition energyAcceptor() {
        return energyAcceptor;
    }

    // Used to control in which order static constructors are called
    public static void init() {
    }

}
