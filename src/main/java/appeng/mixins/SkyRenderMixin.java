package appeng.mixins;

import appeng.client.render.SpatialSkyRender;
import appeng.spatial.SpatialDimensionManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class SkyRenderMixin {

    @Shadow(aliases = {"mc"})
    private Minecraft client;

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    public void renderSky(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (client.world.func_234922_V_() == SpatialDimensionManager.STORAGE_DIMENSION_TYPE) {
            SpatialSkyRender.getInstance().render(matrices);
            ci.cancel();
        }
    }

}
