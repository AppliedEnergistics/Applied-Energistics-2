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

package appeng.core.definitions;

import static appeng.core.definitions.AEItems.item;

import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.api.ids.AEPartIds;
import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.registries.PartModels;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.parts.automation.AnnihilationPlanePart;
import appeng.parts.automation.ExportBusPart;
import appeng.parts.automation.FluidAnnihilationPlanePart;
import appeng.parts.automation.FluidExportBusPart;
import appeng.parts.automation.FluidFormationPlanePart;
import appeng.parts.automation.FluidImportBusPart;
import appeng.parts.automation.FluidLevelEmitterPart;
import appeng.parts.automation.FormationPlanePart;
import appeng.parts.automation.IdentityAnnihilationPlanePart;
import appeng.parts.automation.ImportBusPart;
import appeng.parts.automation.LevelEmitterPart;
import appeng.parts.misc.CableAnchorPart;
import appeng.parts.misc.FluidInterfacePart;
import appeng.parts.misc.FluidStorageBusPart;
import appeng.parts.misc.InvertedToggleBusPart;
import appeng.parts.misc.ItemInterfacePart;
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
import appeng.parts.reporting.FluidTerminalPart;
import appeng.parts.reporting.InterfaceTerminalPart;
import appeng.parts.reporting.ItemTerminalPart;
import appeng.parts.reporting.PanelPart;
import appeng.parts.reporting.PatternTerminalPart;
import appeng.parts.reporting.SemiDarkPanelPart;
import appeng.parts.reporting.StorageMonitorPart;

/**
 * Internal implementation for the API parts
 */
