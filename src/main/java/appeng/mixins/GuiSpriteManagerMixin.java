package appeng.mixins;

import appeng.hooks.DarkModeHook;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiSpriteManager.class)
public class GuiSpriteManagerMixin extends TextureAtlasHolder {
    public GuiSpriteManagerMixin(TextureManager textureManager, ResourceLocation textureAtlasLocation, ResourceLocation atlasInfoLocation) {
        super(textureManager, textureAtlasLocation, atlasInfoLocation);
    }

    @WrapOperation(method = "getSprite", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/TextureAtlasHolder;getSprite(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private TextureAtlasSprite redirectToDarkModeSprite(GuiSpriteManager manager, ResourceLocation id, Operation<TextureAtlasSprite> original) {
        var replacedSprite = DarkModeHook.getReplacedSprite(id, textureAtlas.getTextures());
        if (replacedSprite != null) {
            return replacedSprite;
        }

        return original.call(manager, id);
    }
}
