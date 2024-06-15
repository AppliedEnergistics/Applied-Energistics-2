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

package appeng.hooks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.ExtraFaceData;

import appeng.core.AppEng;

/**
 * Implementation details of allowing quads to be defined as "unlit" in JSON models by specifying an "unlit" boolean
 * property on the face.
 */
public class UnlitQuadHooks {

    /**
     * Thread-Local flag to indicate that an enhanced Applied Energistics model is currently being deserialized.
     */
    private static final ThreadLocal<Boolean> ENABLE_UNLIT_EXTENSIONS = new ThreadLocal<>();

    /**
     * Notify the unlit model system that a specific model is about to be deserialized by {@link ModelBakery}.
     */
    public static void beginDeserializingModel(ResourceLocation location) {
        String namespace = location.getNamespace();
        if (namespace.equals(AppEng.MOD_ID)) {
            ENABLE_UNLIT_EXTENSIONS.set(true);
        }
    }

    /**
     * Notify the unlit model system that deserialization of a model has ended.
     */
    public static void endDeserializingModel() {
        ENABLE_UNLIT_EXTENSIONS.set(false);
    }

    public static boolean isUnlitExtensionEnabled() {
        Boolean b = ENABLE_UNLIT_EXTENSIONS.get();
        return b != null && b;
    }

    public static BlockElementFace enhanceModelElementFace(BlockElementFace modelElement, JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (GsonHelper.getAsBoolean(jsonObject, "unlit", false)) {
            return new BlockElementFace(modelElement.cullForDirection(), modelElement.tintIndex(),
                    modelElement.texture(), modelElement.uv(),
                    new ExtraFaceData(0xFFFFFFFF, 15, 15, true),
                    new org.apache.commons.lang3.mutable.MutableObject<>());
        }
        return modelElement;
    }

}
