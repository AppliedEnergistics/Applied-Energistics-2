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

package appeng.client.render;

import appeng.client.render.cablebus.FacadeBuilder;
import appeng.core.AppEng;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 * The model class for facades. Since facades wrap existing models, they don't
 * declare any dependencies here other than the cable anchor.
 */
public class FacadeItemModel implements BasicUnbakedModel {

    // We use this to get the default item transforms and make our lives easier
    private static final Identifier MODEL_BASE = new Identifier(AppEng.MOD_ID, "item/facade_base");

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        BakedModel bakedBaseModel = loader.bake(MODEL_BASE, rotationContainer);
        FacadeBuilder facadeBuilder = new FacadeBuilder(loader);

        return new FacadeDispatcherBakedModel(bakedBaseModel, facadeBuilder);
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.singleton(MODEL_BASE);
    }
}
