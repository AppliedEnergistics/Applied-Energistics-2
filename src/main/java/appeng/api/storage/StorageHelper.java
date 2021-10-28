/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.storage;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.core.stats.AeStats;
import appeng.crafting.CraftingLink;

public final class StorageHelper {
    private StorageHelper() {
    }

    /**
     * load a crafting link from nbt data.
     *
     * @param data to be loaded data
     * @return crafting link
     */
    public static ICraftingLink loadCraftingLink(CompoundTag data, ICraftingRequester req) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(req);

        return new CraftingLink(data, req);
    }

    /**
     * Extracts items from a {@link IMEInventory} respecting power requirements.
     *
     * @param energy  Energy source.
     * @param inv     Inventory to extract from.
     * @param request Requested item and count.
     * @param src     Action source.
     * @return extracted items or {@code null} of nothing was extracted.
     */
    public static <T extends IAEStack> T poweredExtraction(final IEnergySource energy, final IMEInventory<T> inv,
            final T request, final IActionSource src) {
        return poweredExtraction(energy, inv, request, src, Actionable.MODULATE);
    }

    /**
     * Extracts items from a {@link IMEInventory} respecting power requirements.
     *
     * @param energy  Energy source.
     * @param inv     Inventory to extract from.
     * @param request Requested item and count.
     * @param src     Action source.
     * @param mode    Simulate or modulate
     * @return extracted items or {@code null} of nothing was extracted.
     */
    public static <T extends IAEStack> T poweredExtraction(final IEnergySource energy, final IMEInventory<T> inv,
            final T request, final IActionSource src, final Actionable mode) {
        Preconditions.checkNotNull(energy);
        Preconditions.checkNotNull(inv);
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(src);
        Preconditions.checkNotNull(mode);

        final T possible = inv.extractItems(IAEStack.copy(request), Actionable.SIMULATE, src);

        long retrieved = 0;
        if (possible != null) {
            retrieved = possible.getStackSize();
        }

        final double energyFactor = Math.max(1.0, inv.getChannel().transferFactor());
        final double availablePower = energy.extractAEPower(retrieved / energyFactor, Actionable.SIMULATE,
                PowerMultiplier.CONFIG);
        final long itemToExtract = Math.min((long) (availablePower * energyFactor + 0.9), retrieved);

        if (itemToExtract > 0) {
            if (mode == Actionable.MODULATE) {
                energy.extractAEPower(retrieved / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
                possible.setStackSize(itemToExtract);
                final T ret = inv.extractItems(possible, Actionable.MODULATE, src);

                if (ret != null) {
                    src.player()
                            .ifPresent(player -> AeStats.ItemsExtracted.addToPlayer(player, (int) ret.getStackSize()));
                }
                return ret;
            } else {
                possible.setStackSize(itemToExtract);
                return possible;
            }
        }

        return null;
    }

    /**
     * Inserts items into a {@link IMEInventory} respecting power requirements.
     *
     * @param energy Energy source.
     * @param inv    Inventory to insert into.
     * @param input  Items to insert.
     * @param src    Action source.
     * @return items not inserted or {@code null} if everything was inserted.
     */
    public static <T extends IAEStack> T poweredInsert(final IEnergySource energy, final IMEInventory<T> inv,
            final T input, final IActionSource src) {
        return poweredInsert(energy, inv, input, src, Actionable.MODULATE);
    }

    /**
     * Inserts items into a {@link IMEInventory} respecting power requirements.
     *
     * @param energy Energy source.
     * @param inv    Inventory to insert into.
     * @param input  Items to insert.
     * @param src    Action source.
     * @param mode   Simulate or modulate
     * @return items not inserted or {@code null} if everything was inserted.
     */
    public static <T extends IAEStack> T poweredInsert(IEnergySource energy, IMEInventory<T> inv, T input,
            IActionSource src, Actionable mode) {
        Preconditions.checkNotNull(energy);
        Preconditions.checkNotNull(inv);
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(src);
        Preconditions.checkNotNull(mode);

        final T overflow = inv.injectItems(IAEStack.copy(input), Actionable.SIMULATE, src);

        long transferAmount = input.getStackSize();
        if (overflow != null) {
            transferAmount -= overflow.getStackSize();
        }

        final double energyFactor = Math.max(1.0, inv.getChannel().transferFactor());
        final double availablePower = energy.extractAEPower(transferAmount / energyFactor, Actionable.SIMULATE,
                PowerMultiplier.CONFIG);
        final long itemToAdd = Math.min((long) (availablePower * energyFactor + 0.9), transferAmount);

        if (itemToAdd > 0) {
            if (mode == Actionable.MODULATE) {
                energy.extractAEPower(transferAmount / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
                if (itemToAdd < input.getStackSize()) {
                    final long original = input.getStackSize();
                    final T leftover = IAEStack.copy(input);
                    final T split = IAEStack.copy(input);

                    leftover.decStackSize(itemToAdd);
                    split.setStackSize(itemToAdd);
                    IAEStack.add(leftover, inv.injectItems(split, Actionable.MODULATE, src));

                    src.player().ifPresent(player -> {
                        final long diff = original - leftover.getStackSize();
                        AeStats.ItemsInserted.addToPlayer(player, (int) diff);
                    });

                    return leftover;
                }

                final T ret = inv.injectItems(input, Actionable.MODULATE, src);

                src.player().ifPresent(player -> {
                    final long diff = ret == null ? input.getStackSize() : input.getStackSize() - ret.getStackSize();
                    AeStats.ItemsInserted.addToPlayer(player, (int) diff);
                });

                return ret;
            } else {
                var ret = IAEStack.copy(input, input.getStackSize() - itemToAdd);
                return ret != null && ret.getStackSize() > 0 ? ret : null;
            }
        }

        return input;
    }

    /**
     * A utility function to notify the {@link IStorageService} of grid inventory changes that result from storage cells
     * being removed or added to the grid.
     *
     * @param service     the storage service to notify
     * @param removedCell the removed cell. May be {@link ItemStack#EMPTY} if no cell was removed.
     * @param addedCell   the added cell. May be {@link ItemStack#EMPTY} if no cell was added.
     * @param src         the action source
     */
    public static void postWholeCellChanges(IStorageService service,
            ItemStack removedCell,
            ItemStack addedCell,
            IActionSource src) {
        Preconditions.checkNotNull(service);
        Preconditions.checkNotNull(removedCell);
        Preconditions.checkNotNull(addedCell);
        Preconditions.checkNotNull(src);

        for (var channel : StorageChannels.getAll()) {
            postWholeCellChanges(service, channel, removedCell, addedCell, src);
        }
    }

    private static <T extends IAEStack> void postWholeCellChanges(IStorageService service,
            IStorageChannel<T> channel,
            ItemStack removedCell,
            ItemStack addedCell,
            IActionSource src) {
        var myChanges = channel.createList();

        if (!removedCell.isEmpty()) {
            var myInv = StorageCells.getCellInventory(removedCell, null, channel);
            if (myInv != null) {
                myInv.getAvailableStacks(myChanges);
                for (var is : myChanges) {
                    is.setStackSize(-is.getStackSize());
                }
            }
        }
        if (!addedCell.isEmpty()) {
            var myInv = StorageCells.getCellInventory(addedCell, null, channel);
            if (myInv != null) {
                myInv.getAvailableStacks(myChanges);
            }

        }
        service.postAlterationOfStoredItems(channel, myChanges, src);
    }

    public static <T extends IAEStack> void postListChanges(final IAEStackList<T> before,
            final IAEStackList<T> after,
            final IMEMonitorListener<T> monitor,
            final IActionSource source) {
        final List<T> changes = new ArrayList<>();

        for (final T is : before) {
            is.setStackSize(-is.getStackSize());
        }

        for (final T is : after) {
            before.add(is);
        }

        for (final T is : before) {
            if (is.getStackSize() != 0) {
                changes.add(is);
            }
        }

        if (!changes.isEmpty()) {
            monitor.postChange(null, changes, source);
        }
    }
}
