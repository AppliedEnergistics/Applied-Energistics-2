package appeng.api.stacks;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.callback.BakeCallback;

/**
 * Manages the registry used to synchronize key spaces to the client.
 */
@ApiStatus.Internal
public final class AEKeyTypesInternal {
    @Nullable
    private static Registry<AEKeyType> registry;

    @Nullable
    private static Set<AEKeyType> allTypes;

    private AEKeyTypesInternal() {
    }

    public static Registry<AEKeyType> getRegistry() {
        Preconditions.checkState(registry != null, "AE2 isn't initialized yet.");
        return registry;
    }

    public static void setRegistry(Registry<AEKeyType> registry) {
        Preconditions.checkState(AEKeyTypesInternal.registry == null);
        AEKeyTypesInternal.registry = registry;
        registry.addCallback((BakeCallback<AEKeyType>) (ignored -> {
            var types = new HashSet<AEKeyType>();
            for (var aeKeyType : registry) {
                types.add(aeKeyType);
            }
            allTypes = Set.copyOf(types);
        }));
    }

    public static Set<AEKeyType> getAllTypes() {
        Preconditions.checkState(allTypes != null, "AE2 isn't initialized yet.");
        return allTypes;
    }

    public static void register(AEKeyType keyType) {
        Registry.register(getRegistry(), keyType.getId(), keyType);
    }
}
