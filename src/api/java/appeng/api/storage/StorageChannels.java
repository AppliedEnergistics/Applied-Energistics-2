package appeng.api.storage;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableMap;

import net.minecraft.resources.ResourceLocation;

import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;

@ThreadSafe
public final class StorageChannels {

    private static volatile ClassToInstanceMap<IStorageChannel<?>> registry = ImmutableClassToInstanceMap.of();

    private static volatile Map<ResourceLocation, IStorageChannel<?>> idRegistry = ImmutableMap.of();

    private StorageChannels() {
    }

    /**
     * Register a new storage channel.
     * <p>
     * AE2 already provides native channels for {@link IAEItemStack} and {@link IAEFluidStack}.
     * <p>
     * Each {@link IAEStack} subtype can only have a single factory instance. Overwriting is not intended. Each subtype
     * should be a direct one, this might be enforced at any time.
     * <p>
     * Channel class and factory instance can be used interchangeable as identifier. In most cases the factory instance
     * is used as key as having direct access the methods is more beneficial compared to being forced to query the
     * registry each time.
     * <p>
     * Caching the factory instance in a field or local variable is perfectly for performance reasons. But do not use
     * any AE2 internal field as they can change randomly between releases.
     *
     * @param channel        The channel type, must be a subtype of {@link IStorageChannel}
     * @param implementation An instance implementing the channel.
     */
    public static synchronized <T extends IAEStack<T>, C extends IStorageChannel<T>> void register(
            @Nonnull Class<C> channel,
            @Nonnull C implementation) {
        Preconditions.checkNotNull(channel, "channel");
        Preconditions.checkNotNull(implementation, "implementation");
        Preconditions.checkNotNull(implementation.getId(), "implementation.id");
        var existingChannel = registry.get(channel);
        Preconditions.checkState(existingChannel == null, "Implementation for channel %s is already registered: %s",
                channel, existingChannel);
        existingChannel = idRegistry.get(implementation.getId());
        Preconditions.checkState(existingChannel == null, "Implementation for channel ID %s is already registered: %s",
                implementation.getId(), existingChannel);

        registry = ImmutableClassToInstanceMap.<IStorageChannel<?>>builder()
                .putAll(registry)
                .put(channel, implementation)
                .build();
        idRegistry = ImmutableMap.<ResourceLocation, IStorageChannel<?>>builder()
                .putAll(idRegistry)
                .put(implementation.getId(), implementation)
                .build();
    }

    /**
     * Fetch the factory instance for a specific storage channel.
     * <p>
     * Channel must be a direct subtype of {@link IStorageChannel}.
     *
     * @param channel The channel type
     * @return the factory instance
     * @throws IllegalArgumentException when fetching an unregistered channel.
     */
    @Nonnull
    public static <T extends IAEStack<T>, C extends IStorageChannel<T>> C get(@Nonnull Class<C> channel) {
        var result = registry.getInstance(channel);
        if (result == null) {
            throw new IllegalArgumentException("No implementation registered for " + channel);
        }
        return result;
    }

    /**
     * An unmodifiable collection of all registered storage channels.
     * <p>
     * This is mainly used as helper to let storage grids construct their internal storage for each type.
     */
    @Nonnull
    public static Collection<IStorageChannel<? extends IAEStack<?>>> getAll() {
        return registry.values();
    }

    /**
     * @return AE2's storage channel for items.
     */
    @Nonnull
    public static IItemStorageChannel items() {
        return get(IItemStorageChannel.class);
    }

    /**
     * @return AE2's storage channel for fluids.
     */
    @Nonnull
    public static IFluidStorageChannel fluids() {
        return get(IFluidStorageChannel.class);
    }

}
