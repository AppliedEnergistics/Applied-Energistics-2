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

package appeng.parts;


import appeng.api.parts.IPartModel;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;

import java.util.List;


public class PartModel implements IPartModel {
    private final boolean isSolid;

    private final List<ResourceLocation> resources;

    public PartModel(ResourceLocation resource) {
        this(true, resource);
    }

    public PartModel(ResourceLocation... resources) {
        this(true, resources);
    }

    public PartModel(boolean isSolid, ResourceLocation resource) {
        this(isSolid, ImmutableList.of(resource));
    }

    public PartModel(boolean isSolid, ResourceLocation... resources) {
        this(isSolid, ImmutableList.copyOf(resources));
    }

    public PartModel(List<ResourceLocation> resources) {
        this(true, resources);
    }

    public PartModel(boolean isSolid, List<ResourceLocation> resources) {
        this.isSolid = isSolid;
        this.resources = resources;
    }

    @Override
    public boolean requireCableConnection() {
        return this.isSolid;
    }

    @Override
    public List<ResourceLocation> getModels() {
        return this.resources;
    }

}
