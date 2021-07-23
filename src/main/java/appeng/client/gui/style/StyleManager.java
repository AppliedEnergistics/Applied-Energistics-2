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

package appeng.client.gui.style;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import appeng.core.AppEng;

/**
 * Manages AE2 GUI styles found in resource packs.
 */
public final class StyleManager {

    private static final Map<String, ScreenStyle> styleCache = new HashMap<>();
    public static final String PROP_INCLUDES = "includes";

    private static ResourceManager resourceManager;

    private static String getBasePath(String path) {
        int lastSep = path.lastIndexOf('/');
        if (lastSep == -1) {
            return "";
        } else {
            return path.substring(0, lastSep + 1);
        }
    }

    public static ScreenStyle loadStyleDoc(String path) throws IOException {
        ScreenStyle style = loadStyleDocInternal(path);
        // We only require the final style-document to be fully valid,
        // includes are allowed to be partially valid.
        style.validate();
        return style;
    }

    private static JsonObject loadMergedJsonTree(String path, Set<String> loadedFiles, Set<String> resourcePacks)
            throws IOException {
        Preconditions.checkArgument(path.startsWith("/"), "Path needs to start with slash");

        // The resource manager doesn't like relative paths like that, so we resolve them here
        if (path.contains("..")) {
            path = URI.create(path).normalize().toString();
        }

        if (!loadedFiles.add(path)) {
            throw new IllegalStateException("Recursive style includes: " + loadedFiles);
        }

        if (resourceManager == null) {
            throw new IllegalStateException("ResourceManager was not set. Was initialize called?");
        }

        String basePath = getBasePath(path);

        JsonObject document;
        try (Resource resource = resourceManager.getResource(AppEng.makeId(path.substring(1)))) {
            resourcePacks.add(resource.getSourceName());
            document = ScreenStyle.GSON.fromJson(new InputStreamReader(resource.getInputStream()), JsonObject.class);
        }

        // Resolve the includes present in the document
        if (document.has(PROP_INCLUDES)) {
            String[] includes = ScreenStyle.GSON.fromJson(document.get(PROP_INCLUDES), String[].class);

            List<JsonObject> layers = new ArrayList<>();
            for (String include : includes) {
                layers.add(loadMergedJsonTree(basePath + include, loadedFiles, resourcePacks));
            }
            layers.add(document);
            document = combineLayers(layers);
        }

        return document;

    }

    // Builds a new JSON document from layered documents
    private static JsonObject combineLayers(List<JsonObject> layers) {
        JsonObject result = new JsonObject();

        // Start by copying over all properties layer-by-layer while overwriting properties set by
        // previous layers.
        for (JsonObject layer : layers) {
            for (Map.Entry<String, JsonElement> entry : layer.entrySet()) {
                result.add(entry.getKey(), entry.getValue());
            }
        }

        // Merge the following keys by merging their properties
        mergeObjectKeys("slots", layers, result);
        mergeObjectKeys("text", layers, result);
        mergeObjectKeys("palette", layers, result);
        mergeObjectKeys("images", layers, result);
        mergeObjectKeys("terminalStyle", layers, result);
        mergeObjectKeys("widgets", layers, result);

        return result;
    }

    /**
     * Merges a single object property across multiple layers by merging the object keys. Higher layers win when there
     * is a conflict.
     */
    private static void mergeObjectKeys(String propertyName, List<JsonObject> layers, JsonObject target)
            throws JsonParseException {
        JsonObject mergedObject = null;
        for (JsonObject layer : layers) {
            JsonElement layerEl = layer.get(propertyName);
            if (layerEl != null) {
                if (!layerEl.isJsonObject()) {
                    throw new JsonParseException("Expected " + propertyName + " to be an object, but was: " + layerEl);
                }
                JsonObject layerObj = layerEl.getAsJsonObject();

                if (mergedObject == null) {
                    mergedObject = new JsonObject();
                }
                for (Map.Entry<String, JsonElement> entry : layerObj.entrySet()) {
                    mergedObject.add(entry.getKey(), entry.getValue());
                }
            }
        }

        if (mergedObject != null) {
            target.add(propertyName, mergedObject);
        }
    }

    private static ScreenStyle loadStyleDocInternal(String path) {

        ScreenStyle style = styleCache.get(path);
        if (style != null) {
            return style;
        }

        Set<String> resourcePacks = new HashSet<>();
        try {
            JsonObject document = loadMergedJsonTree(path, new HashSet<>(), resourcePacks);

            style = ScreenStyle.GSON.fromJson(document, ScreenStyle.class);

            style.validate();
        } catch (Exception e) {
            throw new JsonParseException("Failed to load style from " + path + " (packs: " + resourcePacks + ")", e);
        }

        styleCache.put(path, style);
        return style;
    }

    public static void initialize(ResourceManager resourceManager) {
        if (resourceManager instanceof ReloadableResourceManager) {
            ((ReloadableResourceManager) resourceManager).registerReloadListener(new ReloadListener());
        }
        setResourceManager(resourceManager);
    }

    private static void setResourceManager(ResourceManager resourceManager) {
        StyleManager.resourceManager = resourceManager;
        StyleManager.styleCache.clear();
    }

    private static class ReloadListener implements ISelectiveResourceReloadListener {
        @Override
        public void onResourceManagerReload(ResourceManager resourceManager,
                                            Predicate<IResourceType> resourcePredicate) {
            setResourceManager(resourceManager);
        }
    }

}
