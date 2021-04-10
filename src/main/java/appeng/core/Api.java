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

package appeng.core;

import appeng.api.AEAddon;
import appeng.api.IAppEngApi;
import appeng.api.client.IClientHelper;
import appeng.api.crafting.ICraftingHelper;
import appeng.api.features.IRegistryContainer;
import appeng.api.networking.IGridHelper;
import appeng.api.storage.IStorageHelper;
import appeng.core.api.ApiClientHelper;
import appeng.core.api.ApiCrafting;
import appeng.core.api.ApiGrid;
import appeng.core.api.ApiPart;
import appeng.core.api.ApiStorage;
import appeng.core.features.registries.PartModels;
import appeng.core.features.registries.RegistryContainer;

public final class Api implements IAppEngApi {

    /**
     * While permitting public access, directly using {@link Api#instance()} is not recommended except in very special
     * cases.
     */
    // FIXME CRAFTING quick n dirty, fix this
    public static Api INSTANCE = null;
    public static IAppEngApi fakeInstance = null;

    public static void setInstance(IAppEngApi api) {
        fakeInstance = api;
        if (api instanceof Api) {
            INSTANCE = (Api) api;
        }
    }

    /**
     * Use primarily to access the API.
     *
     * Using {@link IAppEngApi} intentionally to avoid using functionality not exposed by accident.
     *
     * In some cases we might have to access the API before it is announced via {@link AEAddon}, otherwise we could just
     * inject it into AE2 itself.
     */
    public static IAppEngApi instance() {
        return fakeInstance;
    }

    private final ApiPart partHelper;

    // private MovableTileRegistry MovableRegistry = new MovableTileRegistry();
    private final IRegistryContainer registryContainer;
    private final IStorageHelper storageHelper;
    private final IGridHelper networkHelper;
    private final ApiDefinitions definitions;
    private final ICraftingHelper craftingHelper;
    private final IClientHelper client;

    Api() {
        this.storageHelper = new ApiStorage();
        this.networkHelper = new ApiGrid();
        this.registryContainer = new RegistryContainer();
        this.partHelper = new ApiPart();
        this.definitions = new ApiDefinitions((PartModels) this.registryContainer.partModels());
        this.craftingHelper = new ApiCrafting(this.definitions);
        this.client = new ApiClientHelper(this.definitions);
    }

    public PartModels getPartModels() {
        return (PartModels) this.registryContainer.partModels();
    }

    @Override
    public IRegistryContainer registries() {
        return this.registryContainer;
    }

    @Override
    public IStorageHelper storage() {
        return this.storageHelper;
    }

    @Override
    public ICraftingHelper crafting() {
        return this.craftingHelper;
    }

    @Override
    public IGridHelper grid() {
        return this.networkHelper;
    }

    @Override
    public ApiPart partHelper() {
        return this.partHelper;
    }

    @Override
    public ApiDefinitions definitions() {
        return this.definitions;
    }

    @Override
    public IClientHelper client() {
        return this.client;
    }
}
