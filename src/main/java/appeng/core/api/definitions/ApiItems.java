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
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;

import appeng.api.definitions.IItemDefinition;
import appeng.api.features.AEFeature;
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
import appeng.fluids.items.BasicFluidStorageCell;
import appeng.fluids.items.FluidDummyItem;
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
public final class ApiItems {

    private static final List<IItemDefinition> ITEMS = new ArrayList<>();
    private static final IItemDefinition certusQuartzAxe;
    private static final IItemDefinition certusQuartzHoe;
    private static final IItemDefinition certusQuartzShovel;
    private static final IItemDefinition certusQuartzPick;
    private static final IItemDefinition certusQuartzSword;
    private static final IItemDefinition certusQuartzWrench;
    private static final IItemDefinition certusQuartzKnife;
    private static final IItemDefinition netherQuartzAxe;
    private static final IItemDefinition netherQuartzHoe;
    private static final IItemDefinition netherQuartzShovel;
    private static final IItemDefinition netherQuartzPick;
    private static final IItemDefinition netherQuartzSword;
    private static final IItemDefinition netherQuartzWrench;
    private static final IItemDefinition netherQuartzKnife;
    private static final IItemDefinition entropyManipulator;
    private static final IItemDefinition wirelessTerminal;
    private static final IItemDefinition biometricCard;
    private static final IItemDefinition chargedStaff;
    private static final IItemDefinition massCannon;
    private static final IItemDefinition memoryCard;
    private static final IItemDefinition networkTool;
    private static final IItemDefinition portableCell1k;
    private static final IItemDefinition portableCell4k;
    private static final IItemDefinition portableCell16k;
    private static final IItemDefinition portableCell64k;
    private static final IItemDefinition cellCreative;
    private static final IItemDefinition viewCell;
    private static final IItemDefinition cell1k;
    private static final IItemDefinition cell4k;
    private static final IItemDefinition cell16k;
    private static final IItemDefinition cell64k;
    private static final IItemDefinition fluidCell1k;
    private static final IItemDefinition fluidCell4k;
    private static final IItemDefinition fluidCell16k;
    private static final IItemDefinition fluidCell64k;
    private static final IItemDefinition spatialCell2;
    private static final IItemDefinition spatialCell16;
    private static final IItemDefinition spatialCell128;
    private static final IItemDefinition facade;
    private static final IItemDefinition certusCrystalSeed;
    private static final IItemDefinition fluixCrystalSeed;
    private static final IItemDefinition netherQuartzSeed;
    // rv1
    private static final IItemDefinition encodedPattern;
    private static final IItemDefinition colorApplicator;
    private static final AEColoredItemDefinition coloredPaintBall;
    private static final AEColoredItemDefinition coloredLumenPaintBall;
    // unsupported dev tools
    private static final IItemDefinition toolEraser;
    private static final IItemDefinition toolMeteoritePlacer;
    private static final IItemDefinition toolDebugCard;
    private static final IItemDefinition toolReplicatorCard;
    private static final IItemDefinition dummyFluidItem;

