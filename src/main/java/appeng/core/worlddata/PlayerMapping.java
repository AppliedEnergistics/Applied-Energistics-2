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

package appeng.core.worlddata;


import com.google.common.base.Preconditions;
import net.minecraftforge.common.config.ConfigCategory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


/**
 * Wrapper class for the player mappings.
 * Will grant access to a pre initialized player map
 * based on the "players" category in the settings.cfg
 */
final class PlayerMapping implements IWorldPlayerMapping {
    /**
     * View of player mappings, is not immutable,
     * since it needs to be edited upon runtime,
     * cause new players can join
     */
    private final Map<Integer, UUID> mappings;

    public PlayerMapping(final ConfigCategory category) {
        final PlayerMappingsInitializer init = new PlayerMappingsInitializer(category);

        this.mappings = init.getPlayerMappings();
    }

    @Nonnull
    @Override
    public Optional<UUID> get(final int id) {
        final UUID maybe = this.mappings.get(id);

        return Optional.ofNullable(maybe);
    }

    @Override
    public void put(final int id, @Nonnull final UUID uuid) {
        Preconditions.checkNotNull(uuid);

        this.mappings.put(id, uuid);
    }
}
