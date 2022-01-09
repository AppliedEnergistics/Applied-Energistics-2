package appeng.init.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.AEKeyTypesInternal;
import appeng.core.AppEng;

public final class InitKeyTypes {
    private static final ResourceLocation REGISTRY_ID = AppEng.makeId("keytypes");

    private InitKeyTypes() {
    }

    public static void init() {
        var registry = (ForgeRegistry<AEKeyType>) new RegistryBuilder<AEKeyType>()
                .setType(AEKeyType.class)
                .setMaxID(127)
                .setName(REGISTRY_ID)
                .create();

        AEKeyTypesInternal.setRegistry(registry);

        AEKeyTypes.register(AEKeyType.items());
        AEKeyTypes.register(AEKeyType.fluids());
    }
}
