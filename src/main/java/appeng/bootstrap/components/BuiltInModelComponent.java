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

package appeng.bootstrap.components;


import appeng.client.render.model.BuiltInModelLoader;
import com.google.common.base.Preconditions;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;


@SideOnly(Side.CLIENT)
public class BuiltInModelComponent implements IPreInitComponent {

    private final Map<String, IModel> builtInModels = new HashMap<>();

    private boolean hasInitialized = false;

    public void addModel(String path, IModel model) {
        Preconditions.checkState(!this.hasInitialized);
        this.builtInModels.put(path, model);
    }

    @Override
    public void preInitialize(Side side) {
        this.hasInitialized = true;

        BuiltInModelLoader loader = new BuiltInModelLoader(this.builtInModels);
        ModelLoaderRegistry.registerLoader(loader);
    }
}
