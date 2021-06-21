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

import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.server.ISubCommand;

public class ChunkLogger implements ISubCommand {

    private boolean enabled = false;

    private void displayStack() {
        if (AEConfig.instance().isChunkLoggerTraceEnabled()) {
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

    @SubscribeEvent
    public void onChunkLoadEvent(final ChunkEvent.Load event) {
        if (!event.getWorld().isRemote()) {
            AELog.info("Chunk Loaded:   " + event.getChunk().getPos().x + ", " + event.getChunk().getPos().z);
            this.displayStack();
        }
    }

    @SubscribeEvent
    public void onChunkUnloadEvent(final ChunkEvent.Unload unload) {
        if (!unload.getWorld().isRemote()) {
            AELog.info("Chunk Unloaded: " + unload.getChunk().getPos().x + ", " + unload.getChunk().getPos().z);
            this.displayStack();
        }
    }

    @Override
    public void call(final MinecraftServer srv, final CommandContext<CommandSource> data, final CommandSource sender) {
        this.enabled = !this.enabled;

        if (this.enabled) {
            MinecraftForge.EVENT_BUS.register(this);
            sender.sendFeedback(new TranslationTextComponent("commands.ae2.ChunkLoggerOn"), true);
        } else {
            MinecraftForge.EVENT_BUS.unregister(this);
            sender.sendFeedback(new TranslationTextComponent("commands.ae2.ChunkLoggerOff"), true);
        }
    }
}
