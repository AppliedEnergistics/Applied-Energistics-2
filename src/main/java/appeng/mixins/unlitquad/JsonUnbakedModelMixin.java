package appeng.mixins.unlitquad;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import appeng.hooks.UnlitQuadHooks;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

/**
 * This mixin hooks into conversion from {@link BlockPartFace} to {@link BakedQuad} to apply our unlit extensions if
 * the block part face is an instance of our marker class {@link appeng.hooks.UnlitQuadHooks.UnlitModelElementFace}.
 */
@Mixin(BlockModel.class)
public class JsonUnbakedModelMixin {

    @Inject(method = "createQuad", at = @At("RETURN"), cancellable = true, require = 1, allow = 1)
    private static void onBakeFace(BlockPart element, BlockPartFace elementFace, TextureAtlasSprite sprite, Direction side,
            IModelTransform settings, ResourceLocation id, CallbackInfoReturnable<BakedQuad> cri) {
        if (elementFace instanceof UnlitQuadHooks.UnlitModelElementFace) {
            cri.setReturnValue(UnlitQuadHooks.makeUnlit(cri.getReturnValue()));
        }
    }

}
