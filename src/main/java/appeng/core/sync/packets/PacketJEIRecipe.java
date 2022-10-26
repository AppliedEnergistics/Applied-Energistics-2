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


import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
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
import appeng.container.implementations.ContainerExpandedProcessingPatternTerm;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IContainerCraftingPacket;
import appeng.items.storage.ItemViewCell;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.WrapperInvItemHandler;
import appeng.util.item.AEItemStack;
import appeng.util.prioritylist.IPartitionList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class PacketJEIRecipe extends AppEngPacket {

    private List<ItemStack[]> recipe;
    private List<ItemStack> output;
    static ItemStack[] emptyArray = {ItemStack.EMPTY};


    // automatic.
    public PacketJEIRecipe(final ByteBuf stream) throws IOException {
        final ByteArrayInputStream bytes = this.getPacketByteArray(stream);
        bytes.skip(stream.readerIndex());
        final NBTTagCompound comp = CompressedStreamTools.readCompressed(bytes);
        if (comp != null) {
            this.recipe = new ArrayList<>();

            for (int x = 0; x < comp.getKeySet().size(); x++) {
                if (comp.hasKey("#" + x)) {
                    final NBTTagList list = comp.getTagList("#" + x, 10);
                    if (list.tagCount() > 0) {
                        this.recipe.add(new ItemStack[list.tagCount()]);
                        for (int y = 0; y < list.tagCount(); y++) {
                            this.recipe.get(x)[y] = new ItemStack(list.getCompoundTagAt(y));
                        }
                    } else {
                        this.recipe.add(emptyArray);
                    }
                }
            }

            if (comp.hasKey("outputs")) {
                final NBTTagList outputList = comp.getTagList("outputs", 10);
                this.output = new ArrayList<>();
                for (int z = 0; z < outputList.tagCount(); z++) {
                    this.output.add(new ItemStack(outputList.getCompoundTagAt(z)));
                }
            }
        }

    }

    // api
    public PacketJEIRecipe(final NBTTagCompound recipe) throws IOException {
        final ByteBuf data = Unpooled.buffer();

        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final DataOutputStream outputStream = new DataOutputStream(bytes);

        data.writeInt(this.getPacketID());

        CompressedStreamTools.writeCompressed(recipe, outputStream);
        data.writeBytes(bytes.toByteArray());

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player) {
        final EntityPlayerMP pmp = (EntityPlayerMP) player;
        final Container con = pmp.openContainer;

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
        final IItemHandler craftMatrix = cct.getInventoryByName("crafting");
        final IItemHandler playerInventory = cct.getInventoryByName("player");

        if (inv != null && this.recipe != null && security != null) {
            final IMEMonitor<IAEItemStack> storage = inv.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
            final IPartitionList<IAEItemStack> filter = ItemViewCell.createFilter(cct.getViewCells());

            for (int x = 0; x < craftMatrix.getSlots(); x++) {
                ItemStack currentItem = craftMatrix.getStackInSlot(x);

                if (x >= this.recipe.size()) {
                    currentItem = ItemStack.EMPTY;
                }

                // prepare slots
                if (!currentItem.isEmpty()) {
                    // already the correct item?
                    ItemStack newItem = this.canUseInSlot(x, currentItem);

                    if (!cct.useRealItems() && this.recipe.get(x) != null) {
                        if (this.recipe.get(x).length > 0) {
                            currentItem.setCount(recipe.get(x)[0].getCount());
                        }
                    }

                    // put away old item
                    if (newItem != currentItem && security.hasPermission(player, SecurityPermissions.INJECT)) {
                        final IAEItemStack in = AEItemStack.fromItemStack(currentItem);
                        final IAEItemStack out = cct.useRealItems() ? Platform.poweredInsert(energy, storage, in, cct.getActionSource()) : null;
                        if (out != null) {
                            currentItem = out.createItemStack();
                        } else {
                            currentItem = ItemStack.EMPTY;
                        }
                    }
                }

                if (currentItem.isEmpty() && recipe.size() > x && recipe.get(x) != null) {
                    // for each variant
                    for (int y = 0; y < this.recipe.get(x).length && currentItem.isEmpty(); y++) {
                        final IAEItemStack request = AEItemStack.fromItemStack(this.recipe.get(x)[y]);
                        if (request != null) {
                            // try ae
                            if ((filter == null || filter.isListed(request)) && security.hasPermission(player, SecurityPermissions.EXTRACT)) {
                                request.setStackSize(1);
                                IAEItemStack out;

                                if (cct.useRealItems()) {
                                    out = Platform.poweredExtraction(energy, storage, request, cct.getActionSource());
                                    if (out == null) {
                                        if (request.getItem().isDamageable() || Platform.isGTDamageableItem(request.getItem())) {
                                            Collection<IAEItemStack> outList = inv.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)).getStorageList().findFuzzy(request, FuzzyMode.IGNORE_ALL);
                                            for (IAEItemStack is : outList) {
                                                if (Platform.isGTDamageableItem(request.getItem())) {
                                                    if (!(is.getDefinition().getMetadata() == request.getDefinition().getMetadata())) {
                                                        continue;
                                                    }
                                                }
                                                out = Platform.poweredExtraction(energy, storage, is.copy().setStackSize(1), cct.getActionSource());
                                                if (out != null) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
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
                                    if (!cct.useRealItems()) {
                                        out.setStackSize(recipe.get(x)[y].getCount());
                                    }
                                    currentItem = out.createItemStack();
                                }
                            }

                            // try inventory
                            if (currentItem.isEmpty()) {
                                AdaptorItemHandler ad = new AdaptorItemHandler(playerInventory);

                                if (cct.useRealItems()) {
                                    currentItem = ad.removeSimilarItems(1, this.recipe.get(x)[y], FuzzyMode.IGNORE_ALL, null);
                                } else {
                                    currentItem = ad.simulateSimilarRemove(recipe.get(x)[y].getCount(), this.recipe.get(x)[y], FuzzyMode.IGNORE_ALL, null);
                                }
                            }
                        }
                    }
                    if (!cct.useRealItems()) {
                        if (currentItem.isEmpty() && recipe.size() > x && this.recipe.get(x) != null) {
                            currentItem = this.recipe.get(x)[0].copy();
                        }
                    }
                }
                ItemHandlerUtil.setStackInSlot(craftMatrix, x, currentItem);
            }

            con.onCraftMatrixChanged(new WrapperInvItemHandler(craftMatrix));

            if (this.output != null && ((con instanceof ContainerPatternTerm && !((ContainerPatternTerm) con).isCraftingMode()) || con instanceof ContainerExpandedProcessingPatternTerm)) {
                IItemHandler outputSlots = cct.getInventoryByName("output");
                for (int i = 0; i < outputSlots.getSlots(); ++i) {
                    ItemHandlerUtil.setStackInSlot(outputSlots, i, ItemStack.EMPTY);
                }
                for (int i = 0; i < this.output.size() && i < outputSlots.getSlots(); ++i) {
                    if (this.output.get(i) == null || this.output.get(i) == ItemStack.EMPTY) {
                        continue;
                    }
                    ItemHandlerUtil.setStackInSlot(outputSlots, i, this.output.get(i));
                }
            }
        }
    }

    /**
     * @param slot
     * @param is   itemstack
     * @return is if it can be used, else EMPTY
     */
    private ItemStack canUseInSlot(int slot, ItemStack is) {
        if (this.recipe.get(slot) != null) {
            for (ItemStack option : this.recipe.get(slot)) {
                if (ItemStack.areItemStacksEqual(is, option)) {
                    return is;
                }
            }
        }
        return ItemStack.EMPTY;
    }

}
