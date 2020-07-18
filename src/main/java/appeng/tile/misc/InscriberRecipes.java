package appeng.tile.misc;

import appeng.core.Api;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.features.InscriberProcessType;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.items.materials.MaterialItem;
import appeng.recipes.handlers.InscriberRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * This class indexes all inscriber recipes to find valid inputs for the top and
 * bottom optional slots. This speeds up checks whether inputs for those two
 * slots are valid.
 */
public final class InscriberRecipes {

    public static final Identifier NAMEPLATE_RECIPE_ID = new Identifier(AppEng.MOD_ID, "nameplate");

    private InscriberRecipes() {
    }

    /**
     * Returns an unmodifiable view of all registered inscriber recipes.
     */
    public static Iterable<InscriberRecipe> getRecipes(World world) {
        return world.getRecipeManager().method_30027(InscriberRecipe.TYPE);
    }

    @Nullable
    public static InscriberRecipe findRecipe(World world, ItemStack input, ItemStack plateA, ItemStack plateB,
            boolean supportNamePress) {
        if (supportNamePress) {
            IComparableDefinition namePress = Api.instance().definitions().materials().namePress();
            boolean isNameA = namePress.isSameAs(plateA);
            boolean isNameB = namePress.isSameAs(plateB);

            if ((isNameA && isNameB) || isNameA && plateB.isEmpty()) {
                return makeNamePressRecipe(input, plateA, plateB);
            } else if (plateA.isEmpty() && isNameB) {
                return makeNamePressRecipe(input, plateB, plateA);
            }
        }

        for (final InscriberRecipe recipe : getRecipes(world)) {
            // The recipe can be flipped at will
            final boolean matchA = recipe.getTopOptional().test(plateA) && recipe.getBottomOptional().test(plateB);
            final boolean matchB = recipe.getTopOptional().test(plateB) && recipe.getBottomOptional().test(plateA);

            if (matchA || matchB) {
                if (recipe.getMiddleInput().test(input)) {
                    return recipe;
                }
            }
        }

        return null;
    }

    private static InscriberRecipe makeNamePressRecipe(ItemStack input, ItemStack plateA, ItemStack plateB) {
        String name = "";

        if (!plateA.isEmpty()) {
            final CompoundTag tag = plateA.getOrCreateTag();
            name += tag.getString(MaterialItem.TAG_INSCRIBE_NAME);
        }

        if (!plateB.isEmpty()) {
            final CompoundTag tag = plateB.getOrCreateTag();
            name += " " + tag.getString(MaterialItem.TAG_INSCRIBE_NAME);
        }

        final Ingredient startingItem = Ingredient.ofStacks(input.copy());
        final ItemStack renamedItem = input.copy();

        if (!name.isEmpty()) {
            renamedItem.setCustomName(new LiteralText(name));
        } else {
            renamedItem.setCustomName(null);
        }

        final InscriberProcessType type = InscriberProcessType.INSCRIBE;

        return new InscriberRecipe(NAMEPLATE_RECIPE_ID, "", startingItem, renamedItem,
                plateA.isEmpty() ? Ingredient.EMPTY : Ingredient.ofStacks(plateA),
                plateB.isEmpty() ? Ingredient.EMPTY : Ingredient.ofStacks(plateB), type);
    }

    /**
     * Checks if there is an inscriber recipe that supports the given combination of
     * top/bottom presses. Both the given combination and the reverse will be
     * searched.
     */
    public static boolean isValidOptionalIngredientCombination(World world, ItemStack pressA, ItemStack pressB) {
        for (InscriberRecipe recipe : getRecipes(world)) {
            if (recipe.getTopOptional().test(pressA) && recipe.getBottomOptional().test(pressB)
                    || recipe.getTopOptional().test(pressB) && recipe.getBottomOptional().test(pressA)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if there is an inscriber recipe that would use the given item stack as
     * an optional ingredient. Bottom and top can be used interchangeably here,
     * because the inscriber will flip the recipe if needed.
     */
    public static boolean isValidOptionalIngredient(World world, ItemStack is) {
        for (InscriberRecipe recipe : getRecipes(world)) {
            if (recipe.getTopOptional().test(is) || recipe.getBottomOptional().test(is)) {
                return true;
            }
        }

        return false;
    }

}
