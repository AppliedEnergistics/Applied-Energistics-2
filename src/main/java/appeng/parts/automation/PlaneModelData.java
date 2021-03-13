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

package appeng.parts.automation;

import javax.annotation.Nullable;

import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class PlaneModelData implements IModelData {

    public static final ModelProperty<PlaneConnections> CONNECTIONS = new ModelProperty<>();

    private final PlaneConnections connections;

    public PlaneModelData(PlaneConnections connections) {
        this.connections = connections;
    }

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return prop == CONNECTIONS;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getData(ModelProperty<T> prop) {
        if (prop == CONNECTIONS) {
            return (T) this.connections;
        }
        return null;
    }

    @Override
    @Nullable
    public <T> T setData(ModelProperty<T> prop, T data) {
        return null;
    }

}
