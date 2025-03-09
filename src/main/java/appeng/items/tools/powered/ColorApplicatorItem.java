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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
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
import appeng.items.storage.StorageTier;
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

    // TODO (RID): Sorted the colours according to the colour wheel
    static {
        VANILLA_DYES.put(DyeColor.WHITE, Items.WHITE_DYE);
        VANILLA_DYES.put(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_DYE);
        VANILLA_DYES.put(DyeColor.GRAY, Items.GRAY_DYE);
        VANILLA_DYES.put(DyeColor.BLACK, Items.BLACK_DYE);
        VANILLA_DYES.put(DyeColor.LIME, Items.LIME_DYE);
        VANILLA_DYES.put(DyeColor.YELLOW, Items.YELLOW_DYE);
        VANILLA_DYES.put(DyeColor.ORANGE, Items.ORANGE_DYE);
        VANILLA_DYES.put(DyeColor.BROWN, Items.BROWN_DYE);
        VANILLA_DYES.put(DyeColor.RED, Items.RED_DYE);
        VANILLA_DYES.put(DyeColor.PINK, Items.PINK_DYE);
        VANILLA_DYES.put(DyeColor.MAGENTA, Items.MAGENTA_DYE);
        VANILLA_DYES.put(DyeColor.PURPLE, Items.PURPLE_DYE);
        VANILLA_DYES.put(DyeColor.BLUE, Items.BLUE_DYE);
        VANILLA_DYES.put(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_DYE);
        VANILLA_DYES.put(DyeColor.CYAN, Items.CYAN_DYE);
        VANILLA_DYES.put(DyeColor.GREEN, Items.GREEN_DYE);
    }

    public ColorApplicatorItem(Properties props) {
        super(AEConfig.instance().getColorApplicatorBattery(), props);
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 80d + 80d * Upgrades.getEnergyCardMultiplier(getUpgrades(stack));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);

        var newStack = stack.copy();
        cycleColors(newStack, getColor(stack), 1);
        if (level.isClientSide) {
            player.displayClientMessage(stack.getHoverName(), true);
        }
        return InteractionResult.CONSUME.heldItemTransformedTo(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack is = context.getItemInHand();
        Direction side = context.getClickedFace();
        Player p = context.getPlayer(); // This can be null
        if (p == null && level instanceof ServerLevel) {
            p = Platform.getFakePlayer((ServerLevel) level, null);
        }

        final Block blk = level.getBlockState(pos).getBlock();

        var color = this.getColor(is);

        var source = new PlayerSource(p);

        var inv = StorageCells.getCellInventory(is, null);
        if (inv != null) {
            if (p != null && !Platform.hasPermissions(new DimensionalBlockPos(level, pos), p)) {
                return InteractionResult.FAIL;
            }

            if (!consumeColor(is, color, true)) {
                color = null;
            }

            if (color != null) {
                if (color == AEColor.TRANSPARENT) {
                    // clean cables.
                    if (p != null
                            && level.getBlockEntity(pos) instanceof IColorableBlockEntity colorableBlockEntity
                            && this.getAECurrentPower(is) > POWER_PER_USE
                            && colorableBlockEntity.getColor() != AEColor.TRANSPARENT) {
                        if (colorableBlockEntity.recolourBlock(side, AEColor.TRANSPARENT, p)) {
                            consumeColor(is, color, false);
                            return InteractionResult.SUCCESS;
                        }
                    }

                    // clean paint balls..
                    final Block testBlk = level.getBlockState(pos.relative(side)).getBlock();
                    final BlockEntity painted = level.getBlockEntity(pos.relative(side));
                    if (this.getAECurrentPower(is) > POWER_PER_USE && testBlk instanceof PaintSplotchesBlock
                            && painted instanceof PaintSplotchesBlockEntity) {
                        consumeColor(is, color, false);
                        ((PaintSplotchesBlockEntity) painted).cleanSide(side.getOpposite());
                        return InteractionResult.SUCCESS;
                    }
                }

                if (this.getAECurrentPower(is) > POWER_PER_USE
                        && this.recolourBlock(blk, side, level, pos, color, p)) {
                    consumeColor(is, color, false);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        if (p != null && InteractionUtil.isInAlternateUseMode(p)) {
            this.cycleColors(is, color, 1);
            if (level.isClientSide) {
                p.displayClientMessage(is.getHoverName(), true);
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack is, Player player, LivingEntity interactionTarget,
            InteractionHand usedHand) {
        var paintBallColor = this.getColor(is);

        if (paintBallColor != null && interactionTarget instanceof Sheep sheep) {
            if (sheep.isAlive() && !sheep.isSheared() && sheep.getColor() != paintBallColor.dye) {
                if (!player.level().isClientSide && this.getAECurrentPower(is) > POWER_PER_USE) {
                    sheep.setColor(paintBallColor.dye);
                    sheep.level().playSound(player, sheep, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    this.consumeColor(is, paintBallColor, false);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
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
        return this.getColor(tol);
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
        for (var what : availableItems.keySet()) {
            if (getColorFrom(what) == color) {
                return consumeItem(applicator, what, simulate);
            }
        }

        return false;
    }

    /**
     * Try consuming 1 of the given item.
     */
    public boolean consumeItem(ItemStack applicator, AEKey key, boolean simulate) {
        var inv = StorageCells.getCellInventory(applicator, null);
        if (inv == null) {
            return false;
        }

        var mode = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
        var success = inv.extract(key, 1, mode, new BaseActionSource()) >= 1
                && this.extractAEPower(applicator, POWER_PER_USE, mode) >= POWER_PER_USE;
        // Clear the color once we run out
        if (success
                && !simulate
                && getColorFrom(key) == getColor(applicator)
                && inv.getAvailableStacks().get(key) == 0) {
            setColor(applicator, null);
        }
        return success;
    }

    @Nullable
    private AEColor getColorFrom(AEKey key) {
        if (key instanceof AEItemKey itemKey) {
            var item = itemKey.getItem();

            if (item instanceof SnowballItem) {
                return AEColor.TRANSPARENT;
            }

            if (item instanceof PaintBallItem ipb) {
                return ipb.getColor();
            }

            // Especially during startup when Vanilla builds it's search index, we don't have tags loaded yet
            var vanillaDye = VANILLA_DYES.inverse().get(item);
            if (vanillaDye != null) {
                return AEColor.fromDye(vanillaDye);
            }

            for (var entry : TAG_TO_COLOR.entrySet()) {
                if (item.builtInRegistryHolder().is(entry.getKey())) {
                    return entry.getValue();
                }
            }
        } else if (key instanceof AEFluidKey fluidKey) {
            if (fluidKey.isTagged(FluidTags.WATER)) {
                return AEColor.TRANSPARENT;
            }
        }
        return null;
    }

    public AEColor getColor(ItemStack is) {
        var selectedPaint = is.get(AEComponents.SELECTED_COLOR);
        if (selectedPaint != null) {
            return selectedPaint;
        }

        return this.findNextColor(is, null, 0);
    }

    @Nullable
    private AEColor findNextColor(ItemStack is, @Nullable AEColor anchorColor, int scrollOffset) {
        AEColor newColor = null;

        var inv = StorageCells.getCellInventory(is, null);
        if (inv != null) {
            var keyList = inv.getAvailableStacks();
            if (anchorColor == null) {
                var firstItem = keyList.getFirstKey();
                if (firstItem != null) {
                    newColor = getColorFrom(firstItem);
                }
            } else {
                var list = new LinkedList<AEKey>();

                for (var i : keyList) {
                    list.add(i.getKey());
                }

                if (list.isEmpty()) {
                    return null;
                }

                // Sort by color
                list.sort(Comparator.comparingInt(a -> {
                    var color = getColorFrom(a);
                    return color != null ? color.ordinal() : Integer.MAX_VALUE;
                }));

                var where = list.getFirst();
                int cycles = 1 + list.size();

                while (cycles > 0 && getColorFrom(where) != anchorColor) {
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

                return getColorFrom(list.get(0));
            }
        }

        if (newColor != null) {
            this.setColor(is, newColor);
        }

        return newColor;
    }

    private void setColor(ItemStack is, @Nullable AEColor newColor) {
        is.set(AEComponents.SELECTED_COLOR, newColor);
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

    public void cycleColors(ItemStack is, @Nullable AEColor currentColor, int i) {
        if (currentColor == null) {
            this.setColor(is, this.getColor(is));
        } else {
            this.setColor(is, this.findNextColor(is, currentColor, i));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines,
            TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, context, lines, advancedTooltips);
        addCellInformationToTooltip(stack, lines);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return getCellTooltipImage(stack);
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return StorageTier.SIZE_4K.bytes() / 2;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return StorageTier.SIZE_4K.bytes() / 128;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 27;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, AEKey requestedAddition) {
        return getColorFrom(requestedAddition) == null;
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
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 2, this::onUpgradesChanged);
    }

    private void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        // Item is crafted with a normal cell, base energy card contains a dense cell (x8)
        setAEMaxPowerMultiplier(stack, 1 + Upgrades.getEnergyCardMultiplier(upgrades) * 8);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(Set.of(AEKeyType.items()), is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return is.getOrDefault(AEComponents.STORAGE_CELL_FUZZY_MODE, FuzzyMode.IGNORE_ALL);
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        is.set(AEComponents.STORAGE_CELL_FUZZY_MODE, fzMode);
    }

    @Override
    public void onWheel(ItemStack is, boolean up) {
        this.cycleColors(is, this.getColor(is), up ? 1 : -1);
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        super.addToMainCreativeTab(parameters, output);

        output.accept(createFullColorApplicator());
    }

    /**
     * Create a fully kitted out color applicator.
     */
    public static ItemStack createFullColorApplicator() {
        // Give a fully set up color applicator
        var item = AEItems.COLOR_APPLICATOR.get();
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

    public void setActiveColor(ItemStack applicator, @Nullable AEColor color) {
        if (color == null) {
            setColor(applicator, null);
            return;
        }

        // Check that we actually have the color...
        var inv = StorageCells.getCellInventory(applicator, null);
        if (inv == null) {
            return;
        }

        for (var entry : inv.getAvailableStacks()) {
            if (entry.getKey() instanceof AEItemKey itemKey && getColorFrom(itemKey) == color) {
                setColor(applicator, color);
                return;
            }
        }
    }
}
