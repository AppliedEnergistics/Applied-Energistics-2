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
package appeng.tile;

import appeng.hooks.TickHandler;
import appeng.util.IWorldCallable;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * We need to defer actually unloading tile entities until the end of the tick,
 * after the chunk has been saved to disk. The CHUNK_UNLOAD event runs before
 * the chunk has been saved, and if we disconnect nodes at that point, the saved
 * data will be missing information from the node (such as the player id).
 */
class DeferredTileEntityUnloader implements IWorldCallable<Void> {

    public static void register() {
        ServerChunkEvents.CHUNK_UNLOAD.register((serverWorld, worldChunk) -> {
            List<AEBaseBlockEntity> entitiesToRemove = null;
            for (BlockEntity value : worldChunk.getBlockEntities().values()) {
                if (value instanceof AEBaseBlockEntity) {
                    if (entitiesToRemove == null) {
                        entitiesToRemove = new ArrayList<>();
                    }
                    entitiesToRemove.add((AEBaseBlockEntity) value);
                }
            }
            if (entitiesToRemove != null) {
                TickHandler.instance().addCallable(serverWorld, new DeferredTileEntityUnloader(entitiesToRemove));
            }
        });
    }

    private final List<AEBaseBlockEntity> entitiesToRemove;

    public DeferredTileEntityUnloader(List<AEBaseBlockEntity> entitiesToRemove) {
        this.entitiesToRemove = entitiesToRemove;
    }

    @Override
    public Void call(World world) {
        for (AEBaseBlockEntity blockEntity : entitiesToRemove) {
            blockEntity.onChunkUnloaded();
        }
        return null;
    }

}
