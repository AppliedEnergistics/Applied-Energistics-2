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


import appeng.api.AEApi;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.items.parts.ItemFacade;
import appeng.parts.CableBusStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;
import java.util.Optional;


public class FacadeContainer implements IFacadeContainer {

    private final int facades = 6;
    private final CableBusStorage storage;

    public FacadeContainer(final CableBusStorage cbs) {
        this.storage = cbs;
    }

    @Override
    public boolean addFacade(final IFacadePart a) {
        if (this.getFacade(a.getSide()) == null) {
            this.storage.setFacade(a.getSide().ordinal(), a);
            return true;
        }
        return false;
    }

    @Override
    public void removeFacade(final IPartHost host, final AEPartLocation side) {
        if (side != null && side != AEPartLocation.INTERNAL) {
            if (this.storage.getFacade(side.ordinal()) != null) {
                this.storage.setFacade(side.ordinal(), null);
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
    }

    @Override
    public void writeToNBT(final NBTTagCompound c) {
        for (int x = 0; x < this.facades; x++) {
            if (this.storage.getFacade(x) != null) {
                final NBTTagCompound data = new NBTTagCompound();
                this.storage.getFacade(x).getItemStack().writeToNBT(data);
                c.setTag("facade:" + x, data);
            }
        }
    }

    @Override
    public boolean readFromStream(final ByteBuf out) throws IOException {
        final int facadeSides = out.readByte();

        boolean changed = false;

        final int[] ids = new int[2];
        for (int x = 0; x < this.facades; x++) {
            final AEPartLocation side = AEPartLocation.fromOrdinal(x);
            final int ix = (1 << x);
            if ((facadeSides & ix) == ix) {
                ids[0] = out.readInt();
                ids[1] = out.readInt();
                ids[0] = Math.abs(ids[0]);

                Optional<Item> maybeFacadeItem = AEApi.instance().definitions().items().facade().maybeItem();
                if (maybeFacadeItem.isPresent()) {
                    final ItemFacade ifa = (ItemFacade) maybeFacadeItem.get();
                    final ItemStack facade = ifa.createFromIDs(ids);
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
    public void readFromNBT(final NBTTagCompound c) {
        for (int x = 0; x < this.facades; x++) {
            this.storage.setFacade(x, null);

            final NBTTagCompound t = c.getCompoundTag("facade:" + x);
            if (t != null) {
                final ItemStack is = new ItemStack(t);
                if (!is.isEmpty()) {
                    final Item i = is.getItem();
                    if (i instanceof IFacadeItem) {
                        this.storage.setFacade(x, ((IFacadeItem) i).createPartFromItemStack(is, AEPartLocation.fromOrdinal(x)));
                    }
                }
            }
        }
    }

    @Override
    public void writeToStream(final ByteBuf out) throws IOException {
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
                final int itemID = Item.getIdFromItem(part.getItem());
                final int dmgValue = part.getItemDamage();
                out.writeInt(itemID * (part.notAEFacade() ? -1 : 1));
                out.writeInt(dmgValue);
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
}
