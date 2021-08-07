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

package appeng.core.registries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import appeng.api.movable.IMovableBlockEntity;
import appeng.api.movable.IMovableHandler;
import appeng.api.movable.IMovableRegistry;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.spatial.DefaultSpatialHandler;

public class MovableRegistry implements IMovableRegistry {

    private static final ResourceLocation TAG_WHITELIST = new ResourceLocation(AppEng.MOD_ID, "spatial/whitelist");
    private static final ResourceLocation TAG_BLACKLIST = new ResourceLocation(AppEng.MOD_ID, "spatial/blacklist");

    private final Set<Block> blacklisted = new HashSet<>();

    private final Map<BlockEntityType<?>, IMovableHandler> valid = new IdentityHashMap<>();
    private final Set<BlockEntityType<?>> whitelistedTypes = new HashSet<>();
    private final List<IMovableHandler> handlers = new ArrayList<>();
    private final DefaultSpatialHandler dsh = new DefaultSpatialHandler();
    private final IMovableHandler nullHandler = new DefaultSpatialHandler();
    private final Tag.Named<Block> blockTagWhiteList;
    private final Tag.Named<Block> blockTagBlackList;

    public MovableRegistry() {
        this.blockTagWhiteList = BlockTags.createOptional(TAG_WHITELIST);
        this.blockTagBlackList = BlockTags.createOptional(TAG_BLACKLIST);
    }

    @Override
    public void blacklistBlock(final Block blk) {
        this.blacklisted.add(blk);
    }

    @Override
    public void whitelistBlockEntity(final BlockEntityType<?> c) {
        this.whitelistedTypes.add(c);
    }

    @Override
    public boolean askToMove(final BlockEntity te) {
        var type = te.getType();
        IMovableHandler canMove = this.valid.get(type);

        if (canMove == null) {
            canMove = this.resolveHandler(te);
        }

        if (canMove != this.nullHandler) {
            if (te instanceof IMovableBlockEntity) {
                ((IMovableBlockEntity) te).prepareToMove();
            }

            te.setRemoved();
            return true;
        }

        return false;
    }

    private IMovableHandler resolveHandler(BlockEntity te) {
        IMovableHandler handler = null;

        // ask handlers...
        for (final IMovableHandler han : this.handlers) {
            if (han.canHandle(te.getType())) {
                handler = han;
                break;
            }
        }

        // if you have a handler your opted in
        if (handler != null) {
            this.valid.put(te.getType(), handler);
            return handler;
        }

        // if your movable our opted in
        if (te instanceof IMovableBlockEntity) {
            this.valid.put(te.getType(), this.dsh);
            return this.dsh;
        }

        // if the block itself is via block tags
        if (AEConfig.instance().getSpatialBlockTags()
                && this.blockTagWhiteList.contains(te.getBlockState().getBlock())) {
            this.valid.put(te.getType(), this.dsh);
            return this.dsh;
        }

        // if you are on the white list your opted in.
        // We do this now and not at the time of whitelisting, because whitelists should not overwrite the
        // ability to register a custom handler
        if (this.whitelistedTypes.contains(te.getType())) {
            this.valid.put(te.getType(), this.dsh);
            return this.dsh;
        }

        this.valid.put(te.getType(), this.nullHandler);
        return this.nullHandler;
    }

    @Override
    public void doneMoving(final BlockEntity te) {
        if (te instanceof IMovableBlockEntity mt) {
            mt.doneMoving();
        }
    }

    @Override
    public void addHandler(final IMovableHandler han) {
        this.handlers.add(han);
    }

    @Override
    public IMovableHandler getHandler(BlockEntity te) {
        final IMovableHandler h = this.valid.get(te.getType());
        return h == null ? this.dsh : h;
    }

    @Override
    public IMovableHandler getDefaultHandler() {
        return this.dsh;
    }

    @Override
    public boolean isBlacklisted(final Block blk) {
        return this.blacklisted.contains(blk) || this.blockTagBlackList.contains(blk);
    }
}
