package appeng.bootstrap;

import java.util.Map;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.ResourceLocation;

@FunctionalInterface
public interface ModelsReloadCallback {

    Event<ModelsReloadCallback> EVENT = EventFactory.createArrayBacked(ModelsReloadCallback.class,
            (listeners) -> (loadedModels) -> {
                for (ModelsReloadCallback listener : listeners) {
                    listener.onModelsReloaded(loadedModels);
                }
            });

    void onModelsReloaded(Map<ResourceLocation, IBakedModel> loadedModels);

}
