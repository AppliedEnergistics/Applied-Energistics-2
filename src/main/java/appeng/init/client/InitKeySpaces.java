package appeng.init.client;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.resources.ResourceLocation;

import appeng.api.storage.AEKeySpace;
import appeng.api.storage.AEKeySpaces;
import appeng.api.storage.AEKeySpacesInternal;
import appeng.core.AppEng;

public final class InitKeySpaces {
    private static final ResourceLocation REGISTRY_ID = AppEng.makeId("keyspaces");

    private InitKeySpaces() {
    }

    public static void init() {
        var registry = FabricRegistryBuilder
                .createSimple(AEKeySpace.class, REGISTRY_ID)
                .attribute(RegistryAttribute.SYNCED)
                .attribute(RegistryAttribute.PERSISTED)
                .buildAndRegister();
        AEKeySpacesInternal.setRegistry(registry);

        AEKeySpaces.register(AEKeySpace.items());
        AEKeySpaces.register(AEKeySpace.fluids());
    }
}
