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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import com.google.common.math.StatsAccumulator;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;

import appeng.core.api.definitions.ApiBlocks;
import appeng.server.ISubCommand;

/**
 * This is a testing command to validate quartz ore generation.
 */
public class TestOreGenCommand implements ISubCommand {

    private final BlockState quartzOre;
    private final BlockState chargedQuartzOre;

    public TestOreGenCommand() {
        quartzOre = ApiBlocks.quartzOre.block().getDefaultState();
        chargedQuartzOre = ApiBlocks.quartzOreCharged.block().getDefaultState();
    }

    @Override
    public void call(final MinecraftServer srv, final CommandContext<CommandSource> data, final CommandSource sender) {

        int radius = 1000;

        ServerWorld world;
        BlockPos center;
        try {
            ServerPlayerEntity player = sender.asPlayer();
            world = player.getServerWorld();
            center = new BlockPos(player.getPosX(), 0, player.getPosZ());
        } catch (CommandSyntaxException e) {
            world = srv.getWorld(World.OVERWORLD);
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

        AggregatedStats oreCount = AggregatedStats.create(stats.chunks, cs -> (double) cs.quartzOreCount);
        List<ChunkStats> chunksWithOre = stats.chunks.stream().filter(c -> c.quartzOreCount > 0)
                .collect(Collectors.toList());
        AggregatedStats minHeight = AggregatedStats.create(chunksWithOre, cs -> (double) cs.minHeight);
        AggregatedStats maxHeight = AggregatedStats.create(chunksWithOre, cs -> (double) cs.maxHeight);
        AggregatedStats chargedCount = AggregatedStats.create(chunksWithOre, cs -> (double) cs.chargedOreCount);

        sendLine(sender, "Checked %d chunks", stats.chunks.size());
        sendLine(sender, "  Count: %s", oreCount);
        sendLine(sender, "  Min-Height: %s", minHeight);
        sendLine(sender, "  Max-Height: %s", maxHeight);
        sendLine(sender, "  Sub-Type Count: %s", chargedCount);
    }

    private void checkChunk(CommandSource sender, ServerWorld world, ChunkPos cp, Stats stats) {
        IChunk chunk = world.getChunk(cp.x, cp.z, ChunkStatus.FULL, false);
        if (chunk == null) {
            sendLine(sender, "Skipping chunk %s", cp);
            return;
        }

        ChunkStats chunkStats = new ChunkStats();

        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        sendLine(sender, "Checking chunk %s", cp);
        for (int x = cp.getXStart(); x <= cp.getXEnd(); x++) {
            blockPos.setX(x);
            for (int z = cp.getZStart(); z <= cp.getZEnd(); z++) {
                blockPos.setZ(z);
                for (int y = 0; y < world.func_234938_ad_(); y++) {
                    blockPos.setY(y);
                    BlockState state = chunk.getBlockState(blockPos);
                    if (state == quartzOre || state == chargedQuartzOre) {
                        chunkStats.minHeight = Math.min(chunkStats.minHeight, y);
                        chunkStats.maxHeight = Math.max(chunkStats.maxHeight, y);
                        chunkStats.quartzOreCount++;
                        if (state == chargedQuartzOre) {
                            chunkStats.chargedOreCount++;
                        }
                    }
                }
            }
        }
        stats.chunks.add(chunkStats);
    }

    private static void sendLine(CommandSource sender, String text, Object... args) {
        sender.sendFeedback(new StringTextComponent(String.format(Locale.ROOT, text, args)), true);
    }

    private static class Stats {
        public final List<ChunkStats> chunks = new ArrayList<>();
    }

    private static class ChunkStats {
        public int quartzOreCount = 0;
        public int chargedOreCount = 0;
        public int minHeight = Integer.MAX_VALUE;
        public int maxHeight = Integer.MIN_VALUE;
    }

    private static class AggregatedStats {
        public final double min;
        public final double max;
        public final double mean;
        public final double stdDev;

        public AggregatedStats(double min, double max, double mean, double stdDev) {
            this.min = min;
            this.max = max;
            this.mean = mean;
            this.stdDev = stdDev;
        }

        public static <T> AggregatedStats create(List<T> values, ToDoubleFunction<T> getter) {
            if (values.isEmpty()) {
                return new AggregatedStats(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
            }

            StatsAccumulator accumulator = new StatsAccumulator();
            for (T value : values) {
                accumulator.add(getter.applyAsDouble(value));
            }

            return new AggregatedStats(accumulator.min(), accumulator.max(), accumulator.mean(),
                    accumulator.populationStandardDeviation());
        }

        @Override
        public String toString() {
            if (Double.isNaN(min)) {
                return "Invalid";
            }
            return String.format(Locale.ROOT, "min=%.2f, max=%.2f, mean=%.2f, stdDev=%.2f", min, max, mean, stdDev);
        }
    }

}
