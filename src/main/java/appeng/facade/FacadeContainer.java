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

import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.parts.CableBusStorage;

public class FacadeContainer implements IFacadeContainer {
    private static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE_STREAM_CODEC = ByteBufCodecs
            .idMapper(Block.BLOCK_STATE_REGISTRY);

    /**
     * Key names to store facades
     */
    private static final String[] NBT_KEY_NAMES = Arrays.stream(Direction.values())
            .map(d -> "facade" + StringUtils.capitalize(d.getSerializedName()))
            .toArray(String[]::new);

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

    @Override
    public void readFromNBT(CompoundTag c, HolderLookup.Provider registries) {
        for (var side : Direction.values()) {
            this.storage.removeFacade(side);

            var tag = c.get(NBT_KEY_NAMES[side.ordinal()]);
            var result = BlockState.CODEC.decode(NbtOps.INSTANCE, tag).result();
            if (result.isPresent()) {
                var blockState = result.get().getFirst();
                this.storage.setFacade(side, new FacadePart(blockState, side));
            }
        }
    }

    @Override
    public void writeToNBT(CompoundTag c, HolderLookup.Provider registries) {
        for (var side : Direction.values()) {
            if (this.storage.getFacade(side) != null) {
                var data = BlockState.CODEC.encodeStart(NbtOps.INSTANCE, this.storage.getFacade(side).getBlockState())
                        .getOrThrow();
                c.put(NBT_KEY_NAMES[side.ordinal()], data);
            }
        }
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf out) {
        final int facadeSides = out.readByte();

        boolean changed = false;

        for (var side : Direction.values()) {
            final int ix = 1 << side.ordinal();
            if ((facadeSides & ix) == ix) {
                var facade = BLOCK_STATE_STREAM_CODEC.decode(out);
                changed = changed || this.storage.getFacade(side) == null;
                this.storage.setFacade(side, new FacadePart(facade, side));
            } else {
                changed = changed || this.storage.getFacade(side) != null;
                this.storage.removeFacade(side);
            }
        }

        return changed;
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf out) {
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
                BLOCK_STATE_STREAM_CODEC.encode(out, part.getBlockState());
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
