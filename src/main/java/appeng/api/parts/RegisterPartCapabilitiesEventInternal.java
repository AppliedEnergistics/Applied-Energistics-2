package appeng.api.parts;

import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class RegisterPartCapabilitiesEventInternal {
    private RegisterPartCapabilitiesEventInternal() {
    }

    public static void register(RegisterPartCapabilitiesEvent partEvent, RegisterCapabilitiesEvent event) {

        for (var registration : partEvent.capabilityRegistrations.values()) {
            register(partEvent, event, registration);
        }

    }

    private static <T, C> void register(RegisterPartCapabilitiesEvent partEvent,
                                 RegisterCapabilitiesEvent event, RegisterPartCapabilitiesEvent.BlockCapabilityRegistration<T, C> registration) {
        var provider = registration.buildProvider();
        for (var hostType : partEvent.hostTypes) {
            event.registerBlockEntity(registration.capability(), hostType, provider);
        }
    }
}
