package mekanism.api;

public class PressurizedRecipe
{
	public PressurizedReactants reactants;

	public double extraEnergy;

	public PressurizedProducts products;

	public int ticks;

	public PressurizedRecipe(PressurizedReactants pressurizedReactants, double energy, PressurizedProducts pressurizedProducts, int duration)
	{
		reactants = pressurizedReactants;
		extraEnergy = energy;
		products = pressurizedProducts;
		ticks = duration;
	}

	public PressurizedRecipe copy()
	{
		return new PressurizedRecipe(reactants.copy(), extraEnergy, products.copy(), ticks);
	}
}
