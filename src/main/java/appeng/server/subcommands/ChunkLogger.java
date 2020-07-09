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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import appeng.api.features.AEFeature;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.server.ISubCommand;
import net.minecraft.world.chunk.WorldChunk;

public class ChunkLogger implements ISubCommand {

    private boolean enabled = false;

    // Since we cannot unregister a listener once it's registered, but
    // we still only want to register it if the command is ever used we track it here
    private boolean listenerRegistered = false;

    private void onChunkLoadEvent(ServerWorld world, WorldChunk chunk) {
        if (!this.enabled) {
            return;
        }
        AELog.info("Chunk Loaded:   " + chunk.getPos().x + ", " + chunk.getPos().z);
        this.displayStack();
    }

    private void onChunkUnloadEvent(ServerWorld world, WorldChunk chunk) {
        if (!this.enabled) {
            return;
        }
        AELog.info("Chunk Unloaded: " + chunk.getPos().x + ", " + chunk.getPos().z);
        this.displayStack();
    }

    private void displayStack() {
        if (AEConfig.instance().isFeatureEnabled(AEFeature.CHUNK_LOGGER_TRACE)) {
            boolean output = false;
            for (final StackTraceElement e : Thread.currentThread().getStackTrace()) {
                if (output) {
                    AELog.info(
                            "		" + e.getClassName() + '.' + e.getMethodName() + " (" + e.getLineNumber() + ')');
                } else {
                    output = e.getClassName().contains("EventBus") && e.getMethodName().contains("post");
                }
            }
        }
    }

    @Override
    public synchronized void call(final MinecraftServer srv, final CommandContext<ServerCommandSource> data, final ServerCommandSource sender) {
        this.enabled = !this.enabled;

        if (this.enabled) {
            if (!this.listenerRegistered) {
                ServerChunkEvents.CHUNK_LOAD.register(this::onChunkLoadEvent);
                ServerChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnloadEvent);
                this.listenerRegistered = true;
            }

            sender.sendFeedback(new TranslatableText("commands.ae2.ChunkLoggerOn"), true);
        } else {
            sender.sendFeedback(new TranslatableText("commands.ae2.ChunkLoggerOff"), true);
        }
    }
}
