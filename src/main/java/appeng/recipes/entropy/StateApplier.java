package appeng.recipes.entropy;

import net.minecraft.network.PacketBuffer;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;

import appeng.core.AELog;

/**
 * Generic template to apply named properties to block and fluid states.
 */
abstract class StateApplier<SH extends StateHolder<O, SH>, O> {
    private final String key;
    private final String value;

    public StateApplier(String key, String value) {
        this.key = key;
        this.value = value;
    }

    protected StateApplier(PacketBuffer buffer) {
        this.key = buffer.readString();
        this.value = buffer.readString();
    }

    SH apply(SH state) {
        StateContainer<O, SH> base = getStateContainer(state);

        Property<?> property = base.getProperty(key);
        if (property == null) {
            AELog.warn("Cannot set unknown state property %s in entropy manipulator recipe", key);
            return state;
        }
        return applyProperty(state, property);
    }

    private <T extends Comparable<T>> SH applyProperty(SH state, Property<T> property) {
        T propertyValue = property.parseValue(value).orElse(null);
        if (propertyValue == null) {
            AELog.warn("Cannot set invalid value '%s' for state property %s in entropy manipulator recipe", value,
                    key);
            return state;
        }
        return state.with(property, propertyValue);
    }

    void writeToPacket(PacketBuffer buffer) {
        buffer.writeString(key);
        buffer.writeString(value);
    }

    protected abstract StateContainer<O, SH> getStateContainer(SH state);

}
