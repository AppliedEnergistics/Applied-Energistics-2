package appeng.api.config;

public enum PowerUnits
{
	AE("gui.appliedenergistics2.units.appliedenergstics"), // Native Units - AE Energy
	MJ("gui.appliedenergistics2.units.buildcraft"), // BuildCraft - Minecraft Joules
	EU("gui.appliedenergistics2.units.ic2"), // IndustrialCraft 2 - Energy Units
	WA("gui.appliedenergistics2.units.rotarycraft"), // RotaryCraft - Watts
	RF("gui.appliedenergistics2.units.thermalexpansion"), // ThermalExpansion - Redstone Flux
	MK("gui.appliedenergistics2.units.mekanism"); // Mekanism - Joules

	private PowerUnits(String un) {
		unlocalizedName = un;
	}

	/**
	 * please do not edit this value, it is set when AE loads its config files.
	 */
	public double conversionRatio = 1.0;

	/**
	 * unlocalized name for the power unit.
	 */
	final public String unlocalizedName;

	/**
	 * do power conversion using AE's conversion rates.
	 * 
	 * Example: PowerUnits.EU.convertTo( PowerUnits.AE, 32 );
	 * 
	 * will normally returns 64, as it will convert the EU, to AE with AE's power settings.
	 * 
	 * @param target target power unit
	 * @param value value
	 * @return value converted to target units, from this units.
	 */
	public double convertTo(PowerUnits target, double value)
	{
		return (value * conversionRatio) / target.conversionRatio;
	}

}