package appeng.api.integrations.emi;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Register your {@link EmiStackConverter} instances for EMI here.
 */
public final class EmiStackConverters {
    private static volatile List<EmiStackConverter> converters = ImmutableList.of();

    private EmiStackConverters() {
    }

    /**
     * Registers a new EMI stack-converter for handling custom {@link appeng.api.stacks.AEKey key types} in the AE2 EMI
     * addon.
     *
     * @return false if a converter for the converters type has already been registered.
     */
    public static synchronized boolean register(EmiStackConverter converter) {
        for (var existingConverter : converters) {
            if (existingConverter.getKeyType() == converter.getKeyType()) {
                return false;
            }
        }
        converters = ImmutableList.<EmiStackConverter>builder()
                .addAll(converters)
                .add(converter)
                .build();
        return true;
    }

    /**
     * @return The currently registered converters, called for every stack conversion
     */
    public static List<EmiStackConverter> getConverters() {
        return converters;
    }
}
