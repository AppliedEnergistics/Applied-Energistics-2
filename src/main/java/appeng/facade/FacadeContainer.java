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

package appeng.facade;

import java.util.function.Consumer;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.items.IFacadeItem;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.core.definitions.AEItems;
import appeng.items.parts.FacadeItem;
import appeng.parts.CableBusStorage;

public class FacadeContainer implements IFacadeContainer {

    private final CableBusStorage storage;
    private final Consumer<Direction> changeCallback;

    public FacadeContainer(CableBusStorage cbs, Consumer<Direction> changeCallback) {
        this.storage = cbs;
        this.changeCallback = changeCallback;
    }

    @Override
    public boolean canAddFacade(IFacadePart a) {
        return this.getFacade(a.getSide()) == null;
    }

    @Override
    public boolean addFacade(IFacadePart a) {
        if (canAddFacade(a)) {
            this.storage.setFacade(a.getSide(), a);
            this.notifyChange(a.getSide());
            return true;
        }
        return false;
    }

    @Override
    public void removeFacade(IPartHost host, Direction side) {
        if (side != null && this.storage.getFacade(side) != null) {
            this.storage.removeFacade(side);
            this.notifyChange(side);
            if (host != null) {
                host.markForUpdate();
            }
        }
    }

    @Override
    public IFacadePart getFacade(Direction side) {
        return this.storage.getFacade(side);
    }

    private String getNbtKey(Direction side) {
        return "facade:" + side.ordinal();
    }

    @Override
    public void readFromNBT(CompoundTag c) {
        for (var side : Direction.values()) {
            this.storage.removeFacade(side);

            String key = getNbtKey(side);
            if (c.contains(key, Tag.TAG_COMPOUND)) {
                var is = ItemStack.of(c.getCompound(key));
                if (!is.isEmpty()) {
                    if (is.getItem() instanceof IFacadeItem facadeItem) {
                        this.storage.setFacade(side, facadeItem.createPartFromItemStack(is, side));
                    }
                }
            }
        }
    }

    @Override
    public void writeToNBT(CompoundTag c) {
        for (var side : Direction.values()) {
            if (this.storage.getFacade(side) != null) {
                var data = new CompoundTag();
                this.storage.getFacade(side).getItemStack().save(data);
                c.put(getNbtKey(side), data);
            }
        }
    }

    @Override
    public boolean readFromStream(FriendlyByteBuf out) {
        final int facadeSides = out.readByte();

        boolean changed = false;

        for (var side : Direction.values()) {
            final int ix = 1 << side.ordinal();
            if ((facadeSides & ix) == ix) {
                final int id = out.readVarInt();

                final FacadeItem ifa = AEItems.FACADE.asItem();
                final ItemStack facade = ifa.createFromID(id);
                if (facade != null) {
                    changed = changed || this.storage.getFacade(side) == null;
                    this.storage.setFacade(side, ifa.createPartFromItemStack(facade, side));
                }
            } else {
                changed = changed || this.storage.getFacade(side) != null;
                this.storage.removeFacade(side);
            }
        }

        return changed;
    }

    @Override
    public void writeToStream(FriendlyByteBuf out) {
        int facadeSides = 0;
        for (var side : Direction.values()) {
            if (this.getFacade(side) != null) {
                facadeSides |= 1 << side.ordinal();
            }
        }
        out.writeByte((byte) facadeSides);

        for (var side : Direction.values()) {
            final IFacadePart part = this.getFacade(side);
            if (part != null) {
                final int itemID = Item.getId(part.getItem());
                out.writeVarInt(itemID);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        for (var side : Direction.values()) {
            if (this.storage.getFacade(side) != null) {
                return false;
            }
        }
        return true;
    }

    private void notifyChange(Direction side) {
        this.changeCallback.accept(side);
    }

}
