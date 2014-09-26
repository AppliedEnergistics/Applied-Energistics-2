package net.mcft.copy.betterstorage.api.crafting;

import net.minecraft.item.ItemStack;

/** Represents a particular crafting instance for a recipe from a specific input.
 *  Only calculate the output, experience cost, crafting time and craft requirements once. */
public class StationCrafting {
	
	protected final ItemStack[] output;
	protected final IRecipeInput[] requiredInput;
			
	protected int requiredExperience;
	protected int craftingTime;
	
	public StationCrafting(ItemStack[] output, IRecipeInput[] requiredInput,
	                       int requiredExperience, int craftingTime) {
		this.output = output;
		this.requiredInput = requiredInput;
		this.requiredExperience = requiredExperience;
		this.craftingTime = craftingTime;
	}
	public StationCrafting(ItemStack[] output, IRecipeInput[] requiredInput, int requiredExperience) {
		this(output, requiredInput, requiredExperience, 0);
	}
	public StationCrafting(ItemStack[] output, IRecipeInput[] requiredInput) {
		this(output, requiredInput, 0, 0);
	}
	
	/** Returns the crafting output. */
	public ItemStack[] getOutput() { return output; }
	
	/** Returns the amount of experience required. */
	public int getRequiredExperience() { return requiredExperience; }
	
	/** Returns the time (in game ticks) needed to craft this recipe. <br>
	 *  Return 0 for the recipe to be instantly available.
	 *  Keep within 0 to 32000 ticks. */
	public int getCraftingTime() { return craftingTime; }
	
	/** Returns if the recipe can be crafted. Experience is checked automatically.
	 * @param source Contains information of where this recipe is crafted and who crafts it. */
	public boolean canCraft(ICraftingSource source) { return true; }
	
	/** Returns the requirements for crafting this recipe again.
	 *  Called after {@link #canCraft} and before {@link #craft}. <br>
	 *  After crafting, the crafting grid is filled with items from the
	 *  crafting station's internal storage that match the requirements.
	 * @param requiredInput The items needed to craft this recipe again. */
	public IRecipeInput[] getCraftRequirements() { return requiredInput; }
	
	/** Called after the recipe is successfully crafted. Experience is removed automatically.
	 * @param source contains information of where this recipe was crafted and who crafted it. */
	public void craft(ICraftingSource source) {  }
	
}