@SuppressWarnings("unused")
public final class AEParts {
    // spotless:off
    public static final ColoredItemDefinition SMART_CABLE = constructColoredDefinition("smart_cable", SmartCablePart.class, SmartCablePart::new);
    public static final ColoredItemDefinition COVERED_CABLE = constructColoredDefinition("covered_cable", CoveredCablePart.class, CoveredCablePart::new);
    public static final ColoredItemDefinition GLASS_CABLE = constructColoredDefinition("glass_cable", GlassCablePart.class, GlassCablePart::new);
    public static final ColoredItemDefinition COVERED_DENSE_CABLE = constructColoredDefinition("covered_dense_cable", CoveredDenseCablePart.class, CoveredDenseCablePart::new);
    public static final ColoredItemDefinition SMART_DENSE_CABLE = constructColoredDefinition("smart_dense_cable", SmartDenseCablePart.class, SmartDenseCablePart::new);
    public static final ItemDefinition<PartItem<QuartzFiberPart>> QUARTZ_FIBER = createPart(AEPartIds.QUARTZ_FIBER, QuartzFiberPart.class, QuartzFiberPart::new);
    public static final ItemDefinition<PartItem<ToggleBusPart>> TOGGLE_BUS = createPart(AEPartIds.TOGGLE_BUS, ToggleBusPart.class, ToggleBusPart::new);
    public static final ItemDefinition<PartItem<InvertedToggleBusPart>> INVERTED_TOGGLE_BUS = createPart(AEPartIds.INVERTED_TOGGLE_BUS, InvertedToggleBusPart.class, InvertedToggleBusPart::new);
    public static final ItemDefinition<PartItem<CableAnchorPart>> CABLE_ANCHOR = createPart(AEPartIds.CABLE_ANCHOR, CableAnchorPart.class, CableAnchorPart::new);
    public static final ItemDefinition<PartItem<PanelPart>> MONITOR = createPart(AEPartIds.MONITOR, PanelPart.class, PanelPart::new);
    public static final ItemDefinition<PartItem<SemiDarkPanelPart>> SEMI_DARK_MONITOR = createPart(AEPartIds.SEMI_DARK_MONITOR, SemiDarkPanelPart.class, SemiDarkPanelPart::new);
    public static final ItemDefinition<PartItem<DarkPanelPart>> DARK_MONITOR = createPart(AEPartIds.DARK_MONITOR, DarkPanelPart.class, DarkPanelPart::new);
    public static final ItemDefinition<PartItem<StorageBusPart>> ITEM_STORAGE_BUS = createPart(AEPartIds.ITEM_STORAGE_BUS, StorageBusPart.class, StorageBusPart::new);
    public static final ItemDefinition<PartItem<FluidStorageBusPart>> FLUID_STORAGE_BUS = createPart(AEPartIds.FLUID_STORAGE_BUS, FluidStorageBusPart.class, FluidStorageBusPart::new);
    public static final ItemDefinition<PartItem<ImportBusPart>> IMPORT_BUS = createPart(AEPartIds.ITEM_IMPORT_BUS, ImportBusPart.class, ImportBusPart::new);
    public static final ItemDefinition<PartItem<FluidImportBusPart>> FLUID_IMPORT_BUS = createPart(AEPartIds.FLUID_IMPORT_BUS, FluidImportBusPart.class, FluidImportBusPart::new);
    public static final ItemDefinition<PartItem<ExportBusPart>> EXPORT_BUS = createPart(AEPartIds.ITEM_EXPORT_BUS, ExportBusPart.class, ExportBusPart::new);
    public static final ItemDefinition<PartItem<FluidExportBusPart>> FLUID_EXPORT_BUS = createPart(AEPartIds.FLUID_EXPORT_BUS, FluidExportBusPart.class, FluidExportBusPart::new);
    public static final ItemDefinition<PartItem<LevelEmitterPart>> LEVEL_EMITTER = createPart(AEPartIds.ITEM_LEVEL_EMITTER, LevelEmitterPart.class, LevelEmitterPart::new);
    public static final ItemDefinition<PartItem<FluidLevelEmitterPart>> FLUID_LEVEL_EMITTER = createPart(AEPartIds.FLUID_LEVEL_EMITTER, FluidLevelEmitterPart.class, FluidLevelEmitterPart::new);
    public static final ItemDefinition<PartItem<AnnihilationPlanePart>> ANNIHILATION_PLANE = createPart(AEPartIds.ITEM_ANNIHILATION_PLANE, AnnihilationPlanePart.class, AnnihilationPlanePart::new);
    public static final ItemDefinition<PartItem<IdentityAnnihilationPlanePart>> IDENTITY_ANNIHILATION_PLANE = createPart(AEPartIds.ITEM_IDENTITY_ANNIHILATION_PLANE, IdentityAnnihilationPlanePart.class, IdentityAnnihilationPlanePart::new);
    public static final ItemDefinition<PartItem<FluidAnnihilationPlanePart>> FLUID_ANNIHILATION_PLANE = createPart(AEPartIds.FLUID_ANNIHILATION_PLANE, FluidAnnihilationPlanePart.class, FluidAnnihilationPlanePart::new);
    public static final ItemDefinition<PartItem<FormationPlanePart>> FORMATION_PLANE = createPart(AEPartIds.ITEM_FORMATION_PLANE, FormationPlanePart.class, FormationPlanePart::new);
    public static final ItemDefinition<PartItem<FluidFormationPlanePart>> FLUID_FORMATION_PLANE = createPart(AEPartIds.FLUID_FORMATION_PLANE, FluidFormationPlanePart.class, FluidFormationPlanePart::new);
    public static final ItemDefinition<PartItem<PatternTerminalPart>> PATTERN_TERMINAL = createPart(AEPartIds.PATTERN_TERMINAL, PatternTerminalPart.class, PatternTerminalPart::new);
    public static final ItemDefinition<PartItem<CraftingTerminalPart>> CRAFTING_TERMINAL = createPart(AEPartIds.CRAFTING_TERMINAL, CraftingTerminalPart.class, CraftingTerminalPart::new);
    public static final ItemDefinition<PartItem<ItemTerminalPart>> TERMINAL = createPart(AEPartIds.TERMINAL, ItemTerminalPart.class, ItemTerminalPart::new);
    public static final ItemDefinition<PartItem<StorageMonitorPart>> STORAGE_MONITOR = createPart(AEPartIds.STORAGE_MONITOR, StorageMonitorPart.class, StorageMonitorPart::new);
    public static final ItemDefinition<PartItem<ConversionMonitorPart>> CONVERSION_MONITOR = createPart(AEPartIds.ITEM_CONVERSION_MONITOR, ConversionMonitorPart.class, ConversionMonitorPart::new);
    public static final ItemDefinition<PartItem<ItemInterfacePart>> INTERFACE = createPart(AEPartIds.ITEM_INTERFACE, ItemInterfacePart.class, ItemInterfacePart::new);
    public static final ItemDefinition<PartItem<FluidInterfacePart>> FLUID_INTERFACE = createPart(AEPartIds.FLUID_INTERFACE, FluidInterfacePart.class, FluidInterfacePart::new);
    public static final ItemDefinition<PartItem<InterfaceTerminalPart>> INTERFACE_TERMINAL = createPart(AEPartIds.INTERFACE_TERMINAL, InterfaceTerminalPart.class, InterfaceTerminalPart::new);
    public static final ItemDefinition<PartItem<FluidTerminalPart>> FLUID_TERMINAL = createPart(AEPartIds.FLUID_TERMINAL, FluidTerminalPart.class, FluidTerminalPart::new);
    public static final ItemDefinition<PartItem<EnergyAcceptorPart>> ENERGY_ACCEPTOR = createPart(AEPartIds.ENERGY_ACCEPTOR, EnergyAcceptorPart.class, EnergyAcceptorPart::new);
    public static final ItemDefinition<PartItem<MEP2PTunnelPart>> ME_P2P_TUNNEL = createPart(AEPartIds.ME_P2P_TUNNEL, MEP2PTunnelPart.class, MEP2PTunnelPart::new);
    public static final ItemDefinition<PartItem<RedstoneP2PTunnelPart>> REDSTONE_P2P_TUNNEL = createPart(AEPartIds.REDSTONE_P2P_TUNNEL, RedstoneP2PTunnelPart.class, RedstoneP2PTunnelPart::new);
    public static final ItemDefinition<PartItem<ItemP2PTunnelPart>> ITEM_P2P_TUNNEL = createPart(AEPartIds.ITEM_P2P_TUNNEL, ItemP2PTunnelPart.class, ItemP2PTunnelPart::new);
    public static final ItemDefinition<PartItem<FluidP2PTunnelPart>> FLUID_P2P_TUNNEL = createPart(AEPartIds.FLUID_P2P_TUNNEL, FluidP2PTunnelPart.class, FluidP2PTunnelPart::new);
    public static final ItemDefinition<PartItem<FEP2PTunnelPart>> FE_P2P_TUNNEL = createPart(AEPartIds.FE_P2P_TUNNEL, FEP2PTunnelPart.class, FEP2PTunnelPart::new);
    public static final ItemDefinition<PartItem<LightP2PTunnelPart>> LIGHT_P2P_TUNNEL = createPart(AEPartIds.LIGHT_P2P_TUNNEL, LightP2PTunnelPart.class, LightP2PTunnelPart::new);
    // spotless:on

    private static <T extends IPart> ItemDefinition<PartItem<T>> createPart(ResourceLocation id,
            Class<T> partClass,
            Function<ItemStack, T> factory) {

        PartModels partModels = (PartModels) Api.instance().registries().partModels();
        partModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(id, props -> new PartItem<>(props, factory));
    }

    private static <T extends IPart> ColoredItemDefinition constructColoredDefinition(String idSuffix,
            Class<T> partClass,
            Function<ItemStack, T> factory) {

        PartModels partModels = (PartModels) Api.instance().registries().partModels();
        partModels.registerModels(PartModelsHelper.createModels(partClass));

        final ColoredItemDefinition definition = new ColoredItemDefinition();
        for (final AEColor color : AEColor.values()) {
            String id = color.registryPrefix + '_' + idSuffix;

            ItemDefinition<?> itemDef = item(AppEng.makeId(id), props -> new ColoredPartItem<>(props, factory, color));

            definition.add(color, itemDef);
        }

        return definition;
    }

    // Used to control in which order static constructors are called
    public static void init() {
    }

}
