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

import com.google.common.base.Preconditions;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
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

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.helpers.InvalidPatternHelper;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class EncodedPatternItem extends AEBaseItem implements ICraftingPatternItem {

    private static final String NBT_INGREDIENTS = "in";
    private static final String NBT_PRODUCTS = "out";
    private static final String NBT_CRAFTING = "crafting";
    private static final String NBT_SUBSITUTE = "substitute";
    private static final String NBT_RECIPE = "recipe";

    // rather simple client side caching.
    private static final Map<ItemStack, ItemStack> SIMPLE_CACHE = new WeakHashMap<>();

    public EncodedPatternItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final PlayerEntity player, final Hand hand) {
        this.clearPattern(player.getHeldItem(hand), player);

        return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        return this.clearPattern(stack, context.getPlayer()) ? ActionResultType.SUCCESS : ActionResultType.PASS;
    }

    private boolean clearPattern(final ItemStack stack, final PlayerEntity player) {
        if (player.isCrouching()) {
            if (Platform.isClient()) {
                return false;
            }

            final PlayerInventory inv = player.inventory;

            ItemStack is = Api.instance().definitions().materials().blankPattern().maybeStack(stack.getCount())
                    .orElse(ItemStack.EMPTY);
            if (!is.isEmpty()) {
                for (int s = 0; s < player.inventory.getSizeInventory(); s++) {
                    if (inv.getStackInSlot(s) == stack) {
                        inv.setInventorySlotContents(s, is);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
        final ICraftingPatternDetails details = Api.instance().crafting().getPattern(stack, world);

        if (details == null) {
            if (!stack.hasTag()) {
                return;
            }

            stack.setDisplayName(GuiText.InvalidPattern.textComponent().applyTextStyle(TextFormatting.RED));

            InvalidPatternHelper invalid = new InvalidPatternHelper(stack);

            final ITextComponent label = (invalid.isCraftable() ? GuiText.Crafts.textComponent()
                    : GuiText.Creates.textComponent()).appendText(": ");
            final ITextComponent and = new StringTextComponent(" ").appendSibling(GuiText.And.textComponent())
                    .appendText(" ");
            final ITextComponent with = GuiText.With.textComponent().appendText(": ");

            boolean first = true;
            for (final InvalidPatternHelper.PatternIngredient output : invalid.getOutputs()) {
                lines.add((first ? label : and).deepCopy().appendSibling(output.getFormattedToolTip()));
                first = false;
            }

            first = true;
            for (final InvalidPatternHelper.PatternIngredient input : invalid.getInputs()) {
                lines.add((first ? with : and).deepCopy().appendSibling(input.getFormattedToolTip()));
                first = false;
            }

            if (invalid.isCraftable()) {
                final ITextComponent substitutionLabel = GuiText.Substitute.textComponent().appendText(" ");
                final ITextComponent canSubstitute = invalid.canSubstitute() ? GuiText.Yes.textComponent()
                        : GuiText.No.textComponent();

                lines.add(substitutionLabel.appendSibling(canSubstitute));
            }

            return;
        }

        if (stack.hasDisplayName()) {
            stack.removeChildTag("display");
        }

        final boolean isCrafting = details.isCraftable();
        final boolean substitute = details.canSubstitute();

        final IAEItemStack[] in = details.getCondensedInputs();
        final IAEItemStack[] out = details.getCondensedOutputs();

        final ITextComponent label = (isCrafting ? GuiText.Crafts.textComponent() : GuiText.Creates.textComponent())
                .appendText(": ");
        final ITextComponent and = new StringTextComponent(" ").appendSibling(GuiText.And.textComponent())
                .appendText(" ");
        final ITextComponent with = GuiText.With.textComponent().appendText(": ");

        boolean first = true;
        for (final IAEItemStack anOut : out) {
            if (anOut == null) {
                continue;
            }

            lines.add((first ? label : and).deepCopy().appendText(anOut.getStackSize() + "x ")
                    .appendSibling(Platform.getItemDisplayName(anOut)));
            first = false;
        }

        first = true;
        for (final IAEItemStack anIn : in) {
            if (anIn == null) {
                continue;
            }

            lines.add((first ? with : and).deepCopy().appendText(anIn.getStackSize() + "x ")
                    .appendSibling(Platform.getItemDisplayName(anIn)));
            first = false;
        }

        if (isCrafting) {
            final ITextComponent substitutionLabel = GuiText.Substitute.textComponent().appendText(" ");
            final ITextComponent canSubstitute = substitute ? GuiText.Yes.textComponent() : GuiText.No.textComponent();

            lines.add(substitutionLabel.appendSibling(canSubstitute));
        }
    }

    public ItemStack getOutput(final ItemStack item) {
        ItemStack out = SIMPLE_CACHE.get(item);

        if (out != null) {
            return out;
        }

        final World w = AppEng.proxy.getWorld();
        if (w == null) {
            return ItemStack.EMPTY;
        }

        final ICraftingPatternDetails details = Api.instance().crafting().getPattern(item, w);

        out = details != null ? details.getOutputs()[0].createItemStack() : ItemStack.EMPTY;

        SIMPLE_CACHE.put(item, out);
        return out;
    }

    @Override
    public ResourceLocation recipe(ItemStack itemStack) {
        final CompoundNBT encodedValue = itemStack.getTag();
        Preconditions.checkArgument(encodedValue != null, "itemStack missing a NBT tag");

        return null;
    }

    @Override
    public List<IAEItemStack> ingredients(ItemStack itemStack) {
        final CompoundNBT encodedValue = itemStack.getTag();
        Preconditions.checkArgument(encodedValue != null, "itemStack missing a NBT tag");

        final ListNBT inTag = encodedValue.getList(NBT_INGREDIENTS, 10);
        Preconditions.checkArgument(inTag.size() < 10, "Cannot use more than 9 ingredients");

        final List<IAEItemStack> in = new ArrayList<>();
        for (int x = 0; x < inTag.size(); x++) {
            CompoundNBT ingredient = inTag.getCompound(x);
            final ItemStack gs = ItemStack.read(ingredient);

            Preconditions.checkArgument(!(!ingredient.isEmpty() && gs.isEmpty()), "invalid itemStack in slot", x);

            in.add(Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(gs));
        }

        return in;
    }

    @Override
    public List<IAEItemStack> products(ItemStack itemStack) {
        final CompoundNBT encodedValue = itemStack.getTag();

        Preconditions.checkArgument(encodedValue != null, "itemStack missing a NBT tag");

        final ListNBT outTag = encodedValue.getList(NBT_PRODUCTS, 10);
        Preconditions.checkArgument(outTag.size() < 4, "Cannot use more than 3 ingredients");

        final List<IAEItemStack> out = new ArrayList<>();
        for (int x = 0; x < outTag.size(); x++) {
            CompoundNBT ingredient = outTag.getCompound(x);
            final ItemStack gs = ItemStack.read(ingredient);

            Preconditions.checkArgument(!(!ingredient.isEmpty() && gs.isEmpty()), "invalid itemStack in slot", x);

            out.add(Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(gs));
        }

        return out;

    }

    @Override
    public boolean isCrafting(ItemStack itemStack) {
        final CompoundNBT encodedValue = itemStack.getTag();

        Preconditions.checkArgument(encodedValue != null, "itemStack missing a NBT tag");

        return encodedValue.getBoolean(NBT_CRAFTING);
    }

    @Override
    public boolean allowsSubstitution(ItemStack itemStack) {
        final CompoundNBT encodedValue = itemStack.getTag();

        Preconditions.checkArgument(encodedValue != null, "itemStack missing a NBT tag");

        return this.isCrafting(itemStack) && encodedValue.getBoolean(NBT_SUBSITUTE);
    }
}
