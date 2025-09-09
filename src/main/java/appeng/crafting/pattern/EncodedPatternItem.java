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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.misc.WrappedGenericStack;
import appeng.util.InteractionUtil;

public abstract class EncodedPatternItem extends AEBaseItem {
    public static final boolean FANCY_TOOLTIPS = AEConfig.instance().isFancyPatternTooltips();

    // rather simple client side caching.
    private static final Map<ItemStack, ItemStack> SIMPLE_CACHE = new WeakHashMap<>();

    public EncodedPatternItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.Output output) {
        // Don't show in creative mode, since it's not useful without NBT
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
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

    private boolean clearPattern(ItemStack stack, Player player) {
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
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
            TooltipFlag advancedTooltips) {
        if (!stack.hasTag() || FANCY_TOOLTIPS) {
            // This can be called very early to index tooltips for search. In those cases,
            // there is no encoded pattern present.
            return;
        }

        List<Component> keyRenderer = new ArrayList<>();

        var details = decode(stack, level, false);
        if (details == null) {
            // TODO: needs update for new pattern logic
            stack.setHoverName(GuiText.InvalidPattern.text().copy().withStyle(ChatFormatting.RED));

            var invalid = new InvalidPatternHelper(stack);

            var label = (invalid.isCraftable() ? GuiText.Crafts.text() : GuiText.Produces.text())
                    .withStyle(ChatFormatting.DARK_AQUA);
            var ingredients = Component.empty().append(GuiText.Ingredients.text().withStyle(ChatFormatting.DARK_GREEN));

            boolean first = true;
            for (var output : invalid.getOutputs()) {
                if (first)
                    keyRenderer.add(label);
                keyRenderer.add(Component.literal("  ").append(output.getFormattedToolTip()));
                first = false;
            }

            first = true;
            for (var input : invalid.getInputs()) {
                if (first)
                    keyRenderer.add(ingredients);
                keyRenderer.add(Component.literal("  ").append(input.getFormattedToolTip()));
                first = false;
            }

            if (invalid.isCraftable()) {
                var canSubstitute = invalid.canSubstitute() ? GuiText.Yes.text() : GuiText.No.text();
                var substitutionLabel = GuiText.Substitute.text(canSubstitute);

                keyRenderer.add(substitutionLabel);
            }

            return;
        }

        if (stack.hasCustomHoverName()) {
            stack.resetHoverName();
        }

        var isCrafting = details instanceof AECraftingPattern;
        var substitute = isCrafting && ((AECraftingPattern) details).canSubstitute;
        var substituteFluids = isCrafting && ((AECraftingPattern) details).canSubstituteFluids;
        var author = details.getAuthor();

        var in = details.getInputs();
        var out = details.getOutputs();

        var label = (isCrafting ? GuiText.Crafts.text() : GuiText.Produces.text()).withStyle(ChatFormatting.DARK_AQUA);
        var ingredients = GuiText.Ingredients.text().withStyle(ChatFormatting.DARK_GREEN);

        boolean first = true;
        for (var anOut : out) {
            if (anOut == null) {
                continue;
            }
            if (first)
                keyRenderer.add(label);

            keyRenderer.add(Component.literal("  ").append(getStackComponent(anOut, false)));
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
            if (first)
                keyRenderer.add(ingredients);

            keyRenderer.add(Component.literal("  ").append(getStackComponent(primaryInput, true)));
            first = false;
        }

        if (isCrafting) {
            var yes = GuiText.Yes.text().withStyle(ChatFormatting.GREEN);
            var no = GuiText.No.text().withStyle(ChatFormatting.RED);
            var canSubstitute = substitute ? yes : no;
            var canSubstituteFluids = substituteFluids ? yes : no;

            var substitutionLabel = GuiText.Substitute.text(canSubstitute);
            var fluidSubstitutionLabel = GuiText.FluidSubstitutions.text(canSubstituteFluids);

            keyRenderer.add(substitutionLabel);
            keyRenderer.add(fluidSubstitutionLabel);
        }

        if (!details.getAuthor().isEmpty()) {
            keyRenderer.add(GuiText.EncodedBy.text(details.getAuthor()).withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        lines.addAll(keyRenderer);
    }

    protected static Component getStackComponent(GenericStack stack, boolean isInput) {
        var type = stack.what().getType();
        var what = stack.what();
        var displayName = what.getDisplayName().plainCopy();
        if (type == AEKeyType.items()) {
            displayName.withStyle(isInput ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
        } else {
            displayName.withStyle(isInput ? ChatFormatting.AQUA : ChatFormatting.BLUE);
        }
        var amountInfo = what.formatAmount(stack.amount(), AmountFormat.FULL);
        return Component.literal(amountInfo + " §7x ").append(displayName);
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
            } else {
                out = WrappedGenericStack.wrap(output.what(), 0);
            }
        }

        SIMPLE_CACHE.put(item, out);
        return out;
    }

    @Nullable
    public abstract IPatternDetails decode(ItemStack stack, Level level, boolean tryRecovery);

    @Nullable
    public abstract IPatternDetails decode(AEItemKey what, Level level);

    @Override
    @OnlyIn(Dist.CLIENT)
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!stack.hasTag() || !FANCY_TOOLTIPS)
            return Optional.empty();

        var details = decode(stack, AppEng.instance().getClientLevel(), false);
        if (details == null)
            return Optional.empty();

        var isCrafting = details instanceof AECraftingPattern;
        var substitute = isCrafting && ((AECraftingPattern) details).canSubstitute;
        var substituteFluids = isCrafting && ((AECraftingPattern) details).canSubstituteFluids;
        var author = details.getAuthor();

        var inputs = new ArrayList<GenericStack>();
        var outputs = new ArrayList<GenericStack>();

        for (var entry : details.getOutputs()) {
            outputs.add(new GenericStack(entry.what(), entry.amount()));
        }
        for (var entry : details.getInputs()) {
            inputs.add(new GenericStack(entry.getPossibleInputs()[0].what(), entry.getMultiplier()));
        }

        return Optional.of(new PatternKeyTooltipComponent(inputs, outputs, author, isCrafting, substitute,
                substituteFluids, false));
    }

}
