package appeng.block.crafting;

import java.util.function.Function;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;

public interface ICraftingUnitType {
    int getStorageBytes();

    boolean isAccelerator();

    boolean isStatus();

    default boolean isStorage() {
        return this.getStorageBytes() > 0;
    }

    default Material getLightMaterial() {
        throw new IllegalArgumentException("Crafting unit type does not use a light texture.");
    };

    default BakedModel getBakedModel(Function<Material, TextureAtlasSprite> spriteGetter,
            TextureAtlasSprite ringCorner,
            TextureAtlasSprite ringSideHor,
            TextureAtlasSprite ringSideVer) {
        throw new IllegalArgumentException("Unsupported crafting unit type.");
    }
}
