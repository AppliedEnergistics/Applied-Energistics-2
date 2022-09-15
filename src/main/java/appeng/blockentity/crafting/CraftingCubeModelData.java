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

import net.minecraft.core.Direction;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import appeng.client.render.model.AEModelData;

public final class CraftingCubeModelData {

    // Contains information on which sides of the block are connected to other parts
    // of a formed crafting cube
    public static final ModelProperty<EnumSet<Direction>> CONNECTIONS = new ModelProperty<>();

    private CraftingCubeModelData() {
    }

    public static ModelData.Builder builder(Direction up, Direction forward, EnumSet<Direction> connections) {
        return AEModelData.builder(up, forward)
                .with(AEModelData.SKIP_CACHE, true)
                .with(CONNECTIONS, connections);
    }

    public static ModelData create(Direction up, Direction forward, EnumSet<Direction> connections) {
        return builder(up, forward, connections).build();
    }
}
