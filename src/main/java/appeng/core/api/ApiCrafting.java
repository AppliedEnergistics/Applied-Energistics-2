/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2020, TeamAppliedEnergistics, All rights reserved.
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

import com.google.common.base.Preconditions;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.api.crafting.ICraftingHelper;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.helpers.PatternHelper;

public class ApiCrafting implements ICraftingHelper {

    // // Cache is disabled for now.
    // final private Map<ItemStack, PatternHelper> patternCache;

    public ApiCrafting() {
        // this.patternCache = new IdentityHashMap<>();
    }

    @Override
    public ICraftingPatternDetails getPattern(final ItemStack is, final World world) {
        if (is == null || world == null) {
            return null;
        }
        Preconditions.checkArgument(is.getItem() instanceof ICraftingPatternItem,
                "Item needs to implement ICraftingPatternItem");

        // We use the shared itemstack for an identity lookup.
        IAEItemStack ais = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(is);
        // final ItemStack sharedStack = ais.getDefinition();

        // return this.patternCache.computeIfAbsent(sharedStack, key -> {
        try {
            return new PatternHelper(ais, world);
        } catch (final Throwable t) {
            return null;
        }
        // });
    }
}
