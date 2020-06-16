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

package appeng.debug;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.server.ServerWorld;

import appeng.items.AEBaseItem;
import appeng.worldgen.MeteoritePlacer;
import appeng.worldgen.MeteoriteSpawner;
import appeng.worldgen.PlacedMeteoriteSettings;
import appeng.worldgen.meteorite.StandardWorld;

public class ToolMeteoritePlacer extends AEBaseItem {

    public ToolMeteoritePlacer(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (context.getWorld().isRemote()) {
            return ActionResultType.PASS;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        ServerWorld world = (ServerWorld) context.getWorld();
        BlockPos pos = context.getPos();

        if (player == null) {
            return ActionResultType.PASS;
        }

        final MeteoriteSpawner ms = new MeteoriteSpawner();
        PlacedMeteoriteSettings spawned = ms.trySpawnMeteorite(world, pos);

        if (spawned == null) {
            player.sendMessage(new StringTextComponent("Un-suitable Location."));
            return ActionResultType.FAIL;
        }

        final MeteoritePlacer placer = new MeteoritePlacer(world, spawned);
        placer.place();

        // The placer will not send chunks to the player since it's used as part
        // of world-gen normally, so we'll have to do it ourselves. Since this
        // is a debug tool, we'll not care about being terribly efficient here
        ChunkPos.getAllInBox(new ChunkPos(spawned.getPos()), 1).forEach(cp -> {
            Chunk c = world.getChunk(cp.x, cp.z);
            player.connection.sendPacket(new SChunkDataPacket(c, 65535)); // 65535 == full chunk
        });

        return ActionResultType.SUCCESS;
    }
}
