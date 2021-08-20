/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
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

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import appeng.api.ids.AEConstants;
import appeng.api.networking.security.IActionSource;

/**
 * Allows storage buses to request access to another ME network so it can be used as a subnetwork. This interface is
 * used in conjunction with capabilities, so when an object of this is obtained, it already knows about which face the
 * access was requested from.
 * <p/>
 * To get access to the capability for this, use @CapabilityInject with this interface as the argument to the
 * annotation.
 */
public interface IStorageMonitorableAccessor {

    BlockApiLookup<IStorageMonitorableAccessor, Direction> SIDED = BlockApiLookup.get(
            new ResourceLocation(AEConstants.MOD_ID, "storage"), IStorageMonitorableAccessor.class, Direction.class);

    /**
     * @return Null if the network cannot be accessed by the given action source (i.e. security doesn't permit it).
     */
    @Nullable
    IStorageMonitorable getInventory(IActionSource src);
}
