package appeng.client.render.crafting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.SpriteGetter;

import appeng.block.crafting.CraftingUnitType;
import appeng.core.AppEng;
import net.minecraft.client.resources.model.sprite.SpriteId;

public class CraftingUnitModelProvider extends AbstractCraftingUnitModelProvider<CraftingUnitType>
        implements ModelDebugName {

    protected final static SpriteId RING_CORNER = texture("ring_corner");
    protected final static SpriteId RING_SIDE_HOR = texture("ring_side_hor");
    protected final static SpriteId RING_SIDE_VER = texture("ring_side_ver");
    protected final static SpriteId UNIT_BASE = texture("unit_base");
    protected final static SpriteId LIGHT_BASE = texture("light_base");
    protected final static SpriteId ACCELERATOR_LIGHT = texture("accelerator_light");
    protected final static SpriteId STORAGE_1K_LIGHT = texture("1k_storage_light");
    protected final static SpriteId STORAGE_4K_LIGHT = texture("4k_storage_light");
    protected final static SpriteId STORAGE_16K_LIGHT = texture("16k_storage_light");
    protected final static SpriteId STORAGE_64K_LIGHT = texture("64k_storage_light");
    protected final static SpriteId STORAGE_256K_LIGHT = texture("256k_storage_light");
    protected final static SpriteId MONITOR_BASE = texture("monitor_base");
    protected final static SpriteId MONITOR_LIGHT_DARK = texture("monitor_light_dark");
    protected final static SpriteId MONITOR_LIGHT_MEDIUM = texture("monitor_light_medium");
    protected final static SpriteId MONITOR_LIGHT_BRIGHT = texture("monitor_light_bright");

    public CraftingUnitModelProvider(CraftingUnitType type) {
        super(type);
    }

    public TextureAtlasSprite getLightMaterial(SpriteGetter textureGetter) {
        return switch (this.type) {
            case ACCELERATOR -> textureGetter.get(ACCELERATOR_LIGHT);
            case STORAGE_1K -> textureGetter.get(STORAGE_1K_LIGHT);
            case STORAGE_4K -> textureGetter.get(STORAGE_4K_LIGHT);
            case STORAGE_16K -> textureGetter.get(STORAGE_16K_LIGHT);
            case STORAGE_64K -> textureGetter.get(STORAGE_64K_LIGHT);
            case STORAGE_256K -> textureGetter.get(STORAGE_256K_LIGHT);
            default -> throw new IllegalArgumentException(
                    "Crafting unit type " + this.type + " does not use a light texture.");
        };
    }

    @Override
    public BlockStateModel bake(SpriteGetter spriteGetter) {
        TextureAtlasSprite ringCorner = spriteGetter.get(RING_CORNER);
        TextureAtlasSprite ringSideHor = spriteGetter.get(RING_SIDE_HOR);
        TextureAtlasSprite ringSideVer = spriteGetter.get(RING_SIDE_VER);

        return switch (type) {
            case UNIT -> new UnitBakedModel(ringCorner, ringSideHor, ringSideVer,
                    spriteGetter.get(UNIT_BASE));
            case ACCELERATOR, STORAGE_1K, STORAGE_4K, STORAGE_16K, STORAGE_64K, STORAGE_256K -> new LightBakedModel(
                    ringCorner, ringSideHor, ringSideVer, spriteGetter.get(LIGHT_BASE),
                    this.getLightMaterial(spriteGetter));
            case MONITOR -> new MonitorBakedModel(ringCorner, ringSideHor, ringSideVer,
                    spriteGetter.get(UNIT_BASE), spriteGetter.get(MONITOR_BASE),
                    spriteGetter.get(MONITOR_LIGHT_DARK),
                    spriteGetter.get(MONITOR_LIGHT_MEDIUM),
                    spriteGetter.get(MONITOR_LIGHT_BRIGHT));
        };
    }

    private static SpriteId texture(String name) {
        return Sheets.BLOCKS_MAPPER.apply(AppEng.makeId("block/crafting/" + name));
    }

    @Override
    public String debugName() {
        return getClass().toString();
    }
}
