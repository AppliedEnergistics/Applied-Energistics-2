package appeng.api.integrations.jei;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Register your {@link IngredientConverter} instances for JEI here.
 */
public final class IngredientConverters {
    private static List<IngredientConverter<?>> converters = ImmutableList.of();

    private IngredientConverters() {
    }

    /**
     * Registers a new ingredient converter for handling custom {@link appeng.api.stacks.AEKey key types} in the AE2 JEI
     * addon.
     *
     * @return false if a converter for the converters type has already been registered.
     */
    public static synchronized boolean register(IngredientConverter<?> converter) {
        for (var existingConverter : converters) {
            if (existingConverter.getIngredientType() == converter.getIngredientType()) {
                return false;
            }
        }
        converters = ImmutableList.<IngredientConverter<?>>builder()
                .addAll(converters)
                .add(converter)
                .build();
        return true;
    }

    /**
     * @return The currently registered converters.
     */
    public static synchronized List<IngredientConverter<?>> getConverters() {
        return converters;
    }
}
