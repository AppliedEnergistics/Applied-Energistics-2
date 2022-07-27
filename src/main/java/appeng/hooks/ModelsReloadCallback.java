package appeng.hooks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface ModelsReloadCallback {

    Event<ModelsReloadCallback> EVENT = EventFactory.createArrayBacked(ModelsReloadCallback.class,
            (listeners) -> (location, model, missingModel) -> {
                for (ModelsReloadCallback listener : listeners) {
                    model = listener.onModelLoaded(location, model, missingModel);
                }
                return model;
            });

    BakedModel onModelLoaded(ResourceLocation location, BakedModel model, BakedModel missingModel);

}
