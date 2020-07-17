package appeng.hooks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.registry.RegistryTracker;

public interface RegisterDimensionTypeCallback {

    Event<RegisterDimensionTypeCallback> EVENT = EventFactory.createArrayBacked(RegisterDimensionTypeCallback.class, (listeners) -> (registryTracker) -> {
        for (RegisterDimensionTypeCallback listener : listeners) {
            listener.addDimensionTypes(registryTracker);
        }
    });

    void addDimensionTypes(RegistryTracker.Modifiable registryTracker);

}
