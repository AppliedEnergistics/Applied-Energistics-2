/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.API;

import net.minecraft.world.World;

public interface ThermalMachine {

	/** For fetching the temperature for display */
	public int getTemperature();

	/** For overwriting the temperature */
	public void setTemperature(int T);

	/** For updating the temperature */
	public void addTemperature(int T);

	/** For display purposes */
	public String getName();

	/** For overheating tests */
	public int getMaxTemperature();

	/** Actions to take on overheat */
	public void onOverheat(World world, int x, int y, int z);

	/** Can the friction heater heat this machine */
	public boolean canBeFrictionHeated();

}
