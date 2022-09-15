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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.eventbus.api.IEventBus;

import appeng.block.AEBaseBlock;
import appeng.client.render.crafting.MonitorBakedModel;
import appeng.client.render.model.AutoRotatingBakedModel;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;

public final class InitAutoRotatingModel {

    /**
     * Blocks that should not use the auto rotation model.
     */
    private static final Set<BlockDefinition<?>> NO_AUTO_ROTATION = ImmutableSet.of(
            AEBlocks.CABLE_BUS,
            AEBlocks.CONTROLLER,
            AEBlocks.PAINT,
            AEBlocks.QUANTUM_LINK,
            AEBlocks.QUANTUM_RING,
            AEBlocks.CRAFTING_UNIT,
            AEBlocks.CRAFTING_ACCELERATOR,
            AEBlocks.CRAFTING_MONITOR,
            AEBlocks.CRAFTING_STORAGE_1K,
            AEBlocks.CRAFTING_STORAGE_4K,
            AEBlocks.CRAFTING_STORAGE_16K,
            AEBlocks.CRAFTING_STORAGE_64K,
            AEBlocks.CRAFTING_STORAGE_256K);

    // Maps from resource path to customizer
    private static final Map<String, Function<BakedModel, BakedModel>> CUSTOMIZERS = new HashMap<>();

    private InitAutoRotatingModel() {
    }

    public static void init(IEventBus modEventBus) {
        register(AEBlocks.CRAFTING_MONITOR, InitAutoRotatingModel::customizeCraftingMonitorModel);

        for (var block : AEBlocks.getBlocks()) {
            if (NO_AUTO_ROTATION.contains(block)) {
                continue;
            }

            if (block.block() instanceof AEBaseBlock) {
                // This is a default rotating model if the base-block uses an AE block entity
                // which exposes UP/FRONT as extended props
                register(block, AutoRotatingBakedModel::new);
            }
        }

        modEventBus.addListener(InitAutoRotatingModel::onModelBake);
    }

    private static void register(BlockDefinition<?> block, Function<BakedModel, BakedModel> customizer) {
        String path = block.id().getPath();
        CUSTOMIZERS.put(path, customizer);
    }

    private static BakedModel customizeCraftingMonitorModel(BakedModel model) {
        // The formed model handles rotations itself, the unformed one does not
        if (model instanceof MonitorBakedModel) {
            return model;
        }
        return new AutoRotatingBakedModel(model);
    }

    private static void onModelBake(BakingCompleted event) {
        Map<ResourceLocation, BakedModel> modelRegistry = event.getModels();
        Set<ResourceLocation> keys = Sets.newHashSet(modelRegistry.keySet());
        BakedModel missingModel = modelRegistry.get(ModelBakery.MISSING_MODEL_LOCATION);

        for (ResourceLocation location : keys) {
            if (!location.getNamespace().equals(AppEng.MOD_ID)) {
                continue;
            }

            BakedModel orgModel = modelRegistry.get(location);

            // Don't customize the missing model. This causes Forge to swallow exceptions
            if (orgModel == missingModel) {
                continue;
            }

            Function<BakedModel, BakedModel> customizer = CUSTOMIZERS.get(location.getPath());
            if (customizer != null) {
                BakedModel newModel = customizer.apply(orgModel);

                if (newModel != orgModel) {
                    modelRegistry.put(location, newModel);
                }
            }
        }
    }

}
