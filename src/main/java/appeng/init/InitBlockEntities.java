package appeng.init;

import java.util.Map;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.core.api.definitions.ApiBlockEntities;

public final class InitBlockEntities {
    private InitBlockEntities() {
    }

    public static void init(IForgeRegistry<TileEntityType<?>> registry) {
        for (Map.Entry<ResourceLocation, TileEntityType<?>> entry : ApiBlockEntities.getTileEntityTypes().entrySet()) {
            registry.register(entry.getValue());
        }
    }
}
