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

package appeng.client.render.cablebus;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public final class P2PTunnelFrequencyModelData implements IModelData {

    public static final ModelProperty<Long> FREQUENCY = new ModelProperty<>();

    private final Long frequency;

    public P2PTunnelFrequencyModelData(long frequency) {
        this.frequency = frequency;
    }

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return prop == FREQUENCY;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getData(ModelProperty<T> prop) {
        if (prop == FREQUENCY) {
            return (T) this.frequency;
        }

        return null;
    }

    @Override
    @Nullable
    public <T> T setData(ModelProperty<T> prop, T data) {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(frequency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        P2PTunnelFrequencyModelData that = (P2PTunnelFrequencyModelData) o;
        return frequency.equals(that.frequency);
    }
}
