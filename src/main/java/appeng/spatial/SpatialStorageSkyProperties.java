package appeng.spatial;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.SkyProperties;
import net.minecraft.util.math.Vec3d;

/**
 * Defines properties for how the sky in the spatial storage world is rendered.
 */
@Environment(EnvType.CLIENT)
public class SpatialStorageSkyProperties {

    // See the fabric version of this to get any idea what its doing
    public static final SkyProperties INSTANCE = new SkyProperties(Float.NaN /* disables clouds */, false,
            SkyProperties.SkyType.NONE /* we use a custom render mixin */, true, false) {
        @Override
        public Vec3d adjustSkyColor(Vec3d color, float sunHeight) {
            return Vec3d.ZERO;
        }

        @Override
        public boolean useThickFog(int camX, int camY) {
            return false;
        }

        @Nullable
        @Override
        public float[] getSkyColor(float skyAngle, float tickDelta) {
            return null;
        }
    };

}
