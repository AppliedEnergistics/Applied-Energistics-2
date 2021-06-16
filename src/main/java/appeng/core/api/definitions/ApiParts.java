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
import java.util.List;
import java.util.function.Function;

import appeng.api.definitions.AEPartIds;
import net.minecraft.item.ItemStack;

import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.core.Api;
import appeng.core.AppEng;
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
import net.minecraft.util.ResourceLocation;

/**
 * Internal implementation for the API parts
 */
@SuppressWarnings("unused")
public final class ApiParts {
    private static final List<ItemDefinition> UNCOLORED_PARTS = new ArrayList<>();
    private static final List<AEColoredItemDefinition> COLORED_PARTS = new ArrayList<>();

    // spotless:off
    public static final AEColoredItemDefinition SMART_CABLE = constructColoredDefinition("smart_cable", SmartCablePart.class, SmartCablePart::new);
    public static final AEColoredItemDefinition COVERED_CABLE = constructColoredDefinition("covered_cable", CoveredCablePart.class, CoveredCablePart::new);
    public static final AEColoredItemDefinition GLASS_CABLE = constructColoredDefinition("glass_cable", GlassCablePart.class, GlassCablePart::new);
    public static final AEColoredItemDefinition COVERED_DENSE_CABLE = constructColoredDefinition("covered_dense_cable", CoveredDenseCablePart.class, CoveredDenseCablePart::new);
    public static final AEColoredItemDefinition SMART_DENSE_CABLE = constructColoredDefinition("smart_dense_cable", SmartDenseCablePart.class, SmartDenseCablePart::new);
    public static final ItemDefinition QUARTZ_FIBER = createPart(AEPartIds.QUARTZ_FIBER, QuartzFiberPart.class, QuartzFiberPart::new);
    public static final ItemDefinition TOGGLE_BUS = createPart(AEPartIds.TOGGLE_BUS, ToggleBusPart.class, ToggleBusPart::new);
    public static final ItemDefinition INVERTED_TOGGLE_BUS = createPart(AEPartIds.INVERTED_TOGGLE_BUS, InvertedToggleBusPart.class, InvertedToggleBusPart::new);
    public static final ItemDefinition CABLE_ANCHOR = createPart(AEPartIds.CABLE_ANCHOR, CableAnchorPart.class, CableAnchorPart::new);
    public static final ItemDefinition MONITOR = createPart(AEPartIds.MONITOR, PanelPart.class, PanelPart::new);
    public static final ItemDefinition SEMI_DARK_MONITOR = createPart(AEPartIds.SEMI_DARK_MONITOR, SemiDarkPanelPart.class, SemiDarkPanelPart::new);
    public static final ItemDefinition DARK_MONITOR = createPart(AEPartIds.DARK_MONITOR, DarkPanelPart.class, DarkPanelPart::new);
    public static final ItemDefinition STORAGE_BUS = createPart(AEPartIds.STORAGE_BUS, StorageBusPart.class, StorageBusPart::new);
    public static final ItemDefinition FLUID_STORAGE_BUS = createPart(AEPartIds.FLUID_STORAGE_BUS, FluidStorageBusPart.class, FluidStorageBusPart::new);
    public static final ItemDefinition IMPORT_BUS = createPart(AEPartIds.IMPORT_BUS, ImportBusPart.class, ImportBusPart::new);
    public static final ItemDefinition FLUID_IMPORT_BUS = createPart(AEPartIds.FLUID_IMPORT_BUS, FluidImportBusPart.class, FluidImportBusPart::new);
    public static final ItemDefinition EXPORT_BUS = createPart(AEPartIds.EXPORT_BUS, ExportBusPart.class, ExportBusPart::new);
    public static final ItemDefinition FLUID_EXPORT_BUS = createPart(AEPartIds.FLUID_EXPORT_BUS, FluidExportBusPart.class, FluidExportBusPart::new);
    public static final ItemDefinition LEVEL_EMITTER = createPart(AEPartIds.LEVEL_EMITTER, LevelEmitterPart.class, LevelEmitterPart::new);
    public static final ItemDefinition FLUID_LEVEL_EMITTER = createPart(AEPartIds.FLUID_LEVEL_EMITTER, FluidLevelEmitterPart.class, FluidLevelEmitterPart::new);
    public static final ItemDefinition ANNIHILATION_PLANE = createPart(AEPartIds.ANNIHILATION_PLANE, AnnihilationPlanePart.class, AnnihilationPlanePart::new);
    public static final ItemDefinition IDENTITY_ANNIHILATION_PLANE = createPart(AEPartIds.IDENTITY_ANNIHILATION_PLANE, IdentityAnnihilationPlanePart.class, IdentityAnnihilationPlanePart::new);
    public static final ItemDefinition FLUID_ANNIHILATION_PLANE = createPart(AEPartIds.FLUID_ANNIHILATION_PLANE, FluidAnnihilationPlanePart.class, FluidAnnihilationPlanePart::new);
    public static final ItemDefinition FORMATION_PLANE = createPart(AEPartIds.FORMATION_PLANE, FormationPlanePart.class, FormationPlanePart::new);
    public static final ItemDefinition FLUID_FORMATION_PLANE = createPart(AEPartIds.FLUID_FORMATION_PLANE, FluidFormationPlanePart.class, FluidFormationPlanePart::new);
    public static final ItemDefinition PATTERN_TERMINAL = createPart(AEPartIds.PATTERN_TERMINAL, PatternTerminalPart.class, PatternTerminalPart::new);
    public static final ItemDefinition CRAFTING_TERMINAL = createPart(AEPartIds.CRAFTING_TERMINAL, CraftingTerminalPart.class, CraftingTerminalPart::new);
    public static final ItemDefinition TERMINAL = createPart(AEPartIds.TERMINAL, TerminalPart.class, TerminalPart::new);
    public static final ItemDefinition STORAGE_MONITOR = createPart(AEPartIds.STORAGE_MONITOR, StorageMonitorPart.class, StorageMonitorPart::new);
    public static final ItemDefinition CONVERSION_MONITOR = createPart(AEPartIds.CONVERSION_MONITOR, ConversionMonitorPart.class, ConversionMonitorPart::new);
    public static final ItemDefinition INTERFACE = createPart(AEPartIds.INTERFACE, InterfacePart.class, InterfacePart::new);
    public static final ItemDefinition FLUID_INTERFACE = createPart(AEPartIds.FLUID_INTERFACE, FluidInterfacePart.class, FluidInterfacePart::new);
    public static final ItemDefinition INTERFACE_TERMINAL = createPart(AEPartIds.INTERFACE_TERMINAL, InterfaceTerminalPart.class, InterfaceTerminalPart::new);
    public static final ItemDefinition FLUID_TERMINAL = createPart(AEPartIds.FLUID_TERMINAL, FluidTerminalPart.class, FluidTerminalPart::new);
    public static final ItemDefinition ENERGY_ACCEPTOR = createPart(AEPartIds.ENERGY_ACCEPTOR, EnergyAcceptorPart.class, EnergyAcceptorPart::new);
    public static final ItemDefinition ME_P2P_TUNNEL = createPart(AEPartIds.ME_P2P_TUNNEL, MEP2PTunnelPart.class, MEP2PTunnelPart::new);
    public static final ItemDefinition REDSTONE_P2P_TUNNEL = createPart(AEPartIds.REDSTONE_P2P_TUNNEL, RedstoneP2PTunnelPart.class, RedstoneP2PTunnelPart::new);
    public static final ItemDefinition ITEM_P2P_TUNNEL = createPart(AEPartIds.ITEM_P2P_TUNNEL, ItemP2PTunnelPart.class, ItemP2PTunnelPart::new);
    public static final ItemDefinition FLUID_P2P_TUNNEL = createPart(AEPartIds.FLUID_P2P_TUNNEL, FluidP2PTunnelPart.class, FluidP2PTunnelPart::new);
    public static final ItemDefinition FE_P2P_TUNNEL = createPart(AEPartIds.FE_P2P_TUNNEL, FEP2PTunnelPart.class, FEP2PTunnelPart::new);
    public static final ItemDefinition LIGHT_P2P_TUNNEL = createPart(AEPartIds.LIGHT_P2P_TUNNEL, LightP2PTunnelPart.class, LightP2PTunnelPart::new);
    // spotless:on

    private static <T extends IPart> ItemDefinition createPart(ResourceLocation id,
                                                               Class<T> partClass,
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

            ItemDefinition itemDef = ApiItems
                    .item(AppEng.makeId(id), props -> new ColoredPartItem<>(props, factory, color)).build();

            definition.add(color, new ItemStackSrc(itemDef.item(), ActivityState.Enabled));
        }

        COLORED_PARTS.add(definition);

        return definition;
    }

    // Used to control in which order static constructors are called
    public static void init() {
    }

}
