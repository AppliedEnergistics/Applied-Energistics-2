package appeng.hooks;

import com.google.common.base.Preconditions;
import com.mojang.math.Transformation;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.util.Mth;

/**
 * Hooks into the loading of blockstate definition files to load additional variants.
 */
public final class BlockstateDefinitionHook {
    private static final Transformation[] TRANSFORMS = createTransformations();

    private static Transformation[] createTransformations() {
        var result = new Transformation[4 * 4 * 4];

        for (var xRot = 0; xRot < 360; xRot += 90) {
            for (var yRot = 0; yRot < 360; yRot += 90) {
                // Reuse existing transform from Vanilla
                result[indexFromAngles(xRot, yRot, 0)] = BlockModelRotation.by(xRot, yRot).getRotation();

                for (var zRot = 90; zRot < 360; zRot += 90) {
                    var idx = indexFromAngles(xRot, yRot, zRot);

                    // NOTE: Mojangs block model rotation rotates in the opposite direction
                    var quaternion = new Quaternionf().rotateYXZ(
                            -yRot * Mth.DEG_TO_RAD,
                            -xRot * Mth.DEG_TO_RAD,
                            -zRot * Mth.DEG_TO_RAD);

                    var rotationMatrix = new Matrix4f()
                            .identity()
                            .rotate(quaternion);
                    result[idx] = new Transformation(rotationMatrix);
                }
            }
        }

        return result;
    }

    private BlockstateDefinitionHook() {
    }

    public static Variant rotateVariant(Variant variant, int xRot, int yRot, int zRot) {
        var idx = indexFromAngles(xRot, yRot, zRot);

        return new Variant(
                variant.getModelLocation(),
                TRANSFORMS[idx],
                variant.isUvLocked(),
                variant.getWeight());
    }

    private static int indexFromAngles(int xRot, int yRot, int zRot) {
        Preconditions.checkArgument(xRot >= 0 && xRot < 360 && xRot % 90 == 0);
        Preconditions.checkArgument(yRot >= 0 && yRot < 360 && yRot % 90 == 0);
        Preconditions.checkArgument(zRot >= 0 && zRot < 360 && zRot % 90 == 0);
        return xRot / 90 * 16 + yRot / 90 * 4 + zRot / 90;
    }
}
