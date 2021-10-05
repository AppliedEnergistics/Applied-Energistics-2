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

package appeng.api;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import appeng.api.client.IClientHelper;
import appeng.api.crafting.IPatternDetailsHelper;
import appeng.api.networking.IGridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartHelper;
import appeng.api.storage.IStorageHelper;

/**
 * Gameplay API for Applied Energistics 2.
 * <p/>
 * Most of the classes that are reachable via this interface can only be used once a world has been loaded.
 * <p/>
 * The AE2 API should be available as soon as your mod starts initializing.
 * <p/>
 * For registering your content with AE2, see the following classes, which are thread-safe and can be used within your
 * mods constructor:
 * <ul>
 * <li>{@link appeng.api.storage.StorageChannels}</li>
 * <li>{@link appeng.api.networking.GridServices}</li>
 * <li>{@link appeng.api.features.AEWorldGen}</li>
 * <li>{@link appeng.api.features.ChargerRegistry}</li>
 * <li>{@link appeng.api.movable.BlockEntityMoveStrategies}</li>
 * <li>{@link appeng.api.features.WirelessTerminals}</li>
 * <li>{@link appeng.api.features.GridLinkables}</li>
 * <li>{@link appeng.api.storage.StorageCells}</li>
 * <li>{@link appeng.api.features.Locatables}</li>
 * <li>{@link appeng.api.parts.PartModels}</li>
 * <li>{@link appeng.api.features.P2PTunnelAttunement}</li>
 * <li>{@link appeng.api.client.StorageCellModels}</li>
 * </ul>
 */
public final class AEApi {

    private AEApi() {
    }

    /**
     * @return A helper for working with storage data types.
     */
    @Nonnull
    public static IStorageHelper storage() {
        Preconditions.checkState(apiInitialized, "AE2 API is not initialized yet.");
        return storage;
    }

    @Nonnull
    public static IPatternDetailsHelper patterns() {
        Preconditions.checkState(apiInitialized, "AE2 API is not initialized yet.");
        return patterns;
    }

    /**
     * @return A helper to create {@link IGridNode} and other grid related objects.
     */
    @Nonnull
    public static IGridHelper grid() {
        Preconditions.checkState(apiInitialized, "AE2 API is not initialized yet.");
        return grid;
    }

    /**
     * @return A helper for working with grids, and buses.
     */
    @Nonnull
    public static IPartHelper partHelper() {
        Preconditions.checkState(apiInitialized, "AE2 API is not initialized yet.");
        return partHelper;
    }

    /**
     * @return Utility methods primarily useful for client side stuff
     */
    @Nonnull
    public static IClientHelper client() {
        Preconditions.checkState(apiInitialized, "AE2 API is not initialized yet.");
        return client;
    }

    /**
     * Run the given callback once AE2 has fully initialized, and registered all its game objects. If AE2 is already
     * initialized, the code will run immediately.
     * <p/>
     * This can be useful for addons that rely on AE2's items being available.
     */
    public static synchronized void whenModInitialized(Runnable r) {
        if (modInitialized) {
            r.run();
        } else {
            afterModRunnables.add(r);
        }
    }

    private static boolean apiInitialized;
    private static boolean modInitialized;
    private static final List<Runnable> afterModRunnables = new ArrayList<>();
    private static IStorageHelper storage;
    private static IPatternDetailsHelper patterns;
    private static IGridHelper grid;
    private static IPartHelper partHelper;
    private static IClientHelper client;

    synchronized static void initialize(
            IStorageHelper storage,
            IPatternDetailsHelper patternDetails,
            IGridHelper grid,
            IPartHelper partHelper,
            IClientHelper client) {
        Preconditions.checkState(!apiInitialized, "AE2 API was already initialized");
        AEApi.storage = storage;
        AEApi.patterns = patternDetails;
        AEApi.grid = grid;
        AEApi.partHelper = partHelper;
        AEApi.client = client;
        apiInitialized = true;
    }

    synchronized static void runAfterModInitialized() {
        Preconditions.checkState(apiInitialized, "AE2 API must be initialized");
        Preconditions.checkState(!modInitialized, "AE2 Mod was already initialized");
        modInitialized = true; // In case any runnable calls whenModInitialized() recursively
        for (var delayedRunnable : afterModRunnables) {
            delayedRunnable.run();
        }
        afterModRunnables.clear();
    }
}
