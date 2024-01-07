package appeng.integration.modules.emi;

import java.util.List;
import java.util.Optional;

import net.minecraft.world.item.ItemStack;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import appeng.core.definitions.AEParts;
import appeng.items.parts.FacadeItem;

/**
 * This plugin will dynamically add facade recipes for any item that can be turned into a facade.
 */
class EmiFacadeGenerator {
    private final ItemStack cableAnchor;

    EmiFacadeGenerator() {
        this.cableAnchor = AEParts.CABLE_ANCHOR.stack();
    }

    public Optional<EmiRecipe> getRecipeFor(EmiStack emiStack) {
        var itemStack = emiStack.getItemStack();
        if (itemStack.isEmpty()) {
            return Optional.empty(); // We only have items
        }

        // Looking up how a certain facade is crafted
        if (itemStack.getItem() instanceof FacadeItem facadeItem) {
            ItemStack textureItem = facadeItem.getTextureItem(itemStack);
            return Optional.of(make(textureItem, this.cableAnchor, itemStack.copy()));
        }

        return Optional.empty();
    }

    private EmiRecipe make(ItemStack textureItem, ItemStack cableAnchor, ItemStack result) {
        var input = List.<EmiIngredient>of(
                EmiStack.EMPTY,
                EmiStack.of(cableAnchor),
                EmiStack.EMPTY,
                EmiStack.of(cableAnchor),
                EmiStack.of(textureItem),
                EmiStack.of(cableAnchor),
                EmiStack.EMPTY,
                EmiStack.of(cableAnchor),
                EmiStack.EMPTY);

        return new EmiCraftingRecipe(
                input,
                EmiStack.of(result, 4),
                null,
                false);
    }

}
