package appeng.api.integrations.emi;

import appeng.api.stacks.GenericStack;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

/**
 * Implement this interface to provide AE2s EMI integration with a new way to convert between AE2 {@link GenericStack}
 * and {@link dev.emi.emi.api.stack.EmiStack}.
 * <ul>
 * <li>Recipe transfers</li>
 * <li>Pressing R/U on custom stacks in AE2 user interfaces</li>
 * <li>Dragging ghost items of custom types from EMI to AE2 interfaces</li>
 * </ul>
 * <p/>
 * To register your converter, see {@link EmiStackConverters}.
 */
public interface EmiStackConverter {
    /**
     * The EMI {@link EmiStack#getKeyOfType key type} handled by this converter.
     * AE2 handles {@link net.minecraft.world.level.material.Fluid} and {@link net.minecraft.world.item.Item} already.
     */
    Class<?> getKeyType();

    /**
     * Converts a generic stack into an EmiStack subtype handled by this converter.
     * <p/>
     * The converter needs to ensure the minimum amount of the returned ingredient is 1 if the resulting ingredient
     * represents amounts of 0 as "empty", since this would not preserve the ingredient type correctly.
     * <p/>
     * Example: <code>Math.max(1, Ints.saturatedCast(stack.amount()))</code> (for Item and Fluid stacks).
     *
     * @return Null if the converter can't handle the stack.
     */
    @Nullable
    EmiStack getIngredientFromStack(GenericStack stack);

    /**
     * Converts an EmiStack handled by this converter into a generic stack.
     *
     * @return Null if the ingredient represents an "empty" ingredient (i.e.
     *         {@link EmiStack#EMPTY}.
     */
    @Nullable
    GenericStack getStackFromIngredient(EmiStack stack);
}
