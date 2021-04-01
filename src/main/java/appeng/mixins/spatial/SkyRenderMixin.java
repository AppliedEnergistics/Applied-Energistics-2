package appeng.mixins.spatial;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import appeng.client.render.SpatialSkyRender;
import appeng.spatial.SpatialStorageDimensionIds;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;

@Mixin(WorldRenderer.class)
public class SkyRenderMixin {

    @Shadow
    private Minecraft client;

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    public void renderSky(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (client.world.getDimensionKey() == SpatialStorageDimensionIds.WORLD_ID) {
            SpatialSkyRender.getInstance().render(matrices);
            ci.cancel();
        }
    }

}
