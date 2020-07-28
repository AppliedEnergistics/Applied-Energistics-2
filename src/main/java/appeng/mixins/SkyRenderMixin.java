package appeng.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;

import appeng.client.render.SpatialSkyRender;
import appeng.spatial.SpatialDimensionManager;

@Mixin(value = WorldRenderer.class)
public class SkyRenderMixin {

    @Shadow
    private Minecraft mc;

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "renderSky(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", at = @At("HEAD"), cancellable = true)
    public void renderSky(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (mc.world.func_234922_V_() == SpatialDimensionManager.STORAGE_DIMENSION_TYPE) {
            SpatialSkyRender.getInstance().render(matrices);
            ci.cancel();
        }
    }

}
