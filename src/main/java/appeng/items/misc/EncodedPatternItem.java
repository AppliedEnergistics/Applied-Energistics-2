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

package appeng.items.misc;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.AEApi;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.IAEPatternDetails;
import appeng.helpers.InvalidPatternHelper;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public class EncodedPatternItem extends AEBaseItem {
    // rather simple client side caching.
    private static final Map<ItemStack, ItemStack> SIMPLE_CACHE = new WeakHashMap<>();

    public EncodedPatternItem(Item.Properties properties) {
        super(properties);
    }

    public static boolean isAE2Pattern(ItemStack stack) {
        return stack.getItem() instanceof EncodedPatternItem;
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
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level level, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        var details = (IAEPatternDetails) AEApi.patterns().decodePattern(stack, level);

        if (details == null) {
            if (!stack.hasTag()) {
                return;
            }

            // TODO: needs update for new pattern logic
            stack.setHoverName(GuiText.InvalidPattern.text().copy().withStyle(ChatFormatting.RED));

            InvalidPatternHelper invalid = new InvalidPatternHelper(stack);

            final Component label = (invalid.isCraftable() ? GuiText.Crafts.text() : GuiText.Creates.text())
                    .copy().append(": ");
            final Component and = new TextComponent(" ").copy().append(GuiText.And.text())
                    .copy()
                    .append(" ");
            final Component with = GuiText.With.text().copy().append(": ");

            boolean first = true;
            for (final InvalidPatternHelper.PatternIngredient output : invalid.getOutputs()) {
                lines.add((first ? label : and).copy().append(output.getFormattedToolTip()));
                first = false;
            }

            first = true;
            for (final InvalidPatternHelper.PatternIngredient input : invalid.getInputs()) {
                lines.add((first ? with : and).copy().append(input.getFormattedToolTip()));
                first = false;
            }

            if (invalid.isCraftable()) {
                final Component substitutionLabel = GuiText.Substitute.text().copy().append(" ");
                final Component canSubstitute = invalid.canSubstitute() ? GuiText.Yes.text() : GuiText.No.text();

                lines.add(substitutionLabel.copy().append(canSubstitute));
            }

            return;
        }

        if (stack.hasCustomHoverName()) {
            stack.removeTagKey("display");
        }

        final boolean isCrafting = details instanceof AECraftingPattern;
        final boolean substitute = isCrafting && ((AECraftingPattern) details).canSubstitute;

        var in = details.getSparseInputs();
        var out = details.getSparseOutputs();

        final Component label = (isCrafting ? GuiText.Crafts.text() : GuiText.Creates.text()).copy()
                .append(": ");
        final Component and = new TextComponent(" ").copy().append(GuiText.And.text())
                .append(" ");
        final Component with = GuiText.With.text().copy().append(": ");

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

            lines.add((first ? with : and).copy().append(getStackComponent(anIn)));
            first = false;
        }

        if (isCrafting) {
            final Component substitutionLabel = GuiText.Substitute.text().copy().append(" ");
            final Component canSubstitute = substitute ? GuiText.Yes.text() : GuiText.No.text();

            lines.add(substitutionLabel.copy().append(canSubstitute));
        }
    }

    private static Component getStackComponent(IAEStack stack) {
        String amountInfo;
        Component displayName;
        if (stack.getChannel() == StorageChannels.items()) {
            amountInfo = String.valueOf(stack.getStackSize());
            displayName = Platform.getItemDisplayName(stack);
        } else if (stack.getChannel() == StorageChannels.fluids()) {
            amountInfo = Platform.formatFluidAmount(stack.getStackSize());
            displayName = Platform.getFluidDisplayName(stack.cast(StorageChannels.fluids()));
        } else {
            throw new IllegalArgumentException("Unsupported storage channel: " + stack.getChannel());
        }
        return new TextComponent(amountInfo + " x ").append(displayName);
    }

    public ItemStack getOutput(final ItemStack item) {
        ItemStack out = SIMPLE_CACHE.get(item);

        if (out != null) {
            return out;
        }

        final Level level = AppEng.instance().getClientLevel();
        if (level == null) {
            return ItemStack.EMPTY;
        }

        var details = AEApi.patterns().decodePattern(item, level);
        out = ItemStack.EMPTY;

        if (details != null) {
            var output = details.getPrimaryOutput();

            // Can only be an item or fluid stack.
            if (output instanceof IAEItemStack itemStack) {
                out = itemStack.createItemStack();
            } else if (output instanceof IAEFluidStack fluidStack) {
                var dummyFluid = AEItems.DUMMY_FLUID_ITEM.asItem();
                out = new ItemStack(dummyFluid);
                dummyFluid.setFluidStack(out, fluidStack.getFluidStack());
            }
        }

        SIMPLE_CACHE.put(item, out);
        return out;
    }
}
