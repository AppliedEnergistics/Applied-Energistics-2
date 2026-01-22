package appeng.client.integrations.jei;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.types.IRecipeType;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.parts.FacadeItem;

/**
 * This plugin will dynamically add facade recipes for any item that can be turned into a facade.
 */
class FacadeRegistryPlugin implements IRecipeManagerPlugin {

    private final FacadeItem itemFacade;

    private final Ingredient cableAnchor;

    FacadeRegistryPlugin() {
        this.itemFacade = AEItems.FACADE.get();
        this.cableAnchor = Ingredient.of(AEParts.CABLE_ANCHOR);
    }

    @Override
    public <V> List<IRecipeType<?>> getRecipeTypes(IFocus<V> focus) {
        var stackFocus = focus.getTypedValue()
                .getIngredient(VanillaTypes.ITEM_STACK)
                .orElse(null);

        if (focus.getRole() == RecipeIngredientRole.OUTPUT && stackFocus != null) {
            // Looking up how a certain facade is crafted
            if (stackFocus.getItem() instanceof FacadeItem) {
                return Collections.singletonList(RecipeTypes.CRAFTING);
            }
        } else if (focus.getRole() == RecipeIngredientRole.INPUT && stackFocus != null) {
            // Looking up if a certain block can be used to make a facade

            if (!this.itemFacade.createFacadeForItem(stackFocus, true).isEmpty()) {
                return Collections.singletonList(RecipeTypes.CRAFTING);
            }
        }

        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, V> List<T> getRecipes(IRecipeType<T> recipeType, IFocus<V> focus) {
        if (!RecipeTypes.CRAFTING.equals(recipeType)) {
            return Collections.emptyList();
        }

        var focusStack = focus.getTypedValue()
                .getIngredient(VanillaTypes.ITEM_STACK)
                .orElse(null);

        if (focus.getRole() == RecipeIngredientRole.OUTPUT && focusStack != null) {
            // Looking up how a certain facade is crafted
            if (focusStack.getItem() instanceof FacadeItem facadeItem) {
                ItemStack textureItem = facadeItem.getTextureItem(focusStack);
                return Collections.singletonList((T) make(textureItem, focusStack));
            }
        } else if (focus.getRole() == RecipeIngredientRole.INPUT && focusStack != null) {
            // Looking up if a certain block can be used to make a facade

            ItemStack facade = this.itemFacade.createFacadeForItem(focusStack, false);

            if (!facade.isEmpty()) {
                return Collections.singletonList((T) make(focusStack, facade));
            }
        }

        return Collections.emptyList();
    }

    private RecipeHolder<ShapedRecipe> make(ItemStack textureItem, ItemStack result) {
        var ingredients = NonNullList.<Optional<Ingredient>>withSize(9, Optional.empty());
        ingredients.set(1, Optional.of(cableAnchor));
        ingredients.set(3, Optional.of(cableAnchor));
        ingredients.set(5, Optional.of(cableAnchor));
        ingredients.set(7, Optional.of(cableAnchor));
        ingredients.set(4, Optional.of(Ingredient.of(textureItem.getItem())));
        var pattern = new ShapedRecipePattern(3, 3, ingredients, Optional.empty());

        result.setCount(4);

        // This id should only be used within REI and not really matter
        Identifier id = AppEng.makeId("facade/" + Item.getId(textureItem.getItem()));
        return new RecipeHolder<>(
                ResourceKey.create(Registries.RECIPE, id),
                new ShapedRecipe("", CraftingBookCategory.MISC, pattern, ItemStackTemplate.fromNonEmptyStack(result)));
    }

    @Override
    public <T> List<T> getRecipes(IRecipeType<T> recipeType) {
        return List.of();
    }
}
