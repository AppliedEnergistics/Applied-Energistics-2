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

package appeng.parts.automation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.IModelTransform;
import net.minecraft.client.render.model.IUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

/**
 * Built-in model for annihilation planes that supports connected textures.
 */
public class PlaneModel implements BasicUnbakedModel {

    private final SpriteIdentifier frontTexture;
    private final SpriteIdentifier sidesTexture;
    private final SpriteIdentifier backTexture;

    public PlaneModel(Identifier frontTexture, Identifier sidesTexture, Identifier backTexture) {
        this.frontTexture = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, frontTexture);
        this.sidesTexture = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, sidesTexture);
        this.backTexture = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, backTexture);
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelLoader bakery,
                           Function<SpriteIdentifier, Sprite> spriteGetter, IModelTransform modelTransform,
                           ModelOverrideList overrides, Identifier modelLocation) {
        Sprite frontSprite = spriteGetter.apply(this.frontTexture);
        Sprite sidesSprite = spriteGetter.apply(this.sidesTexture);
        Sprite backSprite = spriteGetter.apply(this.backTexture);

        return new PlaneBakedModel(frontSprite, sidesSprite, backSprite);
    }

    @Override
    public Collection<SpriteIdentifier> getTextures(IModelConfiguration owner,
                                                    Function<Identifier, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Arrays.asList(frontTexture, sidesTexture, backTexture);
    }

}
