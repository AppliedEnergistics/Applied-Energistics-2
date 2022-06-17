/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.items.tools.powered;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.block.networking.CableBusBlock;
import appeng.block.paint.PaintSplotchesBlock;
import appeng.blockentity.misc.PaintSplotchesBlockEntity;
import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.IBlockTool;
import appeng.items.contents.CellConfig;
import appeng.items.misc.PaintBallItem;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.cells.BasicCellHandler;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.PlayerSource;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public class ColorApplicatorItem extends AEBasePoweredItem
        implements IBasicCellItem, IBlockTool, IMouseWheelItem {

    private static final double POWER_PER_USE = 100;

    private static final Map<TagKey<Item>, AEColor> TAG_TO_COLOR = AEColor.VALID_COLORS.stream()
            .collect(Collectors.toMap(
                    aeColor -> ConventionTags.dye(aeColor.dye),
                    Function.identity()));
    private static final BiMap<DyeColor, Item> VANILLA_DYES = EnumHashBiMap.create(DyeColor.class);

    static {
        VANILLA_DYES.put(DyeColor.WHITE, Items.WHITE_DYE);
        VANILLA_DYES.put(DyeColor.ORANGE, Items.ORANGE_DYE);
        VANILLA_DYES.put(DyeColor.MAGENTA, Items.MAGENTA_DYE);
        VANILLA_DYES.put(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_DYE);
        VANILLA_DYES.put(DyeColor.YELLOW, Items.YELLOW_DYE);
        VANILLA_DYES.put(DyeColor.LIME, Items.LIME_DYE);
        VANILLA_DYES.put(DyeColor.PINK, Items.PINK_DYE);
        VANILLA_DYES.put(DyeColor.GRAY, Items.GRAY_DYE);
        VANILLA_DYES.put(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_DYE);
        VANILLA_DYES.put(DyeColor.CYAN, Items.CYAN_DYE);
        VANILLA_DYES.put(DyeColor.PURPLE, Items.PURPLE_DYE);
        VANILLA_DYES.put(DyeColor.BLUE, Items.BLUE_DYE);
        VANILLA_DYES.put(DyeColor.BROWN, Items.BROWN_DYE);
        VANILLA_DYES.put(DyeColor.GREEN, Items.GREEN_DYE);
        VANILLA_DYES.put(DyeColor.RED, Items.RED_DYE);
        VANILLA_DYES.put(DyeColor.BLACK, Items.BLACK_DYE);
    }

    private static final String TAG_COLOR = "color";

    public ColorApplicatorItem(Item.Properties props) {
        super(AEConfig.instance().getColorApplicatorBattery(), props);
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 80d + 80d * getUpgrades(stack).getInstalledUpgrades(AEItems.ENERGY_CARD);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack is = context.getItemInHand();
        Direction side = context.getClickedFace();
        Player p = context.getPlayer(); // This can be null
        if (p == null && level instanceof ServerLevel) {
            p = Platform.getPlayer((ServerLevel) level);
        }

        final Block blk = level.getBlockState(pos).getBlock();

        var paintBall = this.getColor(is);
        var paintBallKey = AEItemKey.of(paintBall);

        var source = new PlayerSource(p);

        var inv = StorageCells.getCellInventory(is, null);
        if (inv != null) {
            var extracted = inv.extract(paintBallKey, 1, Actionable.SIMULATE, source);

            if (extracted > 0) {
                paintBall = paintBall.copy();
                paintBall.setCount(1);
            } else {
                paintBall = ItemStack.EMPTY;
            }

            if (p != null && !Platform.hasPermissions(new DimensionalBlockPos(level, pos), p)) {
                return InteractionResult.FAIL;
            }

            if (!paintBall.isEmpty() && paintBall.getItem() instanceof SnowballItem) {
                var be = level.getBlockEntity(pos);
                // clean cables.
                if (p != null
                        && be instanceof IColorableBlockEntity colorableBlockEntity
                        && this.getAECurrentPower(is) > POWER_PER_USE
                        && colorableBlockEntity.getColor() != AEColor.TRANSPARENT) {
                    if (colorableBlockEntity.recolourBlock(side, AEColor.TRANSPARENT, p)) {
                        consumeItem(is, paintBallKey, false);
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                }

                // clean paint balls..
                final Block testBlk = level.getBlockState(pos.relative(side)).getBlock();
                final BlockEntity painted = level.getBlockEntity(pos.relative(side));
                if (this.getAECurrentPower(is) > POWER_PER_USE && testBlk instanceof PaintSplotchesBlock
                        && painted instanceof PaintSplotchesBlockEntity) {
                    consumeItem(is, paintBallKey, false);
                    ((PaintSplotchesBlockEntity) painted).cleanSide(side.getOpposite());
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            } else if (!paintBall.isEmpty()) {
                final AEColor color = this.getColorFromItem(paintBall);

                if (color != null
                        && this.getAECurrentPower(is) > POWER_PER_USE
                        && color != AEColor.TRANSPARENT
                        && this.recolourBlock(blk, side, level, pos, color, p)) {
                    consumeItem(is, paintBallKey, false);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }
        }

        if (p != null && InteractionUtil.isInAlternateUseMode(p)) {
            this.cycleColors(is, paintBall, 1);
        }

        return InteractionResult.FAIL;
    }

    @Override
    public Component getName(ItemStack is) {
        Component extra = GuiText.Empty.text();

        final AEColor selected = this.getActiveColor(is);

        if (selected != null && Platform.isClient()) {
            extra = Component.translatable(selected.translationKey);
        }

        return super.getName(is).copy().append(" - ").append(extra);
    }

    public AEColor getActiveColor(ItemStack tol) {
        return this.getColorFromItem(this.getColor(tol));
    }

    /**
     * Try consuming 1 of the given color.
     */
    public boolean consumeColor(ItemStack applicator, AEColor color, boolean simulate) {
        var inv = StorageCells.getCellInventory(applicator, null);
        if (inv == null) {
            return false;
        }

        var availableItems = inv.getAvailableStacks();
        AEItemKey paintItem = null;
        for (var what : availableItems.keySet()) {
            if (what instanceof AEItemKey itemKey && getColorFromItem(itemKey.getItem()) == color) {
                paintItem = itemKey;
                break;
            }
        }

        if (paintItem != null) {
            return consumeItem(applicator, paintItem, simulate);
        }

        return false;
    }

    /**
     * Try consuming 1 of the given item.
     */
    public boolean consumeItem(ItemStack applicator, AEItemKey paintItem, boolean simulate) {
        var inv = StorageCells.getCellInventory(applicator, null);
        if (inv == null) {
            return false;
        }

        var mode = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
        var success = inv.extract(paintItem, 1, mode, new BaseActionSource()) >= 1
                && this.extractAEPower(applicator, POWER_PER_USE, mode) >= POWER_PER_USE;
        // Clear the color once we run out
        if (success
                && !simulate
                && paintItem.matches(getColor(applicator))
                && inv.getAvailableStacks().get(paintItem) == 0) {
            setColor(applicator, ItemStack.EMPTY);
        }
        return success;
    }

    private AEColor getColorFromItem(ItemStack paintBall) {
        if (paintBall.isEmpty()) {
            return null;
        }

        return getColorFromItem(paintBall.getItem());
    }

    private AEColor getColorFromItem(Item paintBall) {
        if (paintBall instanceof SnowballItem) {
            return AEColor.TRANSPARENT;
        }

        if (paintBall instanceof PaintBallItem ipb) {
            return ipb.getColor();
        }

        // Especially during startup when Vanilla builds it's search index, we don't have tags loaded yet
        var vanillaDye = VANILLA_DYES.inverse().get(paintBall);
        if (vanillaDye != null) {
            return AEColor.fromDye(vanillaDye);
        }

        for (var entry : TAG_TO_COLOR.entrySet()) {
            if (paintBall.builtInRegistryHolder().is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    public ItemStack getColor(ItemStack is) {
        final CompoundTag c = is.getTag();
        if (c != null && c.contains(TAG_COLOR)) {
            final CompoundTag color = c.getCompound(TAG_COLOR);
            final ItemStack oldColor = ItemStack.of(color);
            if (!oldColor.isEmpty()) {
                return oldColor;
            }
        }

        return this.findNextColor(is, ItemStack.EMPTY, 0);
    }

    private ItemStack findNextColor(ItemStack is, ItemStack anchor, int scrollOffset) {
        ItemStack newColor = ItemStack.EMPTY;

        var inv = StorageCells.getCellInventory(is, null);
        if (inv != null) {
            var itemList = inv.getAvailableStacks();
            if (anchor.isEmpty()) {
                var firstItem = itemList.getFirstKey(AEItemKey.class);
                if (firstItem != null) {
                    newColor = firstItem.toStack();
                }
            } else {
                var list = new LinkedList<AEItemKey>();

                for (var i : itemList) {
                    if (i.getKey() instanceof AEItemKey itemKey) {
                        list.add(itemKey);
                    }
                }

                if (list.isEmpty()) {
                    return ItemStack.EMPTY;
                }

                // Sort by color
                list.sort(Comparator.comparingInt(a -> {
                    var color = getColorFromItem(a.getItem());
                    return color != null ? color.ordinal() : Integer.MAX_VALUE;
                }));

                var where = list.getFirst();
                int cycles = 1 + list.size();

                AEColor anchorColor = getColorFromItem(anchor);
                while (cycles > 0 && getColorFromItem(where.getItem()) != anchorColor) {
                    list.addLast(list.removeFirst());
                    cycles--;
                    where = list.getFirst();
                }

                if (scrollOffset > 0) {
                    list.addLast(list.removeFirst());
                }

                if (scrollOffset < 0) {
                    list.addFirst(list.removeLast());
                }

                return list.get(0).toStack();
            }
        }

        if (!newColor.isEmpty()) {
            this.setColor(is, newColor);
        }

        return newColor;
    }

    private void setColor(ItemStack is, ItemStack newColor) {
        final CompoundTag data = is.getOrCreateTag();
        if (newColor.isEmpty()) {
            data.remove(TAG_COLOR);
        } else {
            final CompoundTag color = new CompoundTag();
            newColor.save(color);
            data.put(TAG_COLOR, color);
        }
    }

    private boolean recolourBlock(Block blk, Direction side, Level level, BlockPos pos,
            AEColor newColor, @Nullable Player p) {
        var state = level.getBlockState(pos);

        Block recolored = BlockRecolorer.recolor(blk, newColor);
        if (recolored != blk) {
            BlockState newState = recolored.defaultBlockState();
            for (Property<?> prop : newState.getProperties()) {
                newState = copyProp(state, newState, prop);
            }

            return level.setBlockAndUpdate(pos, newState);
        }

        if (blk instanceof CableBusBlock cableBusBlock && p != null) {
            return cableBusBlock.recolorBlock(level, pos, side, newColor.dye, p);
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IColorableBlockEntity ct) {
            if (ct.getColor() != newColor) {
                ct.recolourBlock(side, newColor, p);
                return true;
            }
        }

        return false;
    }

    private static <T extends Comparable<T>> BlockState copyProp(BlockState oldState, BlockState newState,
            Property<T> prop) {
        if (newState.hasProperty(prop)) {
            return newState.setValue(prop, oldState.getValue(prop));
        }
        return newState;
    }

    public void cycleColors(ItemStack is, ItemStack paintBall, int i) {
        if (paintBall.isEmpty()) {
            this.setColor(is, this.getColor(is));
        } else {
            this.setColor(is, this.findNextColor(is, paintBall, i));
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
            TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);
        addCellInformationToTooltip(stack, lines);
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return 512;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 8;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 27;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, AEKey requestedAddition) {
        if (requestedAddition instanceof AEItemKey itemKey) {
            return getColorFromItem(itemKey.getItem()) == null;
        }
        return true;
    }

    @Override
    public boolean storableInStorageCell() {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Override
    public AEKeyType getKeyType() {
        return AEKeyType.items();
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 2, this::onUpgradesChanged);
    }

    private void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        var energyCards = upgrades.getInstalledUpgrades(AEItems.ENERGY_CARD);
        // Item is crafted with a normal cell, card contains a dense cell (x8)
        setAEMaxPowerMultiplier(stack, 1 + energyCards * 8);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(AEItemKey.filter(), is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = is.getOrCreateTag().getString("FuzzyMode");
        if (fz.isEmpty()) {
            return FuzzyMode.IGNORE_ALL;
        }
        try {
            return FuzzyMode.valueOf(fz);
        } catch (Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        is.getOrCreateTag().putString("FuzzyMode", fzMode.name());
    }

    @Override
    public void onWheel(ItemStack is, boolean up) {
        this.cycleColors(is, this.getColor(is), up ? 1 : -1);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
        if (allowedIn(group)) {
            items.add(createFullColorApplicator());
        }
    }

    /**
     * Create a fully kitted out color applicator.
     */
    public static ItemStack createFullColorApplicator() {
        // Give a fully set up color applicator
        var item = AEItems.COLOR_APPLICATOR.asItem();
        var applicator = new ItemStack(item);

        // Add all dyes
        var dyeStorage = BasicCellHandler.INSTANCE.getCellInventory(applicator, null);

        for (var dyeItem : VANILLA_DYES.values()) {
            dyeStorage.insert(AEItemKey.of(dyeItem), 128, Actionable.MODULATE, new BaseActionSource());
        }
        dyeStorage.insert(AEItemKey.of(Items.SNOWBALL), 128, Actionable.MODULATE, new BaseActionSource());

        // Upgrade energy storage
        var upgrades = item.getUpgrades(applicator);
        upgrades.addItems(AEItems.ENERGY_CARD.stack());
        upgrades.addItems(AEItems.ENERGY_CARD.stack());

        // Fill it up with power
        item.injectAEPower(applicator, item.getAEMaxPower(applicator), Actionable.MODULATE);
        return applicator;
    }

    public boolean setActiveColor(ItemStack applicator, @Nullable AEColor color) {
        if (color == null) {
            setColor(applicator, ItemStack.EMPTY);
            return true;
        }

        var inv = StorageCells.getCellInventory(applicator, null);
        if (inv == null) {
            return false;
        }

        for (var entry : inv.getAvailableStacks()) {
            if (entry.getKey() instanceof AEItemKey itemKey && getColorFromItem(itemKey.getItem()) == color) {
                setColor(applicator, itemKey.toStack());
                return true;
            }
        }

        return false;
    }
}
