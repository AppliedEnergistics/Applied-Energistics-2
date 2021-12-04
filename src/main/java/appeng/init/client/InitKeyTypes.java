package appeng.init.client;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.resources.ResourceLocation;

import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.AEKeyTypesInternal;
import appeng.core.AppEng;

public final class InitKeyTypes {
    private static final ResourceLocation REGISTRY_ID = AppEng.makeId("keytypes");

    private InitKeyTypes() {
    }

    public static void init() {
        var registry = FabricRegistryBuilder
                .createSimple(AEKeyType.class, REGISTRY_ID)
                .attribute(RegistryAttribute.SYNCED)
                .attribute(RegistryAttribute.PERSISTED)
                .buildAndRegister();
        AEKeyTypesInternal.setRegistry(registry);

        AEKeyTypes.register(AEKeyType.items());
        AEKeyTypes.register(AEKeyType.fluids());
    }
}
