/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.core.features.registries;


import appeng.api.parts.IPartModels;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class PartModels implements IPartModels {

    private final Set<ResourceLocation> models = new HashSet<>();

    private boolean initialized = false;

    @Override
    public void registerModels(Collection<ResourceLocation> partModels) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot register models after the pre-initialization phase!");
        }

        this.models.addAll(partModels);
    }

    public Set<ResourceLocation> getModels() {
        return this.models;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
