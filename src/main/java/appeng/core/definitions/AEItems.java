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
import appeng.api.stacks.AEKeyType;
import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.crafting.pattern.CraftingPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;
import appeng.debug.DebugCardItem;
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
import appeng.items.misc.CrystalSeedItem;
import appeng.items.misc.PaintBallItem;
import appeng.items.misc.WrappedGenericStack;
import appeng.items.parts.FacadeItem;
import appeng.items.storage.BasicStorageCell;
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
import appeng.items.tools.powered.WirelessCraftingTerminalItem;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.items.tools.quartz.QuartzAxeItem;
import appeng.items.tools.quartz.QuartzCuttingKnifeItem;
import appeng.items.tools.quartz.QuartzHoeItem;
import appeng.items.tools.quartz.QuartzPickaxeItem;
import appeng.items.tools.quartz.QuartzSpadeItem;
import appeng.items.tools.quartz.QuartzSwordItem;
import appeng.items.tools.quartz.QuartzToolType;
import appeng.items.tools.quartz.QuartzWrenchItem;
import appeng.menu.me.common.MEStorageMenu;

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

    public static final ItemDefinition<QuartzAxeItem> CERTUS_QUARTZ_AXE = item("Certus Quartz Axe", AEItemIds.CERTUS_QUARTZ_AXE, p -> new QuartzAxeItem(p, QuartzToolType.CERTUS), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzHoeItem> CERTUS_QUARTZ_HOE = item("Certus Quartz Hoe", AEItemIds.CERTUS_QUARTZ_HOE, p -> new QuartzHoeItem(p, QuartzToolType.CERTUS), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzSpadeItem> CERTUS_QUARTZ_SHOVEL = item("Certus Quartz Shovel", AEItemIds.CERTUS_QUARTZ_SHOVEL, p -> new QuartzSpadeItem(p, QuartzToolType.CERTUS), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzPickaxeItem> CERTUS_QUARTZ_PICK = item("Certus Quartz Pickaxe", AEItemIds.CERTUS_QUARTZ_PICK, p -> new QuartzPickaxeItem(p, QuartzToolType.CERTUS), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzSwordItem> CERTUS_QUARTZ_SWORD = item("Certus Quartz Sword", AEItemIds.CERTUS_QUARTZ_SWORD, p -> new QuartzSwordItem(p, QuartzToolType.CERTUS), CreativeModeTab.TAB_COMBAT);
    public static final ItemDefinition<QuartzWrenchItem> CERTUS_QUARTZ_WRENCH = item("Certus Quartz Wrench", AEItemIds.CERTUS_QUARTZ_WRENCH, p -> new QuartzWrenchItem(p.stacksTo(1)), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzCuttingKnifeItem> CERTUS_QUARTZ_KNIFE = item("Certus Quartz Cutting Knife", AEItemIds.CERTUS_QUARTZ_KNIFE, p -> new QuartzCuttingKnifeItem(p.durability(50), QuartzToolType.CERTUS), CreativeModeTab.TAB_TOOLS);

    ///
    /// NETHER QUARTZ TOOLS
    ///

    public static final ItemDefinition<QuartzAxeItem> NETHER_QUARTZ_AXE = item("Nether Quartz Axe", AEItemIds.NETHER_QUARTZ_AXE, p -> new QuartzAxeItem(p, QuartzToolType.NETHER), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzHoeItem> NETHER_QUARTZ_HOE = item("Nether Quartz Hoe", AEItemIds.NETHER_QUARTZ_HOE, p -> new QuartzHoeItem(p, QuartzToolType.NETHER), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzSpadeItem> NETHER_QUARTZ_SHOVEL = item("Nether Quartz Shovel", AEItemIds.NETHER_QUARTZ_SHOVEL, p -> new QuartzSpadeItem(p, QuartzToolType.NETHER), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzPickaxeItem> NETHER_QUARTZ_PICK = item("Nether Quartz Pickaxe", AEItemIds.NETHER_QUARTZ_PICK, p -> new QuartzPickaxeItem(p, QuartzToolType.NETHER), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzSwordItem> NETHER_QUARTZ_SWORD = item("Nether Quartz Sword", AEItemIds.NETHER_QUARTZ_SWORD, p -> new QuartzSwordItem(p, QuartzToolType.NETHER), CreativeModeTab.TAB_COMBAT);
    public static final ItemDefinition<QuartzWrenchItem> NETHER_QUARTZ_WRENCH = item("Nether Quartz Wrench", AEItemIds.NETHER_QUARTZ_WRENCH, p -> new QuartzWrenchItem(p.stacksTo(1)), CreativeModeTab.TAB_TOOLS);
    public static final ItemDefinition<QuartzCuttingKnifeItem> NETHER_QUARTZ_KNIFE = item("Nether Quartz Cutting Knife", AEItemIds.NETHER_QUARTZ_KNIFE, p -> new QuartzCuttingKnifeItem(p.stacksTo(1).durability(50), QuartzToolType.NETHER), CreativeModeTab.TAB_TOOLS);

    ///
    /// VARIOUS POWERED TOOLS
    ///

    public static final ItemDefinition<EntropyManipulatorItem> ENTROPY_MANIPULATOR = item("Entropy Manipulator", AEItemIds.ENTROPY_MANIPULATOR, p -> new EntropyManipulatorItem(p.stacksTo(1)));
    public static final ItemDefinition<WirelessTerminalItem> WIRELESS_TERMINAL = item("Wireless Terminal", AEItemIds.WIRELESS_TERMINAL, p -> new WirelessTerminalItem(AEConfig.instance().getWirelessTerminalBattery(), p.stacksTo(1)));
    public static final ItemDefinition<WirelessTerminalItem> WIRELESS_CRAFTING_TERMINAL = item("Wireless Crafting Terminal", AEItemIds.WIRELESS_CRAFTING_TERMINAL, p -> new WirelessCraftingTerminalItem(AEConfig.instance().getWirelessTerminalBattery(), p.stacksTo(1)));
    public static final ItemDefinition<ChargedStaffItem> CHARGED_STAFF = item("Charged Staff", AEItemIds.CHARGED_STAFF, p -> new ChargedStaffItem(p.stacksTo(1)));
    public static final ItemDefinition<ColorApplicatorItem> COLOR_APPLICATOR = item("Color Applicator", AEItemIds.COLOR_APPLICATOR, p -> new ColorApplicatorItem(p.stacksTo(1)));
    public static final ItemDefinition<MatterCannonItem> MASS_CANNON = item("Matter Cannon", AEItemIds.MATTER_CANNON, p -> new MatterCannonItem(p.stacksTo(1)));

    ///
    /// PORTABLE CELLS
    ///
    private static ItemDefinition<PortableCellItem> makePortableItemCell(ResourceLocation id, StorageTier tier) {
        var name = tier.namePrefix() + " Portable Item Cell";
        return item(name, id, p -> new PortableCellItem(AEKeyType.items(), MEStorageMenu.PORTABLE_ITEM_CELL_TYPE, tier, p.stacksTo(1)));
    }
    private static ItemDefinition<PortableCellItem> makePortableFluidCell(ResourceLocation id, StorageTier tier) {
        var name = tier.namePrefix() + " Portable Fluid Cell";
        return item(name, id, p -> new PortableCellItem(AEKeyType.fluids(), MEStorageMenu.PORTABLE_FLUID_CELL_TYPE, tier, p.stacksTo(1)));
    }

    public static final ItemDefinition<PortableCellItem> PORTABLE_ITEM_CELL1K = makePortableItemCell(AEItemIds.PORTABLE_ITEM_CELL1K, PortableCellItem.SIZE_1K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_ITEM_CELL4k = makePortableItemCell(AEItemIds.PORTABLE_ITEM_CELL4K, PortableCellItem.SIZE_4K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_ITEM_CELL16K = makePortableItemCell(AEItemIds.PORTABLE_ITEM_CELL16K, PortableCellItem.SIZE_16K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_ITEM_CELL64K = makePortableItemCell(AEItemIds.PORTABLE_ITEM_CELL64K, PortableCellItem.SIZE_64K);

    public static final ItemDefinition<PortableCellItem> PORTABLE_FLUID_CELL1K = makePortableFluidCell(AEItemIds.PORTABLE_FLUID_CELL1K, PortableCellItem.SIZE_1K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_FLUID_CELL4k = makePortableFluidCell(AEItemIds.PORTABLE_FLUID_CELL4K, PortableCellItem.SIZE_4K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_FLUID_CELL16K = makePortableFluidCell(AEItemIds.PORTABLE_FLUID_CELL16K, PortableCellItem.SIZE_16K);
    public static final ItemDefinition<PortableCellItem> PORTABLE_FLUID_CELL64K = makePortableFluidCell(AEItemIds.PORTABLE_FLUID_CELL64K, PortableCellItem.SIZE_64K);

    ///
    /// NETWORK RELATED TOOLS
    ///

    public static final ItemDefinition<BiometricCardItem> BIOMETRIC_CARD = item("Biometric Card", AEItemIds.BIOMETRIC_CARD, p -> new BiometricCardItem(p.stacksTo(1)));
    public static final ItemDefinition<MemoryCardItem> MEMORY_CARD = item("Memory Card", AEItemIds.MEMORY_CARD, p -> new MemoryCardItem(p.stacksTo(1)));
    public static final ItemDefinition<NetworkToolItem> NETWORK_TOOL = item("Network Tool", AEItemIds.NETWORK_TOOL, p -> new NetworkToolItem(p.stacksTo(1)));

    public static final ItemDefinition<FacadeItem> FACADE = item("Cable Facade", AEItemIds.FACADE, FacadeItem::new);
    public static final ItemDefinition<MaterialItem> BLANK_PATTERN = item("Blank Pattern", AEItemIds.BLANK_PATTERN, MaterialItem::new);
    public static final ItemDefinition<CraftingPatternItem> CRAFTING_PATTERN = item("Crafting Pattern", AEItemIds.CRAFTING_PATTERN, p -> new CraftingPatternItem(p.stacksTo(1)));
    public static final ItemDefinition<ProcessingPatternItem> PROCESSING_PATTERN = item("Processing Pattern", AEItemIds.PROCESSING_PATTERN, p -> new ProcessingPatternItem(p.stacksTo(1)));

    public static final ColoredItemDefinition<PaintBallItem> COLORED_PAINT_BALL = createPaintBalls("Paint Ball", AEItemIds.COLORED_PAINT_BALL, false);
    public static final ColoredItemDefinition<PaintBallItem> COLORED_LUMEN_PAINT_BALL = createPaintBalls("Lumen Paint Ball", AEItemIds.COLORED_LUMEN_PAINT_BALL, true);

    ///
    /// MATERIALS
    ///

    public static final ItemDefinition<MaterialItem> CERTUS_QUARTZ_CRYSTAL = item("Certus Quartz Crystal", AEItemIds.CERTUS_QUARTZ_CRYSTAL, MaterialItem::new);
    public static final ItemDefinition<CustomEntityItem> CERTUS_QUARTZ_CRYSTAL_CHARGED = item("Charged Certus Quartz Crystal", AEItemIds.CERTUS_QUARTZ_CRYSTAL_CHARGED, p -> new CustomEntityItem(p, ChargedQuartzEntity::new));
    public static final ItemDefinition<MaterialItem> CERTUS_QUARTZ_DUST = item("Certus Quartz Dust", AEItemIds.CERTUS_QUARTZ_DUST, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SILICON = item("Silicon", AEItemIds.SILICON, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> MATTER_BALL = item("Matter Ball", AEItemIds.MATTER_BALL, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUIX_CRYSTAL = item("Fluix Crystal", AEItemIds.FLUIX_CRYSTAL, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUIX_DUST = item("Fluix Dust", AEItemIds.FLUIX_DUST, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUIX_PEARL = item("Fluix Pearl", AEItemIds.FLUIX_PEARL, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> CALCULATION_PROCESSOR_PRESS = item("Inscriber Calculation Press", AEItemIds.CALCULATION_PROCESSOR_PRESS, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ENGINEERING_PROCESSOR_PRESS = item("Inscriber Engineering Press", AEItemIds.ENGINEERING_PROCESSOR_PRESS, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> LOGIC_PROCESSOR_PRESS = item("Inscriber Logic Press", AEItemIds.LOGIC_PROCESSOR_PRESS, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> CALCULATION_PROCESSOR_PRINT = item("Printed Calculation Circuit", AEItemIds.CALCULATION_PROCESSOR_PRINT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ENGINEERING_PROCESSOR_PRINT = item("Printed Engineering Circuit", AEItemIds.ENGINEERING_PROCESSOR_PRINT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> LOGIC_PROCESSOR_PRINT = item("Printed Logic Circuit", AEItemIds.LOGIC_PROCESSOR_PRINT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SILICON_PRESS = item("Inscriber Silicon Press", AEItemIds.SILICON_PRESS, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SILICON_PRINT = item("Printed Silicon", AEItemIds.SILICON_PRINT, MaterialItem::new);
    public static final ItemDefinition<NamePressItem> NAME_PRESS = item("Inscriber Name Press", AEItemIds.NAME_PRESS, NamePressItem::new);
    public static final ItemDefinition<MaterialItem> LOGIC_PROCESSOR = item("Logic Processor", AEItemIds.LOGIC_PROCESSOR, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> CALCULATION_PROCESSOR = item("Calculation Processor", AEItemIds.CALCULATION_PROCESSOR, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ENGINEERING_PROCESSOR = item("Engineering Processor", AEItemIds.ENGINEERING_PROCESSOR, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> BASIC_CARD = item("Basic Card", AEItemIds.BASIC_CARD, MaterialItem::new);
    public static final ItemDefinition<UpgradeCardItem> REDSTONE_CARD = item("Redstone Card", AEItemIds.REDSTONE_CARD, p -> new UpgradeCardItem(p, Upgrades.REDSTONE));
    public static final ItemDefinition<UpgradeCardItem> CAPACITY_CARD = item("Capacity Card", AEItemIds.CAPACITY_CARD, p -> new UpgradeCardItem(p, Upgrades.CAPACITY));
    public static final ItemDefinition<MaterialItem> ADVANCED_CARD = item("Advanced Card", AEItemIds.ADVANCED_CARD, MaterialItem::new);
    public static final ItemDefinition<UpgradeCardItem> FUZZY_CARD = item("Fuzzy Card", AEItemIds.FUZZY_CARD, p -> new UpgradeCardItem(p, Upgrades.FUZZY));
    public static final ItemDefinition<UpgradeCardItem> SPEED_CARD = item("Acceleration Card", AEItemIds.SPEED_CARD, p -> new UpgradeCardItem(p, Upgrades.SPEED));
    public static final ItemDefinition<UpgradeCardItem> INVERTER_CARD = item("Inverter Card", AEItemIds.INVERTER_CARD, p -> new UpgradeCardItem(p, Upgrades.INVERTER));
    public static final ItemDefinition<UpgradeCardItem> CRAFTING_CARD = item("Crafting Card", AEItemIds.CRAFTING_CARD, p -> new UpgradeCardItem(p, Upgrades.CRAFTING));
    public static final ItemDefinition<MaterialItem> SPATIAL_2_CELL_COMPONENT = item("2³ Spatial Component", AEItemIds.SPATIAL_2_CELL_COMPONENT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SPATIAL_16_CELL_COMPONENT = item("16³ Spatial Component", AEItemIds.SPATIAL_16_CELL_COMPONENT, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SPATIAL_128_CELL_COMPONENT = item("128³ Spatial Component", AEItemIds.SPATIAL_128_CELL_COMPONENT, MaterialItem::new);
    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_1K = item("1k ME Storage Component", AEItemIds.CELL_COMPONENT_1K, p -> new StorageComponentItem(p, 1));
    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_4K = item("4k ME Storage Component", AEItemIds.CELL_COMPONENT_4K, p -> new StorageComponentItem(p, 4));
    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_16K = item("16k ME Storage Component", AEItemIds.CELL_COMPONENT_16K, p -> new StorageComponentItem(p, 16));
    public static final ItemDefinition<StorageComponentItem> CELL_COMPONENT_64K = item("64k ME Storage Component", AEItemIds.CELL_COMPONENT_64K, p -> new StorageComponentItem(p, 64));
    public static final ItemDefinition<MaterialItem> ITEM_CELL_HOUSING = item("ME Item Cell Housing", AEItemIds.ITEM_CELL_HOUSING, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FLUID_CELL_HOUSING = item("ME Fluid Cell Housing", AEItemIds.FLUID_CELL_HOUSING, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> WIRELESS_RECEIVER = item("Wireless Receiver", AEItemIds.WIRELESS_RECEIVER, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> WIRELESS_BOOSTER = item("Wireless Booster", AEItemIds.WIRELESS_BOOSTER, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> FORMATION_CORE = item("Formation Core", AEItemIds.FORMATION_CORE, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> ANNIHILATION_CORE = item("Annihilation Core", AEItemIds.ANNIHILATION_CORE, MaterialItem::new);
    public static final ItemDefinition<MaterialItem> SKY_DUST = item("Sky Stone Dust", AEItemIds.SKY_DUST, MaterialItem::new);
    public static final ItemDefinition<CustomEntityItem> ENDER_DUST = item("Ender Dust", AEItemIds.ENDER_DUST, p -> new CustomEntityItem(p, SingularityEntity::new));
    public static final ItemDefinition<CustomEntityItem> SINGULARITY = item("Singularity", AEItemIds.SINGULARITY, p -> new CustomEntityItem(p, SingularityEntity::new));
    public static final ItemDefinition<CustomEntityItem> QUANTUM_ENTANGLED_SINGULARITY = item("Quantum Entangled Singularity", AEItemIds.QUANTUM_ENTANGLED_SINGULARITY, p -> new CustomEntityItem(p, SingularityEntity::new));

    ///
    /// SEEDS
    ///

    public static final ItemDefinition<CrystalSeedItem> CERTUS_CRYSTAL_SEED = item("Certus Quartz Seed", AEItemIds.CERTUS_CRYSTAL_SEED,
            p -> new CrystalSeedItem(p, CERTUS_QUARTZ_CRYSTAL.asItem()))
            ;
    public static final ItemDefinition<CrystalSeedItem> FLUIX_CRYSTAL_SEED = item("Fluix Seed", AEItemIds.FLUIX_CRYSTAL_SEED,
            p -> new CrystalSeedItem(p, FLUIX_CRYSTAL.asItem()))
            ;

    ///
    /// CELLS
    ///

    public static final ItemDefinition<CreativeCellItem> ITEM_CELL_CREATIVE = item("Creative ME Item Cell", AEItemIds.ITEM_CELL_CREATIVE, p -> new CreativeCellItem(p.stacksTo(1).rarity(Rarity.EPIC)));
    public static final ItemDefinition<CreativeCellItem> FLUID_CELL_CREATIVE = item("Creative ME Fluid Cell", AEItemIds.FLUID_CELL_CREATIVE, p -> new CreativeCellItem(p.stacksTo(1).rarity(Rarity.EPIC)));
    public static final ItemDefinition<ViewCellItem> VIEW_CELL = item("View Cell", AEItemIds.VIEW_CELL, p -> new ViewCellItem(p.stacksTo(1)));

    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_1K = item("1k ME Item Storage Cell", AEItemIds.ITEM_CELL_1K, p -> new BasicStorageCell(p.stacksTo(1), CELL_COMPONENT_1K, ITEM_CELL_HOUSING, 0.5f,1, 8, 63, AEKeyType.items()));
    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_4K = item("4k ME Item Storage Cell", AEItemIds.ITEM_CELL_4K, p -> new BasicStorageCell(p.stacksTo(1), CELL_COMPONENT_4K, ITEM_CELL_HOUSING, 1.0f,4, 32, 63, AEKeyType.items()));
    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_16K = item("16k ME Item Storage Cell", AEItemIds.ITEM_CELL_16K, p -> new BasicStorageCell(p.stacksTo(1), CELL_COMPONENT_16K, ITEM_CELL_HOUSING, 1.5f,16, 128, 63, AEKeyType.items()));
    public static final ItemDefinition<BasicStorageCell> ITEM_CELL_64K = item("64k ME Item Storage Cell", AEItemIds.ITEM_CELL_64K, p -> new BasicStorageCell(p.stacksTo(1), CELL_COMPONENT_64K, ITEM_CELL_HOUSING, 2.0f,64, 512, 63, AEKeyType.items()));

    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_1K = item("1k ME Fluid Storage Cell", AEItemIds.FLUID_CELL_1K, p -> new BasicStorageCell(p.stacksTo(1), CELL_COMPONENT_1K, FLUID_CELL_HOUSING, 0.5f, 1, 8, 5, AEKeyType.fluids()));
    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_4K = item("4k ME Fluid Storage Cell", AEItemIds.FLUID_CELL_4K, p -> new BasicStorageCell(p.stacksTo(1), CELL_COMPONENT_4K, FLUID_CELL_HOUSING, 1.0f, 4, 32, 5, AEKeyType.fluids()));
    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_16K = item("16k ME Fluid Storage Cell", AEItemIds.FLUID_CELL_16K, p -> new BasicStorageCell(p.stacksTo(1), CELL_COMPONENT_16K, FLUID_CELL_HOUSING, 1.5f, 16, 128, 5, AEKeyType.fluids()));
    public static final ItemDefinition<BasicStorageCell> FLUID_CELL_64K = item("64k ME Fluid Storage Cell", AEItemIds.FLUID_CELL_64K, p -> new BasicStorageCell(p.stacksTo(1), CELL_COMPONENT_64K, FLUID_CELL_HOUSING, 2.0f, 64, 512, 5, AEKeyType.fluids()));

    public static final ItemDefinition<SpatialStorageCellItem> SPATIAL_CELL2 = item("2³ Spatial Storage Cell", AEItemIds.SPATIAL_CELL_2, p -> new SpatialStorageCellItem(p.stacksTo(1), 2));
    public static final ItemDefinition<SpatialStorageCellItem> SPATIAL_CELL16 = item("16³ Spatial Storage Cell", AEItemIds.SPATIAL_CELL_16, p -> new SpatialStorageCellItem(p.stacksTo(1), 16));
    public static final ItemDefinition<SpatialStorageCellItem> SPATIAL_CELL128 = item("128³ Spatial Storage Cell", AEItemIds.SPATIAL_CELL_128, p -> new SpatialStorageCellItem(p.stacksTo(1), 128));

    ///
    /// UNSUPPORTED DEV TOOLS
    ///

    public static final ItemDefinition<EraserItem> DEBUG_ERASER = item("Dev.Eraser", AppEng.makeId("debug_eraser"), EraserItem::new);
    public static final ItemDefinition<MeteoritePlacerItem> DEBUG_METEORITE_PLACER = item("Dev.MeteoritePlacer", AppEng.makeId("debug_meteorite_placer"), MeteoritePlacerItem::new);
    public static final ItemDefinition<DebugCardItem> DEBUG_CARD = item("Dev.DebugCard", AppEng.makeId("debug_card"), DebugCardItem::new);
    public static final ItemDefinition<ReplicatorCardItem> DEBUG_REPLICATOR_CARD = item("Dev.ReplicatorCard", AppEng.makeId("debug_replicator_card"), ReplicatorCardItem::new);
    public static final ItemDefinition<WrappedGenericStack> WRAPPED_GENERIC_STACK = item("Wrapped Generic Stack", AEItemIds.WRAPPED_GENERIC_STACK, WrappedGenericStack::new);

    // spotless:on

    public static List<ItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    private static ColoredItemDefinition<PaintBallItem> createPaintBalls(String name,
            Map<AEColor, ResourceLocation> ids, boolean lumen) {
        var colors = new ColoredItemDefinition<PaintBallItem>();
        for (AEColor color : AEColor.values()) {
            ResourceLocation id = ids.get(color);
            if (id == null) {
                continue;
            }

            var fullName = color.getEnglishName() + " " + name;

            colors.add(color, id, item(fullName, id, p -> new PaintBallItem(p, color, lumen)));
        }
        return colors;
    }

    static <T extends Item> ItemDefinition<T> item(String name, ResourceLocation id,
            Function<FabricItemSettings, T> factory) {
        return item(name, id, factory, CreativeTab.INSTANCE);
    }

    static <T extends Item> ItemDefinition<T> item(String name, ResourceLocation id,
            Function<FabricItemSettings, T> factory,
            CreativeModeTab group) {

        FabricItemSettings p = new FabricItemSettings().group(group);

        T item = factory.apply(p);

        ItemDefinition<T> definition = new ItemDefinition<>(name, id, item);

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
