package appeng.client.render.crafting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

import appeng.block.crafting.CraftingUnitType;
import appeng.core.AppEng;

public class CraftingUnitModelProvider extends AbstractCraftingUnitModelProvider<CraftingUnitType> {

    private static final List<Material> MATERIALS = new ArrayList<>();

    protected final static Material RING_CORNER = texture("ring_corner");
    protected final static Material RING_SIDE_HOR = texture("ring_side_hor");
    protected final static Material RING_SIDE_VER = texture("ring_side_ver");
    protected final static Material UNIT_BASE = texture("unit_base");
    protected final static Material LIGHT_BASE = texture("light_base");
    protected final static Material ACCELERATOR_LIGHT = texture("accelerator_light");
    protected final static Material STORAGE_1K_LIGHT = texture("1k_storage_light");
    protected final static Material STORAGE_4K_LIGHT = texture("4k_storage_light");
    protected final static Material STORAGE_16K_LIGHT = texture("16k_storage_light");
    protected final static Material STORAGE_64K_LIGHT = texture("64k_storage_light");
    protected final static Material STORAGE_256K_LIGHT = texture("256k_storage_light");
    protected final static Material MONITOR_BASE = texture("monitor_base");
    protected final static Material MONITOR_LIGHT_DARK = texture("monitor_light_dark");
    protected final static Material MONITOR_LIGHT_MEDIUM = texture("monitor_light_medium");
    protected final static Material MONITOR_LIGHT_BRIGHT = texture("monitor_light_bright");

    public CraftingUnitModelProvider(CraftingUnitType type) {
        super(type);
    }

    @Override
    public List<Material> getMaterials() {
        return Collections.unmodifiableList(MATERIALS);
    }

    public TextureAtlasSprite getLightMaterial(Function<Material, TextureAtlasSprite> textureGetter) {
        return switch (this.type) {
            case ACCELERATOR -> textureGetter.apply(ACCELERATOR_LIGHT);
            case STORAGE_1K -> textureGetter.apply(STORAGE_1K_LIGHT);
            case STORAGE_4K -> textureGetter.apply(STORAGE_4K_LIGHT);
            case STORAGE_16K -> textureGetter.apply(STORAGE_16K_LIGHT);
            case STORAGE_64K -> textureGetter.apply(STORAGE_64K_LIGHT);
            case STORAGE_256K -> textureGetter.apply(STORAGE_256K_LIGHT);
            default -> throw new IllegalArgumentException(
                    "Crafting unit type " + this.type + " does not use a light texture.");
        };
    }

    @Override
    public BakedModel getBakedModel(Function<Material, TextureAtlasSprite> spriteGetter) {
        TextureAtlasSprite ringCorner = spriteGetter.apply(RING_CORNER);
        TextureAtlasSprite ringSideHor = spriteGetter.apply(RING_SIDE_HOR);
        TextureAtlasSprite ringSideVer = spriteGetter.apply(RING_SIDE_VER);

        return switch (type) {
            case UNIT -> new UnitBakedModel(ringCorner, ringSideHor, ringSideVer,
                    spriteGetter.apply(UNIT_BASE));
            case ACCELERATOR, STORAGE_1K, STORAGE_4K, STORAGE_16K, STORAGE_64K, STORAGE_256K -> new LightBakedModel(
                    ringCorner, ringSideHor, ringSideVer, spriteGetter.apply(LIGHT_BASE),
                    this.getLightMaterial(spriteGetter));
            case MONITOR -> new MonitorBakedModel(ringCorner, ringSideHor, ringSideVer,
                    spriteGetter.apply(UNIT_BASE), spriteGetter.apply(MONITOR_BASE),
                    spriteGetter.apply(MONITOR_LIGHT_DARK),
                    spriteGetter.apply(MONITOR_LIGHT_MEDIUM),
                    spriteGetter.apply(MONITOR_LIGHT_BRIGHT));
        };
    }

    private static Material texture(String name) {
        var mat = new Material(TextureAtlas.LOCATION_BLOCKS,
                new ResourceLocation(AppEng.MOD_ID, "block/crafting/" + name));
        MATERIALS.add(mat);
        return mat;
    }
}
