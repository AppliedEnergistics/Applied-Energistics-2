package appeng.mixins;

import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPhase.class)
public interface RenderPhaseMixin {

    @Accessor("TRANSLUCENT_TRANSPARENCY")
    static RenderPhase.Transparency getTranslucentTransparency() {
        throw new AssertionError("Mixin dummy");
    }

}
