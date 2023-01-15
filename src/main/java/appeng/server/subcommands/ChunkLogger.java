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

package appeng.server.subcommands;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.server.ISubCommand;

public class ChunkLogger implements ISubCommand {

    private boolean enabled = false;

    private void displayStack() {
        if (AEConfig.instance().isChunkLoggerTraceEnabled()) {
            boolean output = false;
            for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                if (output) {
                    AELog.info(
                            "		" + e.getClassName() + '.' + e.getMethodName() + " (" + e.getLineNumber() + ')');
                } else {
                    output = e.getClassName().contains("EventBus") && e.getMethodName().contains("post");
                }
            }
        }
    }

    @SubscribeEvent
    public void onChunkLoadEvent(final ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            var chunk = event.getChunk();
            var chunkPos = chunk.getPos();
            var center = getCenter(chunk);
            AELog.info("Loaded chunk " + chunkPos.x + "," + chunkPos.z + " [center: " + center + "] in "
                    + level.dimension().location());
            this.displayStack();
        }
    }

    @SubscribeEvent
    public void onChunkUnloadEvent(final ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            var chunk = event.getChunk();
            var chunkPos = chunk.getPos();
            var center = getCenter(chunk);
            AELog.info("Unloaded chunk " + chunkPos.x + "," + chunkPos.z + " [center: " + center + "] in "
                    + level.dimension().location());
            this.displayStack();
        }
    }

    private static String getCenter(ChunkAccess chunk) {
        var chunkPos = chunk.getPos();
        var x = chunkPos.getMiddleBlockX();
        var z = chunkPos.getMiddleBlockZ();
        var y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
        return x + " " + y + " " + z;
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> data,
            CommandSourceStack sender) {
        this.enabled = !this.enabled;

        if (this.enabled) {
            MinecraftForge.EVENT_BUS.register(this);
            sender.sendSuccess(Component.translatable("commands.ae2.ChunkLoggerOn"), true);
        } else {
            MinecraftForge.EVENT_BUS.unregister(this);
            sender.sendSuccess(Component.translatable("commands.ae2.ChunkLoggerOff"), true);
        }
    }
}
