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

import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
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
        Objects.requireNonNull(data);
        Objects.requireNonNull(req);

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
    public static <T extends AEKey> long poweredExtraction(IEnergySource energy, IMEInventory<T> inv,
            T request, long amount, IActionSource src) {
        return poweredExtraction(energy, inv, request, amount, src, Actionable.MODULATE);
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
    public static <T extends AEKey> long poweredExtraction(IEnergySource energy, IMEInventory<T> inv,
            T request, long amount, IActionSource src, Actionable mode) {
        Objects.requireNonNull(energy, "energy");
        Objects.requireNonNull(inv, "inv");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(src, "src");
        Objects.requireNonNull(mode, "mode");

        var retrieved = inv.extract(request, amount, Actionable.SIMULATE, src);

        var energyFactor = Math.max(1.0, inv.getChannel().transferFactor());
        var availablePower = energy.extractAEPower(retrieved / energyFactor, Actionable.SIMULATE,
                PowerMultiplier.CONFIG);
        var itemToExtract = Math.min((long) (availablePower * energyFactor + 0.9), retrieved);

        if (itemToExtract > 0) {
            if (mode == Actionable.MODULATE) {
                energy.extractAEPower(retrieved / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
                var ret = inv.extract(request, itemToExtract, Actionable.MODULATE, src);

                if (ret != 0) {
                    src.player()
                            .ifPresent(player -> AeStats.ItemsExtracted.addToPlayer(player, (int) ret));
                }
                return ret;
            } else {
                return itemToExtract;
            }
        }

        return 0;
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
    public static <T extends AEKey> long poweredInsert(final IEnergySource energy, final IMEInventory<T> inv,
            final T input, long amount, final IActionSource src) {
        return poweredInsert(energy, inv, input, amount, src, Actionable.MODULATE);
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
    public static <T extends AEKey> long poweredInsert(IEnergySource energy, IMEInventory<T> inv, T input, long amount,
            IActionSource src, Actionable mode) {
        Objects.requireNonNull(energy);
        Objects.requireNonNull(inv);
        Objects.requireNonNull(input);
        Objects.requireNonNull(src);
        Objects.requireNonNull(mode);

        amount = inv.insert(input, amount, Actionable.SIMULATE, src);

        final double energyFactor = Math.max(1.0, inv.getChannel().transferFactor());
        final double availablePower = energy.extractAEPower(amount / energyFactor, Actionable.SIMULATE,
                PowerMultiplier.CONFIG);
        amount = Math.min((long) (availablePower * energyFactor + 0.9), amount);

        if (amount > 0) {
            if (mode == Actionable.MODULATE) {
                energy.extractAEPower(amount / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
                var inserted = inv.insert(input, amount, Actionable.MODULATE, src);

                src.player().ifPresent(player -> {
                    AeStats.ItemsInserted.addToPlayer(player, (int) inserted);
                });

                return inserted;
            } else {
                return amount;
            }
        }

        return 0;
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
        Objects.requireNonNull(service);
        Objects.requireNonNull(removedCell);
        Objects.requireNonNull(addedCell);
        Objects.requireNonNull(src);

        for (var channel : StorageChannels.getAll()) {
            postWholeCellChanges(service, channel, removedCell, addedCell, src);
        }
    }

    private static <T extends AEKey> void postWholeCellChanges(IStorageService service,
            IStorageChannel<T> channel,
            ItemStack removedCell,
            ItemStack addedCell,
            IActionSource src) {
        var myChanges = new KeyCounter<T>();

        if (!removedCell.isEmpty()) {
            var myInv = StorageCells.getCellInventory(removedCell, null, channel);
            if (myInv != null) {
                myInv.getAvailableStacks(myChanges);
                for (var is : myChanges) {
                    is.setValue(-is.getLongValue());
                }
            }
        }
        if (!addedCell.isEmpty()) {
            var myInv = StorageCells.getCellInventory(addedCell, null, channel);
            if (myInv != null) {
                myInv.getAvailableStacks(myChanges);
            }

        }
        service.postAlterationOfStoredItems(channel, myChanges.keySet(), src);
    }

    public static <T extends AEKey> void postListChanges(KeyCounter<T> before,
            KeyCounter<T> after,
            IMEMonitorListener<T> monitor,
            IActionSource source) {
        var changes = new KeyCounter<T>();
        changes.removeAll(before);
        changes.addAll(after);
        changes.removeZeros();

        if (!changes.isEmpty()) {
            monitor.postChange(null, changes.keySet(), source);
        }
    }
}
