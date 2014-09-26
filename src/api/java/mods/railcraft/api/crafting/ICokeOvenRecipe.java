package mods.railcraft.api.crafting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface ICokeOvenRecipe
{

    public int getCookTime();

    public ItemStack getInput();

    public FluidStack getFluidOutput();

    public ItemStack getOutput();
}
