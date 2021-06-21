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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import appeng.api.parts.CableRenderMode;
import appeng.client.EffectType;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.sync.BasePacket;

public interface AppEng {

    String MOD_NAME = "Applied Energistics 2";
    String MOD_ID = "appliedenergistics2";

    static AppEng instance() {
        return AppEngBase.INSTANCE;
    }

    static ResourceLocation makeId(String id) {
        return new ResourceLocation(MOD_ID, id);
    }

    @Nonnull
    AdvancementTriggers getAdvancementTriggers();

    /**
     * Allows common item use methods to get the current mouse over without relying on client-only methods.
     */
    default RayTraceResult getCurrentMouseOver() {
        return null;
    }

    /**
     * @return A stream of all players in the game. On the client it'll be empty if no world is loaded.
     */
    Collection<ServerPlayerEntity> getPlayers();

    void sendToAllNearExcept(PlayerEntity p, double x, double y, double z, double dist, World w, BasePacket packet);

    void spawnEffect(final EffectType effect, final World world, final double posX, final double posY,
            final double posZ, final Object o);

    /**
     * Sets the player that is currently interacting with a cable or part attached to a cable. This will return that
     * player's cable render mode from calls to {@link #getCableRenderMode()}, until another player or null is set.
     *
     * @param player Null to revert to the default cable render mode.
     */
    void setPartInteractionPlayer(PlayerEntity player);

    CableRenderMode getCableRenderMode();

    /**
     * Can be used to get the current world the client is in.
     *
     * @return null if no client world is available (i.e. on a dedicated server)
     */
    @Nullable
    World getClientWorld();
}
