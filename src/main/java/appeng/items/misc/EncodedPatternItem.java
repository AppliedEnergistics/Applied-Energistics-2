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

import appeng.hooks.AEToolItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.helpers.InvalidPatternHelper;
import appeng.helpers.PatternHelper;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class EncodedPatternItem extends AEBaseItem implements ICraftingPatternItem, AEToolItem {
    // rather simple client side caching.
    private static final Map<ItemStack, ItemStack> SIMPLE_CACHE = new WeakHashMap<>();

    public EncodedPatternItem(Settings properties) {
        super(properties);
    }

    @Override
    public TypedActionResult<ItemStack> use(final World w, final PlayerEntity player, final Hand hand) {
        this.clearPattern(player.getStackInHand(hand), player);

        return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }

    @Override
    public ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context) {
        return this.clearPattern(stack, context.getPlayer()) ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    private boolean clearPattern(final ItemStack stack, final PlayerEntity player) {
        if (player.isInSneakingPose()) {
            if (Platform.isClient()) {
                return false;
            }

            final PlayerInventory inv = player.inventory;

            ItemStack is = AEApi.instance().definitions().materials().blankPattern().maybeStack(stack.getCount())
                    .orElse(ItemStack.EMPTY);
            if (!is.isEmpty()) {
                for (int s = 0; s < player.inventory.size(); s++) {
                    if (inv.getStack(s) == stack) {
                        inv.setStack(s, is);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(final ItemStack stack, final World world, final List<Text> lines,
            final TooltipContext advancedTooltips) {
        final ICraftingPatternDetails details = this.getPatternForItem(stack, world);

        if (details == null) {
            if (!stack.hasTag()) {
                return;
            }

            stack.setCustomName(GuiText.InvalidPattern.textComponent().copy().formatted(Formatting.RED));

            InvalidPatternHelper invalid = new InvalidPatternHelper(stack);

            final Text label = (invalid.isCraftable() ? GuiText.Crafts.textComponent()
                    : GuiText.Creates.textComponent()).copy().append(": ");
            final Text and = new LiteralText(" ").append(GuiText.And.textComponent())
                    .append(" ");
            final Text with = GuiText.With.textComponent().copy().append(": ");

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
                final MutableText substitutionLabel = GuiText.Substitute.textComponent().copy().append(" ");
                final Text canSubstitute = invalid.canSubstitute() ? GuiText.Yes.textComponent()
                        : GuiText.No.textComponent();

                lines.add(substitutionLabel.append(canSubstitute));
            }

            return;
        }

        if (stack.hasCustomName()) {
            stack.removeSubTag("display");
        }

        final boolean isCrafting = details.isCraftable();
        final boolean substitute = details.canSubstitute();

        final IAEItemStack[] in = details.getCondensedInputs();
        final IAEItemStack[] out = details.getCondensedOutputs();

        final Text label = (isCrafting ? GuiText.Crafts.textComponent() : GuiText.Creates.textComponent())
                .copy().append(": ");
        final Text and = new LiteralText(" ").append(GuiText.And.textComponent())
                .append(" ");
        final Text with = GuiText.With.textComponent().copy().append(": ");

        boolean first = true;
        for (final IAEItemStack anOut : out) {
            if (anOut == null) {
                continue;
            }

            lines.add((first ? label : and).copy().append(anOut.getStackSize() + "x ")
                    .append(Platform.getItemDisplayName(anOut)));
            first = false;
        }

        first = true;
        for (final IAEItemStack anIn : in) {
            if (anIn == null) {
                continue;
            }

            lines.add((first ? with : and).copy().append(anIn.getStackSize() + "x ")
                    .append(Platform.getItemDisplayName(anIn)));
            first = false;
        }

        if (isCrafting) {
            final MutableText substitutionLabel = GuiText.Substitute.textComponent().copy().append(" ");
            final Text canSubstitute = substitute ? GuiText.Yes.textComponent() : GuiText.No.textComponent();

            lines.add(substitutionLabel.append(canSubstitute));
        }
    }

    @Override
    public ICraftingPatternDetails getPatternForItem(final ItemStack is, final World w) {
        try {
            return new PatternHelper(is, w);
        } catch (final Throwable t) {
            return null;
        }
    }

    public ItemStack getOutput(World w, final ItemStack item) {
        ItemStack out = SIMPLE_CACHE.get(item);

        if (out != null) {
            return out;
        }

        final ICraftingPatternDetails details = this.getPatternForItem(item, w);

        out = details != null ? details.getOutputs()[0].createItemStack() : ItemStack.EMPTY;

        SIMPLE_CACHE.put(item, out);
        return out;
    }
}
