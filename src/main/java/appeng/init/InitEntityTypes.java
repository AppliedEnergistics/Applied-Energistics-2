package appeng.init;

import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.core.api.definitions.ApiEntities;

public final class InitEntityTypes {

    private InitEntityTypes() {
    }

    public static void init(IForgeRegistry<EntityType<?>> registry) {
        for (EntityType<?> entityType : ApiEntities.getEntityTypes()) {
            registry.register(entityType);
        }
    }

}
