/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.integration.abstraction.helpers;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.power.PowerHandler.PowerReceiver;

import appeng.transformer.annotations.integration.Method;

public abstract class BaseMJPerdition
{
	@Method(iname = "MJ5")
	public abstract PowerReceiver getPowerReceiver();

	public abstract double useEnergy(double f, double required, boolean b);

	public abstract void addEnergy(float failed);

	public abstract void configure(int i, int j, float f, int k);

	public abstract void writeToNBT(NBTTagCompound tag);

	public abstract void readFromNBT(NBTTagCompound tag);

	public abstract void Tick();
}
