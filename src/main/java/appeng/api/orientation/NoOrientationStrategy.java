package appeng.api.orientation;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.world.level.block.state.properties.Property;

class NoOrientationStrategy implements IOrientationStrategy {
    @Override
    public Collection<Property<?>> getProperties() {
        return Collections.emptyList();
    }
}
