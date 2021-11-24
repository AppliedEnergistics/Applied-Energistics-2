package appeng.api.storage;

import com.google.common.base.Preconditions;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

import javax.annotation.Nullable;

/**
 * Manages the registry used to synchronize key spaces to the client.
 */
public final class AEKeySpacesInternal {
    @Nullable
    private static MappedRegistry<AEKeySpace> registry;

    private AEKeySpacesInternal() {
    }

    public static Registry<AEKeySpace> getRegistry() {
        Preconditions.checkState(registry != null, "AE2 isn't initialized yet.");
        return registry;
    }

    public static void setRegistry(MappedRegistry<AEKeySpace> registry) {
        Preconditions.checkState(AEKeySpacesInternal.registry == null);
        AEKeySpacesInternal.registry = registry;
    }

    public static void register(AEKeySpace keySpace) {
        Registry.register(getRegistry(), keySpace.getId(), keySpace);
    }
}
