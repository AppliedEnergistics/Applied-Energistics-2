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

package appeng.core.api;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AppEng;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;

public final class ItemStorageChannel implements IItemStorageChannel {

    public static final IItemStorageChannel INSTANCE = new ItemStorageChannel();

    private ItemStorageChannel() {
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return AppEng.makeId("item");
    }

    @Override
    public IItemList<IAEItemStack> createList() {
        return new ItemList();
    }

    @Override
    public IAEItemStack createStack(Object input) {
        Preconditions.checkNotNull(input);

        if (input instanceof ItemStack) {
            return AEItemStack.fromItemStack((ItemStack) input);
        }

        return null;
    }

    @Override
    public IAEItemStack createFromNBT(CompoundTag nbt) {
        Preconditions.checkNotNull(nbt);
        return AEItemStack.fromNBT(nbt);
    }

    @Override
    public IAEItemStack readFromPacket(FriendlyByteBuf input) {
        Preconditions.checkNotNull(input);

        return AEItemStack.fromPacket(input);
    }
}
