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

package appeng.client.render.model;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.data.ModelProperty;

public class DriveModelData extends AEModelData {

    public final static ModelProperty<Item[]> STATE = new ModelProperty<>();

    private final Item[] cells;

    public DriveModelData(Direction up, Direction forward, Item[] cells) {
        super(up, forward);
        this.cells = cells;
    }

    @Override
    protected boolean isCacheable() {
        return false; // Too many combinations
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
        DriveModelData that = (DriveModelData) o;
        return Arrays.equals(cells, that.cells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Arrays.hashCode(cells));
    }

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return prop == STATE || super.hasProperty(prop);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getData(ModelProperty<T> prop) {
        if (prop == STATE) {
            return (T) this.cells;
        }
        return super.getData(prop);
    }

}
