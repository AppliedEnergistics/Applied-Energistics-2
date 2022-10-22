package appeng.integration.modules.jei;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;

import appeng.core.AppEng;
import appeng.items.parts.FacadeItem;

/**
 * This plugin will dynamically add facade recipes for any item that can be turned into a facade.
 */
class FacadeRegistryPlugin implements IRecipeManagerPlugin {

    private final FacadeItem itemFacade;

    private final ItemStack cableAnchor;

    FacadeRegistryPlugin(FacadeItem itemFacade, ItemStack cableAnchor) {
        this.itemFacade = itemFacade;
        this.cableAnchor = cableAnchor;
    }

    @Override
    public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
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
    public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        if (!RecipeTypes.CRAFTING.equals(recipeCategory.getRecipeType())) {
            return Collections.emptyList();
        }

        var focusStack = focus.getTypedValue()
                .getIngredient(VanillaTypes.ITEM_STACK)
                .orElse(null);

        if (focus.getRole() == RecipeIngredientRole.OUTPUT && focusStack != null) {
            // Looking up how a certain facade is crafted
            if (focusStack.getItem() instanceof FacadeItem facadeItem) {
                ItemStack textureItem = facadeItem.getTextureItem(focusStack);
                return Collections.singletonList((T) make(textureItem, this.cableAnchor, focusStack));
            }
        } else if (focus.getRole() == RecipeIngredientRole.INPUT && focusStack != null) {
            // Looking up if a certain block can be used to make a facade

            ItemStack facade = this.itemFacade.createFacadeForItem(focusStack, false);

            if (!facade.isEmpty()) {
                return Collections.singletonList((T) make(focusStack, this.cableAnchor, facade));
            }
        }

        return Collections.emptyList();
    }

    private ShapedRecipe make(ItemStack textureItem, ItemStack cableAnchor, ItemStack result) {
        // This id should only be used within JEI and not really matter
        var itemId = BuiltInRegistries.ITEM.getKey(textureItem.getItem());
        ResourceLocation id = new ResourceLocation(AppEng.MOD_ID,
                "facade/" + itemId.getNamespace() + "/" + itemId.getPath());

        NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
        ingredients.set(1, Ingredient.of(cableAnchor));
        ingredients.set(3, Ingredient.of(cableAnchor));
        ingredients.set(5, Ingredient.of(cableAnchor));
        ingredients.set(7, Ingredient.of(cableAnchor));
        ingredients.set(4, Ingredient.of(textureItem));

        var output = result.copy();
        output.setCount(4);

        return new ShapedRecipe(id, "", CraftingBookCategory.MISC, 3, 3, ingredients, output);
    }

    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
        return Collections.emptyList();
    }
}
