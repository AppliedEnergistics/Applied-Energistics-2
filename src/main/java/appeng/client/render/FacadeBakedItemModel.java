/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import appeng.client.render.cablebus.FacadeBuilder;

/**
 * This model used the provided FacadeBuilder to "slice" the item quads for the facade provided.
 *
 * @author covers1624
 */
public class FacadeBakedItemModel extends DelegateBakedModel {

    private final ItemStack textureStack;
    private final FacadeBuilder facadeBuilder;
    private List<BakedQuad> quads = null;

    protected FacadeBakedItemModel(IBakedModel base, ItemStack textureStack, FacadeBuilder facadeBuilder) {
        super(base);
        this.textureStack = textureStack;
        this.facadeBuilder = facadeBuilder;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand,
            IModelData data) {
        if (side != null) {
            return Collections.emptyList();
        }
        if (quads == null) {
            quads = new ArrayList<>();
            quads.addAll(this.facadeBuilder.buildFacadeItemQuads(this.textureStack, Direction.NORTH));
            quads.addAll(this.getBaseModel().getQuads(state, side, rand, data));
            quads = Collections.unmodifiableList(quads);
        }
        return quads;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}
