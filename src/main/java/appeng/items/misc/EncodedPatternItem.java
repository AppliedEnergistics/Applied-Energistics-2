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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.google.common.base.Preconditions;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.common.util.Constants;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.helpers.InvalidPatternHelper;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public class EncodedPatternItem extends AEBaseItem {

    public static final String NBT_INGREDIENTS = "in";
    public static final String NBT_PRODUCTS = "out";
    public static final String NBT_SUBSITUTE = "substitute";
    public static final String NBT_RECIPE_ID = "recipe";

    // rather simple client side caching.
    private static final Map<ItemStack, ItemStack> SIMPLE_CACHE = new WeakHashMap<>();

    public EncodedPatternItem(Item.Properties properties) {
        super(properties);
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
        final ICraftingPatternDetails details = AEApi.crafting().decodePattern(stack, level);

        if (details == null) {
            if (!stack.hasTag()) {
                return;
            }

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

        final boolean isCrafting = details.isCraftable();
        final boolean substitute = details.canSubstitute();

        final Collection<IAEItemStack> in = details.getInputs();
        final Collection<IAEItemStack> out = details.getOutputs();

        final Component label = (isCrafting ? GuiText.Crafts.text() : GuiText.Creates.text()).copy()
                .append(": ");
        final Component and = new TextComponent(" ").copy().append(GuiText.And.text())
                .append(" ");
        final Component with = GuiText.With.text().copy().append(": ");

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
            final Component substitutionLabel = GuiText.Substitute.text().copy().append(" ");
            final Component canSubstitute = substitute ? GuiText.Yes.text() : GuiText.No.text();

            lines.add(substitutionLabel.copy().append(canSubstitute));
        }
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

        final ICraftingPatternDetails details = AEApi.crafting().decodePattern(item, level);

        out = details != null ? details.getOutputs().get(0).createItemStack() : ItemStack.EMPTY;

        SIMPLE_CACHE.put(item, out);
        return out;
    }

    public boolean isEncodedPattern(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && itemStack.getItem() == this && itemStack.getTag() != null
                && itemStack.getTag().contains(NBT_INGREDIENTS, Constants.NBT.TAG_LIST)
                && itemStack.getTag().contains(NBT_PRODUCTS, Constants.NBT.TAG_LIST);
    }

    public ResourceLocation getCraftingRecipeId(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack.getItem() == this, "Given item stack %s is not an encoded pattern.",
                itemStack);
        final CompoundTag tag = itemStack.getTag();
        Preconditions.checkArgument(tag != null, "itemStack missing a NBT tag");

        return tag.contains(NBT_RECIPE_ID, Constants.NBT.TAG_STRING)
                ? new ResourceLocation(tag.getString(NBT_RECIPE_ID))
                : null;
    }

    public List<IAEItemStack> getIngredients(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack.getItem() == this, "Given item stack %s is not an encoded pattern.",
                itemStack);
        final CompoundTag tag = itemStack.getTag();
        Preconditions.checkArgument(tag != null, "itemStack missing a NBT tag");

        final ListTag inTag = tag.getList(NBT_INGREDIENTS, 10);
        Preconditions.checkArgument(inTag.size() < 10, "Cannot use more than 9 ingredients");

        final List<IAEItemStack> in = new ArrayList<>(inTag.size());
        for (int x = 0; x < inTag.size(); x++) {
            CompoundTag ingredient = inTag.getCompound(x);
            final ItemStack gs = ItemStack.of(ingredient);

            Preconditions.checkArgument(!(!ingredient.isEmpty() && gs.isEmpty()), "invalid itemStack in slot", x);

            in.add(StorageChannels.items().createStack(gs));
        }

        return in;
    }

    public List<IAEItemStack> getProducts(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack.getItem() == this, "Given item stack %s is not an encoded pattern.",
                itemStack);
        final CompoundTag tag = itemStack.getTag();
        Preconditions.checkArgument(tag != null, "itemStack missing a NBT tag");

        final ListTag outTag = tag.getList(NBT_PRODUCTS, 10);
        Preconditions.checkArgument(outTag.size() < 4, "Cannot use more than 3 ingredients");

        final List<IAEItemStack> out = new ArrayList<>(outTag.size());
        for (int x = 0; x < outTag.size(); x++) {
            CompoundTag ingredient = outTag.getCompound(x);
            final ItemStack gs = ItemStack.of(ingredient);

            Preconditions.checkArgument(!(!ingredient.isEmpty() && gs.isEmpty()), "invalid itemStack in slot", x);

            out.add(StorageChannels.items().createStack(gs));
        }

        return out;

    }

    public boolean allowsSubstitution(ItemStack itemStack) {
        final CompoundTag tag = itemStack.getTag();

        Preconditions.checkArgument(tag != null, "itemStack missing a NBT tag");

        return getCraftingRecipeId(itemStack) != null && tag.getBoolean(NBT_SUBSITUTE);
    }

    /**
     * Use the public API instead {@link appeng.core.api.ApiCrafting}
     */
    public static void encodeCraftingPattern(ItemStack stack, ItemStack[] in, ItemStack[] out,
            ResourceLocation recipeId, boolean allowSubstitutes) {
        CompoundTag encodedValue = encodeInputsAndOutputs(in, out);
        encodedValue.putString(EncodedPatternItem.NBT_RECIPE_ID, recipeId.toString());
        encodedValue.putBoolean(EncodedPatternItem.NBT_SUBSITUTE, allowSubstitutes);
        stack.setTag(encodedValue);
    }

    /**
     * Use the public API instead {@link appeng.core.api.ApiCrafting}
     */
    public static void encodeProcessingPattern(ItemStack stack, ItemStack[] in, ItemStack[] out) {
        stack.setTag(encodeInputsAndOutputs(in, out));
    }

    private static CompoundTag encodeInputsAndOutputs(ItemStack[] in, ItemStack[] out) {
        final CompoundTag encodedValue = new CompoundTag();

        final ListTag tagIn = new ListTag();
        final ListTag tagOut = new ListTag();

        boolean hasInput = false;
        for (final ItemStack i : in) {
            tagIn.add(createItemTag(i));
            if (!i.isEmpty()) {
                hasInput = true;
            }
        }

        Preconditions.checkArgument(hasInput, "cannot encode a pattern that has no inputs.");

        boolean hasNonEmptyOutput = false;
        for (final ItemStack i : out) {
            tagOut.add(createItemTag(i));
            if (!i.isEmpty()) {
                hasNonEmptyOutput = true;
            }
        }

        // Patterns without any outputs are corrupt! Never encode such a pattern.
        Preconditions.checkArgument(hasNonEmptyOutput, "cannot encode a pattern that has no output.");

        encodedValue.put(EncodedPatternItem.NBT_INGREDIENTS, tagIn);
        encodedValue.put(EncodedPatternItem.NBT_PRODUCTS, tagOut);
        return encodedValue;
    }

    private static Tag createItemTag(final ItemStack i) {
        final CompoundTag c = new CompoundTag();

        if (!i.isEmpty()) {
            i.save(c);
        }

        return c;
    }

}
