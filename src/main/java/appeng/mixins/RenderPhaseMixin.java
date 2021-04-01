package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.RenderState;

@Mixin(RenderState.class)
public interface RenderPhaseMixin {

    @Accessor("TRANSLUCENT_TRANSPARENCY")
    static RenderState.TransparencyState getTranslucentTransparency() {
        throw new AssertionError("Mixin dummy");
    }

}
