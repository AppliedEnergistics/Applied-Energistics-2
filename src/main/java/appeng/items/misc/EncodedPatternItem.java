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
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import appeng.core.Api;
import appeng.hooks.AEToolItem;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.item.TooltipContext;
import com.google.common.base.Preconditions;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.*;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.localization.GuiText;
import appeng.helpers.InvalidPatternHelper;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class EncodedPatternItem extends AEBaseItem implements AEToolItem {

    public static final String NBT_INGREDIENTS = "in";
    public static final String NBT_PRODUCTS = "out";
    public static final String NBT_SUBSITUTE = "substitute";
    public static final String NBT_RECIPE_ID = "recipe";

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

            ItemStack is = Api.instance().definitions().materials().blankPattern().maybeStack(stack.getCount())
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
        final ICraftingPatternDetails details = Api.instance().crafting().decodePattern(stack, world);

        if (details == null) {
            if (!stack.hasTag()) {
                return;
            }

            stack.setCustomName(GuiText.InvalidPattern.text().copy().formatted(Formatting.RED));

            InvalidPatternHelper invalid = new InvalidPatternHelper(stack);

            final Text label = (invalid.isCraftable() ? GuiText.Crafts.text()
                    : GuiText.Creates.text()).copy().append(": ");
            final Text and = new LiteralText(" ").append(GuiText.And.text())
                    .append(" ");
            final Text with = GuiText.With.text().copy().append(": ");

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
                final MutableText substitutionLabel = GuiText.Substitute.text().copy().append(" ");
                final Text canSubstitute = invalid.canSubstitute() ? GuiText.Yes.text()
                        : GuiText.No.text();

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

        final Text label = (isCrafting ? GuiText.Crafts.text() : GuiText.Creates.text())
                .copy().append(": ");
        final Text and = new LiteralText(" ").append(GuiText.And.text())
                .append(" ");
        final Text with = GuiText.With.text().copy().append(": ");

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
            final MutableText substitutionLabel = GuiText.Substitute.text().copy().append(" ");
            final Text canSubstitute = substitute ? GuiText.Yes.text() : GuiText.No.text();

            lines.add(substitutionLabel.append(canSubstitute));
        }
    }

    public ItemStack getOutput(World w, final ItemStack item) {
        ItemStack out = SIMPLE_CACHE.get(item);

        if (out != null) {
            return out;
        }

        final ICraftingPatternDetails details = Api.instance().crafting().decodePattern(item, w);

        out = details != null ? details.getOutputs()[0].createItemStack() : ItemStack.EMPTY;

        SIMPLE_CACHE.put(item, out);
        return out;
    }

    public boolean isEncodedPattern(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && itemStack.getItem() == this && itemStack.getTag() != null
                && itemStack.getTag().contains(NBT_INGREDIENTS, NbtType.LIST)
                && itemStack.getTag().contains(NBT_PRODUCTS, NbtType.LIST);
    }

    public Identifier getCraftingRecipeId(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack.getItem() == this, "Given item stack %s is not an encoded pattern.",
                itemStack);
        final CompoundTag tag = itemStack.getTag();
        Preconditions.checkArgument(tag != null, "itemStack missing a NBT tag");

        return new Identifier(tag.getString(NBT_RECIPE_ID));
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
            final ItemStack gs = ItemStack.fromTag(ingredient);

            Preconditions.checkArgument(!(!ingredient.isEmpty() && gs.isEmpty()), "invalid itemStack in slot", x);

            in.add(Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(gs));
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
            final ItemStack gs = ItemStack.fromTag(ingredient);

            Preconditions.checkArgument(!(!ingredient.isEmpty() && gs.isEmpty()), "invalid itemStack in slot", x);

            out.add(Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(gs));
        }

        return out;

    }

    public boolean allowsSubstitution(ItemStack itemStack) {
        final CompoundTag tag = itemStack.getTag();

        Preconditions.checkArgument(tag != null, "itemStack missing a NBT tag");

        return getCraftingRecipeId(itemStack) == null && tag.getBoolean(NBT_SUBSITUTE);
    }

    /**
     * Use the public API instead {@link appeng.core.api.ApiCrafting}
     */
    public static void encodeCraftingPattern(ItemStack stack, ItemStack[] in, ItemStack[] out,
            Identifier recipeId, boolean allowSubstitutes) {
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

        for (final ItemStack i : in) {
            tagIn.add(createItemTag(i));
        }

        for (final ItemStack i : out) {
            tagOut.add(createItemTag(i));
        }

        encodedValue.put(EncodedPatternItem.NBT_INGREDIENTS, tagIn);
        encodedValue.put(EncodedPatternItem.NBT_PRODUCTS, tagOut);
        return encodedValue;
    }

    private static Tag createItemTag(final ItemStack i) {
        final CompoundTag c = new CompoundTag();

        if (!i.isEmpty()) {
            i.toTag(c);
        }

        return c;
    }

}
