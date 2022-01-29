package appeng.api.integrations.jei;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.ingredients.IIngredientType;

import appeng.api.stacks.GenericStack;

/**
 * Implement this interface to provide AE2s JEI integration with a new ingredient type converter for use in:
 * <ul>
 * <li>Recipe transfers</li>
 * <li>Pressing R/U on custom stacks in AE2 user interfaces</li>
 * <li>Dragging ghost items of custom types from JEI to AE2 interfaces</li>
 * </ul>
 * <p/>
 * To register your converter, implement this interface and place the fully qualified class-name in a standard
 * {@link java.util.ServiceLoader} file
 * (<code>/META-INF/services/appeng.api.integrations.jei.IngredientTypeConverter</code>).
 */
public interface IngredientTypeConverter<T> {
    /**
     * The JEI ingredient type handled by this converter.
     */
    IIngredientType<T> getIngredientType();

    /**
     * Converts a generic stack into one of the ingredient handled by this converter.
     * <p/>
     * The converter needs to ensure the minimum amount of the returned ingredient is 1 if the resulting ingredient
     * represents amounts of 0 as "empty", since this would not preserve the ingredient type correctly.
     * <p/>
     * Example: <code>Math.max(1, Ints.saturatedCast(stack.amount()))</code> (for Item and Fluid stacks).
     *
     * @return Null if the converter can't handle the stack.
     */
    @Nullable
    T getIngredientFromStack(GenericStack stack);

    /**
     * Converts an ingredient handled by this converter into a generic stack.
     *
     * @return Null if the ingredient represents an "empty" ingredient (i.e.
     *         {@link net.minecraft.world.item.ItemStack#EMPTY}.
     */
    @Nullable
    GenericStack getStackFromIngredient(T ingredient);

    /**
     * Converts a display GUI ingredient (which usually contains multiple alternative ingredients) to a list of generic
     * stacks.
     */
    default List<GenericStack> getStacksFromGuiIngredient(IGuiIngredient<T> ingredient) {
        return ingredient.getAllIngredients().stream()
                .map(this::getStackFromIngredient)
                .filter(Objects::nonNull)
                .toList();
    }
}
