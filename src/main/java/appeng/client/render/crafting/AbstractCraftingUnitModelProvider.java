package appeng.client.render.crafting;

import java.util.List;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;

import appeng.block.crafting.ICraftingUnitType;
import net.minecraft.client.resources.model.SpriteGetter;

/**
 * Provides material and model information for custom crafting CPU blocks for use with {@link CraftingCubeModel}.
 * 
 * @param <T> The "type" associated with the crafting block being rendered.
 */
public abstract class AbstractCraftingUnitModelProvider<T extends ICraftingUnitType> {

    protected final T type;

    public AbstractCraftingUnitModelProvider(T type) {
        this.type = type;
    }

    /**
     * @return a list of any materials used for the model to be passed into
     *         {@link CraftingCubeModel#getAdditionalTextures()}.
     */
    public abstract List<Material> getMaterials();

    public abstract BakedModel getBakedModel(SpriteGetter spriteGetter);
}
