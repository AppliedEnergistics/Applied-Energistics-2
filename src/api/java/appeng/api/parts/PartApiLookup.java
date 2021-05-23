package appeng.api.parts;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.util.Direction;

import appeng.api.util.AEPartLocation;

/**
 * Exposes {@linkplain BlockApiLookup Block APIs} for parts. This allows parts to answer to API queries made via
 * {@link BlockApiLookup#find} on AE2's multipart blocks.
 */
public interface PartApiLookup {
    /**
     * Register a function mapping the context of the passed lookup to {@link AEPartLocation}. This is necessary when
     * the context is not {@link Direction} nor {@link AEPartLocation}!
     * <p>
     * The location is only used to resolve which part the query targets; the api provider will receive the original
     * context. If the mapping function returns null, no API will be returned for the query.
     * <p>
     * If multiple mapping functions are registered for a given lookup, it is the first that will be used.
     */
    <A, C> void registerCustomContext(BlockApiLookup<A, C> lookup, Function<C, AEPartLocation> mappingFunction);

    /**
     * Expose an API for a part class.
     * <p>
     * When looking for an API instance, providers are queried starting from the class of the part, and then moving up
     * to its superclass, and so on, until a provider returning a nonnull API is found.
     * <p>
     * If the context of the lookup is not {@link Direction} nor {@link AEPartLocation}, you need to register a mapping
     * function for your custom context! That must be done before this function is called. Currently the query will fail
     * silently, but IT WILL throw an exception in the future!
     */
    <A, C, P extends IPart> void register(BlockApiLookup<A, C> lookup, PartApiProvider<A, C, P> provider,
            Class<P> partClass);

    @FunctionalInterface
    interface PartApiProvider<A, C, P extends IPart> {
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
