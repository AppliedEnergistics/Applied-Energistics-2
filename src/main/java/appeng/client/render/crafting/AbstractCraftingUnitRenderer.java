package appeng.client.render.crafting;

import java.util.function.Function;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;

import appeng.block.crafting.ICraftingUnitType;

public abstract class AbstractCraftingUnitRenderer<T extends ICraftingUnitType> {

    protected final T type;

    public AbstractCraftingUnitRenderer(T type) {
        this.type = type;
    }

    public abstract TextureAtlasSprite getLightMaterial(Function<Material, TextureAtlasSprite> textureGetter);

    public abstract BakedModel getBakedModel(Function<Material, TextureAtlasSprite> spriteGetter,
            TextureAtlasSprite ringCorner,
            TextureAtlasSprite ringSideHor,
            TextureAtlasSprite ringSideVer);
}
