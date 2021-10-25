/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.crafting.execution;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;

public class GenericStackHelper {
    public static CompoundTag writeGenericStack(IAEStack stack) {
        if (stack == null) {
            return new CompoundTag();
        }
        var tag = new CompoundTag();
        tag.putString("chan", stack.getChannel().getId().toString());
        stack.writeToNBT(tag);
        return tag;
    }

    public static IAEStack readGenericStack(CompoundTag tag) {
        if (tag.isEmpty()) {
            return null;
        }
        // TODO: what if a channel gets removed?
        var channel = StorageChannels.get(new ResourceLocation(tag.getString("chan")));
        return channel.createFromNBT(tag);
    }

    public static void writeGenericStack(FriendlyByteBuf buf, @Nullable IAEStack stack) {
        buf.writeBoolean(stack == null);
        if (stack != null) {
            buf.writeResourceLocation(stack.getChannel().getId());
            stack.writeToPacket(buf);
        }
    }

    @Nullable
    public static IAEStack readGenericStack(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            return null;
        }
        var channel = StorageChannels.get(buf.readResourceLocation());
        return channel.readFromPacket(buf);
    }

    @Nullable
    public static IAEStack injectMonitorable(IStorageMonitorable monitorable, IAEStack what, Actionable mode,
            IActionSource src) {
        if (what == null) {
            return null;
        }
        return channelInjectItems(what.getChannel(), monitorable, what, mode, src);
    }

    private static <T extends IAEStack> T channelInjectItems(IStorageChannel<T> channel,
            IStorageMonitorable monitorable, IAEStack what, Actionable type, IActionSource src) {
        var castedWhat = what.cast(channel);
        var inventory = monitorable.getInventory(channel);
        if (inventory != null) {
            return inventory.injectItems(castedWhat, type, src);
        }
        return null;
    }

    @Nullable
    public static IAEStack extractMonitorable(IStorageMonitorable monitorable, IAEStack what, Actionable mode,
            IActionSource src) {
        if (what == null) {
            return null;
        }
        return channelExtractItems(what.getChannel(), monitorable, what, mode, src);
    }

    private static <T extends IAEStack> T channelExtractItems(IStorageChannel<T> channel,
            IStorageMonitorable monitorable, IAEStack what, Actionable type, IActionSource src) {
        var castedWhat = what.cast(channel);
        var inventory = monitorable.getInventory(channel);
        if (inventory != null) {
            return inventory.extractItems(castedWhat, type, src);
        }
        return null;
    }
}
