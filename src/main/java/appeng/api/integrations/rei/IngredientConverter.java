package appeng.api.integrations.rei;

import javax.annotation.Nullable;

import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;

import appeng.api.stacks.GenericStack;

/**
 * Implement this interface to provide AE2s REI integration with a new ingredient type converter for use in:
 * <ul>
 * <li>Recipe transfers</li>
 * <li>Pressing R/U on custom stacks in AE2 user interfaces</li>
 * <li>Dragging ghost items of custom types from REI to AE2 interfaces</li>
 * </ul>
 * <p/>
 * To register your converter, see {@link IngredientConverters}.
 */
public interface IngredientConverter<T> {
    /**
     * The REI ingredient type handled by this converter.
     */
    EntryType<T> getIngredientType();

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
    EntryStack<T> getIngredientFromStack(GenericStack stack);

    /**
     * Converts an ingredient handled by this converter into a generic stack.
     *
     * @return Null if the ingredient represents an "empty" ingredient (i.e.
     *         {@link net.minecraft.world.item.ItemStack#EMPTY}.
     */
    @Nullable
    GenericStack getStackFromIngredient(EntryStack<T> ingredient);
}
