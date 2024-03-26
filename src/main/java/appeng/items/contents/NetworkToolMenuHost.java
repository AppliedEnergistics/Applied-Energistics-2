/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.items.contents;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;

import appeng.api.config.Actionable;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.items.tools.NetworkToolItem;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.util.inv.SupplierInternalInventory;

public class NetworkToolMenuHost<T extends NetworkToolItem> extends ItemMenuHost<T> {
    @Nullable
    private final IInWorldGridNodeHost host;

    private final SupplierInternalInventory<InternalInventory> supplierInv;

    public NetworkToolMenuHost(T item, Player player, ItemMenuHostLocator locator,
            @Nullable IInWorldGridNodeHost host) {
        super(item, player, locator);
        this.host = host;
        this.supplierInv = new SupplierInternalInventory<>(
                new StackDependentSupplier<>(this::getItemStack, NetworkToolItem::getInventory));
    }

    @Override
    public long insert(Player player, AEKey what, long amount, Actionable mode) {
        if (what instanceof AEItemKey itemKey) {
            var stack = itemKey.toStack(Ints.saturatedCast(amount));
            var overflow = getInventory().addItems(stack, mode.isSimulate());
            return stack.getCount() - overflow.getCount();
        }

        return 0;
    }

    @Nullable
    public IInWorldGridNodeHost getGridHost() {
        return this.host;
    }

    public InternalInventory getInventory() {
        return this.supplierInv;
    }
}
