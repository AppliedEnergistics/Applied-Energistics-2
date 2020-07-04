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

import alexiil.mc.lib.attributes.item.FixedItemInv;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;

import appeng.api.AEApi;
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
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IContainerCraftingPacket;
import appeng.items.storage.ViewCellItem;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorFixedInv;
import appeng.util.inv.WrapperInvItemHandler;
import appeng.util.item.AEItemStack;
import appeng.util.prioritylist.IPartitionList;

public class JEIRecipePacket extends BasePacket {

    private ItemStack[][] recipe;

    public JEIRecipePacket(final PacketByteBuf stream) {
        final CompoundTag comp = stream.readCompoundTag();
        if (comp != null) {
            this.recipe = new ItemStack[9][];
            for (int x = 0; x < this.recipe.length; x++) {
                final ListTag list = comp.getList("#" + x, 10);
                if (list.size() > 0) {
                    this.recipe[x] = new ItemStack[list.size()];
                    for (int y = 0; y < list.size(); y++) {
                        this.recipe[x][y] = ItemStack.fromTag(list.getCompound(y));
                    }
                }
            }
        }
    }

    // api
    public JEIRecipePacket(final CompoundTag recipe) {
        final PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());

        data.writeCompoundTag(recipe);

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final PlayerEntity player) {
        final ServerPlayerEntity pmp = (ServerPlayerEntity) player;
        final ScreenHandler con = pmp.currentScreenHandler;

        if (!(con instanceof IContainerCraftingPacket)) {
            return;
        }

        final IContainerCraftingPacket cct = (IContainerCraftingPacket) con;
        final IGridNode node = cct.getNetworkNode();

        if (node == null) {
            return;
        }

        final IGrid grid = node.getGrid();
        if (grid == null) {
            return;
        }

        final IStorageGrid inv = grid.getCache(IStorageGrid.class);
        final IEnergyGrid energy = grid.getCache(IEnergyGrid.class);
        final ISecurityGrid security = grid.getCache(ISecurityGrid.class);
        final ICraftingGrid crafting = grid.getCache(ICraftingGrid.class);
        final FixedItemInv craftMatrix = cct.getInventoryByName("crafting");
        final FixedItemInv playerInventory = cct.getInventoryByName("player");

        if (inv != null && this.recipe != null && security != null) {
            final IMEMonitor<IAEItemStack> storage = inv
                    .getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
            final IPartitionList<IAEItemStack> filter = ViewCellItem.createFilter(cct.getViewCells());

            for (int x = 0; x < craftMatrix.getSlotCount(); x++) {
                ItemStack currentItem = craftMatrix.getInvStack(x);

                // prepare slots
                if (!currentItem.isEmpty()) {
                    // already the correct item?
                    ItemStack newItem = this.canUseInSlot(x, currentItem);

                    // put away old item
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

                if (currentItem.isEmpty() && this.recipe[x] != null) {
                    // for each variant
                    for (int y = 0; y < this.recipe[x].length && currentItem.isEmpty(); y++) {
                        final IAEItemStack request = AEItemStack.fromItemStack(this.recipe[x][y]);
                        if (request != null) {
                            // try ae
                            if ((filter == null || filter.isListed(request))
                                    && security.hasPermission(player, SecurityPermissions.EXTRACT)) {
                                request.setStackSize(1);
                                IAEItemStack out;

                                if (cct.useRealItems()) {
                                    out = Platform.poweredExtraction(energy, storage, request, cct.getActionSource());
                                } else {
                                    // Query the crafting grid if there is a pattern providing the item
                                    if (!crafting.getCraftingFor(request, null, 0, null).isEmpty()) {
                                        out = request;
                                    } else {
                                        // Fall back using an existing item
                                        out = storage.extractItems(request, Actionable.SIMULATE, cct.getActionSource());
                                    }
                                }

                                if (out != null) {
                                    currentItem = out.createItemStack();
                                }
                            }

                            // try inventory
                            if (currentItem.isEmpty()) {
                                AdaptorFixedInv ad = new AdaptorFixedInv(playerInventory);

                                if (cct.useRealItems()) {
                                    currentItem = ad.removeItems(1, this.recipe[x][y], null);
                                } else {
                                    currentItem = ad.simulateRemove(1, this.recipe[x][y], null);
                                }
                            }
                        }
                    }
                }
                ItemHandlerUtil.setStackInSlot(craftMatrix, x, currentItem);
            }
            con.onContentChanged(new WrapperInvItemHandler(craftMatrix));
        }
    }

    /**
     *
     * @param slot
     * @param is   itemstack
     * @return is if it can be used, else EMPTY
     */
    private ItemStack canUseInSlot(int slot, ItemStack is) {
        if (this.recipe[slot] != null) {
            for (ItemStack option : this.recipe[slot]) {
                if (is.isItemEqual(option)) {
                    return is;
                }
            }
        }
        return ItemStack.EMPTY;
    }

}
