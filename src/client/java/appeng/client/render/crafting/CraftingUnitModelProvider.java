package appeng.client.render.crafting;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;

import appeng.block.crafting.CraftingUnitType;
import appeng.core.AppEng;

public class CraftingUnitModelProvider extends AbstractCraftingUnitModelProvider<CraftingUnitType>
        implements ModelDebugName {

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

    public Material.Baked getLightMaterial(MaterialBaker materialBaker) {
        return switch (this.type) {
            case ACCELERATOR -> materialBaker.get(ACCELERATOR_LIGHT, this);
            case STORAGE_1K -> materialBaker.get(STORAGE_1K_LIGHT, this);
            case STORAGE_4K -> materialBaker.get(STORAGE_4K_LIGHT, this);
            case STORAGE_16K -> materialBaker.get(STORAGE_16K_LIGHT, this);
            case STORAGE_64K -> materialBaker.get(STORAGE_64K_LIGHT, this);
            case STORAGE_256K -> materialBaker.get(STORAGE_256K_LIGHT, this);
            default -> throw new IllegalArgumentException(
                    "Crafting unit type " + this.type + " does not use a light texture.");
        };
    }

    @Override
    public BlockStateModel bake(MaterialBaker materialBaker) {
        Material.Baked ringCorner = materialBaker.get(RING_CORNER, this);
        Material.Baked ringSideHor = materialBaker.get(RING_SIDE_HOR, this);
        Material.Baked ringSideVer = materialBaker.get(RING_SIDE_VER, this);

        return switch (type) {
            case UNIT -> new UnitBakedModel(ringCorner, ringSideHor, ringSideVer,
                    materialBaker.get(UNIT_BASE, this));
            case ACCELERATOR, STORAGE_1K, STORAGE_4K, STORAGE_16K, STORAGE_64K, STORAGE_256K -> new LightBakedModel(
                    ringCorner, ringSideHor, ringSideVer, materialBaker.get(LIGHT_BASE, this),
                    this.getLightMaterial(materialBaker));
            case MONITOR -> new MonitorBakedModel(ringCorner, ringSideHor, ringSideVer,
                    materialBaker.get(UNIT_BASE, this), materialBaker.get(MONITOR_BASE, this),
                    materialBaker.get(MONITOR_LIGHT_DARK, this),
                    materialBaker.get(MONITOR_LIGHT_MEDIUM, this),
                    materialBaker.get(MONITOR_LIGHT_BRIGHT, this));
        };
    }

    private static Material texture(String name) {
        return new Material(AppEng.makeId("block/crafting/" + name)); // TODO 26.1 - Mithi83 - this needs to be
                                                                      // double-checked
    }

    @Override
    public String debugName() {
        return getClass().toString();
    }
}
