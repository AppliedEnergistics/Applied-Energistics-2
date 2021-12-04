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

package appeng.crafting.pattern;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.items.misc.WrappedGenericStack;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public abstract class EncodedPatternItem extends AEBaseItem implements AEToolItem {
    // rather simple client side caching.
    private static final Map<ItemStack, ItemStack> SIMPLE_CACHE = new WeakHashMap<>();

    public EncodedPatternItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        // Don't show in creative mode, since it's not useful without NBT
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        this.clearPattern(player.getItemInHand(hand), player);

        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                player.getItemInHand(hand));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return this.clearPattern(stack, context.getPlayer())
                ? InteractionResult.sidedSuccess(context.getLevel().isClientSide())
                : InteractionResult.PASS;
    }

    private boolean clearPattern(final ItemStack stack, final Player player) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            if (player.getCommandSenderWorld().isClientSide()) {
                return false;
            }

            final Inventory inv = player.getInventory();

            ItemStack is = AEItems.BLANK_PATTERN.stack(stack.getCount());
            if (!is.isEmpty()) {
                for (int s = 0; s < player.getInventory().getContainerSize(); s++) {
                    if (inv.getItem(s) == stack) {
                        inv.setItem(s, is);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level level, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        if (!stack.hasTag()) {
            // This can be called very early to index tooltips for search. In those cases,
            // there is no encoded pattern present.
            return;
        }

        var details = decode(stack, level, false);
        if (details == null) {
            // TODO: needs update for new pattern logic
            stack.setHoverName(GuiText.InvalidPattern.text().copy().withStyle(ChatFormatting.RED));

            InvalidPatternHelper invalid = new InvalidPatternHelper(stack);

            var label = (invalid.isCraftable() ? GuiText.Crafts.text() : GuiText.Produces.text())
                    .copy().append(": ");
            var and = new TextComponent(" ").copy().append(GuiText.And.text())
                    .copy()
                    .append(" ");
            var with = GuiText.With.text().copy().append(": ");

            boolean first = true;
            for (var output : invalid.getOutputs()) {
                lines.add((first ? label : and).copy().append(output.getFormattedToolTip()));
                first = false;
            }

            first = true;
            for (final InvalidPatternHelper.PatternIngredient input : invalid.getInputs()) {
                lines.add((first ? with : and).copy().append(input.getFormattedToolTip()));
                first = false;
            }

            if (invalid.isCraftable()) {
                var substitutionLabel = GuiText.Substitute.text().copy().append(" ");
                var canSubstitute = invalid.canSubstitute() ? GuiText.Yes.text() : GuiText.No.text();

                lines.add(substitutionLabel.copy().append(canSubstitute));
            }

            return;
        }

        if (stack.hasCustomHoverName()) {
            stack.removeTagKey("display");
        }

        var isCrafting = details instanceof AECraftingPattern;
        var substitute = isCrafting && ((AECraftingPattern) details).canSubstitute;

        var in = details.getInputs();
        var out = details.getOutputs();

        var label = (isCrafting ? GuiText.Crafts.text() : GuiText.Produces.text()).copy()
                .append(": ");
        var and = new TextComponent(" ").copy().append(GuiText.And.text())
                .append(" ");
        var with = GuiText.With.text().copy().append(": ");

        boolean first = true;
        for (var anOut : out) {
            if (anOut == null) {
                continue;
            }

            lines.add((first ? label : and).copy().append(getStackComponent(anOut)));
            first = false;
        }

        first = true;
        for (var anIn : in) {
            if (anIn == null) {
                continue;
            }

            var primaryInputTemplate = anIn.getPossibleInputs()[0];
            var primaryInput = new GenericStack(primaryInputTemplate.what(),
                    primaryInputTemplate.amount() * anIn.getMultiplier());
            lines.add((first ? with : and).copy().append(getStackComponent(primaryInput)));
            first = false;
        }

        if (isCrafting) {
            var substitutionLabel = GuiText.Substitute.text().copy().append(" ");
            var canSubstitute = substitute ? GuiText.Yes.text() : GuiText.No.text();

            lines.add(substitutionLabel.copy().append(canSubstitute));
        }
    }

    protected static Component getStackComponent(GenericStack stack) {
        String amountInfo;
        Component displayName;
        var what = stack.what();
        if (what instanceof AEItemKey itemKey) {
            amountInfo = String.valueOf(stack.amount());
            displayName = Platform.getItemDisplayName(itemKey);
        } else if (what instanceof AEFluidKey fluidKey) {
            amountInfo = Platform.formatFluidAmount(stack.amount());
            displayName = Platform.getFluidDisplayName(fluidKey);
        } else {
            throw new IllegalArgumentException("Unsupported storage channel: " + what);
        }
        return new TextComponent(amountInfo + " x ").append(displayName);
    }

    /**
     * Returns the item stack that should be shown for this pattern when shift is held down.
     */
    public ItemStack getOutput(ItemStack item) {
        var out = SIMPLE_CACHE.get(item);

        if (out != null) {
            return out;
        }

        var level = AppEng.instance().getClientLevel();
        if (level == null) {
            return ItemStack.EMPTY;
        }

        var details = decode(item, level, false);
        out = ItemStack.EMPTY;

        if (details != null) {
            var output = details.getPrimaryOutput();

            // Can only be an item or fluid stack.
            if (output.what() instanceof AEItemKey itemKey) {
                out = itemKey.toStack();
            } else if (output.what() instanceof AEFluidKey fluidKey) {
                out = WrappedGenericStack.wrap(fluidKey, 0);
            }
        }

        SIMPLE_CACHE.put(item, out);
        return out;
    }

    @Nullable
    public abstract IAEPatternDetails decode(ItemStack stack, Level level, boolean tryRecovery);

    @Nullable
    public abstract IAEPatternDetails decode(AEItemKey what, Level level);
}
