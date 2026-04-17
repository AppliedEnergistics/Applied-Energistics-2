package appeng.client.render.crafting;

import java.util.List;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.SpriteGetter;

import appeng.block.crafting.ICraftingUnitType;

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

    public abstract BlockStateModel bake(SpriteGetter spriteGetter);
}
