package appeng.block.orientation;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.world.level.block.state.properties.Property;

public class NoOrientationStrategy implements IOrientationStrategy {
    @Override
    public Collection<Property<?>> getProperties() {
        return Collections.emptyList();
    }
}
