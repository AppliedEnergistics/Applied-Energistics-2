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

package appeng.core;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import appeng.api.ids.AEConstants;
import appeng.api.parts.CableRenderMode;
import appeng.client.EffectType;
import appeng.core.network.ClientboundPacket;

public interface AppEng {
    String MOD_ID = AEConstants.MOD_ID;

    static AppEng instance() {
        return AppEngBase.INSTANCE;
    }

    static ResourceLocation makeId(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }

    /**
     * Allows common item use methods to get the current mouse over without relying on client-only methods.
     */
    default HitResult getCurrentMouseOver() {
        return null;
    }

    /**
     * @return A stream of all players in the game. On the client it'll be empty if no level is loaded.
     */
    Collection<ServerPlayer> getPlayers();

    void sendToAllNearExcept(Player p, double x, double y, double z, double dist, Level level,
            ClientboundPacket packet);

    void spawnEffect(EffectType effect, Level level, double posX, double posY,
            double posZ, Object o);

    /**
     * Sets the player that is currently interacting with a cable or part attached to a cable. This will return that
     * player's cable render mode from calls to {@link #getCableRenderMode()}, until another player or null is set.
     *
     * @param player Null to revert to the default cable render mode.
     */
    void setPartInteractionPlayer(Player player);

    CableRenderMode getCableRenderMode();

    /**
     * Can be used to get the current level the client is in.
     *
     * @return null if no client level is available (i.e. on a dedicated server)
     */
    @Nullable
    Level getClientLevel();

    /**
     * Since in a Minecraft client, multiple servers can be launched and stopped during a single session, the result of
     * this method should not be stored globally.
     *
     * @return The currently running Minecraft server instance, if there is one.
     */
    @Nullable
    MinecraftServer getCurrentServer();

    /**
     * registers Hotkeys for {@link appeng.hotkeys.HotkeyActions}
     */
    void registerHotkey(String id);

    /**
     * Shows a system chat message to the player
     */
    void sendSystemMessage(Player player, Component text);

    /**
     * Handles a clientbound packet when on the client (throws on the server).
     */
    <T extends ClientboundPacket> void handleClientboundPacket(CustomPacketPayload.Type<T> type, T payload,
            IPayloadContext context);

    <T extends Recipe<?>> RecipeHolder<T> getRecipeById(Level level, RecipeType<T> type, ResourceKey<Recipe<?>> id);
}
