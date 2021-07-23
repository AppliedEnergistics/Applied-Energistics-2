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

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.container.me.items.CraftingTermContainer;
import appeng.core.Api;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ViewCellItem;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.inv.WrapperInvItemHandler;
import appeng.util.item.AEItemStack;

public class CraftingTermSlot extends AppEngCraftingSlot {

    private final IItemHandler craftInv;
    private final IItemHandler pattern;

    private final IActionSource mySrc;
    private final IEnergySource energySrc;
    private final IStorageMonitorable storage;
    private final IContainerCraftingPacket container;

    public CraftingTermSlot(final Player player, final IActionSource mySrc, final IEnergySource energySrc,
                            final IStorageMonitorable storage, final IItemHandler cMatrix, final IItemHandler secondMatrix,
                            final IContainerCraftingPacket ccp) {
        super(player, cMatrix);
        this.energySrc = energySrc;
        this.storage = storage;
        this.mySrc = mySrc;
        this.pattern = cMatrix;
        this.craftInv = secondMatrix;
        this.container = ccp;
    }

    public IItemHandler getCraftingMatrix() {
        return this.craftInv;
    }

    @Override
    public boolean mayPickup(final Player player) {
        return false;
    }

    @Override
    public ItemStack onTake(final Player p, final ItemStack is) {
        return is;
    }

    public void doClick(final InventoryAction action, final Player who) {
        if (this.getItem().isEmpty()) {
            return;
        }
        if (isRemote()) {
            return;
        }

        final IMEMonitor<IAEItemStack> inv = this.storage
                .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
        final int howManyPerCraft = this.getItem().getCount();
        int maxTimesToCraft = 0;

        InventoryAdaptor ia = null;
        if (action == InventoryAction.CRAFT_SHIFT) // craft into player inventory...
        {
            ia = InventoryAdaptor.getAdaptor(who);
            maxTimesToCraft = (int) Math.floor((double) this.getItem().getMaxStackSize() / (double) howManyPerCraft);
        } else if (action == InventoryAction.CRAFT_STACK) // craft into hand, full stack
        {
            ia = new AdaptorItemHandler(new WrapperCursorItemHandler(who.inventory));
            maxTimesToCraft = (int) Math.floor((double) this.getItem().getMaxStackSize() / (double) howManyPerCraft);
        } else
        // pick up what was crafted...
        {
            ia = new AdaptorItemHandler(new WrapperCursorItemHandler(who.inventory));
            maxTimesToCraft = 1;
        }

        maxTimesToCraft = this.capCraftingAttempts(maxTimesToCraft);

        if (ia == null) {
            return;
        }

        final ItemStack rs = this.getItem().copy();
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
                    Platform.spawnDrops(who.level,
                            new BlockPos((int) who.getX(), (int) who.getY(), (int) who.getZ()), drops);
                    return;
                }
            }
        }
    }

    // TODO: This is really hacky and NEEDS to be solved with a full container/gui
    // refactoring.
    protected Recipe<CraftingContainer> findRecipe(CraftingContainer ic, Level world) {
        if (this.container instanceof CraftingTermContainer) {
            final CraftingTermContainer containerTerminal = (CraftingTermContainer) this.container;
            final Recipe<CraftingContainer> recipe = containerTerminal.getCurrentRecipe();

            if (recipe != null && recipe.matches(ic, world)) {
                return containerTerminal.getCurrentRecipe();
            }
        }

        return world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, ic, world).orElse(null);
    }

    // TODO: This is really hacky and NEEDS to be solved with a full container/gui
    // refactoring.
    @Override
    protected NonNullList<ItemStack> getRemainingItems(CraftingContainer ic, Level world) {
        if (this.container instanceof CraftingTermContainer) {
            final CraftingTermContainer containerTerminal = (CraftingTermContainer) this.container;
            final Recipe<CraftingContainer> recipe = containerTerminal.getCurrentRecipe();

            if (recipe != null && recipe.matches(ic, world)) {
                return containerTerminal.getCurrentRecipe().getRemainingItems(ic);
            }
        }

        return super.getRemainingItems(ic, world);
    }

    private int capCraftingAttempts(final int maxTimesToCraft) {
        return maxTimesToCraft;
    }

    private ItemStack craftItem(final Player p, final ItemStack request, final IMEMonitor<IAEItemStack> inv,
                                                         final IItemList all) {
        // update crafting matrix...
        ItemStack is = this.getItem();

        if (!is.isEmpty() && ItemStack.isSame(request, is)) {
            final ItemStack[] set = new ItemStack[this.getPattern().getSlots()];
            // Safeguard for empty slots in the inventory for now
            Arrays.fill(set, ItemStack.EMPTY);

            // add one of each item to the items on the board...
            Level world = p.level;
            if (!world.isClientSide()) {
                final CraftingContainer ic = new CraftingContainer(new ContainerNull(), 3, 3);
                for (int x = 0; x < 9; x++) {
                    ic.setItem(x, this.getPattern().getStackInSlot(x));
                }

                final Recipe<CraftingContainer> r = this.findRecipe(ic, world);

                if (r == null) {
                    final Item target = request.getItem();
                    if (target.canBeDepleted() && target.isRepairable(request)) {
                        boolean isBad = false;
                        for (int x = 0; x < ic.getContainerSize(); x++) {
                            final ItemStack pis = ic.getItem(x);
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
                            p.containerMenu.slotsChanged(new WrapperInvItemHandler(this.craftInv));
                            return request;
                        }
                    }
                    return ItemStack.EMPTY;
                }

                is = r.assemble(ic);

                if (inv != null) {
                    for (int x = 0; x < this.getPattern().getSlots(); x++) {
                        if (!this.getPattern().getStackInSlot(x).isEmpty()) {
                            set[x] = Platform.extractItemsByRecipe(this.energySrc, this.mySrc, inv, world, r, is, ic,
                                    this.getPattern().getStackInSlot(x), x, all, Actionable.MODULATE,
                                    ViewCellItem.createFilter(this.container.getViewCells()));
                            ic.setItem(x, set[x]);
                        }
                    }
                }
            }

            if (this.preCraft(p, inv, set, is)) {
                this.makeItem(p, is);

                this.postCraft(p, inv, set, is);
            }

            p.containerMenu.slotsChanged(new WrapperInvItemHandler(this.craftInv));

            return is;
        }

        return ItemStack.EMPTY;
    }

    private boolean preCraft(final Player p, final IMEMonitor<IAEItemStack> inv, final ItemStack[] set,
                             final ItemStack result) {
        return true;
    }

    private void makeItem(final Player p, final ItemStack is) {
        super.onTake(p, is);
    }

    private void postCraft(final Player p, final IMEMonitor<IAEItemStack> inv, final ItemStack[] set,
                           final ItemStack result) {
        final List<ItemStack> drops = new ArrayList<>();

        // add one of each item to the items on the board...
        if (!p.getCommandSenderWorld().isClientSide()) {
            // set new items onto the crafting table...
            for (int x = 0; x < this.craftInv.getSlots(); x++) {
                if (this.craftInv.getStackInSlot(x).isEmpty()) {
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
            Platform.spawnDrops(p.level, new BlockPos((int) p.getX(), (int) p.getY(), (int) p.getZ()), drops);
        }
    }

    IItemHandler getPattern() {
        return this.pattern;
    }
}
