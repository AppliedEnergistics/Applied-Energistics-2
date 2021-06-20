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
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;

import appeng.api.config.Upgrades;
import appeng.api.features.AEFeature;
import appeng.api.ids.AEItemIds;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.core.AEItemGroup;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.core.features.ActivityState;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.ItemDefinition;
import appeng.core.features.ItemStackSrc;
import appeng.debug.DebugCardItem;
import appeng.debug.DebugPartPlacerItem;
import appeng.debug.EraserItem;
import appeng.debug.MeteoritePlacerItem;
import appeng.debug.ReplicatorCardItem;
import appeng.entity.ChargedQuartzEntity;
import appeng.entity.SingularityEntity;
import appeng.fluids.items.BasicFluidStorageCell;
import appeng.fluids.items.FluidDummyItem;
import appeng.items.materials.CustomEntityItem;
import appeng.items.materials.MaterialItem;
import appeng.items.materials.NamePressItem;
import appeng.items.materials.StorageComponentItem;
import appeng.items.materials.UpgradeCardItem;
import appeng.items.misc.CrystalSeedItem;
import appeng.items.misc.EncodedPatternItem;
import appeng.items.misc.PaintBallItem;
import appeng.items.parts.FacadeItem;
import appeng.items.storage.BasicStorageCellItem;
import appeng.items.storage.CreativeStorageCellItem;
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
import appeng.items.tools.quartz.QuartzWrenchItem;

/**
 * Internal implementation for the API items
 */
@SuppressWarnings("unused")
public final class ApiItems {

    // spotless:off
    private static final List<ItemDefinition> ITEMS = new ArrayList<>();
    private static final Consumer<Item.Properties> UNSTACKABLE = props -> props.maxStackSize(1);

    ///
    /// CERTUS QUARTZ TOOLS
    ///

