package mekanism.api;

import java.lang.reflect.Method;

import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfusionInput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Use this handy class to add recipes to Mekanism machinery.
 * @author AidanBrady
 *
 */
public final class RecipeHelper
{
	/**
	 * Add an Enrichment Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addEnrichmentChamberRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addEnrichmentChamberRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add an Osmium Compressor recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addOsmiumCompressorRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addOsmiumCompressorRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Combiner recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCombinerRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addCombinerRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Crusher recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCrusherRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addCrusherRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Purification Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addPurificationChamberRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addPurificationChamberRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Chemical Oxidizer recipe.
	 * @param input - input ItemStack
	 * @param output - output GasStack
	 */
	public static void addChemicalOxidizerRecipe(ItemStack input, GasStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addChemicalOxidizerRecipe", ItemStack.class, GasStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Chemical Infuser recipe.
	 * @param input - input ChemicalInput
	 * @param output - output GasStack
	 */
	public static void addChemicalInfuserRecipe(ChemicalPair input, GasStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addChemicalInfuserRecipe", ChemicalPair.class, GasStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Precision Sawmill recipe.
	 * @param input - input ItemStack
	 * @param output - output ChanceOutput
	 */
	public static void addPrecisionSawmillRecipe(ItemStack input, ChanceOutput output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addPrecisionSawmillRecipe", ItemStack.class, ChanceOutput.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Chemical Injection Chamber recipe.
	 * @param input - input AdvancedInput
	 * @param output - output ItemStack
	 */
	public static void addChemicalInjectionChamberRecipe(AdvancedInput input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addChemicalInjectionChamberRecipe", AdvancedInput.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add an Electrolytic Separator recipe.
	 * @param input - input FluidStack
	 * @param output - output ChemicalPair
	 */
	public static void addElectrolyticSeparatorRecipe(FluidStack input, ChemicalPair output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addElectrolyticSeparatorRecipe", FluidStack.class, ChemicalPair.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Chemical Dissolution Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output GasStack
	 */
	public static void addChemicalDissolutionChamberRecipe(ItemStack input, GasStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addChemicalDissolutionChamberRecipe", ItemStack.class, GasStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Chemical Washer recipe.
	 * @param input - input GasStack
	 * @param output - output GasStack
	 */
	public static void addChemicalWasherRecipe(GasStack input, GasStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addChemicalWasherRecipe", GasStack.class, GasStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Chemical Crystallizer recipe.
	 * @param input - input GasStack
	 * @param output - output ItemStack
	 */
	public static void addChemicalCrystallizerRecipe(GasStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addChemicalCrystallizerRecipe", GasStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Metallurgic Infuser recipe.
	 * @param input - input Infusion
	 * @param output - output ItemStack
	 */
	public static void addMetallurgicInfuserRecipe(InfusionInput input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addMetallurgicInfuserRecipe", InfusionInput.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}
	
	/**
	 * Add a Pressurized Reaction Chamber recipe.
	 * @param input - input PressurizedReactants
	 * @param output - output PressurizedProducts
	 * @param extraEnergy - extra energy needed by the recipe
	 * @param ticks - amount of ticks it takes for this recipe to complete
	 */
	public static void addPRCRecipe(PressurizedReactants input, PressurizedProducts output, double extraEnergy, int ticks)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.recipe.RecipeHandler");
			Method m = recipeClass.getMethod("addPRCRecipe", PressurizedReactants.class, PressurizedProducts.class, Double.TYPE, Integer.TYPE);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("Error while adding recipe: " + e.getMessage());
		}
	}
}
