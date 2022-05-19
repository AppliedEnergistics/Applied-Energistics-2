package appeng.block.crafting;

import java.util.function.Function;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;

import appeng.client.render.crafting.CraftingCubeModel;
import appeng.client.render.crafting.LightBakedModel;
import appeng.client.render.crafting.MonitorBakedModel;
import appeng.client.render.crafting.UnitBakedModel;

public enum CraftingUnitType implements ICraftingUnitType {
    UNIT(0),
    ACCELERATOR(0),
    STORAGE_1K(1),
    STORAGE_4K(4),
    STORAGE_16K(16),
    STORAGE_64K(64),
    STORAGE_256K(256),
    MONITOR(0);

    private final int storageKb;

    CraftingUnitType(int storageKb) {
        this.storageKb = storageKb;
    }

    @Override
    public int getStorageBytes() {
        return 1024 * this.storageKb;
    }

    @Override
    public boolean isAccelerator() {
        return this == ACCELERATOR;
    }

    @Override
    public boolean isStatus() {
        return this == MONITOR;
    }

    @Override
    public Material getLightMaterial() {
        return switch (this) {
            case ACCELERATOR -> CraftingCubeModel.ACCELERATOR_LIGHT;
            case STORAGE_1K -> CraftingCubeModel.STORAGE_1K_LIGHT;
            case STORAGE_4K -> CraftingCubeModel.STORAGE_4K_LIGHT;
            case STORAGE_16K -> CraftingCubeModel.STORAGE_16K_LIGHT;
            case STORAGE_64K -> CraftingCubeModel.STORAGE_64K_LIGHT;
            case STORAGE_256K -> CraftingCubeModel.STORAGE_256K_LIGHT;
            default -> throw new IllegalArgumentException(
                    "Crafting unit type " + this + " does not use a light texture.");
        };
    }

    @Override
    public BakedModel getBakedModel(Function<Material, TextureAtlasSprite> spriteGetter,
            TextureAtlasSprite ringCorner,
            TextureAtlasSprite ringSideHor,
            TextureAtlasSprite ringSideVer) {
        return switch (this) {
            case UNIT -> new UnitBakedModel(ringCorner, ringSideHor, ringSideVer,
                    spriteGetter.apply(CraftingCubeModel.UNIT_BASE));
            case ACCELERATOR, STORAGE_1K, STORAGE_4K, STORAGE_16K, STORAGE_64K, STORAGE_256K -> new LightBakedModel(
                    ringCorner, ringSideHor, ringSideVer, spriteGetter.apply(CraftingCubeModel.LIGHT_BASE),
                    spriteGetter.apply(this.getLightMaterial()));
            case MONITOR -> new MonitorBakedModel(ringCorner, ringSideHor, ringSideVer,
                    spriteGetter.apply(CraftingCubeModel.UNIT_BASE), spriteGetter.apply(CraftingCubeModel.MONITOR_BASE),
                    spriteGetter.apply(CraftingCubeModel.MONITOR_LIGHT_DARK),
                    spriteGetter.apply(CraftingCubeModel.MONITOR_LIGHT_MEDIUM),
                    spriteGetter.apply(CraftingCubeModel.MONITOR_LIGHT_BRIGHT));
        };
    }
}
