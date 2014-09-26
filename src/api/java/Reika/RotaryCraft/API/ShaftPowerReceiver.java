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

import net.minecraftforge.common.util.ForgeDirection;

public interface ShaftPowerReceiver extends ShaftMachine {

	/** RC machines set your machine's rotational speed with this. */
	public void setOmega(int omega);

	/** RC machines set your machine's torque with this. */
	public void setTorque(int torque);

	/** RC machines set your machine's power with this.
	 * You do not need to calculate power=omega*torque;
	 * RC code will do that for you. */
	public void setPower(long power);

	/** x,y,z to read from */
	public boolean canReadFrom(ForgeDirection dir);

	/** Whether your machine is able to receive power right now */
	public boolean isReceiving();

	/** When there is no input machine. Usually used to set power, speed, torque = 0 */
	public void noInputMachine();

	/** The minimum torque the machine requires to operate. Also controls flywheel deceleration.
	 * Pick something reasonable, preferably as realistic as possible. */
	public int getMinTorque(int available);

}
