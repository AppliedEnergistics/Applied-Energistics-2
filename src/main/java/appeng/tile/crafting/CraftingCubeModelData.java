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

package appeng.tile.crafting;

import java.util.EnumSet;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.core.Direction;
import net.minecraftforge.client.model.data.ModelProperty;

import appeng.client.render.model.AEModelData;

public class CraftingCubeModelData extends AEModelData {

    public static final ModelProperty<EnumSet<Direction>> CONNECTIONS = new ModelProperty<>();

    // Contains information on which sides of the block are connected to other parts
    // of a formed crafting cube
    private final EnumSet<Direction> connections;

    public CraftingCubeModelData(Direction up, Direction forward, EnumSet<Direction> connections) {
        super(up, forward);
        this.connections = Preconditions.checkNotNull(connections);
    }

    @Override
    protected boolean isCacheable() {
        return false; // Too many variants
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        CraftingCubeModelData that = (CraftingCubeModelData) o;
        return connections.equals(that.connections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), connections);
    }

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return prop == CONNECTIONS || super.hasProperty(prop);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getData(ModelProperty<T> prop) {
        if (prop == CONNECTIONS) {
            return (T) this.connections;
        }
        return super.getData(prop);
    }

}
