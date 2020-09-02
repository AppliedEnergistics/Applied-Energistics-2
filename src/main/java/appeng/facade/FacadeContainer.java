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

import java.io.IOException;
import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.core.Api;
import appeng.items.parts.FacadeItem;
import appeng.parts.CableBusStorage;

public class FacadeContainer implements IFacadeContainer {

    private final int facades = 6;
    private final CableBusStorage storage;
    private final Runnable changeCallback;

    public FacadeContainer(final CableBusStorage cbs, Runnable changeCallback) {
        this.storage = cbs;
        this.changeCallback = changeCallback;
    }

    @Override
    public boolean addFacade(final IFacadePart a) {
        if (this.getFacade(a.getSide()) == null) {
            this.storage.setFacade(a.getSide().ordinal(), a);
            this.notifyChange();
            return true;
        }
        return false;
    }

    @Override
    public void removeFacade(final IPartHost host, final AEPartLocation side) {
        if (side != null && side != AEPartLocation.INTERNAL) {
            if (this.storage.getFacade(side.ordinal()) != null) {
                this.storage.setFacade(side.ordinal(), null);
                this.notifyChange();
                if (host != null) {
                    host.markForUpdate();
                }
            }
        }
    }

    @Override
    public IFacadePart getFacade(final AEPartLocation s) {
        return this.storage.getFacade(s.ordinal());
    }

    @Override
    public void rotateLeft() {
        final IFacadePart[] newFacades = new FacadePart[6];

        newFacades[AEPartLocation.UP.ordinal()] = this.storage.getFacade(AEPartLocation.UP.ordinal());
        newFacades[AEPartLocation.DOWN.ordinal()] = this.storage.getFacade(AEPartLocation.DOWN.ordinal());

        newFacades[AEPartLocation.EAST.ordinal()] = this.storage.getFacade(AEPartLocation.NORTH.ordinal());
        newFacades[AEPartLocation.SOUTH.ordinal()] = this.storage.getFacade(AEPartLocation.EAST.ordinal());

        newFacades[AEPartLocation.WEST.ordinal()] = this.storage.getFacade(AEPartLocation.SOUTH.ordinal());
        newFacades[AEPartLocation.NORTH.ordinal()] = this.storage.getFacade(AEPartLocation.WEST.ordinal());

        for (int x = 0; x < this.facades; x++) {
            this.storage.setFacade(x, newFacades[x]);
        }
        this.notifyChange();
    }

    @Override
    public void writeToNBT(final CompoundTag c) {
        for (int x = 0; x < this.facades; x++) {
            if (this.storage.getFacade(x) != null) {
                final CompoundTag data = new CompoundTag();
                this.storage.getFacade(x).getItemStack().toTag(data);
                c.put("facade:" + x, data);
            }
        }
    }

    @Override
    public boolean readFromStream(final PacketByteBuf out) throws IOException {
        final int facadeSides = out.readByte();

        boolean changed = false;

        for (int x = 0; x < this.facades; x++) {
            final AEPartLocation side = AEPartLocation.fromOrdinal(x);
            final int ix = (1 << x);
            if ((facadeSides & ix) == ix) {
                final int id = Math.abs(out.readInt());

                Optional<net.minecraft.item.Item> maybeFacadeItem = Api.instance().definitions().items().facade()
                        .maybeItem();
                if (maybeFacadeItem.isPresent()) {
                    final FacadeItem ifa = (FacadeItem) maybeFacadeItem.get();
                    final ItemStack facade = ifa.createFromID(id);
                    if (facade != null) {
                        changed = changed || this.storage.getFacade(x) == null;
                        this.storage.setFacade(x, ifa.createPartFromItemStack(facade, side));
                    }
                }
            } else {
                changed = changed || this.storage.getFacade(x) != null;
                this.storage.setFacade(x, null);
            }
        }

        return changed;
    }

    @Override
    public void readFromNBT(final CompoundTag c) {
        for (int x = 0; x < this.facades; x++) {
            this.storage.setFacade(x, null);

            final CompoundTag t = c.getCompound("facade:" + x);
            if (t != null) {
                final ItemStack is = ItemStack.fromTag(t);
                if (!is.isEmpty()) {
                    final net.minecraft.item.Item i = is.getItem();
                    if (i instanceof IFacadeItem) {
                        this.storage.setFacade(x,
                                ((IFacadeItem) i).createPartFromItemStack(is, AEPartLocation.fromOrdinal(x)));
                    }
                }
            }
        }
    }

    @Override
    public void writeToStream(final PacketByteBuf out) throws IOException {
        int facadeSides = 0;
        for (int x = 0; x < this.facades; x++) {
            if (this.getFacade(AEPartLocation.fromOrdinal(x)) != null) {
                facadeSides |= (1 << x);
            }
        }
        out.writeByte((byte) facadeSides);

        for (int x = 0; x < this.facades; x++) {
            final IFacadePart part = this.getFacade(AEPartLocation.fromOrdinal(x));
            if (part != null) {
                final int itemID = Item.getRawId(part.getItem());
                out.writeInt(itemID * (part.notAEFacade() ? -1 : 1));
            }
        }
    }

    @Override
    public boolean isEmpty() {
        for (int x = 0; x < this.facades; x++) {
            if (this.storage.getFacade(x) != null) {
                return false;
            }
        }
        return true;
    }

    private void notifyChange() {
        this.changeCallback.run();
    }

}
