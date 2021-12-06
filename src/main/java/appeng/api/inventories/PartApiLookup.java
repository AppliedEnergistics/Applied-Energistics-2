package appeng.api.inventories;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;

/**
 * Exposes {@linkplain BlockApiLookup Block APIs} for parts. This allows parts to answer to API queries made via
 * {@link BlockApiLookup#find} on AE2's multipart blocks.
 */
public final class PartApiLookup {

    private PartApiLookup() {
    }

    // Used as identity hash maps - BlockApiLookup has identity semantics.
    // We use ApiProviderMap because it ensures non-null keys and values.
    private static final ApiProviderMap<BlockApiLookup<?, Direction>, ApiProviderMap<Class<?>, PartApiProvider<?, ?>>> providers = ApiProviderMap
            .create();
    private static final Set<BlockApiLookup<?, Direction>> cableBusRegisteredLookups = ConcurrentHashMap.newKeySet();
    private static final Set<BlockEntityType<? extends IPartHost>> hostTypes = ConcurrentHashMap.newKeySet();

    /**
     * Expose an API for a part class.
     * <p>
     * When looking for an API instance, providers are queried starting from the class of the part, and then moving up
     * to its superclass, and so on, until a provider returning a nonnull API is found.
     */
    @SuppressWarnings("ConstantConditions")
    public static <A, C, P extends IPart> void register(BlockApiLookup<A, Direction> lookup,
            PartApiProvider<A, P> provider,
            Class<P> partClass) {
        Objects.requireNonNull(lookup, "Registered lookup may not be null.");

        if (partClass.isInterface()) {
            throw new IllegalArgumentException(
                    "Part lookup cannot be registered for interface:" + partClass.getCanonicalName());
        }

        providers.putIfAbsent(lookup, ApiProviderMap.create());
        ApiProviderMap<Class<?>, PartApiProvider<?, ?>> toProviderMap = providers.get(lookup);

        if (toProviderMap.putIfAbsent(partClass, provider) != null) {
            throw new IllegalArgumentException(
                    "Duplicate provider registration for part class " + partClass.getCanonicalName());
        }

        if (cableBusRegisteredLookups.add(lookup)) {
            for (var hostType : hostTypes) {
                registerLookup(hostType, lookup);
            }
        }
    }

    private static <A> void registerLookup(BlockEntityType<? extends IPartHost> hostType,
            BlockApiLookup<A, Direction> lookup) {

        lookup.registerForBlockEntity((be, direction) -> {
            if (direction == null) {
                return null;
            } else {
                var part = be.getPart(direction);

                if (part != null) {
                    return find(lookup, part);
                } else {
                    return null;
                }
            }
        }, hostType);
    }

    /**
     * Adds a new type of block entity that will participate in forwarding API lookups to its attached parts.
     */
    public static <T extends BlockEntity & IPartHost> void addHostType(BlockEntityType<T> hostType) {
        if (hostTypes.add(hostType)) {
            for (var api : cableBusRegisteredLookups) {
                registerLookup(hostType, api);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <A> A find(BlockApiLookup<A, Direction> lookup, IPart part) {
        ApiProviderMap<Class<?>, PartApiProvider<?, ?>> toProviderMap = providers.get(lookup);

        if (lookup == null)
            return null;

        for (Class<?> klass = part.getClass(); klass != Object.class; klass = klass.getSuperclass()) {
            PartApiProvider<A, IPart> provider = (PartApiProvider<A, IPart>) toProviderMap.get(klass);

            if (provider != null) {
                A instance = provider.find(part);

                if (instance != null) {
                    return instance;
                }
            }
        }

        return null;
    }

    @FunctionalInterface
    public interface PartApiProvider<A, P extends IPart> {
        /**
         * Return an API of type {@code A} if available in the given part, or {@code null} otherwise.
         *
         * @param part The part.
         * @return An API of type {@code A}, or {@code null} if no API is available.
         */
        @Nullable
        A find(P part);
    }

}