    public static final ItemDefinition CERTUS_QUARTZ_AXE = item(AEItemIds.CERTUS_QUARTZ_AXE, props -> new QuartzAxeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
            .itemGroup(ItemGroup.TOOLS).build();
    public static final ItemDefinition CERTUS_QUARTZ_HOE = item(AEItemIds.CERTUS_QUARTZ_HOE, props -> new QuartzHoeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
            .itemGroup(ItemGroup.TOOLS).build();
    public static final ItemDefinition CERTUS_QUARTZ_SHOVEL = item(AEItemIds.CERTUS_QUARTZ_SHOVEL,
            props -> new QuartzSpadeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
            .itemGroup(ItemGroup.TOOLS).build();
    public static final ItemDefinition CERTUS_QUARTZ_PICK = item(AEItemIds.CERTUS_QUARTZ_PICK,
            props -> new QuartzPickaxeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
            .itemGroup(ItemGroup.TOOLS).build();
    public static final ItemDefinition CERTUS_QUARTZ_SWORD = item(AEItemIds.CERTUS_QUARTZ_SWORD,
            props -> new QuartzSwordItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
            .itemGroup(ItemGroup.COMBAT).build();
    public static final ItemDefinition CERTUS_QUARTZ_WRENCH = item(AEItemIds.CERTUS_QUARTZ_WRENCH, props -> new QuartzWrenchItem(props.maxStackSize(1)))
            .itemGroup(ItemGroup.TOOLS)
            .build();
    public static final ItemDefinition CERTUS_QUARTZ_KNIFE = item(AEItemIds.CERTUS_QUARTZ_KNIFE,
            props -> new QuartzCuttingKnifeItem(props.maxStackSize(1).maxDamage(50).setNoRepair(), AEFeature.CERTUS_QUARTZ_TOOLS))
            .itemGroup(ItemGroup.TOOLS)
            .build();

    ///
    /// NETHER QUARTZ TOOLS
    ///

    public static final ItemDefinition NETHER_QUARTZ_AXE = item(AEItemIds.NETHER_QUARTZ_AXE, props -> new QuartzAxeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
            .itemGroup(ItemGroup.TOOLS).build();
    public static final ItemDefinition NETHER_QUARTZ_HOE = item(AEItemIds.NETHER_QUARTZ_HOE, props -> new QuartzHoeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
            .itemGroup(ItemGroup.TOOLS).build();
    public static final ItemDefinition NETHER_QUARTZ_SHOVEL = item(AEItemIds.NETHER_QUARTZ_SHOVEL,
            props -> new QuartzSpadeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
            .itemGroup(ItemGroup.TOOLS).build();
    public static final ItemDefinition NETHER_QUARTZ_PICK = item(AEItemIds.NETHER_QUARTZ_PICK,
            props -> new QuartzPickaxeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
            .itemGroup(ItemGroup.TOOLS).build();
    public static final ItemDefinition NETHER_QUARTZ_SWORD = item(AEItemIds.NETHER_QUARTZ_SWORD,
            props -> new QuartzSwordItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
            .itemGroup(ItemGroup.COMBAT).build();
    public static final ItemDefinition NETHER_QUARTZ_WRENCH = item(AEItemIds.NETHER_QUARTZ_WRENCH, props -> new QuartzWrenchItem(props.maxStackSize(1)))
            .itemGroup(ItemGroup.TOOLS)
            .build();
    public static final ItemDefinition NETHER_QUARTZ_KNIFE = item(AEItemIds.NETHER_QUARTZ_KNIFE,
            props -> new QuartzCuttingKnifeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
            .itemGroup(ItemGroup.TOOLS).props(props -> props.maxStackSize(1).maxDamage(50).setNoRepair())
            .build();

    ///
    /// VARIOUS POWERED TOOLS
    ///

    public static final ItemDefinition ENTROPY_MANIPULATOR = item(AEItemIds.ENTROPY_MANIPULATOR, EntropyManipulatorItem::new)
            .props(UNSTACKABLE)
            .build();
    public static final ItemDefinition WIRELESS_TERMINAL = item(AEItemIds.WIRELESS_TERMINAL, WirelessTerminalItem::new).props(UNSTACKABLE)
            .build();
    public static final ItemDefinition CHARGED_STAFF = item(AEItemIds.CHARGED_STAFF, ChargedStaffItem::new).props(UNSTACKABLE)
            .build();
    public static final ItemDefinition COLOR_APPLICATOR = item(AEItemIds.COLOR_APPLICATOR, ColorApplicatorItem::new).props(UNSTACKABLE).build();
    public static final ItemDefinition MASS_CANNON = item(AEItemIds.MASS_CANNON, MatterCannonItem::new).props(UNSTACKABLE)
            .build();

    ///
    /// PORTABLE CELLS
    ///

    public static final ItemDefinition PORTABLE_CELL1K = item(AEItemIds.PORTABLE_ITEM_CELL1K, props -> new PortableCellItem(StorageTier.SIZE_1K, props))
            .props(UNSTACKABLE)
            .build();
    public static final ItemDefinition PORTABLE_CELL4k = item(AEItemIds.PORTABLE_ITEM_CELL4K, props -> new PortableCellItem(StorageTier.SIZE_4K, props))
            .props(UNSTACKABLE)
            .build();
    public static final ItemDefinition PORTABLE_CELL16K = item(AEItemIds.PORTABLE_ITEM_CELL16K, props -> new PortableCellItem(StorageTier.SIZE_16K, props))
            .props(UNSTACKABLE)
            .build();
    public static final ItemDefinition PORTABLE_CELL64K = item(AEItemIds.PORTABLE_ITEM_CELL64K, props -> new PortableCellItem(StorageTier.SIZE_64K, props))
            .props(UNSTACKABLE)
            .build();

    ///
    /// NETWORK RELATED TOOLS
    ///

    public static final ItemDefinition BIOMETRIC_CARD = item(AEItemIds.BIOMETRIC_CARD, BiometricCardItem::new)
            .props(props -> props.maxStackSize(1)).build();
    public static final ItemDefinition MEMORY_CARD = item(AEItemIds.MEMORY_CARD, MemoryCardItem::new).props(props -> props.maxStackSize(1))
            .build();
    public static final ItemDefinition NETWORK_TOOL = item(AEItemIds.NETWORK_TOOL, NetworkToolItem::new)
            .props(props -> props.maxStackSize(1).addToolType(ToolType.get("wrench"), 0))
            .build();

    public static final ItemDefinition FACADE = item(AEItemIds.FACADE, FacadeItem::new).build();

    public static final ItemDefinition ENCODED_PATTERN = item(AEItemIds.ENCODED_PATTERN, EncodedPatternItem::new)
            .props(props -> props.maxStackSize(1)).build();

    public static final AEColoredItemDefinition COLORED_PAINT_BALL = createPaintBalls(AEItemIds.COLORED_PAINT_BALL, false);
    public static final AEColoredItemDefinition COLORED_LUMEN_PAINT_BALL = createPaintBalls(AEItemIds.COLORED_LUMEN_PAINT_BALL, true);

    ///
    /// MATERIALS
    ///

    public static final ItemDefinition CERTUS_QUARTZ_CRYSTAL = item(AEItemIds.CERTUS_QUARTZ_CRYSTAL, MaterialItem::new).build();
    public static final ItemDefinition CERTUS_QUARTZ_CRYSTAL_CHARGED = item(AEItemIds.CERTUS_QUARTZ_CRYSTAL_CHARGED, props -> new CustomEntityItem(props, ChargedQuartzEntity::new)).build();
    public static final ItemDefinition CERTUS_QUARTZ_DUST = item(AEItemIds.CERTUS_QUARTZ_DUST, MaterialItem::new).build();
    public static final ItemDefinition NETHER_QUARTZ_DUST = item(AEItemIds.NETHER_QUARTZ_DUST, MaterialItem::new).build();
    public static final ItemDefinition FLOUR = item(AEItemIds.FLOUR, MaterialItem::new).build();
    public static final ItemDefinition GOLD_DUST = item(AEItemIds.GOLD_DUST, MaterialItem::new).build();
    public static final ItemDefinition IRON_DUST = item(AEItemIds.IRON_DUST, MaterialItem::new).build();
    public static final ItemDefinition SILICON = item(AEItemIds.SILICON, MaterialItem::new).build();
    public static final ItemDefinition MATTER_BALL = item(AEItemIds.MATTER_BALL, MaterialItem::new).build();
    public static final ItemDefinition FLUIX_CRYSTAL = item(AEItemIds.FLUIX_CRYSTAL, MaterialItem::new).build();
    public static final ItemDefinition FLUIX_DUST = item(AEItemIds.FLUIX_DUST, MaterialItem::new).build();
    public static final ItemDefinition FLUIX_PEARL = item(AEItemIds.FLUIX_PEARL, MaterialItem::new).build();
    public static final ItemDefinition PURIFIED_CERTUS_QUARTZ_CRYSTAL = item(AEItemIds.PURIFIED_CERTUS_QUARTZ_CRYSTAL, MaterialItem::new).build();
    public static final ItemDefinition PURIFIED_NETHER_QUARTZ_CRYSTAL = item(AEItemIds.PURIFIED_NETHER_QUARTZ_CRYSTAL, MaterialItem::new).build();
    public static final ItemDefinition PURIFIED_FLUIX_CRYSTAL = item(AEItemIds.PURIFIED_FLUIX_CRYSTAL, MaterialItem::new).build();
    public static final ItemDefinition CALCULATION_PROCESSOR_PRESS = item(AEItemIds.CALCULATION_PROCESSOR_PRESS, MaterialItem::new).build();
    public static final ItemDefinition ENGINEERING_PROCESSOR_PRESS = item(AEItemIds.ENGINEERING_PROCESSOR_PRESS, MaterialItem::new).build();
    public static final ItemDefinition LOGIC_PROCESSOR_PRESS = item(AEItemIds.LOGIC_PROCESSOR_PRESS, MaterialItem::new).build();
    public static final ItemDefinition CALCULATION_PROCESSOR_PRINT = item(AEItemIds.CALCULATION_PROCESSOR_PRINT, MaterialItem::new).build();
    public static final ItemDefinition ENGINEERING_PROCESSOR_PRINT = item(AEItemIds.ENGINEERING_PROCESSOR_PRINT, MaterialItem::new).build();
    public static final ItemDefinition LOGIC_PROCESSOR_PRINT = item(AEItemIds.LOGIC_PROCESSOR_PRINT, MaterialItem::new).build();
    public static final ItemDefinition SILICON_PRESS = item(AEItemIds.SILICON_PRESS, MaterialItem::new).build();
    public static final ItemDefinition SILICON_PRINT = item(AEItemIds.SILICON_PRINT, MaterialItem::new).build();
    public static final ItemDefinition NAME_PRESS = item(AEItemIds.NAME_PRESS, NamePressItem::new).build();
    public static final ItemDefinition LOGIC_PROCESSOR = item(AEItemIds.LOGIC_PROCESSOR, MaterialItem::new).build();
    public static final ItemDefinition CALCULATION_PROCESSOR = item(AEItemIds.CALCULATION_PROCESSOR, MaterialItem::new).build();
    public static final ItemDefinition ENGINEERING_PROCESSOR = item(AEItemIds.ENGINEERING_PROCESSOR, MaterialItem::new).build();
    public static final ItemDefinition BASIC_CARD = item(AEItemIds.BASIC_CARD, MaterialItem::new).build();
    public static final ItemDefinition REDSTONE_CARD = item(AEItemIds.REDSTONE_CARD, props -> new UpgradeCardItem(props, Upgrades.REDSTONE)).build();
    public static final ItemDefinition CAPACITY_CARD = item(AEItemIds.CAPACITY_CARD, props -> new UpgradeCardItem(props, Upgrades.CAPACITY)).build();
    public static final ItemDefinition ADVANCED_CARD = item(AEItemIds.ADVANCED_CARD, MaterialItem::new).build();
    public static final ItemDefinition FUZZY_CARD = item(AEItemIds.FUZZY_CARD, props -> new UpgradeCardItem(props, Upgrades.FUZZY)).build();
    public static final ItemDefinition SPEED_CARD = item(AEItemIds.SPEED_CARD, props -> new UpgradeCardItem(props, Upgrades.SPEED)).build();
    public static final ItemDefinition INVERTER_CARD = item(AEItemIds.INVERTER_CARD, props -> new UpgradeCardItem(props, Upgrades.INVERTER)).build();
    public static final ItemDefinition SPATIAL_2_CELL_COMPONENT = item(AEItemIds.SPATIAL_2_CELL_COMPONENT, MaterialItem::new).build();
    public static final ItemDefinition SPATIAL_16_CELL_COMPONENT = item(AEItemIds.SPATIAL_16_CELL_COMPONENT, MaterialItem::new).build();
    public static final ItemDefinition SPATIAL_128_CELL_COMPONENT = item(AEItemIds.SPATIAL_128_CELL_COMPONENT, MaterialItem::new).build();
    public static final ItemDefinition ITEM_1K_CELL_COMPONENT = item(AEItemIds.ITEM_CELL_COMPONENT_1K, props -> new StorageComponentItem(props, 1)).build();
    public static final ItemDefinition ITEM_4K_CELL_COMPONENT = item(AEItemIds.ITEM_CELL_COMPONENT_4K, props -> new StorageComponentItem(props, 4)).build();
    public static final ItemDefinition ITEM_16K_CELL_COMPONENT = item(AEItemIds.ITEM_CELL_COMPONENT_16K, props -> new StorageComponentItem(props, 16)).build();
    public static final ItemDefinition ITEM_64K_CELL_COMPONENT = item(AEItemIds.ITEM_CELL_COMPONENT_64K, props -> new StorageComponentItem(props, 64)).build();
    public static final ItemDefinition EMPTY_STORAGE_CELL = item(AEItemIds.EMPTY_STORAGE_CELL, MaterialItem::new).build();
    public static final ItemDefinition WOODEN_GEAR = item(AEItemIds.WOODEN_GEAR, MaterialItem::new).build();
    public static final ItemDefinition WIRELESS_RECEIVER = item(AEItemIds.WIRELESS_RECEIVER, MaterialItem::new).build();
    public static final ItemDefinition WIRELESS_BOOSTER = item(AEItemIds.WIRELESS_BOOSTER, MaterialItem::new).build();
    public static final ItemDefinition FORMATION_CORE = item(AEItemIds.FORMATION_CORE, MaterialItem::new).build();
    public static final ItemDefinition ANNIHILATION_CORE = item(AEItemIds.ANNIHILATION_CORE, MaterialItem::new).build();
    public static final ItemDefinition SKY_DUST = item(AEItemIds.SKY_DUST, MaterialItem::new).build();
    public static final ItemDefinition ENDER_DUST = item(AEItemIds.ENDER_DUST, props -> new CustomEntityItem(props, SingularityEntity::new)).build();
    public static final ItemDefinition SINGULARITY = item(AEItemIds.SINGULARITY, props -> new CustomEntityItem(props, SingularityEntity::new)).build();
    public static final ItemDefinition QUANTUM_ENTANGLED_SINGULARITY = item(AEItemIds.QUANTUM_ENTANGLED_SINGULARITY, props -> new CustomEntityItem(props, SingularityEntity::new)).build();
    public static final ItemDefinition BLANK_PATTERN = item(AEItemIds.BLANK_PATTERN, MaterialItem::new).build();
    public static final ItemDefinition CARD_CRAFTING = item(AEItemIds.CARD_CRAFTING, MaterialItem::new).build();
    public static final ItemDefinition FLUID_1K_CELL_COMPONENT = item(AEItemIds.FLUID_CELL_COMPONENT_1K, MaterialItem::new).build();
    public static final ItemDefinition FLUID_4K_CELL_COMPONENT = item(AEItemIds.FLUID_CELL_COMPONENT_4K, MaterialItem::new).build();
    public static final ItemDefinition FLUID_16K_CELL_COMPONENT = item(AEItemIds.FLUID_CELL_COMPONENT_16K, MaterialItem::new).build();
    public static final ItemDefinition FLUID_64K_CELL_COMPONENT = item(AEItemIds.FLUID_CELL_COMPONENT_64K, MaterialItem::new).build();

    ///
    /// SEEDS
    ///

    public static final ItemDefinition CERTUS_CRYSTAL_SEED = item(AEItemIds.CERTUS_CRYSTAL_SEED,
            props -> new CrystalSeedItem(props, PURIFIED_CERTUS_QUARTZ_CRYSTAL.item()))
            .build();
    public static final ItemDefinition FLUIX_CRYSTAL_SEED = item(AEItemIds.FLUIX_CRYSTAL_SEED,
            props -> new CrystalSeedItem(props, PURIFIED_FLUIX_CRYSTAL.item()))
            .build();
    public static final ItemDefinition NETHER_QUARTZ_SEED = item(AEItemIds.NETHER_QUARTZ_SEED,
            props -> new CrystalSeedItem(props, PURIFIED_NETHER_QUARTZ_CRYSTAL.item()))
            .build();

    ///
    /// CELLS
    ///

    public static final ItemDefinition CELL_CREATIVE = item(AEItemIds.ITEM_CELL_CREATIVE, CreativeStorageCellItem::new)
            .props(props -> props.maxStackSize(1).rarity(Rarity.EPIC))
            .build();
    public static final ItemDefinition VIEW_CELL = item(AEItemIds.VIEW_CELL, ViewCellItem::new).props(props -> props.maxStackSize(1))
            .build();

    public static final ItemDefinition CELL1K = item(AEItemIds.ITEM_CELL_1K,
            props -> new BasicStorageCellItem(props, ITEM_1K_CELL_COMPONENT, 1, 0.5f, 8))
            .props(UNSTACKABLE).build();
    public static final ItemDefinition CELL4K = item(AEItemIds.ITEM_CELL_4K,
            props -> new BasicStorageCellItem(props, ITEM_4K_CELL_COMPONENT, 4, 1.0f, 32))
            .props(UNSTACKABLE).build();
    public static final ItemDefinition CELL16K = item(AEItemIds.ITEM_CELL_16K,
            props -> new BasicStorageCellItem(props, ITEM_16K_CELL_COMPONENT, 16, 1.5f, 128))
            .props(UNSTACKABLE).build();
    public static final ItemDefinition CELL64K = item(AEItemIds.ITEM_CELL_64K,
            props -> new BasicStorageCellItem(props, ITEM_64K_CELL_COMPONENT, 64, 2.0f, 512))
            .props(UNSTACKABLE).build();

    public static final ItemDefinition FLUID_CELL1K = item(AEItemIds.FLUID_CELL_1K,
            props -> new BasicFluidStorageCell(props, FLUID_1K_CELL_COMPONENT, 1, 0.5f, 8))
            .props(UNSTACKABLE).build();
    public static final ItemDefinition FLUID_CELL4K = item(AEItemIds.FLUID_CELL_4K,
            props -> new BasicFluidStorageCell(props, FLUID_4K_CELL_COMPONENT, 4, 1.0f, 32))
            .props(UNSTACKABLE).build();
    public static final ItemDefinition FLUID_CELL16K = item(AEItemIds.FLUID_CELL_16K,
            props -> new BasicFluidStorageCell(props, FLUID_16K_CELL_COMPONENT, 16, 1.5f, 128))
            .props(UNSTACKABLE).build();
    public static final ItemDefinition FLUID_CELL64K = item(AEItemIds.FLUID_CELL_64K,
            props -> new BasicFluidStorageCell(props, FLUID_64K_CELL_COMPONENT, 64, 2.0f, 512))
            .props(UNSTACKABLE).build();

    public static final ItemDefinition SPATIAL_CELL2 = item(AEItemIds.SPATIAL_CELL_2, props -> new SpatialStorageCellItem(props, 2))
            .props(UNSTACKABLE).build();
    public static final ItemDefinition SPATIAL_CELL16 = item(AEItemIds.SPATIAL_CELL_16, props -> new SpatialStorageCellItem(props, 16))
            .props(UNSTACKABLE).build();
    public static final ItemDefinition SPATIAL_CELL128 = item(AEItemIds.SPATIAL_CELL_128, props -> new SpatialStorageCellItem(props, 128))
            .props(UNSTACKABLE).build();

    ///
    /// UNSUPPORTED DEV TOOLS
    ///

    public static final ItemDefinition DEBUG_ERASER = item(AppEng.makeId("debug_eraser"), EraserItem::new).build();
    public static final ItemDefinition DEBUG_METEORITE_PLACER = item(AppEng.makeId("debug_meteorite_placer"), MeteoritePlacerItem::new).build();
    public static final ItemDefinition DEBUG_CARD = item(AppEng.makeId("debug_card"), DebugCardItem::new).build();
    public static final ItemDefinition DEBUG_REPLICATOR_CARD = item(AppEng.makeId("debug_replicator_card"), ReplicatorCardItem::new).build();
    public static final ItemDefinition DEBUG_PART_PLACER = item(AppEng.makeId("debug_part_placer"), DebugPartPlacerItem::new).build();
    public static final ItemDefinition DUMMY_FLUID_ITEM = item(AEItemIds.DUMMY_FLUID_ITEM, FluidDummyItem::new).build();

    // spotless:on

    public static List<ItemDefinition> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    private static AEColoredItemDefinition createPaintBalls(Map<AEColor, ResourceLocation> ids, boolean lumen) {
        ColoredItemDefinition colors = new ColoredItemDefinition();
        for (AEColor color : AEColor.values()) {
            ResourceLocation id = ids.get(color);
            if (id == null) {
                continue;
            }

            ItemDefinition paintBall = item(id, props -> new PaintBallItem(props, color, lumen))
                    .build();
            colors.add(color, new ItemStackSrc(paintBall.item(), ActivityState.Enabled));
        }
        return colors;
    }

    static Builder item(ResourceLocation id, Function<Item.Properties, Item> factory) {
        return new Builder(id, factory);
    }

    // Used to control in which order static constructors are called
    public static void init() {
    }

    static class Builder {

        private final ResourceLocation id;

        private final Function<Item.Properties, Item> itemFactory;

        private final Item.Properties props = new Item.Properties();

        private ItemGroup itemGroup = CreativeTab.INSTANCE;

        Builder(ResourceLocation id, Function<Item.Properties, Item> itemFactory) {
            this.id = id;
            this.itemFactory = itemFactory;
        }

        public Builder itemGroup(ItemGroup itemGroup) {
            this.itemGroup = itemGroup;
            return this;
        }

        public Builder props(Consumer<Item.Properties> consumer) {
            consumer.accept(props);
            return this;
        }

        public ItemDefinition build() {
            props.group(itemGroup);

            Item item = this.itemFactory.apply(props);
            item.setRegistryName(id);

            ItemDefinition definition = new ItemDefinition(this.id, item);

            if (itemGroup instanceof AEItemGroup) {
                ((AEItemGroup) itemGroup).add(definition);
            }

            ITEMS.add(definition);

            return definition;
        }

    }

}
