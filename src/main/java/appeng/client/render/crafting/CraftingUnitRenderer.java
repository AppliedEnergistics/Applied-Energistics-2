package appeng.client.render.crafting;

import java.util.function.Function;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;

import appeng.block.crafting.CraftingUnitType;

public class CraftingUnitRenderer extends AbstractCraftingUnitRenderer<CraftingUnitType> {

    public CraftingUnitRenderer(CraftingUnitType type) {
        super(type);
    }

    @Override
    public TextureAtlasSprite getLightMaterial(Function<Material, TextureAtlasSprite> textureGetter) {
        return switch (this.type) {
            case ACCELERATOR -> textureGetter.apply(CraftingCubeModel.ACCELERATOR_LIGHT);
            case STORAGE_1K -> textureGetter.apply(CraftingCubeModel.STORAGE_1K_LIGHT);
            case STORAGE_4K -> textureGetter.apply(CraftingCubeModel.STORAGE_4K_LIGHT);
            case STORAGE_16K -> textureGetter.apply(CraftingCubeModel.STORAGE_16K_LIGHT);
            case STORAGE_64K -> textureGetter.apply(CraftingCubeModel.STORAGE_64K_LIGHT);
            case STORAGE_256K -> textureGetter.apply(CraftingCubeModel.STORAGE_256K_LIGHT);
            default -> throw new IllegalArgumentException(
                    "Crafting unit type " + this.type + " does not use a light texture.");
        };
    }

    @Override
    public BakedModel getBakedModel(Function<Material, TextureAtlasSprite> spriteGetter,
            TextureAtlasSprite ringCorner,
            TextureAtlasSprite ringSideHor,
            TextureAtlasSprite ringSideVer) {
        return switch (type) {
            case UNIT -> new UnitBakedModel(ringCorner, ringSideHor, ringSideVer,
                    spriteGetter.apply(CraftingCubeModel.UNIT_BASE));
            case ACCELERATOR, STORAGE_1K, STORAGE_4K, STORAGE_16K, STORAGE_64K, STORAGE_256K -> new LightBakedModel(
                    ringCorner, ringSideHor, ringSideVer, spriteGetter.apply(CraftingCubeModel.LIGHT_BASE),
                    this.getLightMaterial(spriteGetter));
            case MONITOR -> new MonitorBakedModel(ringCorner, ringSideHor, ringSideVer,
                    spriteGetter.apply(CraftingCubeModel.UNIT_BASE), spriteGetter.apply(CraftingCubeModel.MONITOR_BASE),
                    spriteGetter.apply(CraftingCubeModel.MONITOR_LIGHT_DARK),
                    spriteGetter.apply(CraftingCubeModel.MONITOR_LIGHT_MEDIUM),
                    spriteGetter.apply(CraftingCubeModel.MONITOR_LIGHT_BRIGHT));
        };
    }
}
