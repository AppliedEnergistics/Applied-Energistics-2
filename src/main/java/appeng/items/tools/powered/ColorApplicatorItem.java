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

import java.util.*;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.SnowballItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.IProperty;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.block.networking.CableBusBlock;
import appeng.block.paint.PaintSplotchesBlock;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.IBlockTool;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.misc.PaintBallItem;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.BaseActionSource;
import appeng.tile.misc.PaintSplotchesTileEntity;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class ColorApplicatorItem extends AEBasePoweredItem
        implements IStorageCell<IAEItemStack>, IItemGroup, IBlockTool, IMouseWheelItem {

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
        addPropertyOverride(new ResourceLocation(AppEng.MOD_ID, "colored"), (itemStack, world, entity) -> {
            // If the stack has no color, don't use the colored model since the impact of
            // calling getColor for every quad is extremely high, if the stack tries to
            // re-search its
            // inventory for a new paintball everytime
            AEColor col = getActiveColor(itemStack);
            return (col != null) ? 1 : 0;
        });
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World w = context.getWorld();
        BlockPos pos = context.getPos();
        ItemStack is = context.getItem();
        Direction side = context.getFace();
        PlayerEntity p = context.getPlayer(); // This can be null
        if (p == null && w instanceof ServerWorld) {
            p = Platform.getPlayer((ServerWorld) w);
        }

        final Block blk = w.getBlockState(pos).getBlock();

        ItemStack paintBall = this.getColor(is);

        final IMEInventory<IAEItemStack> inv = AEApi.instance().registries().cell().getCellInventory(is, null,
                AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
        if (inv != null) {
            final IAEItemStack option = inv.extractItems(AEItemStack.fromItemStack(paintBall), Actionable.SIMULATE,
                    new BaseActionSource());

            if (option != null) {
                paintBall = option.createItemStack();
                paintBall.setCount(1);
            } else {
                paintBall = ItemStack.EMPTY;
            }

            if (p != null && !Platform.hasPermissions(new DimensionalCoord(w, pos), p)) {
                return ActionResultType.FAIL;
            }

            final double powerPerUse = 100;
            if (!paintBall.isEmpty() && paintBall.getItem() instanceof SnowballItem) {
                final TileEntity te = w.getTileEntity(pos);
                // clean cables.
                if (te instanceof IColorableTile && p != null) {
                    if (this.getAECurrentPower(is) > powerPerUse
                            && ((IColorableTile) te).getColor() != AEColor.TRANSPARENT) {
                        if (((IColorableTile) te).recolourBlock(side, AEColor.TRANSPARENT, p)) {
                            inv.extractItems(AEItemStack.fromItemStack(paintBall), Actionable.MODULATE,
                                    new BaseActionSource());
                            this.extractAEPower(is, powerPerUse, Actionable.MODULATE);
                            return ActionResultType.SUCCESS;
                        }
                    }
                }

                // clean paint balls..
                final Block testBlk = w.getBlockState(pos.offset(side)).getBlock();
                final TileEntity painted = w.getTileEntity(pos.offset(side));
                if (this.getAECurrentPower(is) > powerPerUse && testBlk instanceof PaintSplotchesBlock
                        && painted instanceof PaintSplotchesTileEntity) {
                    inv.extractItems(AEItemStack.fromItemStack(paintBall), Actionable.MODULATE, new BaseActionSource());
                    this.extractAEPower(is, powerPerUse, Actionable.MODULATE);
                    ((PaintSplotchesTileEntity) painted).cleanSide(side.getOpposite());
                    return ActionResultType.SUCCESS;
                }
            } else if (!paintBall.isEmpty()) {
                final AEColor color = this.getColorFromItem(paintBall);

                if (color != null && this.getAECurrentPower(is) > powerPerUse) {
                    if (color != AEColor.TRANSPARENT && this.recolourBlock(blk, side, w, pos, color, p)) {
                        inv.extractItems(AEItemStack.fromItemStack(paintBall), Actionable.MODULATE,
                                new BaseActionSource());
                        this.extractAEPower(is, powerPerUse, Actionable.MODULATE);
                        return ActionResultType.SUCCESS;
                    }
                }
            }
        }

        if (p != null && p.isCrouching()) {
            this.cycleColors(is, paintBall, 1);
        }

        return ActionResultType.FAIL;
    }

    @Override
    public ITextComponent getDisplayName(final ItemStack is) {
        ITextComponent extra = GuiText.Empty.textComponent();

        final AEColor selected = this.getActiveColor(is);

        if (selected != null && Platform.isClient()) {
            extra = new TranslationTextComponent(selected.translationKey);
        }

        return super.getDisplayName(is).appendText(" - ").appendSibling(extra);
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
                Tag<Item> tag = ItemTags.getCollection().get(entry.getKey());
                if (tag != null && paintBall.getItem().isIn(tag)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    public ItemStack getColor(final ItemStack is) {
        final CompoundNBT c = is.getTag();
        if (c != null && c.contains(TAG_COLOR)) {
            final CompoundNBT color = c.getCompound(TAG_COLOR);
            final ItemStack oldColor = ItemStack.read(color);
            if (!oldColor.isEmpty()) {
                return oldColor;
            }
        }

        return this.findNextColor(is, ItemStack.EMPTY, 0);
    }

    private ItemStack findNextColor(final ItemStack is, final ItemStack anchor, final int scrollOffset) {
        ItemStack newColor = ItemStack.EMPTY;

        final IMEInventory<IAEItemStack> inv = AEApi.instance().registries().cell().getCellInventory(is, null,
                AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
        if (inv != null) {
            final IItemList<IAEItemStack> itemList = inv.getAvailableItems(
                    AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList());
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
        final CompoundNBT data = is.getOrCreateTag();
        if (newColor.isEmpty()) {
            data.remove(TAG_COLOR);
        } else {
            final CompoundNBT color = new CompoundNBT();
            newColor.write(color);
            data.put(TAG_COLOR, color);
        }
    }

    private boolean recolourBlock(final Block blk, final Direction side, final World w, final BlockPos pos,
            final AEColor newColor, @Nullable final PlayerEntity p) {
        final BlockState state = w.getBlockState(pos);

        Block recolored = BlockRecolorer.recolor(blk, newColor);
        if (recolored != blk) {
            BlockState newState = recolored.getDefaultState();
            for (IProperty<?> prop : newState.getProperties()) {
                newState = copyProp(state, newState, prop);
            }

            return w.setBlockState(pos, newState);
        }

        if (blk instanceof CableBusBlock && p != null) {
            return ((CableBusBlock) blk).recolorBlock(w, pos, side, newColor.dye, p);
        }

        return blk.recolorBlock(state, w, pos, side, newColor.dye);
    }

    private static <T extends Comparable<T>> BlockState copyProp(BlockState oldState, BlockState newState,
            IProperty<T> prop) {
        if (newState.has(prop)) {
            return newState.with(prop, oldState.get(prop));
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
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
        super.addInformation(stack, world, lines, advancedTooltips);

        final ICellInventoryHandler<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory(stack,
                null, AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));

        AEApi.instance().client().addCellInformation(cdi, lines);
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
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public String getUnlocalizedGroupName(final Set<ItemStack> others, final ItemStack is) {
        return GuiText.StorageCells.getTranslationKey();
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
