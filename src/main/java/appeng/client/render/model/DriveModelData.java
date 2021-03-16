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

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.ModelProperty;

import appeng.block.storage.DriveSlotsState;

public class DriveModelData extends AEModelData {

    public final static ModelProperty<DriveSlotsState> STATE = new ModelProperty<>();

    private final DriveSlotsState slotsState;

    public DriveModelData(Direction up, Direction forward, DriveSlotsState slotsState) {
        super(up, forward);
        this.slotsState = slotsState;
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
        return slotsState.equals(that.slotsState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), slotsState);
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
            return (T) this.slotsState;
        }
        return super.getData(prop);
    }

}
