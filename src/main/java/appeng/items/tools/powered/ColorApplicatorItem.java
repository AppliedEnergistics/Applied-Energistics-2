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

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.block.networking.CableBusBlock;
import appeng.block.paint.PaintSplotchesBlock;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.localization.GuiText;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.IBlockTool;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.misc.PaintBallItem;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.BaseActionSource;
import appeng.tile.misc.PaintSplotchesTileEntity;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class ColorApplicatorItem extends AEBasePoweredItem
        implements IStorageCell<IAEItemStack>, IBlockTool, IMouseWheelItem {

    private static final Map<ResourceLocation, AEColor> TAG_TO_COLOR = ImmutableMap.<ResourceLocation, AEColor>builder()
            .put(new ResourceLocation("forge:dyes/black"), AEColor.BLACK)
            .put(new ResourceLocation("forge:dyes/blue"), AEColor.BLUE)
            .put(new ResourceLocation("forge:dyes/brown"), AEColor.BROWN)
            .put(new ResourceLocation("forge:dyes/cyan"), AEColor.CYAN)
            .put(new ResourceLocation("forge:dyes/gray"), AEColor.GRAY)
            .put(new ResourceLocation("forge:dyes/green"), AEColor.GREEN)
            .put(new ResourceLocation("forge:dyes/light_blue"), AEColor.LIGHT_BLUE)
            .put(new ResourceLocation("forge:dyes/light_gray"), AEColor.LIGHT_GRAY)
            .put(new ResourceLocation("forge:dyes/lime"), AEColor.LIME)
            .put(new ResourceLocation("forge:dyes/magenta"), AEColor.MAGENTA)
            .put(new ResourceLocation("forge:dyes/orange"), AEColor.ORANGE)
            .put(new ResourceLocation("forge:dyes/pink"), AEColor.PINK)
            .put(new ResourceLocation("forge:dyes/purple"), AEColor.PURPLE)
            .put(new ResourceLocation("forge:dyes/red"), AEColor.RED)
            .put(new ResourceLocation("forge:dyes/white"), AEColor.WHITE)
            .put(new ResourceLocation("forge:dyes/yellow"), AEColor.YELLOW).build();

    private static final String TAG_COLOR = "color";

    public ColorApplicatorItem(Item.Properties props) {
        super(AEConfig.instance().getColorApplicatorBattery(), props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level w = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack is = context.getItemInHand();
        Direction side = context.getClickedFace();
        Player p = context.getPlayer(); // This can be null
        if (p == null && w instanceof ServerLevel) {
            p = Platform.getPlayer((ServerLevel) w);
        }

        final Block blk = w.getBlockState(pos).getBlock();

        ItemStack paintBall = this.getColor(is);

        final IMEInventory<IAEItemStack> inv = Api.instance().registries().cell().getCellInventory(is, null,
                Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
        if (inv != null) {
            final IAEItemStack option = inv.extractItems(AEItemStack.fromItemStack(paintBall), Actionable.SIMULATE,
                    new BaseActionSource());

            if (option != null) {
                paintBall = option.createItemStack();
                paintBall.setCount(1);
            } else {
                paintBall = ItemStack.EMPTY;
            }

            if (p != null && !Platform.hasPermissions(new DimensionalBlockPos(w, pos), p)) {
                return InteractionResult.FAIL;
            }

            final double powerPerUse = 100;
            if (!paintBall.isEmpty() && paintBall.getItem() instanceof SnowballItem) {
                final BlockEntity te = w.getBlockEntity(pos);
                // clean cables.
                if (te instanceof IColorableTile && p != null && this.getAECurrentPower(is) > powerPerUse
                        && ((IColorableTile) te).getColor() != AEColor.TRANSPARENT) {
                    if (((IColorableTile) te).recolourBlock(side, AEColor.TRANSPARENT, p)) {
                        inv.extractItems(AEItemStack.fromItemStack(paintBall), Actionable.MODULATE,
                                new BaseActionSource());
                        this.extractAEPower(is, powerPerUse, Actionable.MODULATE);
                        return InteractionResult.sidedSuccess(w.isClientSide());
                    }
                }

                // clean paint balls..
                final Block testBlk = w.getBlockState(pos.relative(side)).getBlock();
                final BlockEntity painted = w.getBlockEntity(pos.relative(side));
                if (this.getAECurrentPower(is) > powerPerUse && testBlk instanceof PaintSplotchesBlock
                        && painted instanceof PaintSplotchesTileEntity) {
                    inv.extractItems(AEItemStack.fromItemStack(paintBall), Actionable.MODULATE, new BaseActionSource());
                    this.extractAEPower(is, powerPerUse, Actionable.MODULATE);
                    ((PaintSplotchesTileEntity) painted).cleanSide(side.getOpposite());
                    return InteractionResult.sidedSuccess(w.isClientSide());
                }
            } else if (!paintBall.isEmpty()) {
                final AEColor color = this.getColorFromItem(paintBall);

                if (color != null && this.getAECurrentPower(is) > powerPerUse
                        && color != AEColor.TRANSPARENT && this.recolourBlock(blk, side, w, pos, color, p)) {
                    inv.extractItems(AEItemStack.fromItemStack(paintBall), Actionable.MODULATE,
                            new BaseActionSource());
                    this.extractAEPower(is, powerPerUse, Actionable.MODULATE);
                    return InteractionResult.sidedSuccess(w.isClientSide());
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

        if (paintBall.getItem() instanceof SnowballItem) {
            return AEColor.TRANSPARENT;
        }

        if (paintBall.getItem() instanceof PaintBallItem) {
            final PaintBallItem ipb = (PaintBallItem) paintBall.getItem();
            return ipb.getColor();
        } else {
            for (Map.Entry<ResourceLocation, AEColor> entry : TAG_TO_COLOR.entrySet()) {
                Tag<Item> tag = ItemTags.getAllTags().getTag(entry.getKey());
                if (tag != null && paintBall.getItem().is(tag)) {
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

        final IMEInventory<IAEItemStack> inv = Api.instance().registries().cell().getCellInventory(is, null,
                Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
        if (inv != null) {
            final IItemList<IAEItemStack> itemList = inv.getAvailableItems(
                    Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList());
            if (anchor.isEmpty()) {
                final IAEItemStack firstItem = itemList.getFirstItem();
                if (firstItem != null) {
                    newColor = firstItem.asItemStackRepresentation();
                }
            } else {
                final LinkedList<IAEItemStack> list = new LinkedList<>();

                for (final IAEItemStack i : itemList) {
                    list.add(i);
                }

                if (list.isEmpty()) {
                    return ItemStack.EMPTY;
                }

                // Sort by color
                list.sort(Comparator.comparingInt(a -> {
                    AEColor color = getColorFromItem(a.getDefinition());
                    return color != null ? color.ordinal() : Integer.MAX_VALUE;
                }));

                IAEItemStack where = list.getFirst();
                int cycles = 1 + list.size();

                AEColor anchorColor = getColorFromItem(anchor);
                while (cycles > 0 && getColorFromItem(where.getDefinition()) != anchorColor) {
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

                return list.get(0).asItemStackRepresentation();
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

    private boolean recolourBlock(final Block blk, final Direction side, final Level w, final BlockPos pos,
                                  final AEColor newColor, @Nullable final Player p) {
        final BlockState state = w.getBlockState(pos);

        Block recolored = BlockRecolorer.recolor(blk, newColor);
        if (recolored != blk) {
            BlockState newState = recolored.defaultBlockState();
            for (Property<?> prop : newState.getProperties()) {
                newState = copyProp(state, newState, prop);
            }

            return w.setBlockAndUpdate(pos, newState);
        }

        if (blk instanceof CableBusBlock && p != null) {
            return ((CableBusBlock) blk).recolorBlock(w, pos, side, newColor.dye, p);
        }

        BlockEntity be = w.getBlockEntity(pos);
        if (be instanceof IColorableTile) {
            IColorableTile ct = (IColorableTile) be;
            AEColor c = ct.getColor();
            if (c != newColor) {
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
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level world, final List<Component> lines,
                                final TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, world, lines, advancedTooltips);

        final ICellInventoryHandler<IAEItemStack> cdi = Api.instance().registries().cell().getCellInventory(stack, null,
                Api.instance().storage().getStorageChannel(IItemStorageChannel.class));

        Api.instance().client().addCellInformation(cdi, lines);
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
    public boolean isBlackListed(final ItemStack cellItem, final IAEItemStack requestedAddition) {
        if (requestedAddition != null) {
            return getColorFromItem(requestedAddition.getDefinition()) == null;
        }
        return true;
    }

    @Override
    public boolean storableInStorageCell() {
        return true;
    }

    @Override
    public boolean isStorageCell(final ItemStack i) {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return Api.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public IItemHandler getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IItemHandler getConfigInventory(final ItemStack is) {
        return new CellConfig(is);
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
