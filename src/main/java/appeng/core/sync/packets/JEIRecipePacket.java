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

package appeng.core.sync.packets;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;

import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.PatternTermContainer;
import appeng.core.Api;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IContainerCraftingPacket;
import appeng.items.storage.ViewCellItem;
import appeng.mixins.IngredientAccessor;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorFixedInv;
import appeng.util.inv.WrapperInvItemHandler;
import appeng.util.item.AEItemStack;
import appeng.util.prioritylist.IPartitionList;

public class JEIRecipePacket extends BasePacket {

    /**
     * Transmit only a recipe ID.
     */
    private static final int INLINE_RECIPE_NONE = 1;

    /**
     * Transmit the information about the recipe we actually need. This is
     * explicitly limited since this is untrusted client->server info.
     */
    private static final int INLINE_RECIPE_SHAPED = 2;

    private Identifier recipeId;
    /**
     * This is optional, in case the client already knows it could not resolve the
     * recipe id.
     */
    @Nullable
    private Recipe<?> recipe;
    private boolean crafting;

    public JEIRecipePacket(final PacketByteBuf stream) {
        this.crafting = stream.readBoolean();
        final String id = stream.readString(Short.MAX_VALUE);
        this.recipeId = new Identifier(id);

        int inlineRecipeType = stream.readVarInt();
        switch (inlineRecipeType) {
            case INLINE_RECIPE_NONE:
                break;
            case INLINE_RECIPE_SHAPED:
                recipe = RecipeSerializer.SHAPED.read(this.recipeId, stream);
                break;
            default:
                throw new IllegalArgumentException("Invalid inline recipe type.");
        }
    }

    /**
     * Sends a recipe identified by the given recipe ID to the server for either
     * filling a crafting grid or a pattern.
     */
    public JEIRecipePacket(final Identifier recipeId, final boolean crafting) {
        PacketByteBuf data = createCommonHeader(recipeId, crafting, INLINE_RECIPE_NONE);
        this.configureWrite(data);
    }

    /**
     * Sends a recipe to the server for either filling a crafting grid or a pattern.
     * <p>
     * Prefer the id-based constructor above whereever possible.
     */
    public JEIRecipePacket(final ShapedRecipe recipe, final boolean crafting) {
        PacketByteBuf data = createCommonHeader(recipe.getId(), crafting, INLINE_RECIPE_SHAPED);
        RecipeSerializer.SHAPED.write(data, recipe);
        this.configureWrite(data);
    }

    private PacketByteBuf createCommonHeader(Identifier recipeId, boolean crafting, int inlineRecipeType) {
        final PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeBoolean(crafting);
        data.writeIdentifier(recipeId);
        data.writeVarInt(inlineRecipeType);

        return data;
    }

