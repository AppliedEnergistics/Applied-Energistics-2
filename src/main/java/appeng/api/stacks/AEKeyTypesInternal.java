package appeng.api.stacks;

import java.util.function.Supplier;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.ApiStatus;

import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Manages the registry used to synchronize key spaces to the client.
 */
@ApiStatus.Internal
public final class AEKeyTypesInternal {
    private static Supplier<IForgeRegistry<AEKeyType>> registry;

    private AEKeyTypesInternal() {
    }

    public static ForgeRegistry<AEKeyType> getRegistry() {
        var registry = AEKeyTypesInternal.registry.get();
        Preconditions.checkState(registry != null, "AE2 isn't initialized yet.");
        return (ForgeRegistry<AEKeyType>) registry;
    }

    public static void setRegistry(Supplier<IForgeRegistry<AEKeyType>> registry) {
        Preconditions.checkState(AEKeyTypesInternal.registry == null);
        AEKeyTypesInternal.registry = registry;
    }

    public static void register(AEKeyType keyType) {
        getRegistry().register(keyType.getId(), keyType);
    }
}
