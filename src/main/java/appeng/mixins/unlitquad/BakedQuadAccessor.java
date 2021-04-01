package appeng.mixins.unlitquad;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BakedQuad.class)
public interface BakedQuadAccessor {

    @Accessor
    TextureAtlasSprite getSprite();

}
