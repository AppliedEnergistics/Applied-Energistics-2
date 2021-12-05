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

package appeng.util.item;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class TestItemWithCaps extends net.minecraft.world.item.Item {
    public TestItemWithCaps() {
        super(new net.minecraft.world.item.Item.Properties());
        setRegistryName("ae2:test_item");
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt == null) {
            return null;
        } else {
            return new CapabilityProvider();
        }
    }

    /**
     * Simple capability provider that just has a single counter value to produce different NBT.
     */
    public static class CapabilityProvider implements ICapabilityProvider, INBTSerializable<IntTag> {
        private int counter;

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            return LazyOptional.empty();
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap) {
            return LazyOptional.empty();
        }

        @Override
        public IntTag serializeNBT() {
            return IntTag.valueOf(counter);
        }

        @Override
        public void deserializeNBT(IntTag nbt) {
            counter = nbt.getAsInt();
        }
    }
}
