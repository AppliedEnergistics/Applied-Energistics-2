package appeng.recipes.entropy;

import net.minecraft.network.PacketBuffer;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.core.AELog;

/**
 * Generic template to apply named properties to block and fluid states.
 */
class StateApplier<T extends Comparable<T>> {
    private final Property<T> property;
    private final T value;

    private StateApplier(Property<T> property, String valueName) {
        this.property = property;
        this.value = PropertyUtils.getRequiredPropertyValue(property, valueName);
    }

    <SH extends StateHolder<O, SH>, O> SH apply(SH state) {
        return state.with(property, value);
    }

    void writeToPacket(PacketBuffer buffer) {
        buffer.writeString(property.getName());
        buffer.writeString(property.getName(value));
    }

    static StateApplier<?> create(StateContainer<?, ?> stateContainer, String propertyName, String value) {
        Property<?> property = PropertyUtils.getRequiredProperty(stateContainer, propertyName);
        return new StateApplier<>(property, value);
    }

    @OnlyIn(Dist.CLIENT)
    static StateApplier<?> readFromPacket(StateContainer<?, ?> stateContainer, PacketBuffer buffer) {
        String propertyName = buffer.readString();
        String value = buffer.readString();
        return create(stateContainer, propertyName, value);
    }

}
