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


import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.server.ISubCommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class ChunkLogger implements ISubCommand {

    private boolean enabled = false;

    @SubscribeEvent
    public void onChunkLoadEvent(final ChunkEvent.Load event) {
        if (!event.getWorld().isRemote) {
            AELog.info("Chunk Loaded:   " + event.getChunk().x + ", " + event.getChunk().z);
            this.displayStack();
        }
    }

    private void displayStack() {
        if (AEConfig.instance().isFeatureEnabled(AEFeature.CHUNK_LOGGER_TRACE)) {
            boolean output = false;
            for (final StackTraceElement e : Thread.currentThread().getStackTrace()) {
                if (output) {
                    AELog.info("		" + e.getClassName() + '.' + e.getMethodName() + " (" + e.getLineNumber() + ')');
                } else {
                    output = e.getClassName().contains("EventBus") && e.getMethodName().contains("post");
                }
            }
        }
    }

    @SubscribeEvent
    public void onChunkUnloadEvent(final ChunkEvent.Unload unload) {
        if (!unload.getWorld().isRemote) {
            AELog.info("Chunk Unloaded: " + unload.getChunk().x + ", " + unload.getChunk().z);
            this.displayStack();
        }
    }

    @Override
    public String getHelp(final MinecraftServer srv) {
        return "commands.ae2.ChunkLogger";
    }

    @Override
    public void call(final MinecraftServer srv, final String[] data, final ICommandSender sender) {
        this.enabled = !this.enabled;

        if (this.enabled) {
            MinecraftForge.EVENT_BUS.register(this);
            sender.sendMessage(new TextComponentTranslation("commands.ae2.ChunkLoggerOn"));
        } else {
            MinecraftForge.EVENT_BUS.unregister(this);
            sender.sendMessage(new TextComponentTranslation("commands.ae2.ChunkLoggerOff"));
        }
    }
}
