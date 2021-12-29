package appeng.mixins;

import appeng.client.render.tesr.CellLedRenderer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedMap;

@Mixin(RenderBuffers.class)
public class RenderBuffersMixin {
    @Shadow
    private SortedMap<RenderType, BufferBuilder> fixedBuffers;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addLedRenderType(CallbackInfo ci) {
        fixedBuffers.put(CellLedRenderer.RENDER_LAYER, new BufferBuilder(CellLedRenderer.RENDER_LAYER.bufferSize()));
    }
}
