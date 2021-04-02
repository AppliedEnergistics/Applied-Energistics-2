/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.container.slot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.container.implementations.CraftingTermContainer;
import appeng.core.Api;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ViewCellItem;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorFixedInv;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.inv.WrapperInvItemHandler;
import appeng.util.item.AEItemStack;

public class CraftingTermSlot extends AppEngCraftingSlot {

    private final FixedItemInv craftInv;
    private final FixedItemInv pattern;

    private final IActionSource mySrc;
    private final IEnergySource energySrc;
    private final IStorageMonitorable storage;
    private final IContainerCraftingPacket container;

    public CraftingTermSlot(final PlayerEntity player, final IActionSource mySrc, final IEnergySource energySrc,
            final IStorageMonitorable storage, final FixedItemInv cMatrix, final FixedItemInv secondMatrix,
            final FixedItemInv output, final int x, final int y, final IContainerCraftingPacket ccp) {
        super(player, cMatrix, output, 0, x, y);
        this.energySrc = energySrc;
        this.storage = storage;
        this.mySrc = mySrc;
        this.pattern = cMatrix;
        this.craftInv = secondMatrix;
        this.container = ccp;
    }

    public FixedItemInv getCraftingMatrix() {
        return this.craftInv;
    }

    @Override
    public boolean canTakeStack(final PlayerEntity player) {
        return false;
    }

    @Override
    public ItemStack onTake(final PlayerEntity p, final ItemStack is) {
        return is;
    }

    public void doClick(final InventoryAction action, final PlayerEntity who) {
        if (this.getStack().isEmpty()) {
            return;
        }
        if (isRemote()) {
            return;
        }

        final IMEMonitor<IAEItemStack> inv = this.storage
                .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
        final int howManyPerCraft = this.getStack().getCount();
        int maxTimesToCraft = 0;

        InventoryAdaptor ia = null;
        if (action == InventoryAction.CRAFT_SHIFT) // craft into player inventory...
        {
            ia = InventoryAdaptor.getAdaptor(who);
            maxTimesToCraft = (int) Math.floor((double) this.getStack().getMaxStackSize() / (double) howManyPerCraft);
        } else if (action == InventoryAction.CRAFT_STACK) // craft into hand, full stack
        {
            ia = new AdaptorFixedInv(new WrapperCursorItemHandler(who.inventory));
            maxTimesToCraft = (int) Math.floor((double) this.getStack().getMaxStackSize() / (double) howManyPerCraft);
        } else
        // pick up what was crafted...
        {
            ia = new AdaptorFixedInv(new WrapperCursorItemHandler(who.inventory));
            maxTimesToCraft = 1;
        }

        maxTimesToCraft = this.capCraftingAttempts(maxTimesToCraft);

        if (ia == null) {
            return;
        }

        final ItemStack rs = this.getStack().copy();
        if (rs.isEmpty()) {
            return;
        }

        for (int x = 0; x < maxTimesToCraft; x++) {
            if (ia.simulateAdd(rs).isEmpty()) {
                final IItemList<IAEItemStack> all = inv.getStorageList();
                final ItemStack extra = ia.addItems(this.craftItem(who, rs, inv, all));
                if (!extra.isEmpty()) {
                    final List<ItemStack> drops = new ArrayList<>();
                    drops.add(extra);
                    Platform.spawnDrops(who.world,
                            new BlockPos((int) who.getPosX(), (int) who.getPosY(), (int) who.getPosZ()), drops);
                    return;
                }
            }
        }
    }

