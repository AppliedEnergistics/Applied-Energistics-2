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

import appeng.api.util.AEColor;

public class CraftingMonitorModelData extends CraftingCubeModelData {

    public static final ModelProperty<AEColor> COLOR = new ModelProperty<>();

    private final AEColor color;

    public CraftingMonitorModelData(Direction up, Direction forward, EnumSet<Direction> connections, AEColor color) {
        super(up, forward, connections);
        this.color = Preconditions.checkNotNull(color);
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
        CraftingMonitorModelData that = (CraftingMonitorModelData) o;
        return color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), color);
    }

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return prop == COLOR || super.hasProperty(prop);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getData(ModelProperty<T> prop) {
        if (prop == COLOR) {
            return (T) this.color;
        }
        return super.getData(prop);
    }

}
