package appeng.bootstrap;

import java.util.Map;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.Identifier;

@FunctionalInterface
public interface ModelsReloadCallback {

    Event<ModelsReloadCallback> EVENT = EventFactory.createArrayBacked(ModelsReloadCallback.class,
            (listeners) -> (loadedModels) -> {
                for (ModelsReloadCallback listener : listeners) {
                    listener.onModelsReloaded(loadedModels);
                }
            });

    void onModelsReloaded(Map<Identifier, BakedModel> loadedModels);

}
