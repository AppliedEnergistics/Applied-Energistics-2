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
    private final EmiStack cableAnchor;

    EmiFacadeGenerator() {
        this.cableAnchor = EmiStack.of(AEParts.CABLE_ANCHOR.stack());
    }

    public Optional<EmiRecipe> getRecipeFor(ItemStack potentialFacade) {
        if (potentialFacade.isEmpty()) {
            return Optional.empty(); // We only have items
        }

        if (potentialFacade.getItem() instanceof FacadeItem facadeItem) {
            ItemStack textureItem = facadeItem.getTextureItem(potentialFacade);
            return Optional.of(make(textureItem, potentialFacade.copy()));
        }

        return Optional.empty();
    }

    private EmiRecipe make(ItemStack textureItem, ItemStack result) {
        var textureStack = EmiStack.of(textureItem);
        var resultStack = EmiStack.of(result, 4);

        var input = List.<EmiIngredient>of(
                EmiStack.EMPTY,
                cableAnchor,
                EmiStack.EMPTY,
                cableAnchor,
                textureStack,
                cableAnchor,
                EmiStack.EMPTY,
                cableAnchor,
                EmiStack.EMPTY);

        return new EmiCraftingRecipe(
                input,
                resultStack,
                null,
                false);
    }
}
