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
import java.util.Objects;

import com.google.common.base.Preconditions;

import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.items.IItemHandler;

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
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.WrapperInvItemHandler;
import appeng.util.item.AEItemStack;
import appeng.util.prioritylist.IPartitionList;

public class JEIRecipePacket extends BasePacket {

    private ResourceLocation recipeId;
    private boolean crafting;

    public JEIRecipePacket(final PacketBuffer stream) {
        final String id = stream.readString(Short.MAX_VALUE);
        this.recipeId = new ResourceLocation(id);
        this.crafting = stream.readBoolean();
    }

    // api
    public JEIRecipePacket(final String recipeId, final boolean crafting) {
        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeString(recipeId);
        data.writeBoolean(crafting);

        this.configureWrite(data);
    }

    /**
     * Servside handler for this packet.
     * 
     * Makes use of {@link Preconditions#checkArgument(boolean)} as the
     * {@link BasePacketHandler} is catching them and in general these cases should
     * never happen except in an error case and should be logged then.
     */
    @Override
    public void serverPacketData(final INetworkInfo manager, final PlayerEntity player) {
        // Setup and verification
        final ServerPlayerEntity pmp = (ServerPlayerEntity) player;
        final Container con = pmp.openContainer;
        Preconditions.checkArgument(con instanceof IContainerCraftingPacket);

        final IRecipe<?> recipe = player.getEntityWorld().getRecipeManager().getRecipe(this.recipeId).orElse(null);
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
        final IItemHandler craftMatrix = cct.getInventoryByName("crafting");
        final IItemHandler playerInventory = cct.getInventoryByName("player");

        final IMEMonitor<IAEItemStack> storage = inv
                .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
        final IPartitionList<IAEItemStack> filter = ViewCellItem.createFilter(cct.getViewCells());
        final NonNullList<Ingredient> ingredients = this.ensure3by3CraftingMatrix(recipe);

        // Handle each slot
        for (int x = 0; x < craftMatrix.getSlots(); x++) {
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
                    out = request != null
                            ? Platform.poweredExtraction(energy, storage, request.setStackSize(1),
                                    cct.getActionSource())
                            : null;
                } else {
                    out = findBestMatchingPattern(ingredient, filter, crafting, storage, cct);
                    if (out == null) {
                        out = findBestMatchingItemStack(ingredient, filter, storage, cct);
                    }
                }

                if (out != null) {
                    currentItem = out.createItemStack();
                }
            }

            // If still nothing, search the player inventory.
            if (currentItem.isEmpty()) {
                ItemStack[] matchingStacks = ingredient.getMatchingStacks();
                for (int y = 0; y < matchingStacks.length; y++) {
                    if (currentItem.isEmpty()) {
                        AdaptorItemHandler ad = new AdaptorItemHandler(playerInventory);

                        if (cct.useRealItems()) {
                            currentItem = ad.removeItems(1, matchingStacks[y], null);
                        } else {
                            currentItem = ad.simulateRemove(1, matchingStacks[y], null);
                        }
                    }
                }
            }
            ItemHandlerUtil.setStackInSlot(craftMatrix, x, currentItem);
        }

        if (!this.crafting) {
            this.handleProcessing(con, cct, recipe);
        }

        con.onCraftMatrixChanged(new WrapperInvItemHandler(craftMatrix));
    }

    /**
     * Expand any recipe to a 3x3 matrix.
     * 
     * Will throw an {@link IllegalArgumentException} in case it has more than 9 or
     * a shaped recipe is either wider or higher than 3. ingredients.
     * 
     * @param recipe
     * @return
     */
    private NonNullList<Ingredient> ensure3by3CraftingMatrix(IRecipe<?> recipe) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        NonNullList<Ingredient> expandedIngredients = NonNullList.withSize(9, Ingredient.EMPTY);

        Preconditions.checkArgument(ingredients.size() <= 9);

        // shaped recipes can be either 2x2, 2x3, 3x2, or 3x3. Expand to 3x3
        if (recipe instanceof IShapedRecipe) {
            IShapedRecipe<?> shapedRecipe = (IShapedRecipe<?>) recipe;
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
        // Anything else should a flat list
        else {
            for (int i = 0; i < ingredients.size(); i++) {
                expandedIngredients.set(i, ingredients.get(i));
            }
        }

        return expandedIngredients;
    }

    /**
     *
     * @param ingredient
     * @param slot
     * @param is         itemstack
     * @return is if it can be used, else EMPTY
     */
    private ItemStack canUseInSlot(Ingredient ingredient, ItemStack is) {
        return Arrays.stream(ingredient.getMatchingStacks()).filter(p -> p.isItemEqual(is)).findFirst()
                .orElse(ItemStack.EMPTY);
    }

    /**
     * Finds the first matching itemstack with the highest stored amount.
     * 
     * @param ingredients
     * @param filter
     * @param storage
     * @param cct
     * @return
     */
    private IAEItemStack findBestMatchingItemStack(Ingredient ingredients, IPartitionList<IAEItemStack> filter,
            IMEMonitor<IAEItemStack> storage, IContainerCraftingPacket cct) {
        return Arrays.stream(ingredients.getMatchingStacks()).map(AEItemStack::fromItemStack)
                .filter(r -> (filter == null || filter.isListed(r))).map(s -> s.setStackSize(Long.MAX_VALUE))
                .map(s -> storage.extractItems(s, Actionable.SIMULATE, cct.getActionSource())).filter(Objects::nonNull)
                .sorted((left, right) -> {
                    return Long.compare(right.getStackSize(), left.getStackSize());
                }).findFirst().orElse(null);
    }

    /**
     * This tries to find the first pattern matching the list of ingredients.
     * 
     * As additional condition, it sorts by the stored amount to return the one with
     * the highest stored amount.
     * 
     * @param ingredients
     * @param filter
     * @param crafting
     * @param storage
     * @param cct
     * @return
     */
    private IAEItemStack findBestMatchingPattern(Ingredient ingredients, IPartitionList<IAEItemStack> filter,
            ICraftingGrid crafting, IMEMonitor<IAEItemStack> storage, IContainerCraftingPacket cct) {
        return Arrays.stream(ingredients.getMatchingStacks()).map(AEItemStack::fromItemStack)
                .filter(r -> (filter == null || filter.isListed(r)))
                .map(s -> s.setCraftable(!crafting.getCraftingFor(s, null, 0, null).isEmpty()))
                .filter(IAEItemStack::isCraftable).map(s -> {
                    final IAEItemStack stored = storage.extractItems(s, Actionable.SIMULATE, cct.getActionSource());
                    return s.setStackSize(stored != null ? stored.getStackSize() : 0);
                }).sorted((left, right) -> {
                    final int craftable = Boolean.compare(left.isCraftable(), right.isCraftable());
                    return craftable != 0 ? craftable : Long.compare(right.getStackSize(), left.getStackSize());
                }).findFirst().orElse(null);
    }

    private void handleProcessing(Container con, IContainerCraftingPacket cct, IRecipe<?> recipe) {
        if (con instanceof PatternTermContainer) {
            PatternTermContainer patternTerm = (PatternTermContainer) con;
            if (!patternTerm.craftingMode) {
                final IItemHandler output = cct.getInventoryByName("output");
                ItemHandlerUtil.setStackInSlot(output, 0, recipe.getRecipeOutput());
                ItemHandlerUtil.setStackInSlot(output, 1, ItemStack.EMPTY);
                ItemHandlerUtil.setStackInSlot(output, 2, ItemStack.EMPTY);
            }
        }
    }

}
