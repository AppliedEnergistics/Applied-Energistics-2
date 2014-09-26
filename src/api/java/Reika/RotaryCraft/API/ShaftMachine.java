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

public interface ShaftMachine {

	/** For fetching the current rotational speed. This can be called from
	 * both client and server, so ensure that the TE is prepared for that. */
	public int getOmega();

	/** For fetching the current torque. This can be called from
	 * both client and server, so ensure that the TE is prepared for that. */
	public int getTorque();

	/** For fetching the current power value. This can be called from
	 * both client and server, so ensure that the TE is prepared for that. */
	public long getPower();

	/** For when to write it to chat or the like. This can be called from
	 * both client and server, so ensure that the TE is prepared for that. */
	public String getName();

	/** Analogous to TileEntityIOMachine's "iotick". Used to control I/O render opacity. */
	public int getIORenderAlpha();

	/** Analogous to TileEntityIOMachine's "iotick". Used to control I/O render opacity.
	 * This one is called by tools. */
	public void setIORenderAlpha(int io);

}
