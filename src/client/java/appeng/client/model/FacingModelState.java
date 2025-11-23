package appeng.client.model;

import com.mojang.math.Quadrant;

import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Modelstate to use for rotating AE2 models from their default north-facing orientation to the desired facing
 * (corresponding to {@link BlockStateProperties#FACING}.
 */
public final class FacingModelState {
    private FacingModelState() {
    }

    public static ModelState fromFacing(Direction facing) {
        return switch (facing) {
            case DOWN -> BlockModelRotation.get(Quadrant.fromXYZAngles(Quadrant.R90, Quadrant.R0, Quadrant.R0));
            case UP -> BlockModelRotation.get(Quadrant.fromXYZAngles(Quadrant.R270, Quadrant.R0, Quadrant.R0));
            case NORTH -> BlockModelRotation.get(Quadrant.fromXYZAngles(Quadrant.R0, Quadrant.R0, Quadrant.R0));
            case SOUTH -> BlockModelRotation.get(Quadrant.fromXYZAngles(Quadrant.R0, Quadrant.R180, Quadrant.R0));
            case WEST -> BlockModelRotation.get(Quadrant.fromXYZAngles(Quadrant.R0, Quadrant.R270, Quadrant.R0));
            case EAST -> BlockModelRotation.get(Quadrant.fromXYZAngles(Quadrant.R0, Quadrant.R90, Quadrant.R0));
        };
    }
}
