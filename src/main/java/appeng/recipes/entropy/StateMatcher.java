package appeng.recipes.entropy;

import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;

public interface StateMatcher {
    boolean matches(StateHolder<?, ?> state);

    void writeToPacket(PacketBuffer buffer);

    static StateMatcher read(StateContainer<?, ?> stateContainer, PacketBuffer buffer) {
        MatcherType type = buffer.readEnumValue(MatcherType.class);

        switch (type) {
            case SINGLE:
                return SingleValueMatcher.readFromPacket(stateContainer, buffer);
            case MULTIPLE:
                return MultipleValuesMatcher.readFromPacket(stateContainer, buffer);
            case RANGE:
                return RangeValueMatcher.readFromPacket(stateContainer, buffer);
        }

        return null;
    }

    enum MatcherType {
        SINGLE, MULTIPLE, RANGE;
    }
}
