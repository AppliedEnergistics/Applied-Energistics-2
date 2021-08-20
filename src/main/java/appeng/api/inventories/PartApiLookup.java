package appeng.api.inventories;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

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

    // These two are used as identity hash maps - BlockApiLookup has identity semantics.
    // We use ApiProviderMap because it ensures non-null keys and values.
    private static final ApiProviderMap<BlockApiLookup<?, ?>, Function<?, Direction>> mappings = ApiProviderMap
            .create();
    private static final ApiProviderMap<BlockApiLookup<?, ?>, ApiProviderMap<Class<?>, PartApiProvider<?, ?, ?>>> providers = ApiProviderMap
            .create();
    private static final Set<BlockApiLookup<?, ?>> cableBusRegisteredLookups = ConcurrentHashMap.newKeySet();
    private static final Set<BlockEntityType<? extends IPartHost>> hostTypes = ConcurrentHashMap.newKeySet();

    /**
     * Register a function mapping the context of the passed lookup to {@link Direction}. This is necessary when the
     * context is not {@link Direction}.
     * <p>
     * The location is only used to resolve which part the query targets; the api provider will receive the original
     * context. If the mapping function returns null, no API will be returned for the query.
     * <p>
     * If multiple mapping functions are registered for a given lookup, it is the first that will be used.
     */
    public static <A, C> void registerCustomContext(BlockApiLookup<A, C> lookup,
            Function<C, Direction> mappingFunction) {
        mappings.putIfAbsent(lookup, mappingFunction);
    }

    /**
     * Expose an API for a part class.
     * <p>
     * When looking for an API instance, providers are queried starting from the class of the part, and then moving up
     * to its superclass, and so on, until a provider returning a nonnull API is found.
     * <p>
     * If the context of the lookup is not {@link Direction}, you need to register a mapping function for your custom
     * context! That must be done before this function is called. Currently the query will fail silently, but IT WILL
     * throw an exception in the future!
     */
    @SuppressWarnings("ConstantConditions")
    public static <A, C, P extends IPart> void register(BlockApiLookup<A, C> lookup, PartApiProvider<A, C, P> provider,
            Class<P> partClass) {
        Objects.requireNonNull(lookup, "Registered lookup may not be null.");

        if (partClass.isInterface()) {
            throw new IllegalArgumentException(
                    "Part lookup cannot be registered for interface:" + partClass.getCanonicalName());
        }

        providers.putIfAbsent(lookup, ApiProviderMap.create());
        ApiProviderMap<Class<?>, PartApiProvider<?, ?, ?>> toProviderMap = providers.get(lookup);

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

    private static <A, C> void registerLookup(BlockEntityType<? extends IPartHost> hostType,
            BlockApiLookup<A, C> lookup) {

        lookup.registerForBlockEntities((be, context) -> {
            @Nullable
            Direction location = mapContext(lookup, context);

            if (location == null) {
                return null;
            } else {
                var partHost = (IPartHost) be;
                var part = partHost.getPart(location);

                if (part != null) {
                    return find(lookup, context, part);
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
    public static <C> Direction mapContext(BlockApiLookup<?, C> lookup, C context) {
        Function<C, Direction> mapping = (Function<C, Direction>) mappings.get(lookup);

        if (mapping != null) {
            return mapping.apply(context);
        } else if (context instanceof Direction direction) {
            return direction;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <A, C> A find(BlockApiLookup<A, C> lookup, C context, IPart part) {
        ApiProviderMap<Class<?>, PartApiProvider<?, ?, ?>> toProviderMap = providers.get(lookup);

        if (lookup == null)
            return null;

        for (Class<?> klass = part.getClass(); klass != Object.class; klass = klass.getSuperclass()) {
            PartApiProvider<A, C, IPart> provider = (PartApiProvider<A, C, IPart>) toProviderMap.get(klass);

            if (provider != null) {
                A instance = provider.find(part, context);

                if (instance != null) {
                    return instance;
                }
            }
        }

        return null;
    }

    @FunctionalInterface
    public interface PartApiProvider<A, C, P extends IPart> {
        /**
         * Return an API of type {@code A} if available in the given part with the given context, or {@code null}
         * otherwise.
         *
         * @param part    The part.
         * @param context Additional context passed to the query.
         * @return An API of type {@code A}, or {@code null} if no API is available.
         */
        @Nullable
        A find(P part, C context);
    }

}
