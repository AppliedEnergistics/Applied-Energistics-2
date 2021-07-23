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

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.helpers.InvalidPatternHelper;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

import net.minecraft.item.Item.Properties;

public class EncodedPatternItem extends AEBaseItem {

    public static final String NBT_INGREDIENTS = "in";
    public static final String NBT_PRODUCTS = "out";
    public static final String NBT_SUBSITUTE = "substitute";
    public static final String NBT_RECIPE_ID = "recipe";

    // rather simple client side caching.
    private static final Map<ItemStack, ItemStack> SIMPLE_CACHE = new WeakHashMap<>();

    public EncodedPatternItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(final World w, final PlayerEntity player, final Hand hand) {
        this.clearPattern(player.getItemInHand(hand), player);

        return new ActionResult<>(ActionResultType.sidedSuccess(w.isClientSide()), player.getItemInHand(hand));
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        return this.clearPattern(stack, context.getPlayer())
                ? ActionResultType.sidedSuccess(context.getLevel().isClientSide())
                : ActionResultType.PASS;
    }

    private boolean clearPattern(final ItemStack stack, final PlayerEntity player) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            if (player.getCommandSenderWorld().isClientSide()) {
                return false;
            }

            final PlayerInventory inv = player.inventory;

            ItemStack is = AEItems.BLANK_PATTERN.stack(stack.getCount());
            if (!is.isEmpty()) {
                for (int s = 0; s < player.inventory.getContainerSize(); s++) {
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
    public void appendHoverText(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
        final ICraftingPatternDetails details = Api.instance().crafting().decodePattern(stack, world);

        if (details == null) {
            if (!stack.hasTag()) {
                return;
            }

            stack.setHoverName(GuiText.InvalidPattern.text().copy().withStyle(TextFormatting.RED));

            InvalidPatternHelper invalid = new InvalidPatternHelper(stack);

            final ITextComponent label = (invalid.isCraftable() ? GuiText.Crafts.text() : GuiText.Creates.text())
                    .copy().append(": ");
            final ITextComponent and = new StringTextComponent(" ").copy().append(GuiText.And.text())
                    .copy()
                    .append(" ");
            final ITextComponent with = GuiText.With.text().copy().append(": ");

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
                final ITextComponent substitutionLabel = GuiText.Substitute.text().copy().append(" ");
                final ITextComponent canSubstitute = invalid.canSubstitute() ? GuiText.Yes.text() : GuiText.No.text();

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

        final ITextComponent label = (isCrafting ? GuiText.Crafts.text() : GuiText.Creates.text()).copy()
                .append(": ");
        final ITextComponent and = new StringTextComponent(" ").copy().append(GuiText.And.text())
                .append(" ");
        final ITextComponent with = GuiText.With.text().copy().append(": ");

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
            final ITextComponent substitutionLabel = GuiText.Substitute.text().copy().append(" ");
            final ITextComponent canSubstitute = substitute ? GuiText.Yes.text() : GuiText.No.text();

            lines.add(substitutionLabel.copy().append(canSubstitute));
        }
    }

    public ItemStack getOutput(final ItemStack item) {
        ItemStack out = SIMPLE_CACHE.get(item);

        if (out != null) {
            return out;
        }

        final World w = AppEng.instance().getClientWorld();
        if (w == null) {
            return ItemStack.EMPTY;
        }

        final ICraftingPatternDetails details = Api.instance().crafting().decodePattern(item, w);

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
        final CompoundNBT tag = itemStack.getTag();
        Preconditions.checkArgument(tag != null, "itemStack missing a NBT tag");

        return tag.contains(NBT_RECIPE_ID, Constants.NBT.TAG_STRING)
                ? new ResourceLocation(tag.getString(NBT_RECIPE_ID))
                : null;
    }

    public List<IAEItemStack> getIngredients(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack.getItem() == this, "Given item stack %s is not an encoded pattern.",
                itemStack);
        final CompoundNBT tag = itemStack.getTag();
        Preconditions.checkArgument(tag != null, "itemStack missing a NBT tag");

        final ListNBT inTag = tag.getList(NBT_INGREDIENTS, 10);
        Preconditions.checkArgument(inTag.size() < 10, "Cannot use more than 9 ingredients");

        final List<IAEItemStack> in = new ArrayList<>(inTag.size());
        for (int x = 0; x < inTag.size(); x++) {
            CompoundNBT ingredient = inTag.getCompound(x);
            final ItemStack gs = ItemStack.of(ingredient);

            Preconditions.checkArgument(!(!ingredient.isEmpty() && gs.isEmpty()), "invalid itemStack in slot", x);

            in.add(Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(gs));
        }

        return in;
    }

    public List<IAEItemStack> getProducts(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack.getItem() == this, "Given item stack %s is not an encoded pattern.",
                itemStack);
        final CompoundNBT tag = itemStack.getTag();
        Preconditions.checkArgument(tag != null, "itemStack missing a NBT tag");

        final ListNBT outTag = tag.getList(NBT_PRODUCTS, 10);
        Preconditions.checkArgument(outTag.size() < 4, "Cannot use more than 3 ingredients");

        final List<IAEItemStack> out = new ArrayList<>(outTag.size());
        for (int x = 0; x < outTag.size(); x++) {
            CompoundNBT ingredient = outTag.getCompound(x);
            final ItemStack gs = ItemStack.of(ingredient);

            Preconditions.checkArgument(!(!ingredient.isEmpty() && gs.isEmpty()), "invalid itemStack in slot", x);

            out.add(Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(gs));
        }

        return out;

    }

    public boolean allowsSubstitution(ItemStack itemStack) {
        final CompoundNBT tag = itemStack.getTag();

        Preconditions.checkArgument(tag != null, "itemStack missing a NBT tag");

        return getCraftingRecipeId(itemStack) != null && tag.getBoolean(NBT_SUBSITUTE);
    }

    /**
     * Use the public API instead {@link appeng.core.api.ApiCrafting}
     */
    public static void encodeCraftingPattern(ItemStack stack, ItemStack[] in, ItemStack[] out,
            ResourceLocation recipeId, boolean allowSubstitutes) {
        CompoundNBT encodedValue = encodeInputsAndOutputs(in, out);
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

    private static CompoundNBT encodeInputsAndOutputs(ItemStack[] in, ItemStack[] out) {
        final CompoundNBT encodedValue = new CompoundNBT();

        final ListNBT tagIn = new ListNBT();
        final ListNBT tagOut = new ListNBT();

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

    private static INBT createItemTag(final ItemStack i) {
        final CompoundNBT c = new CompoundNBT();

        if (!i.isEmpty()) {
            i.save(c);
        }

        return c;
    }

}
