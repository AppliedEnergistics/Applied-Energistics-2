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

package appeng.init.client;

import appeng.client.render.tesr.SkyChestTESR;
import appeng.core.definitions.AEEntities;
import appeng.entity.TinyTNTPrimedRenderer;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.function.BiConsumer;

/**
 * Registers custom renderers for our {@link AEEntities}.
 */
public final class InitEntityLayerDefinitions {

    private InitEntityLayerDefinitions() {
    }

    public static void init(BiConsumer<ModelLayerLocation, LayerDefinition> consumer) {
        consumer.accept(SkyChestTESR.MODEL_LAYER, SkyChestTESR.createSingleBodyLayer());
    }

}
