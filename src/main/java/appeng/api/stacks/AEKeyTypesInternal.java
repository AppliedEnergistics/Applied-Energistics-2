package appeng.api.stacks;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.ApiStatus;

import net.minecraftforge.registries.ForgeRegistry;

/**
 * Manages the registry used to synchronize key spaces to the client.
 */
@ApiStatus.Internal
public final class AEKeyTypesInternal {
    @Nullable
    private static ForgeRegistry<AEKeyType> registry;

    private AEKeyTypesInternal() {
    }

    public static ForgeRegistry<AEKeyType> getRegistry() {
        Preconditions.checkState(registry != null, "AE2 isn't initialized yet.");
        return registry;
    }

    public static void setRegistry(ForgeRegistry<AEKeyType> registry) {
        Preconditions.checkState(AEKeyTypesInternal.registry == null);
        AEKeyTypesInternal.registry = registry;
    }

    public static void register(AEKeyType keyType) {
        registry.register(keyType);
    }
}
