package appeng.spatial;

import javax.annotation.Nullable;

import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StorageSkyProperties {

    // See the fabric version of this to get any idea what its doing
    public static final DimensionRenderInfo INSTANCE = new DimensionRenderInfo(Float.NaN /* disables clouds */, false,
            DimensionRenderInfo.FogType.NONE /* we use a custom render mixin */, true, false) {

        @Override
        public Vector3d func_230494_a_(Vector3d p_230494_1_, float p_230494_2_) {
            return Vector3d.ZERO;
        }

        @Override
        public boolean func_230493_a_(int p_230493_1_, int p_230493_2_) {
            return false;
        }

        @Nullable
        @Override
        public float[] func_230492_a_(float p_230492_1_, float p_230492_2_) {
            return null;
        }
    };

}
