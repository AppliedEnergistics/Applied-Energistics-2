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

package appeng.server.subcommands;

import java.util.Locale;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

import appeng.core.Api;
import appeng.server.ISubCommand;

/**
 * This is a testing command to validate quartz ore generation.
 */
public class TestOreGenCommand implements ISubCommand {

    private final BlockState quartzOre;
    private final BlockState chargedQuartzOre;

    public TestOreGenCommand() {
        quartzOre = Api.INSTANCE.definitions().blocks().quartzOre().block().getDefaultState();
        chargedQuartzOre = Api.INSTANCE.definitions().blocks().quartzOreCharged().block().getDefaultState();
    }

    @Override
    public void call(final MinecraftServer srv, final String[] data, final CommandSource sender) {

        int radius = 1000;

        ServerWorld world = srv.getWorld(DimensionType.OVERWORLD);
        BlockPos center;
        try {
            ServerPlayerEntity player = sender.asPlayer();
            center = new BlockPos(player.getPosX(), 0, player.getPosZ());
        } catch (CommandSyntaxException e) {
            center = world.getSpawnPoint();
        }

        ChunkPos tl = new ChunkPos(center.add(-radius, 0, -radius));
        ChunkPos br = new ChunkPos(center.add(radius, 0, radius));

        Stats stats = new Stats();
        for (int cx = tl.x; cx <= br.x; cx++) {
            for (int cz = tl.z; cz <= br.z; cz++) {
                ChunkPos cp = new ChunkPos(cx, cz);
                checkChunk(sender, world, cp, stats);
            }
        }

        sendLine(sender, "Checked %d chunks", stats.chunksChecked);
        sendLine(sender, "Total Ore: %d (%f per chunk)", stats.quartzOreCount,
                stats.quartzOreCount / (float) stats.chunksChecked);
        if (stats.quartzOreCount > 0) {
            sendLine(sender, "Charged ore: %d (%.1f%%)", stats.chargedOreCount,
                    stats.chargedOreCount / (float) stats.quartzOreCount * 100);
            sendLine(sender, "Height range: %d-%d", stats.minHeight, stats.maxHeight);
        }
    }

    private void checkChunk(CommandSource sender, ServerWorld world, ChunkPos cp, Stats stats) {
        IChunk chunk = world.getChunk(cp.x, cp.z, ChunkStatus.FULL, false);
        if (chunk == null) {
            sendLine(sender, "Skipping chunk %s", cp);
            return;
        }

        stats.chunksChecked++;

        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        sendLine(sender, "Checking chunk %s", cp);
        for (int x = cp.getXStart(); x <= cp.getXEnd(); x++) {
            blockPos.setX(x);
            for (int z = cp.getZStart(); z <= cp.getZEnd(); z++) {
                blockPos.setZ(z);
                for (int y = 0; y < world.getMaxHeight(); y++) {
                    blockPos.setY(y);
                    BlockState state = chunk.getBlockState(blockPos);
                    if (state == quartzOre || state == chargedQuartzOre) {
                        stats.minHeight = Math.min(stats.minHeight, y);
                        stats.maxHeight = Math.max(stats.maxHeight, y);
                        stats.quartzOreCount++;
                        if (state == chargedQuartzOre) {
                            stats.chargedOreCount++;
                        }
                    }
                }
            }
        }
    }

    private static void sendLine(CommandSource sender, String text, Object... args) {
        sender.sendFeedback(new StringTextComponent(String.format(Locale.ROOT, text, args)), true);
    }

    private static class Stats {
        public int chunksChecked = 0;
        public int quartzOreCount = 0;
        public int chargedOreCount = 0;
        public int minHeight = Integer.MAX_VALUE;
        public int maxHeight = Integer.MIN_VALUE;
    }

}
