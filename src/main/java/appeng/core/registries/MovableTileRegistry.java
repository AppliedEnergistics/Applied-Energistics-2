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

import net.minecraft.world.level.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;

import appeng.api.exceptions.AppEngException;
import appeng.api.movable.IMovableHandler;
import appeng.api.movable.IMovableRegistry;
import appeng.api.movable.IMovableTile;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.spatial.DefaultSpatialHandler;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MovableTileRegistry implements IMovableRegistry {

    private static final ResourceLocation TAG_WHITELIST = new ResourceLocation(AppEng.MOD_ID, "spatial/whitelist");
    private static final ResourceLocation TAG_BLACKLIST = new ResourceLocation(AppEng.MOD_ID, "spatial/blacklist");

    private final Set<Block> blacklisted = new HashSet<>();

    private final Map<Class<? extends BlockEntity>, IMovableHandler> valid = new IdentityHashMap<>();
    private final List<Class<? extends BlockEntity>> test = new ArrayList<>();
    private final List<IMovableHandler> handlers = new ArrayList<>();
    private final DefaultSpatialHandler dsh = new DefaultSpatialHandler();
    private final IMovableHandler nullHandler = new DefaultSpatialHandler();
    private final Named<Block> blockTagWhiteList;
    private final Named<Block> blockTagBlackList;

    public MovableTileRegistry() {
        this.blockTagWhiteList = BlockTags.createOptional(TAG_WHITELIST);
        this.blockTagBlackList = BlockTags.createOptional(TAG_BLACKLIST);
    }

    @Override
    public void blacklistBlock(final Block blk) {
        this.blacklisted.add(blk);
    }

    @Override
    public void whiteListTileEntity(final Class<? extends BlockEntity> c) {
        if (c.getName().equals(BlockEntity.class.getName())) {
            throw new IllegalArgumentException(new AppEngException("Someone tried to make all tiles movable with " + c
                    + ", this is a clear violation of the purpose of the white list."));
        }

        this.test.add(c);
    }

    @Override
    public boolean askToMove(final BlockEntity te) {
        final Class<? extends BlockEntity> myClass = te.getClass();
        IMovableHandler canMove = this.valid.get(myClass);

        if (canMove == null) {
            canMove = this.testClass(myClass, te);
        }

        if (canMove != this.nullHandler) {
            if (te instanceof IMovableTile) {
                ((IMovableTile) te).prepareToMove();
            }

            te.setRemoved();
            return true;
        }

        return false;
    }

    private IMovableHandler testClass(final Class<? extends BlockEntity> myClass, final BlockEntity te) {
        IMovableHandler handler = null;

        // ask handlers...
        for (final IMovableHandler han : this.handlers) {
            if (han.canHandle(myClass, te)) {
                handler = han;
                break;
            }
        }

        // if you have a handler your opted in
        if (handler != null) {
            this.valid.put(myClass, handler);
            return handler;
        }

        // if your movable our opted in
        if (te instanceof IMovableTile) {
            this.valid.put(myClass, this.dsh);
            return this.dsh;
        }

        // if the block itself is via block tags
        if (AEConfig.instance().getSpatialBlockTags()
                && this.blockTagWhiteList.contains(te.getBlockState().getBlock())) {
            this.valid.put(myClass, this.dsh);
            return this.dsh;
        }

        // if you are on the white list your opted in.
        for (final Class<? extends BlockEntity> testClass : this.test) {
            if (testClass.isAssignableFrom(myClass)) {
                this.valid.put(myClass, this.dsh);
                return this.dsh;
            }
        }

        this.valid.put(myClass, this.nullHandler);
        return this.nullHandler;
    }

    @Override
    public void doneMoving(final BlockEntity te) {
        if (te instanceof IMovableTile) {
            final IMovableTile mt = (IMovableTile) te;
            mt.doneMoving();
        }
    }

    @Override
    public void addHandler(final IMovableHandler han) {
        this.handlers.add(han);
    }

    @Override
    public IMovableHandler getHandler(final BlockEntity te) {
        final Class<? extends BlockEntity> myClass = te.getClass();
        final IMovableHandler h = this.valid.get(myClass);
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
