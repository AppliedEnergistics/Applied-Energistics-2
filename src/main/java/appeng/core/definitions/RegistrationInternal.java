package appeng.core.definitions;

import org.jetbrains.annotations.ApiStatus;

import net.neoforged.bus.api.IEventBus;

@ApiStatus.Internal
public final class RegistrationInternal {
    private RegistrationInternal() {
    }

    public static void subscribe(IEventBus modEventBus) {
        AEBlocks.DR.register(modEventBus);
        AEItems.DR.register(modEventBus);
        AEBlockEntities.DR.register(modEventBus);
    }

}
