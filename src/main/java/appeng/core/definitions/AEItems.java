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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import appeng.api.config.Upgrades;
import appeng.api.ids.AEItemIds;
import appeng.api.storage.StorageChannels;
import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.crafting.pattern.CraftingPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;
import appeng.debug.DebugCardItem;
import appeng.debug.DebugPartPlacerItem;
import appeng.debug.EraserItem;
import appeng.debug.MeteoritePlacerItem;
import appeng.debug.ReplicatorCardItem;
import appeng.entity.ChargedQuartzEntity;
import appeng.entity.SingularityEntity;
import appeng.items.materials.CustomEntityItem;
import appeng.items.materials.MaterialItem;
import appeng.items.materials.NamePressItem;
import appeng.items.materials.StorageComponentItem;
import appeng.items.materials.UpgradeCardItem;
import appeng.items.misc.*;
import appeng.items.parts.FacadeItem;
import appeng.items.storage.BasicFluidStorageCell;
import appeng.items.storage.BasicItemStorageCellItem;
import appeng.items.storage.CreativeCellItem;
import appeng.items.storage.SpatialStorageCellItem;
import appeng.items.storage.ViewCellItem;
import appeng.items.tools.BiometricCardItem;
import appeng.items.tools.MemoryCardItem;
import appeng.items.tools.NetworkToolItem;
import appeng.items.tools.powered.ChargedStaffItem;
import appeng.items.tools.powered.ColorApplicatorItem;
import appeng.items.tools.powered.EntropyManipulatorItem;
import appeng.items.tools.powered.MatterCannonItem;
import appeng.items.tools.powered.PortableCellItem;
import appeng.items.tools.powered.PortableCellItem.StorageTier;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.items.tools.quartz.QuartzAxeItem;
import appeng.items.tools.quartz.QuartzCuttingKnifeItem;
import appeng.items.tools.quartz.QuartzHoeItem;
import appeng.items.tools.quartz.QuartzPickaxeItem;
import appeng.items.tools.quartz.QuartzSpadeItem;
import appeng.items.tools.quartz.QuartzSwordItem;
import appeng.items.tools.quartz.QuartzToolType;
import appeng.items.tools.quartz.QuartzWrenchItem;

/**
 * Internal implementation for the API items
 */
@SuppressWarnings("unused")
public final class AEItems {

    // spotless:off
    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();

    ///
    /// CERTUS QUARTZ TOOLS
    ///

