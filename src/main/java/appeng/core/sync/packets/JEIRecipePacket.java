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
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;

import io.netty.buffer.Unpooled;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEMonitorStorage;
import appeng.api.storage.StorageHelper;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IMenuCraftingPacket;
import appeng.items.storage.ViewCellItem;
import appeng.menu.me.items.PatternTermMenu;
import appeng.util.prioritylist.IPartitionList;

public class JEIRecipePacket extends BasePacket {

    /**
     * Transmit only a recipe ID.
     */
    private static final int INLINE_RECIPE_NONE = 1;

    /**
     * Transmit the information about the recipe we actually need. This is explicitly limited since this is untrusted
     * client->server info.
     */
    private static final int INLINE_RECIPE_SHAPED = 2;

    private ResourceLocation recipeId;
    /**
     * This is optional, in case the client already knows it could not resolve the recipe id.
     */
    @Nullable
    private Recipe<?> recipe;

    public JEIRecipePacket(final FriendlyByteBuf stream) {
        this.recipeId = new ResourceLocation(stream.readUtf());

        var inlineRecipeType = stream.readVarInt();
        switch (inlineRecipeType) {
            case INLINE_RECIPE_NONE:
                break;
            case INLINE_RECIPE_SHAPED:
                recipe = RecipeSerializer.SHAPED_RECIPE.fromNetwork(this.recipeId, stream);
                break;
            default:
                throw new IllegalArgumentException("Invalid inline recipe type.");
        }
    }

    /**
     * Sends a recipe identified by the given recipe ID to the server for either filling a crafting grid or a pattern.
     */
    public JEIRecipePacket(ResourceLocation recipeId) {
        var data = createCommonHeader(recipeId, INLINE_RECIPE_NONE);
        this.configureWrite(data);
    }

    /**
     * Sends a recipe to the server for either filling a crafting grid or a pattern.
     * <p>
     * Prefer the id-based constructor above whereever possible.
     */
    public JEIRecipePacket(ShapedRecipe recipe) {
        var data = createCommonHeader(recipe.getId(), INLINE_RECIPE_SHAPED);
        RecipeSerializer.SHAPED_RECIPE.toNetwork(data, recipe);
        this.configureWrite(data);
    }

    private FriendlyByteBuf createCommonHeader(ResourceLocation recipeId, int inlineRecipeType) {
        final var data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeResourceLocation(recipeId);
        data.writeVarInt(inlineRecipeType);

        return data;
    }

    /**
     * Servside handler for this packet.
     * <p>
     * Makes use of {@link Preconditions#checkArgument(boolean)} as the {@link BasePacketHandler} is catching them and
     * in general these cases should never happen except in an error case and should be logged then.
     */
    @Override
    public void serverPacketData(final INetworkInfo manager, final ServerPlayer player) {
        // Setup and verification
        final var con = player.containerMenu;
        Preconditions.checkArgument(con instanceof IMenuCraftingPacket);

        var recipe = player.getCommandSenderWorld().getRecipeManager().byKey(this.recipeId).orElse(null);
        if (recipe == null && this.recipe != null) {
            // Certain recipes (i.e. AE2 facades) are represented in JEI as ShapedRecipe's,
            // while in reality they
            // are special recipes. Those recipes are sent across the wire...
            recipe = this.recipe;
        }
        if (recipe == null) {
            throw new IllegalArgumentException("Client is requesting to craft unknown recipe " + this.recipeId);
        }

        var cct = (IMenuCraftingPacket) con;
        var node = cct.getNetworkNode();

        Preconditions.checkArgument(node != null);

        var grid = node.getGrid();

        var inv = grid.getStorageService();
        var security = grid.getSecurityService();
        var energy = grid.getEnergyService();
        var crafting = grid.getCraftingService();
        var craftMatrix = cct.getCraftingMatrix();

        var storage = inv.getInventory();
        var filter = ViewCellItem.createItemFilter(cct.getViewCells());
        var ingredients = this.ensure3by3CraftingMatrix(recipe);

        // Handle each slot
        for (var x = 0; x < craftMatrix.size(); x++) {
            var currentItem = craftMatrix.getStackInSlot(x);
            var ingredient = ingredients.get(x);

            // prepare slots
            if (!currentItem.isEmpty()) {
                // already the correct item? True, skip everything else
                var newItem = this.canUseInSlot(ingredient, currentItem);

                // put away old item, if not correct
                if (newItem != currentItem && security.hasPermission(player, SecurityPermissions.INJECT)) {
                    var in = AEItemKey.of(currentItem);
                    var inserted = cct.useRealItems()
                            ? StorageHelper.poweredInsert(energy, storage, in, currentItem.getCount(),
                                    cct.getActionSource())
                            : currentItem.getCount();
                    if (inserted < currentItem.getCount()) {
                        currentItem = currentItem.copy();
                        currentItem.shrink((int) inserted);
                    } else {
                        currentItem = ItemStack.EMPTY;
                    }
                }
            }

            // Find item or pattern from the network
            if (currentItem.isEmpty() && security.hasPermission(player, SecurityPermissions.EXTRACT)) {
                AEItemKey out = null;

                if (cct.useRealItems()) {
                    var request = findBestMatchingItemStack(ingredient, filter, storage, cct);
                    if (request != null) {
                        if (StorageHelper.poweredExtraction(energy, storage, request, 1, cct.getActionSource()) > 0) {
                            out = request;
                        }
                    }
                } else {
                    out = findBestMatchingPattern(ingredient, filter, crafting, storage, cct);
                    if (out == null) {
                        out = findBestMatchingItemStack(ingredient, filter, storage, cct);
                    }
                    if (out == null && ingredient.getItems().length > 0) {
                        out = AEItemKey.of(ingredient.getItems()[0]);
                    }
                }

                if (out != null) {
                    currentItem = out.toStack();
                }
            }

            // If still nothing, search the player inventory.
            if (currentItem.isEmpty()) {
                var matchingStacks = ingredient.getItems();
                for (var matchingStack : matchingStacks) {
                    if (currentItem.isEmpty()) {
                        var playerInv = player.getInventory();
                        var slotMatchingItem = playerInv.findSlotMatchingItem(matchingStack);
                        if (slotMatchingItem != -1) {
                            if (cct.useRealItems()) {
                                currentItem = playerInv.getItem(slotMatchingItem).split(1);
                            } else {
                                currentItem = playerInv.getItem(slotMatchingItem).copy();
                                currentItem.setCount(1);
                            }
                        }
                    }
                }
            }
            craftMatrix.setItemDirect(x, currentItem);
        }

        handleProcessing(con, recipe);

        con.slotsChanged(craftMatrix.toContainer());
    }

