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
import net.minecraftforge.common.crafting.IShapedRecipe;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.ISecurityService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IMenuCraftingPacket;
import appeng.items.storage.ViewCellItem;
import appeng.menu.me.items.PatternTermMenu;
import appeng.parts.reporting.PatternTerminalPart;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
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
    private boolean crafting;

    public JEIRecipePacket(final FriendlyByteBuf stream) {
        this.crafting = stream.readBoolean();
        this.recipeId = new ResourceLocation(stream.readUtf());

        int inlineRecipeType = stream.readVarInt();
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
    public JEIRecipePacket(final ResourceLocation recipeId, final boolean crafting) {
        FriendlyByteBuf data = createCommonHeader(recipeId, crafting, INLINE_RECIPE_NONE);
        this.configureWrite(data);
    }

    /**
     * Sends a recipe to the server for either filling a crafting grid or a pattern.
     * <p>
     * Prefer the id-based constructor above whereever possible.
     */
    public JEIRecipePacket(final ShapedRecipe recipe, final boolean crafting) {
        FriendlyByteBuf data = createCommonHeader(recipe.getId(), crafting, INLINE_RECIPE_SHAPED);
        RecipeSerializer.SHAPED_RECIPE.toNetwork(data, recipe);
        this.configureWrite(data);
    }

    private FriendlyByteBuf createCommonHeader(ResourceLocation recipeId, boolean crafting, int inlineRecipeType) {
        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeBoolean(crafting);
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
        final AbstractContainerMenu con = player.containerMenu;
        Preconditions.checkArgument(con instanceof IMenuCraftingPacket);

        Recipe<?> recipe = player.getCommandSenderWorld().getRecipeManager().byKey(this.recipeId).orElse(null);
        if (recipe == null && this.recipe != null) {
            // Certain recipes (i.e. AE2 facades) are represented in JEI as ShapedRecipe's,
            // while in reality they
            // are special recipes. Those recipes are sent across the wire...
            recipe = this.recipe;
        }
        Preconditions.checkArgument(recipe != null);

        var cct = (IMenuCraftingPacket) con;
        var node = cct.getNetworkNode();

        Preconditions.checkArgument(node != null);

        var grid = node.getGrid();
        Preconditions.checkArgument(grid != null);

        var inv = grid.getService(IStorageService.class);
        Preconditions.checkArgument(inv != null);

        var security = grid.getService(ISecurityService.class);
        Preconditions.checkArgument(security != null);

        var energy = grid.getService(IEnergyService.class);
        var crafting = grid.getService(ICraftingService.class);
        var craftMatrix = cct.getSubInventory(PatternTerminalPart.INV_CRAFTING);

        final IMEMonitor<IAEItemStack> storage = inv
                .getInventory(StorageChannels.items());
        final IPartitionList<IAEItemStack> filter = ViewCellItem.createFilter(cct.getViewCells());
        final NonNullList<Ingredient> ingredients = this.ensure3by3CraftingMatrix(recipe);

        // Handle each slot
        for (int x = 0; x < craftMatrix.size(); x++) {
            ItemStack currentItem = craftMatrix.getStackInSlot(x);
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
                    if (request != null) {
                        request.setStackSize(1);
                        out = Platform.poweredExtraction(energy, storage, request, cct.getActionSource());
                    } else {
                        out = null;
                    }
                } else {
                    out = findBestMatchingPattern(ingredient, filter, crafting, storage, cct);
                    if (out == null) {
                        out = findBestMatchingItemStack(ingredient, filter, storage, cct);
                    }
                    if (out == null && ingredient.getItems().length > 0) {
                        out = AEItemStack.fromItemStack(ingredient.getItems()[0]);
                    }
                }

                if (out != null) {
                    currentItem = out.createItemStack();
                }
            }

            // If still nothing, search the player inventory.
            if (currentItem.isEmpty()) {
                ItemStack[] matchingStacks = ingredient.getItems();
                for (ItemStack matchingStack : matchingStacks) {
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

        if (!this.crafting) {
            this.handleProcessing(con, cct, recipe);
        }

        con.slotsChanged(craftMatrix.toContainer());
    }

    /**
     * Expand any recipe to a 3x3 matrix.
     * <p>
     * Will throw an {@link IllegalArgumentException} in case it has more than 9 or a shaped recipe is either wider or
     * higher than 3. ingredients.
     */
    private NonNullList<Ingredient> ensure3by3CraftingMatrix(Recipe<?> recipe) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        NonNullList<Ingredient> expandedIngredients = NonNullList.withSize(9, Ingredient.EMPTY);

        Preconditions.checkArgument(ingredients.size() <= 9);

        // shaped recipes can be smaller than 3x3, expand to 3x3 to match the crafting
        // matrix
        if (recipe instanceof IShapedRecipe<?>shapedRecipe) {
            int width = shapedRecipe.getRecipeWidth();
            int height = shapedRecipe.getRecipeHeight();
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
        return Arrays.stream(ingredient.getItems()).filter(p -> p.sameItem(is)).findFirst()
                .orElse(ItemStack.EMPTY);
    }

    /**
     * Finds the first matching itemstack with the highest stored amount.
     */
    private IAEItemStack findBestMatchingItemStack(Ingredient ingredients, IPartitionList<IAEItemStack> filter,
            IMEMonitor<IAEItemStack> storage, IMenuCraftingPacket cct) {
        Stream<AEItemStack> stacks = Arrays.stream(ingredients.getItems())//
                .map(AEItemStack::fromItemStack) //
                .filter(r -> r != null && (filter == null || filter.isListed(r)));
        return getMostStored(stacks, storage, cct);
    }

    /**
     * This tries to find the first pattern matching the list of ingredients.
     * <p>
     * As additional condition, it sorts by the stored amount to return the one with the highest stored amount.
     */
    private IAEItemStack findBestMatchingPattern(Ingredient ingredients, IPartitionList<IAEItemStack> filter,
            ICraftingService crafting, IMEMonitor<IAEItemStack> storage, IMenuCraftingPacket cct) {
        var stacks = Arrays.stream(ingredients.getItems())//
                .map(AEItemStack::fromItemStack)//
                .filter(r -> r != null && (filter == null || filter.isListed(r)))//
                .peek(s -> s.setCraftable(!crafting.getCraftingFor(s).isEmpty()))//
                .filter(IAEItemStack::isCraftable);
        return getMostStored(stacks, storage, cct);
    }

    /**
     * From a stream of AE item stacks, pick the one with the highest available amount in the network. Returns null if
     * the stream is empty.
     */
    private static IAEItemStack getMostStored(Stream<? extends IAEItemStack> stacks, IMEMonitor<IAEItemStack> storage,
            IMenuCraftingPacket cct) {
        return stacks//
                .map(s -> {
                    // Determine the stored count
                    var r = IAEStack.copy(s, Long.MAX_VALUE);
                    IAEItemStack stored = storage.extractItems(r, Actionable.SIMULATE, cct.getActionSource());
                    return Pair.of(s, stored != null ? stored.getStackSize() : 0);
                })//
                .min((left, right) -> Long.compare(right.getSecond(), left.getSecond()))//
                .map(Pair::getFirst)//
                .orElse(null);
    }

    private void handleProcessing(AbstractContainerMenu con, IMenuCraftingPacket cct, Recipe<?> recipe) {
        if (con instanceof PatternTermMenu patternTerm) {
            if (!patternTerm.craftingMode) {
                var output = cct.getSubInventory(PatternTerminalPart.INV_OUTPUT);
                output.setItemDirect(0, recipe.getResultItem());
                output.setItemDirect(1, ItemStack.EMPTY);
                output.setItemDirect(2, ItemStack.EMPTY);
            }
        }
    }

}