    // TODO: This is really hacky and NEEDS to be solved with a full container/gui
    // refactoring.
    protected IRecipe<CraftingInventory> findRecipe(CraftingInventory ic, World world) {
        if (this.container instanceof CraftingTermContainer) {
            final CraftingTermContainer containerTerminal = (CraftingTermContainer) this.container;
            final IRecipe<CraftingInventory> recipe = containerTerminal.getCurrentRecipe();

            if (recipe != null && recipe.matches(ic, world)) {
                return containerTerminal.getCurrentRecipe();
            }
        }

        return world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, ic, world).orElse(null);
    }

    // TODO: This is really hacky and NEEDS to be solved with a full container/gui
    // refactoring.
    protected NonNullList<ItemStack> getRemainingItems(CraftingInventory ic, World world) {
        if (this.container instanceof CraftingTermContainer) {
            final CraftingTermContainer containerTerminal = (CraftingTermContainer) this.container;
            final IRecipe<CraftingInventory> recipe = containerTerminal.getCurrentRecipe();

            if (recipe != null && recipe.matches(ic, world)) {
                return containerTerminal.getCurrentRecipe().getRemainingItems(ic);
            }
        }

        return super.getRemainingItems(ic, world);
    }

    private int capCraftingAttempts(final int maxTimesToCraft) {
        return maxTimesToCraft;
    }

    private ItemStack craftItem(final PlayerEntity p, final ItemStack request, final IMEMonitor<IAEItemStack> inv,
            final IItemList all) {
        // update crafting matrix...
        ItemStack is = this.getStack();

        if (!is.isEmpty() && ItemStack.areItemsEqual(request, is)) {
            final ItemStack[] set = new ItemStack[this.getPattern().getSlotCount()];
            // Safeguard for empty slots in the inventory for now
            Arrays.fill(set, ItemStack.EMPTY);

            // add one of each item to the items on the board...
            World world = p.world;
            if (!world.isRemote()) {
                final CraftingInventory ic = new CraftingInventory(new ContainerNull(), 3, 3);
                for (int x = 0; x < 9; x++) {
                    ic.setInventorySlotContents(x, this.getPattern().getInvStack(x));
                }

                final IRecipe<CraftingInventory> r = this.findRecipe(ic, p.world);

                if (r == null) {
                    final Item target = request.getItem();
                    // FIXME FABRIC: This entire code-path may be pointless because RepairItemRecipe
                    // is of type CRAFTING
                    if (target.isDamageable() /* FIXME FABRIC && target.isRepairable(request) */) {
                        boolean isBad = false;
                        for (int x = 0; x < ic.getSizeInventory(); x++) {
                            final ItemStack pis = ic.getStackInSlot(x);
                            if (pis.isEmpty()) {
                                continue;
                            }
                            if (pis.getItem() != target) {
                                isBad = true;
                            }
                        }
                        if (!isBad) {
                            super.onTake(p, is);
                            // actually necessary to cleanup this case...
                            p.openContainer.onCraftMatrixChanged(new WrapperInvItemHandler(this.craftInv));
                            return request;
                        }
                    }
                    return ItemStack.EMPTY;
                }

                is = r.getCraftingResult(ic);

                if (inv != null) {
                    for (int x = 0; x < this.getPattern().getSlotCount(); x++) {
                        if (!this.getPattern().getInvStack(x).isEmpty()) {
                            set[x] = Platform.extractItemsByRecipe(this.energySrc, this.mySrc, inv, p.world, r, is, ic,
                                    this.getPattern().getInvStack(x), x, all, Actionable.MODULATE,
                                    ViewCellItem.createFilter(this.container.getViewCells()));
                            ic.setInventorySlotContents(x, set[x]);
                        }
                    }
                }
            }

            if (this.preCraft(p, inv, set, is)) {
                this.makeItem(p, is);

                this.postCraft(p, inv, set, is);
            }

            p.openContainer.onCraftMatrixChanged(new WrapperInvItemHandler(this.craftInv));

            return is;
        }

        return ItemStack.EMPTY;
    }

    private boolean preCraft(final PlayerEntity p, final IMEMonitor<IAEItemStack> inv, final ItemStack[] set,
            final ItemStack result) {
        return true;
    }

    private void makeItem(final PlayerEntity p, final ItemStack is) {
        super.onTake(p, is);
    }

    private void postCraft(final PlayerEntity p, final IMEMonitor<IAEItemStack> inv, final ItemStack[] set,
            final ItemStack result) {
        final List<ItemStack> drops = new ArrayList<>();

        // add one of each item to the items on the board...
        if (!p.getEntityWorld().isRemote()) {
            // set new items onto the crafting table...
            for (int x = 0; x < this.craftInv.getSlotCount(); x++) {
                if (this.craftInv.getInvStack(x).isEmpty()) {
                    ItemHandlerUtil.setStackInSlot(this.craftInv, x, set[x]);
                } else if (!set[x].isEmpty()) {
                    // eek! put it back!
                    final IAEItemStack fail = inv.injectItems(AEItemStack.fromItemStack(set[x]), Actionable.MODULATE,
                            this.mySrc);
                    if (fail != null) {
                        drops.add(fail.createItemStack());
                    }
                }
            }
        }

        if (drops.size() > 0) {
            Platform.spawnDrops(p.world, new BlockPos((int) p.getPosX(), (int) p.getPosY(), (int) p.getPosZ()), drops);
        }
    }

    FixedItemInv getPattern() {
        return this.pattern;
    }
}