    /**
     * Servside handler for this packet.
     * <p>
     * Makes use of {@link Preconditions#checkArgument(boolean)} as the
     * {@link BasePacketHandler} is catching them and in general these cases should
     * never happen except in an error case and should be logged then.
     */
    @Override
    public void serverPacketData(final INetworkInfo manager, final PlayerEntity player) {
        // Setup and verification
        final ServerPlayerEntity pmp = (ServerPlayerEntity) player;
        final ScreenHandler con = pmp.currentScreenHandler;
        Preconditions.checkArgument(con instanceof IContainerCraftingPacket);

        Recipe<?> recipe = player.getEntityWorld().getRecipeManager().get(this.recipeId).orElse(null);
        if (recipe == null && this.recipe != null) {
            // Certain recipes (i.e. AE2 facades) are represented in JEI as ShapedRecipe's,
            // while in reality they
            // are special recipes. Those recipes are sent across the wire...
            recipe = this.recipe;
        }
        Preconditions.checkArgument(recipe != null);

        final IContainerCraftingPacket cct = (IContainerCraftingPacket) con;
        final IGridNode node = cct.getNetworkNode();

        Preconditions.checkArgument(node != null);

        final IGrid grid = node.getGrid();
        Preconditions.checkArgument(grid != null);

        final IStorageGrid inv = grid.getCache(IStorageGrid.class);
        Preconditions.checkArgument(inv != null);

        final ISecurityGrid security = grid.getCache(ISecurityGrid.class);
        Preconditions.checkArgument(security != null);

        final IEnergyGrid energy = grid.getCache(IEnergyGrid.class);
        final ICraftingGrid crafting = grid.getCache(ICraftingGrid.class);
        final FixedItemInv craftMatrix = cct.getInventoryByName("crafting");
        final FixedItemInv playerInventory = cct.getInventoryByName("player");

        final IMEMonitor<IAEItemStack> storage = inv
                .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
        final IPartitionList<IAEItemStack> filter = ViewCellItem.createFilter(cct.getViewCells());
        final DefaultedList<Ingredient> ingredients = this.ensure3by3CraftingMatrix(recipe);

        // Handle each slot
        for (int x = 0; x < craftMatrix.getSlotCount(); x++) {
            ItemStack currentItem = craftMatrix.getInvStack(x);
            Ingredient ingredient = ingredients.get(x);

            // prepare slots
            if (!currentItem.isEmpty()) {
                // already the correct item? True, skip everything else
                ItemStack newItem = this.canUseInSlot(ingredient, currentItem);

                // put away old item, if not correct
                if (newItem != currentItem && security.hasPermission(player, SecurityPermissions.INJECT)) {
                    final IAEItemStack in = AEItemStack.fromItemStack(currentItem);
                    final IAEItemStack out = cct.useRealItems()
                            ? Platform.poweredInsert(energy, storage, in, cct.getActionSource())
                            : null;
                    if (out != null) {
                        currentItem = out.createItemStack();
                    } else {
                        currentItem = ItemStack.EMPTY;
                    }
                }
            }

            // Find item or pattern from the network
            if (currentItem.isEmpty() && security.hasPermission(player, SecurityPermissions.EXTRACT)) {
                IAEItemStack out;

                if (cct.useRealItems()) {
                    IAEItemStack request = findBestMatchingItemStack(ingredient, filter, storage, cct);
                    out = request != null
                            ? Platform.poweredExtraction(energy, storage, request.setStackSize(1),
                                    cct.getActionSource())
                            : null;
                } else {
                    out = findBestMatchingPattern(ingredient, filter, crafting, storage, cct);
                    if (out == null) {
                        out = findBestMatchingItemStack(ingredient, filter, storage, cct);
                    }
                    if (out == null && getMatchingStacks(ingredient).length > 0) {
                        out = AEItemStack.fromItemStack(getMatchingStacks(ingredient)[0]);
                    }
                }

                if (out != null) {
                    currentItem = out.createItemStack();
                }
            }

            // If still nothing, search the player inventory.
            if (currentItem.isEmpty()) {
                ItemStack[] matchingStacks = getMatchingStacks(ingredient);
                for (ItemStack matchingStack : matchingStacks) {
                    if (currentItem.isEmpty()) {
                        AdaptorFixedInv ad = new AdaptorFixedInv(playerInventory);

                        if (cct.useRealItems()) {
                            currentItem = ad.removeItems(1, matchingStack, null);
                        } else {
                            currentItem = ad.simulateRemove(1, matchingStack, null);
                        }
                    }
                }
            }
            ItemHandlerUtil.setStackInSlot(craftMatrix, x, currentItem);
        }

        if (!this.crafting) {
            this.handleProcessing(con, cct, recipe);
        }

        con.onContentChanged(new WrapperInvItemHandler(craftMatrix));
    }

    @SuppressWarnings("ConstantConditions")
    private static ItemStack[] getMatchingStacks(Ingredient ingredient) {
        IngredientAccessor accessor = (IngredientAccessor) (Object) ingredient;
        accessor.appeng_cacheMatchingStacks();
        if (ingredient.isEmpty()) {
            return new ItemStack[0];
        }
        ItemStack[] stacks = accessor.getMatchingStacks();
        if (stacks.length == 1 && stacks[0].isEmpty()) {
            return new ItemStack[0];
        }
        return stacks;
    }

