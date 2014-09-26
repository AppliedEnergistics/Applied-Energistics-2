package net.mcft.copy.betterstorage.api.crafting;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ShapelessStationRecipe implements IStationRecipe {
	
	public final IRecipeInput[] recipeInput;
	public final ItemStack[] recipeOutput;
	
	public int requiredExperience = 0;
	public int craftingTime = 0;
	
	public ShapelessStationRecipe(IRecipeInput[] input, ItemStack[] output) {
		recipeInput = input;
		recipeOutput = output;
	}
	
	public ShapelessStationRecipe(ItemStack[] output, Object... input) {
		recipeInput = new IRecipeInput[input.length];
		recipeOutput = output;
		for (int i = 0; i < input.length; i++)
			recipeInput[i] = BetterStorageCrafting.makeInput(input[i]);
	}
	public ShapelessStationRecipe(ItemStack output, Object... input) {
		this(new ItemStack[]{ null, null, null, null, output }, input);
	}
	
	public ShapelessStationRecipe setRequiredExperience(int experience) {
		requiredExperience = experience;
		return this;
	}
	public ShapelessStationRecipe setCraftingTime(int time) {
		craftingTime = time;
		return this;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<IRecipeInput[]> getSampleInputs() {
		IRecipeInput[] input = new IRecipeInput[9];
		System.arraycopy(recipeInput, 0, input, 0, recipeInput.length);
		return Arrays.asList(new IRecipeInput[][]{ input });
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<IRecipeInput> getPossibleInputs() { return null; }
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<ItemStack> getPossibleOutputs() { return null; }
	
	// IStationRecipe implementation
	
	@Override
	public StationCrafting checkMatch(ItemStack[] input, RecipeBounds bounds) {
		IRecipeInput[] requiredInput = new IRecipeInput[9];
		List<IRecipeInput> checklist = new LinkedList<IRecipeInput>(Arrays.asList(recipeInput));
		inputLoop:
		for (int i = 0; i < input.length; i++) {
			ItemStack item = input[i];
			if (item == null) continue;
			Iterator<IRecipeInput> iter = checklist.iterator();
			while (iter.hasNext())
				if ((requiredInput[i] = iter.next()).matches(item)) {
					iter.remove();
					continue inputLoop;
				}
			return null;
		}
		if (!checklist.isEmpty()) return null;
		return new StationCrafting(recipeOutput, requiredInput, requiredExperience, craftingTime);
	}
	
}
