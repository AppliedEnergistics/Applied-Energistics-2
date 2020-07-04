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

package appeng.attributes;

import alexiil.mc.lib.attributes.Attribute;
import alexiil.mc.lib.attributes.Attributes;
import alexiil.mc.lib.attributes.SearchOptions;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.parts.AEBasePart;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Utility class that holds various attributes, both by AE2 and other Mods.
 */
public final class MEAttributes {

    private MEAttributes() {
    }

    public static Attribute<IStorageMonitorableAccessor> STORAGE_MONITORABLE_ACCESSOR
            = Attributes.createDefaulted(IStorageMonitorableAccessor.class, new NullMENetworkAccessor());

    public static <T> T getFirstAttributeOnSide(Attribute<T> attribute, BlockEntity be, Direction side) {
        World world = be.getWorld();
        if (world == null) {
            return null;
        }

        return attribute.getFirstOrNull(world, be.getPos().offset(side), SearchOptions.inDirection(side.getOpposite()));
    }

    // Convenience function to get an attribute of the block that is in front of a part
    public static <T> T getAttributeInFrontOfPart(Attribute<T> attribute, AEBasePart part) {
        BlockEntity self = part.getHost().getTile();
        Direction direction = part.getSide().getFacing();
        final World w = self.getWorld();
        BlockPos neighborPos = self.getPos().offset(direction);
        // Do not force-load a neighboring chunk for this.
        ChunkPos chunkPos = new ChunkPos(neighborPos);
        if (!w.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) {
            return null;
        }
        return attribute.getFirstOrNull(w, neighborPos, SearchOptions.inDirection(direction));
    }

}
