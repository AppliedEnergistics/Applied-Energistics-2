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


import appeng.core.AppEng;
import com.google.common.collect.Sets;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;


public class ModelOverrideComponent implements IPreInitComponent {

    private static final ModelResourceLocation MODEL_MISSING = new ModelResourceLocation("builtin/missing", "missing");

    // Maps from resource path to customizer
    private final Map<String, BiFunction<ModelResourceLocation, IBakedModel, IBakedModel>> customizer = new HashMap<>();

    public void addOverride(String resourcePath, BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer) {
        this.customizer.put(resourcePath, customizer);
    }

    @Override
    public void preInitialize(Side side) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onModelBakeEvent(final ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        Set<ModelResourceLocation> keys = Sets.newHashSet(modelRegistry.getKeys());
        // IBakedModel missingModel = modelRegistry.getObject( MODEL_MISSING );
        IModel missingModel = ModelLoaderRegistry.getMissingModel();

        for (ModelResourceLocation location : keys) {
            if (!location.getResourceDomain().equals(AppEng.MOD_ID)) {
                continue;
            }

            IBakedModel orgModel = modelRegistry.getObject(location);

            // Don't customize the missing model. This causes Forge to swallow exceptions
            if (orgModel == missingModel) {
                continue;
            }

            BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer = this.customizer.get(location.getResourcePath());
            if (customizer != null) {
                IBakedModel newModel = customizer.apply(location, orgModel);

                if (newModel != orgModel) {
                    modelRegistry.putObject(location, newModel);
                }
            }
        }
    }
}
