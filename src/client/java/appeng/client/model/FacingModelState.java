package appeng.client.model;

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
            case DOWN -> BlockModelRotation.X90_Y0;
            case UP -> BlockModelRotation.X270_Y0;
            case NORTH -> BlockModelRotation.X0_Y0;
            case SOUTH -> BlockModelRotation.X0_Y180;
            case WEST -> BlockModelRotation.X0_Y270;
            case EAST -> BlockModelRotation.X0_Y90;
        };
    }
}
