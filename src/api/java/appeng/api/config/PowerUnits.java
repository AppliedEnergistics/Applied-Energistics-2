/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.config;


public enum PowerUnits
{
	AE( "gui.appliedenergistics2.units.appliedenergstics" ), // Native Units - AE Energy
	EU( "gui.appliedenergistics2.units.ic2" ), // IndustrialCraft 2 - Energy Units
	WA( "gui.appliedenergistics2.units.rotarycraft" ), // RotaryCraft - Watts
	RF( "gui.appliedenergistics2.units.thermalexpansion" ), // ThermalExpansion - Redstone Flux
	MK( "gui.appliedenergistics2.units.mekanism" ); // Mekanism - Joules

	/**
	 * unlocalized name for the power unit.
	 */
	public final String unlocalizedName;
	/**
	 * please do not edit this value, it is set when AE loads its config files.
	 */
	public double conversionRatio = 1.0;

	PowerUnits( String un )
	{
		this.unlocalizedName = un;
	}

	/**
	 * do power conversion using AE's conversion rates.
	 *
	 * Example: PowerUnits.EU.convertTo( PowerUnits.AE, 32 );
	 *
	 * will normally returns 64, as it will convert the EU, to AE with AE's power settings.
	 *
	 * @param target target power unit
	 * @param value  value
	 *
	 * @return value converted to target units, from this units.
	 */
	public double convertTo( PowerUnits target, double value )
	{
		return ( value * this.conversionRatio ) / target.conversionRatio;
	}

}