    static {
        certusQuartzAxe = item("certus_quartz_axe", props -> new QuartzAxeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).build();
        certusQuartzHoe = item("certus_quartz_hoe", props -> new QuartzHoeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).build();
        certusQuartzShovel = item("certus_quartz_shovel",
                props -> new QuartzSpadeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
                        .itemGroup(ItemGroup.TOOLS).build();
        certusQuartzPick = item("certus_quartz_pickaxe",
                props -> new QuartzPickaxeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
                        .itemGroup(ItemGroup.TOOLS).build();
        certusQuartzSword = item("certus_quartz_sword",
                props -> new QuartzSwordItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
                        .itemGroup(ItemGroup.COMBAT).build();
        certusQuartzWrench = item("certus_quartz_wrench", QuartzWrenchItem::new)
                .itemGroup(ItemGroup.TOOLS).props(props -> props.maxStackSize(1))
                .build();
        certusQuartzKnife = item("certus_quartz_cutting_knife",
                props -> new QuartzCuttingKnifeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
                        .itemGroup(ItemGroup.TOOLS).props(props -> props.maxStackSize(1).maxDamage(50).setNoRepair())
                        .build();

        netherQuartzAxe = item("nether_quartz_axe", props -> new QuartzAxeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).build();
        netherQuartzHoe = item("nether_quartz_hoe", props -> new QuartzHoeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).build();
        netherQuartzShovel = item("nether_quartz_shovel",
                props -> new QuartzSpadeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
                        .itemGroup(ItemGroup.TOOLS).build();
        netherQuartzPick = item("nether_quartz_pickaxe",
                props -> new QuartzPickaxeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
                        .itemGroup(ItemGroup.TOOLS).build();
        netherQuartzSword = item("nether_quartz_sword",
                props -> new QuartzSwordItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
                        .itemGroup(ItemGroup.COMBAT).build();
        netherQuartzWrench = item("nether_quartz_wrench", QuartzWrenchItem::new)
                .itemGroup(ItemGroup.TOOLS).props(props -> props.maxStackSize(1))

                .build();
        netherQuartzKnife = item("nether_quartz_cutting_knife",
                props -> new QuartzCuttingKnifeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
                        .itemGroup(ItemGroup.TOOLS).props(props -> props.maxStackSize(1).maxDamage(50).setNoRepair())
                        .build();

        Consumer<Item.Properties> chargedDefaults = props -> props.maxStackSize(1);

        entropyManipulator = item("entropy_manipulator", EntropyManipulatorItem::new)
                .props(chargedDefaults)
                .build();
        wirelessTerminal = item("wireless_terminal", WirelessTerminalItem::new).props(chargedDefaults)
                .build();
        chargedStaff = item("charged_staff", ChargedStaffItem::new).props(chargedDefaults)
                .build();
        massCannon = item("matter_cannon", MatterCannonItem::new).props(chargedDefaults)
                .build();
        // TODO: change id in 9.0 to 1k_portable_cell
        portableCell1k = item("portable_cell", props -> new PortableCellItem(StorageTier.SIZE_1K, props))
                .props(chargedDefaults)
                .build();
        portableCell4k = item("4k_portable_cell", props -> new PortableCellItem(StorageTier.SIZE_4K, props))
                .props(chargedDefaults)
                .build();
        portableCell16k = item("16k_portable_cell", props -> new PortableCellItem(StorageTier.SIZE_16K, props))
                .props(chargedDefaults)
                .build();
        portableCell64k = item("64k_portable_cell", props -> new PortableCellItem(StorageTier.SIZE_64K, props))
                .props(chargedDefaults)
                .build();
        colorApplicator = item("color_applicator", ColorApplicatorItem::new).props(chargedDefaults)

                .build();

        biometricCard = item("biometric_card", BiometricCardItem::new)
                .props(props -> props.maxStackSize(1)).build();
        memoryCard = item("memory_card", MemoryCardItem::new).props(props -> props.maxStackSize(1))
                .build();
        networkTool = item("network_tool", NetworkToolItem::new)
                .props(props -> props.maxStackSize(1).addToolType(ToolType.get("wrench"), 0))
                .build();

        cellCreative = item("creative_storage_cell", CreativeStorageCellItem::new)
                .props(props -> props.maxStackSize(1).rarity(Rarity.EPIC))
                .build();
        viewCell = item("view_cell", ViewCellItem::new).props(props -> props.maxStackSize(1))
                .build();

        Consumer<Item.Properties> storageCellProps = p -> p.maxStackSize(1);

        cell1k = item("1k_storage_cell",
                props -> new BasicStorageCellItem(props, ApiMaterials.cell1kPart(), 1, 0.5f, 8))
                        .props(storageCellProps).build();
        cell4k = item("4k_storage_cell",
                props -> new BasicStorageCellItem(props, ApiMaterials.cell4kPart(), 4, 1.0f, 32))
                        .props(storageCellProps).build();
        cell16k = item("16k_storage_cell",
                props -> new BasicStorageCellItem(props, ApiMaterials.cell16kPart(), 16, 1.5f, 128))
                        .props(storageCellProps).build();
        cell64k = item("64k_storage_cell",
                props -> new BasicStorageCellItem(props, ApiMaterials.cell64kPart(), 64, 2.0f, 512))
                        .props(storageCellProps).build();

        fluidCell1k = item("1k_fluid_storage_cell",
                props -> new BasicFluidStorageCell(props, ApiMaterials.fluidCell1kPart(), 1, 0.5f, 8))
                        .props(storageCellProps).build();
        fluidCell4k = item("4k_fluid_storage_cell",
                props -> new BasicFluidStorageCell(props, ApiMaterials.fluidCell4kPart(), 4, 1.0f, 32))
                        .props(storageCellProps).build();
        fluidCell16k = item("16k_fluid_storage_cell",
                props -> new BasicFluidStorageCell(props, ApiMaterials.fluidCell16kPart(), 16, 1.5f, 128))
                        .props(storageCellProps).build();
        fluidCell64k = item("64k_fluid_storage_cell",
                props -> new BasicFluidStorageCell(props, ApiMaterials.fluidCell64kPart(), 64, 2.0f, 512))
                        .props(storageCellProps).build();

        spatialCell2 = item("2_cubed_spatial_storage_cell", props -> new SpatialStorageCellItem(props, 2))
                .props(storageCellProps).build();
        spatialCell16 = item("16_cubed_spatial_storage_cell", props -> new SpatialStorageCellItem(props, 16))
                .props(storageCellProps).build();
        spatialCell128 = item("128_cubed_spatial_storage_cell", props -> new SpatialStorageCellItem(props, 128))
                .props(storageCellProps).build();

        facade = item("facade", FacadeItem::new).build();

        certusCrystalSeed = item("certus_crystal_seed",
                props -> new CrystalSeedItem(props, ApiMaterials.purifiedCertusQuartzCrystal().item()))
                        .build();
        fluixCrystalSeed = item("fluix_crystal_seed",
                props -> new CrystalSeedItem(props, ApiMaterials.purifiedFluixCrystal().item()))
                        .build();
        netherQuartzSeed = item("nether_quartz_seed",
                props -> new CrystalSeedItem(props, ApiMaterials.purifiedNetherQuartzCrystal().item()))
                        .build();

        // rv1
        encodedPattern = item("encoded_pattern", EncodedPatternItem::new)
                .props(props -> props.maxStackSize(1)).build();

        coloredPaintBall = createPaintBalls("_paint_ball", false);
        coloredLumenPaintBall = createPaintBalls("_lumen_paint_ball", true);

        toolEraser = item("debug_eraser", EraserItem::new).build();
        toolMeteoritePlacer = item("debug_meteorite_placer", MeteoritePlacerItem::new).build();
        toolDebugCard = item("debug_card", DebugCardItem::new).build();
        toolReplicatorCard = item("debug_replicator_card", ReplicatorCardItem::new).build();
        item("debug_part_placer", DebugPartPlacerItem::new).build();

        dummyFluidItem = item("dummy_fluid_item", FluidDummyItem::new).build();
    }

    public static List<IItemDefinition> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    private static AEColoredItemDefinition createPaintBalls(String idSuffix, boolean lumen) {
        ColoredItemDefinition colors = new ColoredItemDefinition();
        for (AEColor color : AEColor.values()) {
            if (color == AEColor.TRANSPARENT) {
                continue; // Fluix paintballs don't exist
            }

            String id = color.registryPrefix + idSuffix;
            IItemDefinition paintBall = item(id, props -> new PaintBallItem(props, color, lumen))
                    .build();
            colors.add(color, new ItemStackSrc(paintBall.item(), ActivityState.Enabled));
        }
        return colors;
    }

    public static IItemDefinition certusQuartzAxe() {
        return certusQuartzAxe;
    }

    public static IItemDefinition certusQuartzHoe() {
        return certusQuartzHoe;
    }

    public static IItemDefinition certusQuartzShovel() {
        return certusQuartzShovel;
    }

    public static IItemDefinition certusQuartzPick() {
        return certusQuartzPick;
    }

    public static IItemDefinition certusQuartzSword() {
        return certusQuartzSword;
    }

    public static IItemDefinition certusQuartzWrench() {
        return certusQuartzWrench;
    }

    public static IItemDefinition certusQuartzKnife() {
        return certusQuartzKnife;
    }

    public static IItemDefinition netherQuartzAxe() {
        return netherQuartzAxe;
    }

    public static IItemDefinition netherQuartzHoe() {
        return netherQuartzHoe;
    }

    public static IItemDefinition netherQuartzShovel() {
        return netherQuartzShovel;
    }

    public static IItemDefinition netherQuartzPick() {
        return netherQuartzPick;
    }

    public static IItemDefinition netherQuartzSword() {
        return netherQuartzSword;
    }

    public static IItemDefinition netherQuartzWrench() {
        return netherQuartzWrench;
    }

    public static IItemDefinition netherQuartzKnife() {
        return netherQuartzKnife;
    }

    public static IItemDefinition entropyManipulator() {
        return entropyManipulator;
    }

    public static IItemDefinition wirelessTerminal() {
        return wirelessTerminal;
    }

    public static IItemDefinition biometricCard() {
        return biometricCard;
    }

    public static IItemDefinition chargedStaff() {
        return chargedStaff;
    }

    public static IItemDefinition massCannon() {
        return massCannon;
    }

    public static IItemDefinition memoryCard() {
        return memoryCard;
    }

    public static IItemDefinition networkTool() {
        return networkTool;
    }

    public static IItemDefinition portableCell1k() {
        return portableCell1k;
    }

    public static IItemDefinition portableCell4k() {
        return portableCell4k;
    }

    public static IItemDefinition portableCell16k() {
        return portableCell16k;
    }

    public static IItemDefinition portableCell64k() {
        return portableCell64k;
    }

    public static IItemDefinition cellCreative() {
        return cellCreative;
    }

    public static IItemDefinition viewCell() {
        return viewCell;
    }

    public static IItemDefinition cell1k() {
        return cell1k;
    }

    public static IItemDefinition cell4k() {
        return cell4k;
    }

    public static IItemDefinition cell16k() {
        return cell16k;
    }

    public static IItemDefinition cell64k() {
        return cell64k;
    }

    public static IItemDefinition fluidCell1k() {
        return fluidCell1k;
    }

    public static IItemDefinition fluidCell4k() {
        return fluidCell4k;
    }

    public static IItemDefinition fluidCell16k() {
        return fluidCell16k;
    }

    public static IItemDefinition fluidCell64k() {
        return fluidCell64k;
    }

    public static IItemDefinition spatialCell2() {
        return spatialCell2;
    }

    public static IItemDefinition spatialCell16() {
        return spatialCell16;
    }

    public static IItemDefinition spatialCell128() {
        return spatialCell128;
    }

    public static IItemDefinition facade() {
        return facade;
    }

    public static IItemDefinition certusCrystalSeed() {
        return certusCrystalSeed;
    }

    public static IItemDefinition fluixCrystalSeed() {
        return fluixCrystalSeed;
    }

    public static IItemDefinition netherQuartzSeed() {
        return netherQuartzSeed;
    }

    public static IItemDefinition encodedPattern() {
        return encodedPattern;
    }

    public static IItemDefinition colorApplicator() {
        return colorApplicator;
    }

    public static AEColoredItemDefinition coloredPaintBall() {
        return coloredPaintBall;
    }

    public static AEColoredItemDefinition coloredLumenPaintBall() {
        return coloredLumenPaintBall;
    }

    public static IItemDefinition toolEraser() {
        return toolEraser;
    }

    public static IItemDefinition toolMeteoritePlacer() {
        return toolMeteoritePlacer;
    }

    public static IItemDefinition toolDebugCard() {
        return toolDebugCard;
    }

    public static IItemDefinition toolReplicatorCard() {
        return toolReplicatorCard;
    }

    public static IItemDefinition dummyFluidItem() {
        return dummyFluidItem;
    }

    static Builder item(String registryName, Function<Item.Properties, Item> factory) {
        return new Builder(registryName, factory);
    }

    static class Builder {

        private final ResourceLocation id;

        private final Function<Item.Properties, Item> itemFactory;

        private final Item.Properties props = new Item.Properties();

        private ItemGroup itemGroup = CreativeTab.INSTANCE;

        Builder(String registryName, Function<Item.Properties, Item> itemFactory) {
            this.id = new ResourceLocation(AppEng.MOD_ID, registryName);
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

    // Used to control in which order static constructors are called
    public static void init() {
    }

}
