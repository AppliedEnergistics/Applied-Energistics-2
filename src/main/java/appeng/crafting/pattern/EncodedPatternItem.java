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
import java.util.Objects;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import appeng.api.crafting.EncodedPatternDecoder;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.InvalidPatternTooltipStrategy;
import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AmountFormat;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.misc.WrappedGenericStack;
import appeng.util.InteractionUtil;

/**
 * Reusable item class for encoded patterns.
 *
 * @param <T>
 */
public class EncodedPatternItem<T extends IPatternDetails> extends AEBaseItem {
    // rather simple client side caching.
    private static final Map<ItemStack, ItemStack> SIMPLE_CACHE = new WeakHashMap<>();

    private final EncodedPatternDecoder<T> decoder;

    @Nullable
    private final InvalidPatternTooltipStrategy invalidPatternTooltip;

    public EncodedPatternItem(Properties properties,
            EncodedPatternDecoder<T> decoder,
            @Nullable InvalidPatternTooltipStrategy invalidPatternTooltip) {
        super(properties);
        this.decoder = decoder;
        this.invalidPatternTooltip = invalidPatternTooltip;
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
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
            TooltipFlag flags) {
        var what = AEItemKey.of(stack);
        if (what == null || what.getTag() == null) {
            // This can be called very early to index tooltips for search. In those cases,
            // there is no encoded pattern present.
            return;
        }

        PatternDetailsTooltip tooltip;
        try {
            var details = Objects.requireNonNull(decoder.decode(what, level), "decoder returned null");
            tooltip = details.getTooltip(level, flags);
        } catch (Exception e) {
            lines.add(GuiText.InvalidPattern.text().copy().withStyle(ChatFormatting.RED));
            if (invalidPatternTooltip != null) {
                tooltip = invalidPatternTooltip.getTooltip(stack.getTag(), level, e, flags);
            } else {
                tooltip = null;
            }
        }

        if (tooltip != null) {
            var label = Component.empty().append(tooltip.getOutputMethod())
                    .append(": ").withStyle(ChatFormatting.GRAY);
            var and = Component.literal(" ").append(GuiText.And.text())
                    .append(" ").withStyle(ChatFormatting.GRAY);
            var with = GuiText.With.text().copy().append(": ").withStyle(ChatFormatting.GRAY);

            boolean first = true;
            for (var output : tooltip.getOutputs()) {
                lines.add(Component.empty().append(first ? label : and).append(getEntryLine(output)));
                first = false;
            }

            first = true;
            for (var input : tooltip.getInputs()) {
                lines.add(Component.empty().append(first ? with : and).append(getEntryLine(input)));
                first = false;
            }

            for (var property : tooltip.getProperties()) {
                if (property.value() != null) {
                    lines.add(Component.empty().append(property.name())
                            .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                            .append(property.value()));
                } else {
                    lines.add(Component.empty().withStyle(ChatFormatting.GRAY).append(property.name()));
                }
            }
        }
    }

    protected static Component getEntryLine(PatternDetailsTooltip.Entry entry) {
        if (entry instanceof PatternDetailsTooltip.ValidEntry validEntry) {
            return getEntryLine(validEntry.what().getDisplayName(), validEntry.what().getType(), validEntry.amount());
        } else if (entry instanceof PatternDetailsTooltip.InvalidEntry invalidEntry) {
            return getEntryLine(invalidEntry.name(), invalidEntry.type(), invalidEntry.amount());
        } else {
            throw new IncompatibleClassChangeError("Unknown entry type: " + entry.getClass());
        }
    }

    protected static Component getEntryLine(Component displayName, @Nullable AEKeyType amountType, long amount) {
        if (amount > 0) {
            var amountInfo = Component.literal(amountType != null ? amountType.formatAmount(amount, AmountFormat.FULL)
                    : String.valueOf(amount));
            return amountInfo.append(Component.literal(" x ").withStyle(ChatFormatting.GRAY)).append(displayName);
        } else {
            return displayName;
        }
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

        var details = decode(item, level);
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
    public IPatternDetails decode(ItemStack stack, Level level) {
        if (stack.getItem() != this || !stack.hasTag() || level == null) {
            return null;
        }

        var what = AEItemKey.of(stack);
        try {
            return Objects.requireNonNull(decoder.decode(what, level), "decoder returned null");
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public IPatternDetails decode(AEItemKey what, Level level) {
        if (what == null || !what.hasTag()) {
            return null;
        }

        try {
            return decoder.decode(what, level);
        } catch (Exception e) {
            return null;
        }
    }
}
