/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core;

import appeng.api.definitions.IDefinitions;
import appeng.bootstrap.FeatureFactory;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;
import appeng.core.api.definitions.ApiMaterials;
import appeng.core.api.definitions.ApiParts;
import appeng.core.features.registries.PartModels;

/**
 * Internal implementation of the definitions for the API
 */
public final class ApiDefinitions implements IDefinitions {
    private final ApiBlocks blocks;
    private final ApiItems items;
    private final ApiMaterials materials;
    private final ApiParts parts;

    private final FeatureFactory registry = new FeatureFactory();

    public ApiDefinitions(final PartModels partModels) {
        this.blocks = new ApiBlocks(this.registry);
        this.materials = new ApiMaterials(this.registry);
        this.items = new ApiItems(this.registry, this.materials);
        this.parts = new ApiParts(this.registry, partModels);
    }

    public FeatureFactory getRegistry() {
        return registry;
    }

    @Override
    public ApiBlocks blocks() {
        return this.blocks;
    }

    @Override
    public ApiItems items() {
        return items;
    }

    @Override
    public ApiMaterials materials() {
        return materials;
    }

    @Override
    public ApiParts parts() {
        return parts;
    }
}