    /**
     * Expand any recipe to a 3x3 matrix.
     * <p>
     * Will throw an {@link IllegalArgumentException} in case it has more than 9 or a shaped recipe is either wider or
     * higher than 3. ingredients.
     */
    private NonNullList<Ingredient> ensure3by3CraftingMatrix(Recipe<?> recipe) {
        var ingredients = recipe.getIngredients();
        var expandedIngredients = NonNullList.withSize(9, Ingredient.EMPTY);

        Preconditions.checkArgument(ingredients.size() <= 9);

        // shaped recipes can be smaller than 3x3, expand to 3x3 to match the crafting
        // matrix
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            var width = shapedRecipe.getWidth();
            var height = shapedRecipe.getHeight();
            Preconditions.checkArgument(width <= 3 && height <= 3);

            for (var h = 0; h < height; h++) {
                for (var w = 0; w < width; w++) {
                    var source = w + h * width;
                    var target = w + h * 3;
                    var i = ingredients.get(source);
                    expandedIngredients.set(target, i);
                }
            }
        }
        // Anything else should be a flat list
        else {
            for (var i = 0; i < ingredients.size(); i++) {
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
        return Arrays.stream(ingredient.getItems()).filter(p -> p.sameItem(is)).findFirst()
                .orElse(ItemStack.EMPTY);
    }

    /**
     * Finds the first matching itemstack with the highest stored amount.
     */
    private AEItemKey findBestMatchingItemStack(Ingredient ingredients, IPartitionList filter,
            MEMonitorStorage storage, IMenuCraftingPacket cct) {
        var stacks = Arrays.stream(ingredients.getItems())//
                .map(AEItemKey::of) //
                .filter(r -> r != null && (filter == null || filter.isListed(r)));
        return getMostStored(stacks, storage, cct);
    }

    /**
     * This tries to find the first pattern matching the list of ingredients.
     * <p>
     * As additional condition, it sorts by the stored amount to return the one with the highest stored amount.
     */
    private AEItemKey findBestMatchingPattern(Ingredient ingredients, IPartitionList filter,
            ICraftingService crafting, MEMonitorStorage storage, IMenuCraftingPacket cct) {
        var stacks = Arrays.stream(ingredients.getItems())//
                .map(AEItemKey::of)//
                .filter(r -> r != null && (filter == null || filter.isListed(r)))//
                .filter(crafting::isCraftable);
        return getMostStored(stacks, storage, cct);
    }

    /**
     * From a stream of AE item stacks, pick the one with the highest available amount in the network. Returns null if
     * the stream is empty.
     */
    @Nullable
    private static AEItemKey getMostStored(Stream<AEItemKey> stacks, MEMonitorStorage storage,
            IMenuCraftingPacket cct) {
        return stacks//
                .map(s -> {
                    // Determine the stored count
                    var stored = storage.extract(s, Long.MAX_VALUE, Actionable.SIMULATE, cct.getActionSource());
                    return Pair.of(s, stored);
                })//
                .min((left, right) -> Long.compare(right.getSecond(), left.getSecond()))//
                .map(Pair::getFirst)//
                .orElse(null);
    }

    private void handleProcessing(AbstractContainerMenu con, Recipe<?> recipe) {
        if (con instanceof PatternTermMenu patternTerm) {
            if (!patternTerm.craftingMode) {
                patternTerm.setProcessingResult(recipe.getResultItem());
            }
        }
    }

}
