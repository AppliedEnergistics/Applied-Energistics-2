package appeng.spatial;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Defines properties for how the sky in the spatial storage world is rendered.
 */
@Environment(EnvType.CLIENT)
public class SpatialStorageSkyProperties {

    // See the fabric version of this to get any idea what its doing
    public static final DimensionRenderInfo INSTANCE = new DimensionRenderInfo(Float.NaN /* disables clouds */, false,
            DimensionRenderInfo.FogType.field_25639 /* we use a custom render mixin */, true, false) {
        @Override
        public Vector3d method_28112(Vector3d color, float sunHeight) {
            return Vector3d.ZERO;
        }

        @Override
        public boolean method_28110(int camX, int camY) {
            return false;
        }

        @Nullable
        @Override
        public float[] method_28109(float skyAngle, float tickDelta) {
            return null;
        }
    };

}
