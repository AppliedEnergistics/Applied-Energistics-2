package appeng.recipes.entropy;

import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;

final class PropertyUtils {
    private PropertyUtils() {
    }

    static Property<?> getRequiredProperty(StateContainer<?, ?> stateContainer, String name) {
        Property<?> property = stateContainer.getProperty(name);
        if (property == null) {
            throw new IllegalArgumentException("Unknown property: " + name + " on " + stateContainer.getOwner());
        }
        return property;
    }

    static <T extends Comparable<T>> T getRequiredPropertyValue(Property<T> property, String name) {
        return property.parseValue(name)
                .orElseThrow(() -> new IllegalArgumentException("Invalid value '" + name + "' for property "
                        + property.getName()));
    }
}
