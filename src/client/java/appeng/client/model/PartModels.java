package appeng.client.model;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.fml.ModLoader;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.parts.IPartItem;
import appeng.client.api.model.parts.ClientPart;
import appeng.client.api.model.parts.PartModel;
import appeng.client.api.model.parts.RegisterPartModelsEvent;

public final class PartModels {
    private static final Logger LOG = LoggerFactory.getLogger(PartModels.class);

    private static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends PartModel.Unbaked>> PART_MODEL_IDS = new ExtraCodecs.LateBoundIdMapper<>();
    private static final MapCodec<PartModel.Unbaked> MAP_CODEC = PART_MODEL_IDS.codec(ResourceLocation.CODEC)
            .dispatchMap(PartModel.Unbaked::codec, c -> c);
    public static final Codec<PartModel.Unbaked> CODEC = MAP_CODEC.codec();

    private Map<ResourceLocation, ClientPart> clientParts = null;

    public PartModels() {
        ModLoader.postEvent(new RegisterPartModelsEvent(PART_MODEL_IDS));
    }

    public CompletableFuture<Void> reload(ResourceManager resourceManager, Executor executor) {
        var fileToIdConverter = FileToIdConverter.json("ae2/parts");
        return CompletableFuture.supplyAsync(() -> {
            var clientParts = new HashMap<ResourceLocation, ClientPart>();
            SimpleJsonResourceReloadListener.scanDirectory(
                    resourceManager,
                    fileToIdConverter,
                    JsonOps.INSTANCE,
                    ClientPart.CODEC,
                    clientParts);
            return clientParts;
        }, executor)
                .thenAccept(clientParts -> {
                    this.clientParts = clientParts;

                    for (var entry : BuiltInRegistries.ITEM.entrySet()) {
                        var item = entry.getValue();
                        if (item instanceof IPartItem<?> partItem) {
                            // Skip cables, those are special
                            if (partItem.createPart() instanceof ICablePart) {
                                continue;
                            }

                            var itemId = entry.getKey().location();
                            var modelId = fileToIdConverter.idToFile(itemId);
                            if (!this.clientParts.containsKey(itemId)) {
                                LOG.warn("No part model loaded for part item ID {}. Expected at {}", itemId, modelId);
                            }
                        }
                    }
                });
    }

    @Nullable
    public PartModel.Unbaked getPartModel(ResourceLocation id) {
        var clientPart = clientParts.get(id);
        return clientPart != null ? clientPart.model() : null;
    }

    public Map<IPartItem<?>, PartModel.Unbaked> getUnbaked() {
        var result = new IdentityHashMap<IPartItem<?>, PartModel.Unbaked>(this.clientParts.size());

        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            if (entry.getValue() instanceof IPartItem<?> partItem) {
                var model = getPartModel(entry.getKey().location());
                if (model != null) {
                    result.put(partItem, model);
                }
            }
        }

        return result;
    }

    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        for (var clientPart : clientParts.values()) {
            clientPart.model().resolveDependencies(resolver);
        }
    }
}
