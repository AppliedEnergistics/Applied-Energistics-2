package appeng.api.stacks;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

/**
 * Manages the registry used to synchronize key spaces to the client.
 */
@ApiStatus.Internal
public final class AEKeyTypesInternal {
    @Nullable
    private static MappedRegistry<AEKeyType> registry;

    private AEKeyTypesInternal() {
    }

    public static Registry<AEKeyType> getRegistry() {
        Preconditions.checkState(registry != null, "AE2 isn't initialized yet.");
        return registry;
    }

    public static void setRegistry(MappedRegistry<AEKeyType> registry) {
        Preconditions.checkState(AEKeyTypesInternal.registry == null);
        AEKeyTypesInternal.registry = registry;
    }

    public static void register(AEKeyType keyType) {
        Registry.register(getRegistry(), keyType.getId(), keyType);
    }
}
