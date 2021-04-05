package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Mixin(BakedQuad.class)
public interface BakedQuadAccessor {

    @Accessor
    TextureAtlasSprite getSprite();

}
