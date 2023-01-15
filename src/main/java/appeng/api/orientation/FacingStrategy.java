package appeng.api.orientation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Implements a strategy that allows blocks to be oriented using a single directional property.
 */
class FacingStrategy implements IOrientationStrategy {
    private final DirectionProperty property;
    private final List<Property<?>> properties;
    private final boolean attached;

    public FacingStrategy(DirectionProperty property, boolean attached) {
        this.property = property;
        this.properties = Collections.singletonList(property);
        this.attached = attached;
    }

    @Override
    public Direction getFacing(BlockState state) {
        return state.getValue(property);
    }

    @Override
    public BlockState setFacing(BlockState state, Direction facing) {
        if (!property.getPossibleValues().contains(facing)) {
            return state;
        }
        return state.setValue(property, facing);
    }

    @Override
    public BlockState getStateForPlacement(BlockState state, BlockPlaceContext context) {
        return setFacing(state, context.getClickedFace());
    }

    @Override
    public Collection<Property<?>> getProperties() {
        return properties;
    }
}
