/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.blockentity.crafting;

import java.util.EnumSet;
import java.util.Objects;

import net.minecraft.core.Direction;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import appeng.api.util.AEColor;

public final class CraftingMonitorModelData {
    public static final ModelProperty<AEColor> COLOR = new ModelProperty<>();

    public static ModelData.Builder builder(EnumSet<Direction> connections,
            AEColor color) {
        return CraftingCubeModelData.builder(connections)
                .with(COLOR, Objects.requireNonNull(color));
    }

    public static ModelData create(EnumSet<Direction> connections, AEColor color) {
        return builder(connections, color).build();
    }
}
