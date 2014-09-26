package mods.railcraft.api.crafting;

import java.util.List;
import java.util.Map;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface IRockCrusherRecipe {

    public ItemStack getInput();

    /**
     * Adds a new entry to the output list.
     * 
     * @param output the stack to output
     * @param chance the change to output this stack
     */
    void addOutput(ItemStack output, float chance);

    /**
     * Returns a list containing each output entry and its chance of being
     * included.
     *
     * @return
     */
    public List<Map.Entry<ItemStack, Float>> getOutputs();

    /**
     * Returns a list of all possible outputs. This is basically a condensed
     * version of getOutputs() without the chances.
     *
     * @return
     */
    public List<ItemStack> getPossibleOuputs();

    /**
     * Returns a list of outputs after it has passed through the randomizer.
     *
     * @return
     */
    public List<ItemStack> getRandomizedOuputs();
}
