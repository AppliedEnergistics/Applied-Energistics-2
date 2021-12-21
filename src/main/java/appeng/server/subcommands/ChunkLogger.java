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

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.server.ISubCommand;

public class ChunkLogger implements ISubCommand {

    private boolean eventsRegistered = false;
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

    private void onChunkLoadEvent(ServerLevel level, LevelChunk chunk) {
        if (enabled) {
            AELog.info("Chunk Loaded:   " + chunk.getPos().x + ", " + chunk.getPos().z);
            this.displayStack();
        }
    }

    private void onChunkUnloadEvent(ServerLevel level, LevelChunk chunk) {
        if (enabled) {
            AELog.info("Chunk Unloaded: " + chunk.getPos().x + ", " + chunk.getPos().z);
            this.displayStack();
        }
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> data,
            CommandSourceStack sender) {
        if (!eventsRegistered) {
            ServerChunkEvents.CHUNK_LOAD.register(this::onChunkLoadEvent);
            ServerChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnloadEvent);
        }

        this.enabled = !this.enabled;

        if (this.enabled) {
            sender.sendSuccess(new TranslatableComponent("commands.ae2.ChunkLoggerOn"), true);
        } else {
            sender.sendSuccess(new TranslatableComponent("commands.ae2.ChunkLoggerOff"), true);
        }
    }
}
