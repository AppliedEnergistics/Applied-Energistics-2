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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.block.networking.CableBusBlock;
import appeng.block.paint.PaintSplotchesBlock;
import appeng.blockentity.misc.PaintSplotchesBlockEntity;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.IBlockTool;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.misc.PaintBallItem;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.PlayerSource;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public class ColorApplicatorItem extends AEBasePoweredItem
        implements IBasicCellItem, IBlockTool, IMouseWheelItem {

    private static final Map<Tag.Named<Item>, AEColor> TAG_TO_COLOR = AEColor.VALID_COLORS.stream()
            .collect(Collectors.toMap(
                    aeColor -> ConventionTags.dye(aeColor.dye),
                    Function.identity()));

    private static final String TAG_COLOR = "color";

    public ColorApplicatorItem(Item.Properties props) {
        super(AEConfig.instance().getColorApplicatorBattery(), props);
    }

    @Override
    public double getChargeRate() {
        return 80d;
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

            final double powerPerUse = 100;
            if (!paintBall.isEmpty() && paintBall.getItem() instanceof SnowballItem) {
                final BlockEntity te = level.getBlockEntity(pos);
                // clean cables.
                if (te instanceof IColorableBlockEntity && p != null && this.getAECurrentPower(is) > powerPerUse
                        && ((IColorableBlockEntity) te).getColor() != AEColor.TRANSPARENT) {
                    if (((IColorableBlockEntity) te).recolourBlock(side, AEColor.TRANSPARENT, p)) {
                        inv.extract(paintBallKey, 1, Actionable.MODULATE, source);
                        this.extractAEPower(is, powerPerUse, Actionable.MODULATE);
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                }

                // clean paint balls..
                final Block testBlk = level.getBlockState(pos.relative(side)).getBlock();
                final BlockEntity painted = level.getBlockEntity(pos.relative(side));
                if (this.getAECurrentPower(is) > powerPerUse && testBlk instanceof PaintSplotchesBlock
                        && painted instanceof PaintSplotchesBlockEntity) {
                    inv.extract(paintBallKey, 1, Actionable.MODULATE, source);
                    this.extractAEPower(is, powerPerUse, Actionable.MODULATE);
                    ((PaintSplotchesBlockEntity) painted).cleanSide(side.getOpposite());
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            } else if (!paintBall.isEmpty()) {
                final AEColor color = this.getColorFromItem(paintBall);

                if (color != null && this.getAECurrentPower(is) > powerPerUse
                        && color != AEColor.TRANSPARENT && this.recolourBlock(blk, side, level, pos, color, p)) {
                    inv.extract(paintBallKey, 1, Actionable.MODULATE, source);
                    this.extractAEPower(is, powerPerUse, Actionable.MODULATE);
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
    public Component getName(final ItemStack is) {
        Component extra = GuiText.Empty.text();

        final AEColor selected = this.getActiveColor(is);

        if (selected != null && Platform.isClient()) {
            extra = new TranslatableComponent(selected.translationKey);
        }

        return super.getName(is).copy().append(" - ").append(extra);
    }

    public AEColor getActiveColor(final ItemStack tol) {
        return this.getColorFromItem(this.getColor(tol));
    }

    private AEColor getColorFromItem(final ItemStack paintBall) {
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
        } else {
            for (var entry : TAG_TO_COLOR.entrySet()) {
                if (entry.getKey().contains(paintBall)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    public ItemStack getColor(final ItemStack is) {
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

    private ItemStack findNextColor(final ItemStack is, final ItemStack anchor, final int scrollOffset) {
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

    private void setColor(final ItemStack is, final ItemStack newColor) {
        final CompoundTag data = is.getOrCreateTag();
        if (newColor.isEmpty()) {
            data.remove(TAG_COLOR);
        } else {
            final CompoundTag color = new CompoundTag();
            newColor.save(color);
            data.put(TAG_COLOR, color);
        }
    }

    private boolean recolourBlock(final Block blk, final Direction side, final Level level, final BlockPos pos,
            final AEColor newColor, @Nullable final Player p) {
        final BlockState state = level.getBlockState(pos);

        Block recolored = BlockRecolorer.recolor(blk, newColor);
        if (recolored != blk) {
            BlockState newState = recolored.defaultBlockState();
            for (Property<?> prop : newState.getProperties()) {
                newState = copyProp(state, newState, prop);
            }

            return level.setBlockAndUpdate(pos, newState);
        }

        if (blk instanceof CableBusBlock && p != null) {
            return ((CableBusBlock) blk).recolorBlock(level, pos, side, newColor.dye, p);
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

    public void cycleColors(final ItemStack is, final ItemStack paintBall, final int i) {
        if (paintBall.isEmpty()) {
            this.setColor(is, this.getColor(is));
        } else {
            this.setColor(is, this.findNextColor(is, paintBall, i));
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level level, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);
        addCellInformationToTooltip(stack, lines);
    }

    @Override
    public int getBytes(final ItemStack cellItem) {
        return 512;
    }

    @Override
    public int getBytesPerType(final ItemStack cellItem) {
        return 8;
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
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
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public UpgradeInventory getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(AEItemKey.filter(), is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        final String fz = is.getOrCreateTag().getString("FuzzyMode");
        if (fz.isEmpty()) {
            return FuzzyMode.IGNORE_ALL;
        }
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        is.getOrCreateTag().putString("FuzzyMode", fzMode.name());
    }

    @Override
    public void onWheel(final ItemStack is, final boolean up) {
        this.cycleColors(is, this.getColor(is), up ? 1 : -1);
    }

}
