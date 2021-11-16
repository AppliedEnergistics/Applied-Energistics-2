package net.fabricmc.loader.impl.launch.knot;

import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.util.Objects;

public class MixinServiceKnotAccessor {
    public static IMixinTransformer getTransformer() {
        return Objects.requireNonNull(MixinServiceKnot.getTransformer());
    }
}