    /**
     * Expand any recipe to a 3x3 matrix.
     * <p>
     * Will throw an {@link IllegalArgumentException} in case it has more than 9 or
     * a shaped recipe is either wider or higher than 3. ingredients.
     */
    private DefaultedList<Ingredient> ensure3by3CraftingMatrix(Recipe<?> recipe) {
        DefaultedList<Ingredient> ingredients = recipe.getPreviewInputs();
        DefaultedList<Ingredient> expandedIngredients = DefaultedList.ofSize(9, Ingredient.EMPTY);

        Preconditions.checkArgument(ingredients.size() <= 9);

        // shaped recipes can be smaller than 3x3, expand to 3x3 to match the crafting
        // matrix
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            int width = shapedRecipe.getWidth();
            int height = shapedRecipe.getHeight();
            Preconditions.checkArgument(width <= 3 && height <= 3);

            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    int source = w + h * width;
                    int target = w + h * 3;
                    Ingredient i = ingredients.get(source);
                    expandedIngredients.set(target, i);
                }
            }
        }
        // Anything else should be a flat list
        else {
            for (int i = 0; i < ingredients.size(); i++) {
                expandedIngredients.set(i, ingredients.get(i));
            }
        }

        return expandedIngredients;
    }

    /**
     * @param is itemstack
     * @return is if it can be used, else EMPTY
     */
    private ItemStack canUseInSlot(Ingredient ingredient, ItemStack is) {
        return Arrays.stream(getMatchingStacks(ingredient)).filter(p -> p.isItemEqual(is)).findFirst()
                .orElse(ItemStack.EMPTY);
    }

    /**
     * Finds the first matching itemstack with the highest stored amount.
     */
    private IAEItemStack findBestMatchingItemStack(Ingredient ingredients, IPartitionList<IAEItemStack> filter,
            IMEMonitor<IAEItemStack> storage, IContainerCraftingPacket cct) {
        return Arrays.stream(getMatchingStacks(ingredients)).map(AEItemStack::fromItemStack) //
                .filter(r -> r != null && (filter == null || filter.isListed(r))) //
                .map(s -> {
                    // Determine the stored count
                    IAEItemStack stored = storage.extractItems(s.copy().setStackSize(Long.MAX_VALUE),
                            Actionable.SIMULATE, cct.getActionSource());
                    return Pair.of(s, stored != null ? stored.getStackSize() : 0);
                }).min((left, right) -> Long.compare(right.getSecond(), left.getSecond()))//
                .map(Pair::getFirst).orElse(null);
    }

    /**
     * This tries to find the first pattern matching the list of ingredients.
     * <p>
     * As additional condition, it sorts by the stored amount to return the one with
     * the highest stored amount.
     */
    private IAEItemStack findBestMatchingPattern(Ingredient ingredients, IPartitionList<IAEItemStack> filter,
            ICraftingGrid crafting, IMEMonitor<IAEItemStack> storage, IContainerCraftingPacket cct) {
        return Arrays.stream(getMatchingStacks(ingredients)).map(AEItemStack::fromItemStack)
                .filter(r -> r != null && (filter == null || filter.isListed(r)))
                .map(s -> s.setCraftable(!crafting.getCraftingFor(s, null, 0, null).isEmpty()))
                .filter(IAEItemStack::isCraftable).map(s -> {
                    final IAEItemStack stored = storage.extractItems(s, Actionable.SIMULATE, cct.getActionSource());
                    return s.setStackSize(stored != null ? stored.getStackSize() : 0);
                }).min((left, right) -> {
                    final int craftable = Boolean.compare(left.isCraftable(), right.isCraftable());
                    return craftable != 0 ? craftable : Long.compare(right.getStackSize(), left.getStackSize());
                }).orElse(null);
    }

    private void handleProcessing(ScreenHandler con, IContainerCraftingPacket cct, Recipe<?> recipe) {
        if (con instanceof PatternTermContainer) {
            PatternTermContainer patternTerm = (PatternTermContainer) con;
            if (!patternTerm.craftingMode) {
                final FixedItemInv output = cct.getInventoryByName("output");
                ItemHandlerUtil.setStackInSlot(output, 0, recipe.getOutput());
                ItemHandlerUtil.setStackInSlot(output, 1, ItemStack.EMPTY);
                ItemHandlerUtil.setStackInSlot(output, 2, ItemStack.EMPTY);
            }
        }
    }

}
