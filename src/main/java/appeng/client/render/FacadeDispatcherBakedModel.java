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

import java.util.Objects;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.world.entity.LivingEntity;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import appeng.client.render.cablebus.FacadeBuilder;
import appeng.items.parts.FacadeItem;
import net.minecraft.world.item.ItemStack;

/**
 * This baked model class is used as a dispatcher to redirect the renderer to the *real* model that should be used based
 * on the item stack. A custom Item Override List is used to accomplish this.
 */
public class FacadeDispatcherBakedModel extends DelegateBakedModel {
    private final FacadeBuilder facadeBuilder;
    private final Int2ObjectMap<FacadeBakedItemModel> cache = new Int2ObjectArrayMap<>();

    public FacadeDispatcherBakedModel(BakedModel baseModel, FacadeBuilder facadeBuilder) {
        super(baseModel);
        this.facadeBuilder = facadeBuilder;
    }

    @Override
    public ItemOverrides getOverrides() {
        return new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel originalModel, net.minecraft.world.item.ItemStack stack, ClientLevel world,
                                      LivingEntity entity) {
                if (!(stack.getItem() instanceof FacadeItem)) {
                    return originalModel;
                }

                FacadeItem itemFacade = (FacadeItem) stack.getItem();

                ItemStack textureItem = itemFacade.getTextureItem(stack);

                int hash = Objects.hash(textureItem.getItem().getRegistryName(), textureItem.getTag());
                FacadeBakedItemModel model = FacadeDispatcherBakedModel.this.cache.get(hash);
                if (model == null) {
                    model = new FacadeBakedItemModel(FacadeDispatcherBakedModel.this.getBaseModel(), textureItem,
                            FacadeDispatcherBakedModel.this.facadeBuilder);
                    FacadeDispatcherBakedModel.this.cache.put(hash, model);
                }

                return model;
            }
        };
    }
}