    public static final ItemDefinition<QuartzAxeItem> CERTUS_QUARTZ_AXE = item(AEItemIds.CERTUS_QUARTZ_AXE, p -> new QuartzAxeItem(p, QuartzToolType.CERTUS), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzHoeItem> CERTUS_QUARTZ_HOE = item(AEItemIds.CERTUS_QUARTZ_HOE, p -> new QuartzHoeItem(p, QuartzToolType.CERTUS), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzSpadeItem> CERTUS_QUARTZ_SHOVEL = item(AEItemIds.CERTUS_QUARTZ_SHOVEL, p -> new QuartzSpadeItem(p, QuartzToolType.CERTUS), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzPickaxeItem> CERTUS_QUARTZ_PICK = item(AEItemIds.CERTUS_QUARTZ_PICK, p -> new QuartzPickaxeItem(p, QuartzToolType.CERTUS), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzSwordItem> CERTUS_QUARTZ_SWORD = item(AEItemIds.CERTUS_QUARTZ_SWORD, p -> new QuartzSwordItem(p, QuartzToolType.CERTUS), CreativeModeTab.TAB_COMBAT);
    public static final ItemDefinition<QuartzWrenchItem> CERTUS_QUARTZ_WRENCH = item(AEItemIds.CERTUS_QUARTZ_WRENCH, p -> new QuartzWrenchItem(p.stacksTo(1)), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzCuttingKnifeItem> CERTUS_QUARTZ_KNIFE = item(AEItemIds.CERTUS_QUARTZ_KNIFE, p -> new QuartzCuttingKnifeItem(p.durability(50), QuartzToolType.CERTUS), CreativeModeTab.TAB_TOOLS);

    ///
    /// NETHER QUARTZ TOOLS
    ///

    public static final ItemDefinition<QuartzAxeItem> NETHER_QUARTZ_AXE = item(AEItemIds.NETHER_QUARTZ_AXE, p -> new QuartzAxeItem(p, QuartzToolType.NETHER), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzHoeItem> NETHER_QUARTZ_HOE = item(AEItemIds.NETHER_QUARTZ_HOE, p -> new QuartzHoeItem(p, QuartzToolType.NETHER), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzSpadeItem> NETHER_QUARTZ_SHOVEL = item(AEItemIds.NETHER_QUARTZ_SHOVEL, p -> new QuartzSpadeItem(p, QuartzToolType.NETHER), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzPickaxeItem> NETHER_QUARTZ_PICK = item(AEItemIds.NETHER_QUARTZ_PICK, p -> new QuartzPickaxeItem(p, QuartzToolType.NETHER), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzSwordItem> NETHER_QUARTZ_SWORD = item(AEItemIds.NETHER_QUARTZ_SWORD, p -> new QuartzSwordItem(p, QuartzToolType.NETHER), CreativeModeTab.TAB_COMBAT);
    public static final ItemDefinition<QuartzWrenchItem> NETHER_QUARTZ_WRENCH = item(AEItemIds.NETHER_QUARTZ_WRENCH, p -> new QuartzWrenchItem(p.stacksTo(1)), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzCuttingKnifeItem> NETHER_QUARTZ_KNIFE = item(AEItemIds.NETHER_QUARTZ_KNIFE, p -> new QuartzCuttingKnifeItem(p.stacksTo(1).durability(50), QuartzToolType.NETHER), CreativeModeTab.TAB_TOOLS);

    ///
    /// VARIOUS POWERED TOOLS
    ///

    public static final ItemDefinition<EntropyManipulatorItem> ENTROPY_MANIPULATOR = item(AEItemIds.ENTROPY_MANIPULATOR, p -> new EntropyManipulatorItem(p.stacksTo(1)));
    public static final ItemDefinition<WirelessTerminalItem> WIRELESS_TERMINAL = item(AEItemIds.WIRELESS_TERMINAL, p -> new WirelessTerminalItem(p.stacksTo(1)));
    public static final ItemDefinition<ChargedStaffItem> CHARGED_STAFF = item(AEItemIds.CHARGED_STAFF, p -> new ChargedStaffItem(p.stacksTo(1)));
    public static final ItemDefinition<ColorApplicatorItem> COLOR_APPLICATOR = item(AEItemIds.COLOR_APPLICATOR, p -> new ColorApplicatorItem(p.stacksTo(1)));
    public static final ItemDefinition<MatterCannonItem> MASS_CANNON = item(AEItemIds.MATTER_CANNON, p -> new MatterCannonItem(p.stacksTo(1)));

    ///
    /// PORTABLE CELLS
    ///
    private static ItemDefinition<PortableCellItem> makePortableCell(ResourceLocation id, StorageTier tier) {
        return item(id, p -> new PortableCellItem(tier, p.stacksTo(1)));
    }

    public static final ItemDefinition<PortableCellItem> PORTABLE_CELL1K = makePortableCell(AEItemIds.PORTABLE_ITEM_CELL1K, StorageTier.SIZE_1K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_CELL4k = makePortableCell(AEItemIds.PORTABLE_ITEM_CELL4K, StorageTier.SIZE_4K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_CELL16K = makePortableCell(AEItemIds.PORTABLE_ITEM_CELL16K, StorageTier.SIZE_16K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_CELL64K = makePortableCell(AEItemIds.PORTABLE_ITEM_CELL64K, StorageTier.SIZE_64K);

    ///
    /// NETWORK RELATED TOOLS
    ///

    public static final ItemDefinition<BiometricCardItem> BIOMETRIC_CARD = item(AEItemIds.BIOMETRIC_CARD, p -> new BiometricCardItem(p.stacksTo(1)));
    public static final ItemDefinition<MemoryCardItem> MEMORY_CARD = item(AEItemIds.MEMORY_CARD, p -> new MemoryCardItem(p.stacksTo(1)));
    public static final ItemDefinition<NetworkToolItem> NETWORK_TOOL = item(AEItemIds.NETWORK_TOOL, p -> new NetworkToolItem(p.stacksTo(1)));

    public static final ItemDefinition<FacadeItem> FACADE = item(AEItemIds.FACADE, FacadeItem::new);
    public static final ItemDefinition<MaterialItem> BLANK_PATTERN = item(AEItemIds.BLANK_PATTERN, MaterialItem::new);
    public static final ItemDefinition<CraftingPatternItem> CRAFTING_PATTERN = item(AEItemIds.CRAFTING_PATTERN, p -> new CraftingPatternItem(p.stacksTo(1)));
    public static final ItemDefinition<ProcessingPatternItem> PROCESSING_PATTERN = item(AEItemIds.PROCESSING_PATTERN, p -> new ProcessingPatternItem(p.stacksTo(1)));

    public static final ColoredItemDefinition COLORED_PAINT_BALL = createPaintBalls(AEItemIds.COLORED_PAINT_BALL, false);
    public static final ColoredItemDefinition COLORED_LUMEN_PAINT_BALL = createPaintBalls(AEItemIds.COLORED_LUMEN_PAINT_BALL, true);

    ///
    /// MATERIALS
    ///

    public static final ItemDefinition<MaterialItem> CERTUS_QUARTZ_CRYSTAL = item(AEItemIds.CERTUS_QUARTZ_CRYSTAL, MaterialItem::new);
    public static final ItemDefinition<CustomEntityItem> CERTUS_QUARTZ_CRYSTAL_CHARGED = item(AEItemIds.CERTUS_QUARTZ_CRYSTAL_CHARGED, p -> new CustomEntityItem(p, ChargedQuartzEntity::new));
    public static final ItemDefinition<MaterialItem> CERTUS_QUARTZ_DUST = item(AEItemIds.CERTUS_QUARTZ_DUST, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SILICON = item(AEItemIds.SILICON, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> MATTER_BALL = item(AEItemIds.MATTER_BALL, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUIX_CRYSTAL = item(AEItemIds.FLUIX_CRYSTAL, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUIX_DUST = item(AEItemIds.FLUIX_DUST, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUIX_PEARL = item(AEItemIds.FLUIX_PEARL, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> CALCULATION_PROCESSOR_PRESS = item(AEItemIds.CALCULATION_PROCESSOR_PRESS, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ENGINEERING_PROCESSOR_PRESS = item(AEItemIds.ENGINEERING_PROCESSOR_PRESS, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> LOGIC_PROCESSOR_PRESS = item(AEItemIds.LOGIC_PROCESSOR_PRESS, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> CALCULATION_PROCESSOR_PRINT = item(AEItemIds.CALCULATION_PROCESSOR_PRINT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ENGINEERING_PROCESSOR_PRINT = item(AEItemIds.ENGINEERING_PROCESSOR_PRINT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> LOGIC_PROCESSOR_PRINT = item(AEItemIds.LOGIC_PROCESSOR_PRINT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SILICON_PRESS = item(AEItemIds.SILICON_PRESS, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SILICON_PRINT = item(AEItemIds.SILICON_PRINT, MaterialItem::new);
    public static final ItemDefinition<NamePressItem> NAME_PRESS = item(AEItemIds.NAME_PRESS, NamePressItem::new);
    public static final ItemDefinition<MaterialItem> LOGIC_PROCESSOR = item(AEItemIds.LOGIC_PROCESSOR, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> CALCULATION_PROCESSOR = item(AEItemIds.CALCULATION_PROCESSOR, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ENGINEERING_PROCESSOR = item(AEItemIds.ENGINEERING_PROCESSOR, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> BASIC_CARD = item(AEItemIds.BASIC_CARD, MaterialItem::new);
    public static final ItemDefinition<UpgradeCardItem> REDSTONE_CARD = item(AEItemIds.REDSTONE_CARD, p -> new UpgradeCardItem(p, Upgrades.REDSTONE));
    public static final ItemDefinition<UpgradeCardItem> CAPACITY_CARD = item(AEItemIds.CAPACITY_CARD, p -> new UpgradeCardItem(p, Upgrades.CAPACITY));
    public static final ItemDefinition<MaterialItem> ADVANCED_CARD = item(AEItemIds.ADVANCED_CARD, MaterialItem::new);
    public static final ItemDefinition<UpgradeCardItem> FUZZY_CARD = item(AEItemIds.FUZZY_CARD, p -> new UpgradeCardItem(p, Upgrades.FUZZY));
    public static final ItemDefinition<UpgradeCardItem> SPEED_CARD = item(AEItemIds.SPEED_CARD, p -> new UpgradeCardItem(p, Upgrades.SPEED));
    public static final ItemDefinition<UpgradeCardItem> INVERTER_CARD = item(AEItemIds.INVERTER_CARD, p -> new UpgradeCardItem(p, Upgrades.INVERTER));
    public static final ItemDefinition<UpgradeCardItem> CRAFTING_CARD = item(AEItemIds.CRAFTING_CARD, p -> new UpgradeCardItem(p, Upgrades.CRAFTING));
    public static final ItemDefinition<MaterialItem> SPATIAL_2_CELL_COMPONENT = item(AEItemIds.SPATIAL_2_CELL_COMPONENT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SPATIAL_16_CELL_COMPONENT = item(AEItemIds.SPATIAL_16_CELL_COMPONENT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SPATIAL_128_CELL_COMPONENT = item(AEItemIds.SPATIAL_128_CELL_COMPONENT, MaterialItem::new);
    public static final ItemDefinition<StorageComponentItem> ITEM_1K_CELL_COMPONENT = item(AEItemIds.ITEM_CELL_COMPONENT_1K, p -> new StorageComponentItem(p, 1));
    public static final ItemDefinition<StorageComponentItem> ITEM_4K_CELL_COMPONENT = item(AEItemIds.ITEM_CELL_COMPONENT_4K, p -> new StorageComponentItem(p, 4));
    public static final ItemDefinition<StorageComponentItem> ITEM_16K_CELL_COMPONENT = item(AEItemIds.ITEM_CELL_COMPONENT_16K, p -> new StorageComponentItem(p, 16));
    public static final ItemDefinition<StorageComponentItem> ITEM_64K_CELL_COMPONENT = item(AEItemIds.ITEM_CELL_COMPONENT_64K, p -> new StorageComponentItem(p, 64));
    public static final ItemDefinition<MaterialItem> EMPTY_STORAGE_CELL = item(AEItemIds.EMPTY_STORAGE_CELL, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> WIRELESS_RECEIVER = item(AEItemIds.WIRELESS_RECEIVER, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> WIRELESS_BOOSTER = item(AEItemIds.WIRELESS_BOOSTER, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FORMATION_CORE = item(AEItemIds.FORMATION_CORE, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ANNIHILATION_CORE = item(AEItemIds.ANNIHILATION_CORE, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SKY_DUST = item(AEItemIds.SKY_DUST, MaterialItem::new);
    public static final ItemDefinition<CustomEntityItem> ENDER_DUST = item(AEItemIds.ENDER_DUST, p -> new CustomEntityItem(p, SingularityEntity::new));
    public static final ItemDefinition<CustomEntityItem> SINGULARITY = item(AEItemIds.SINGULARITY, p -> new CustomEntityItem(p, SingularityEntity::new));
    public static final ItemDefinition<CustomEntityItem> QUANTUM_ENTANGLED_SINGULARITY = item(AEItemIds.QUANTUM_ENTANGLED_SINGULARITY, p -> new CustomEntityItem(p, SingularityEntity::new));
    public static final ItemDefinition<MaterialItem> FLUID_1K_CELL_COMPONENT = item(AEItemIds.FLUID_CELL_COMPONENT_1K, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUID_4K_CELL_COMPONENT = item(AEItemIds.FLUID_CELL_COMPONENT_4K, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUID_16K_CELL_COMPONENT = item(AEItemIds.FLUID_CELL_COMPONENT_16K, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUID_64K_CELL_COMPONENT = item(AEItemIds.FLUID_CELL_COMPONENT_64K, MaterialItem::new);

    ///
    /// SEEDS
    ///

    public static final ItemDefinition<CrystalSeedItem> CERTUS_CRYSTAL_SEED = item(AEItemIds.CERTUS_CRYSTAL_SEED,
            p -> new CrystalSeedItem(p, CERTUS_QUARTZ_CRYSTAL.asItem()))
            ;
    public static final ItemDefinition<CrystalSeedItem> FLUIX_CRYSTAL_SEED = item(AEItemIds.FLUIX_CRYSTAL_SEED,
            p -> new CrystalSeedItem(p, FLUIX_CRYSTAL.asItem()))
            ;

    ///
    /// CELLS
    ///

    public static final ItemDefinition<CreativeCellItem> ITEM_CELL_CREATIVE = item(AEItemIds.ITEM_CELL_CREATIVE, p -> new CreativeCellItem(p.stacksTo(1).rarity(Rarity.EPIC), StorageChannels.items()));
    public static final ItemDefinition<CreativeCellItem> FLUID_CELL_CREATIVE = item(AEItemIds.FLUID_CELL_CREATIVE, p -> new CreativeCellItem(p.stacksTo(1).rarity(Rarity.EPIC), StorageChannels.fluids()));
    public static final ItemDefinition<ViewCellItem> VIEW_CELL = item(AEItemIds.VIEW_CELL, p -> new ViewCellItem(p.stacksTo(1), StorageChannels.items()));

    public static final ItemDefinition<BasicItemStorageCellItem> CELL1K = item(AEItemIds.ITEM_CELL_1K, p -> new BasicItemStorageCellItem(p.stacksTo(1), ITEM_1K_CELL_COMPONENT, 1, 0.5f, 8));
    public static final ItemDefinition<BasicItemStorageCellItem> CELL4K = item(AEItemIds.ITEM_CELL_4K, p -> new BasicItemStorageCellItem(p.stacksTo(1), ITEM_4K_CELL_COMPONENT, 4, 1.0f, 32));
    public static final ItemDefinition<BasicItemStorageCellItem> CELL16K = item(AEItemIds.ITEM_CELL_16K, p -> new BasicItemStorageCellItem(p.stacksTo(1), ITEM_16K_CELL_COMPONENT, 16, 1.5f, 128));
    public static final ItemDefinition<BasicItemStorageCellItem> CELL64K = item(AEItemIds.ITEM_CELL_64K, p -> new BasicItemStorageCellItem(p.stacksTo(1), ITEM_64K_CELL_COMPONENT, 64, 2.0f, 512));

    public static final ItemDefinition<BasicFluidStorageCell> FLUID_CELL1K = item(AEItemIds.FLUID_CELL_1K, p -> new BasicFluidStorageCell(p.stacksTo(1), FLUID_1K_CELL_COMPONENT, 1, 0.5f, 8));
    public static final ItemDefinition<BasicFluidStorageCell> FLUID_CELL4K = item(AEItemIds.FLUID_CELL_4K, p -> new BasicFluidStorageCell(p.stacksTo(1), FLUID_4K_CELL_COMPONENT, 4, 1.0f, 32));
    public static final ItemDefinition<BasicFluidStorageCell> FLUID_CELL16K = item(AEItemIds.FLUID_CELL_16K, p -> new BasicFluidStorageCell(p.stacksTo(1), FLUID_16K_CELL_COMPONENT, 16, 1.5f, 128));
    public static final ItemDefinition<BasicFluidStorageCell> FLUID_CELL64K = item(AEItemIds.FLUID_CELL_64K, p -> new BasicFluidStorageCell(p.stacksTo(1), FLUID_64K_CELL_COMPONENT, 64, 2.0f, 512));

    public static final ItemDefinition<SpatialStorageCellItem> SPATIAL_CELL2 = item(AEItemIds.SPATIAL_CELL_2, p -> new SpatialStorageCellItem(p.stacksTo(1), 2));
    public static final ItemDefinition<SpatialStorageCellItem> SPATIAL_CELL16 = item(AEItemIds.SPATIAL_CELL_16, p -> new SpatialStorageCellItem(p.stacksTo(1), 16));
    public static final ItemDefinition<SpatialStorageCellItem> SPATIAL_CELL128 = item(AEItemIds.SPATIAL_CELL_128, p -> new SpatialStorageCellItem(p.stacksTo(1), 128));

    ///
    /// UNSUPPORTED DEV TOOLS
    ///

    public static final ItemDefinition<EraserItem> DEBUG_ERASER = item(AppEng.makeId("debug_eraser"), EraserItem::new);
    public static final ItemDefinition<MeteoritePlacerItem> DEBUG_METEORITE_PLACER = item(AppEng.makeId("debug_meteorite_placer"), MeteoritePlacerItem::new);
    public static final ItemDefinition<DebugCardItem> DEBUG_CARD = item(AppEng.makeId("debug_card"), DebugCardItem::new);
    public static final ItemDefinition<ReplicatorCardItem> DEBUG_REPLICATOR_CARD = item(AppEng.makeId("debug_replicator_card"), ReplicatorCardItem::new);
    public static final ItemDefinition<DebugPartPlacerItem> DEBUG_PART_PLACER = item(AppEng.makeId("debug_part_placer"), DebugPartPlacerItem::new);
    public static final ItemDefinition<WrappedGenericStack> WRAPPED_GENERIC_STACK = item(AEItemIds.WRAPPED_GENERIC_STACK, WrappedGenericStack::new);

    // spotless:on

    public static List<ItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    private static ColoredItemDefinition createPaintBalls(Map<AEColor, ResourceLocation> ids, boolean lumen) {
        ColoredItemDefinition colors = new ColoredItemDefinition();
        for (AEColor color : AEColor.values()) {
            ResourceLocation id = ids.get(color);
            if (id == null) {
                continue;
            }

            colors.add(color, id, item(id, p -> new PaintBallItem(p, color, lumen)));
        }
        return colors;
    }

    static <T extends Item> ItemDefinition<T> item(ResourceLocation id, Function<FabricItemSettings, T> factory) {
        return item(id, factory, CreativeTab.INSTANCE);
    }

    static <T extends Item> ItemDefinition<T> item(ResourceLocation id, Function<FabricItemSettings, T> factory,
            CreativeModeTab group) {

        FabricItemSettings p = new FabricItemSettings().group(group);

        T item = factory.apply(p);

        ItemDefinition<T> definition = new ItemDefinition<>(id, item);

        if (group == CreativeTab.INSTANCE) {
            CreativeTab.add(definition);
        }

        ITEMS.add(definition);

        return definition;
    }

    // Used to control in which order static constructors are called
    public static void init() {
    }

}
