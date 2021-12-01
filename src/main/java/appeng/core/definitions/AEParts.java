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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.api.ids.AEPartIds;
import appeng.api.parts.IPart;
import appeng.api.parts.PartModels;
import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.parts.automation.AnnihilationPlanePart;
import appeng.parts.automation.EnergyLevelEmitterPart;
import appeng.parts.automation.ExportBusPart;
import appeng.parts.automation.FormationPlanePart;
import appeng.parts.automation.IdentityAnnihilationPlanePart;
import appeng.parts.automation.ImportBusPart;
import appeng.parts.automation.StorageLevelEmitterPart;
import appeng.parts.crafting.PatternProviderPart;
import appeng.parts.misc.CableAnchorPart;
import appeng.parts.misc.InterfacePart;
import appeng.parts.misc.InvertedToggleBusPart;
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
import appeng.parts.reporting.ItemTerminalPart;
import appeng.parts.reporting.PanelPart;
import appeng.parts.reporting.PatternAccessTerminalPart;
import appeng.parts.reporting.PatternTerminalPart;
import appeng.parts.reporting.SemiDarkPanelPart;
import appeng.parts.reporting.StorageMonitorPart;
import appeng.parts.storagebus.StorageBusPart;

/**
 * Internal implementation for the API parts
 */
@SuppressWarnings("unused")
public final class AEParts {
    public static final List<ColoredItemDefinition> COLORED_PARTS = new ArrayList<>();

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
    public static final ItemDefinition<PartItem<StorageBusPart>> STORAGE_BUS = createPart(AEPartIds.STORAGE_BUS, StorageBusPart.class, StorageBusPart::new);
    public static final ItemDefinition<PartItem<ImportBusPart>> IMPORT_BUS = createPart(AEPartIds.IMPORT_BUS, ImportBusPart.class, ImportBusPart::new);
    public static final ItemDefinition<PartItem<ExportBusPart>> EXPORT_BUS = createPart(AEPartIds.EXPORT_BUS, ExportBusPart.class, ExportBusPart::new);
    public static final ItemDefinition<PartItem<StorageLevelEmitterPart>> level_emitter = createPart(AEPartIds.LEVEL_EMITTER, StorageLevelEmitterPart.class, StorageLevelEmitterPart::new);
    public static final ItemDefinition<PartItem<EnergyLevelEmitterPart>> ENERGY_LEVEL_EMITTER = createPart(AEPartIds.ENERGY_LEVEL_EMITTER, EnergyLevelEmitterPart.class, EnergyLevelEmitterPart::new);
    public static final ItemDefinition<PartItem<AnnihilationPlanePart>> ANNIHILATION_PLANE = createPart(AEPartIds.ANNIHILATION_PLANE, AnnihilationPlanePart.class, AnnihilationPlanePart::new);
    public static final ItemDefinition<PartItem<IdentityAnnihilationPlanePart>> IDENTITY_ANNIHILATION_PLANE = createPart(AEPartIds.IDENTITY_ANNIHILATION_PLANE, IdentityAnnihilationPlanePart.class, IdentityAnnihilationPlanePart::new);
    public static final ItemDefinition<PartItem<FormationPlanePart>> FORMATION_PLANE = createPart(AEPartIds.FORMATION_PLANE, FormationPlanePart.class, FormationPlanePart::new);
    public static final ItemDefinition<PartItem<PatternTerminalPart>> PATTERN_ENCODING_TERMINAL = createPart(AEPartIds.PATTERN_ENCODING_TERMINAL, PatternTerminalPart.class, PatternTerminalPart::new);
    public static final ItemDefinition<PartItem<CraftingTerminalPart>> CRAFTING_TERMINAL = createPart(AEPartIds.CRAFTING_TERMINAL, CraftingTerminalPart.class, CraftingTerminalPart::new);
    public static final ItemDefinition<PartItem<ItemTerminalPart>> TERMINAL = createPart(AEPartIds.TERMINAL, ItemTerminalPart.class, ItemTerminalPart::new);
    public static final ItemDefinition<PartItem<StorageMonitorPart>> STORAGE_MONITOR = createPart(AEPartIds.STORAGE_MONITOR, StorageMonitorPart.class, StorageMonitorPart::new);
    public static final ItemDefinition<PartItem<ConversionMonitorPart>> CONVERSION_MONITOR = createPart(AEPartIds.CONVERSION_MONITOR, ConversionMonitorPart.class, ConversionMonitorPart::new);
    public static final ItemDefinition<PartItem<PatternProviderPart>> PATTERN_PROVIDER = createPart(AEPartIds.PATTERN_PROVIDER, PatternProviderPart.class, PatternProviderPart::new);
    public static final ItemDefinition<PartItem<InterfacePart>> INTERFACE = createPart(AEPartIds.INTERFACE, InterfacePart.class, InterfacePart::new);
    public static final ItemDefinition<PartItem<PatternAccessTerminalPart>> PATTERN_ACCESS_TERMINAL = createPart(AEPartIds.PATTERN_ACCESS_TERMINAL, PatternAccessTerminalPart.class, PatternAccessTerminalPart::new);
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

        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(id, props -> new PartItem<>(props, factory));
    }

    private static <T extends IPart> ColoredItemDefinition constructColoredDefinition(String idSuffix,
            Class<T> partClass,
            Function<ItemStack, T> factory) {

        PartModels.registerModels(PartModelsHelper.createModels(partClass));

        var definition = new ColoredItemDefinition();
        for (final AEColor color : AEColor.values()) {
            String id = color.registryPrefix + '_' + idSuffix;

            ItemDefinition<?> itemDef = item(AppEng.makeId(id), props -> new ColoredPartItem<>(props, factory, color));

            definition.add(color, AppEng.makeId(id), itemDef);
        }

        COLORED_PARTS.add(definition);

        return definition;
    }

    // Used to control in which order static constructors are called
    public static void init() {
    }

}
