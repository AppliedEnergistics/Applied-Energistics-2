package appeng.mixins;

import appeng.client.render.SpatialSkyRender;
import appeng.spatial.SpatialDimensionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class SkyRenderMixin {

    @Shadow
    private MinecraftClient client;

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    public void renderSky(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (client.world.getDimensionRegistryKey() == SpatialDimensionManager.STORAGE_DIMENSION_TYPE) {
            SpatialSkyRender.getInstance().render(matrices);
            ci.cancel();
        }
    }

}
