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

package appeng.parts;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;

public class NullCableBusContainer implements ICableBusContainer
{

	@Override
	public int isProvidingStrongPower(ForgeDirection opposite)
	{
		return 0;
	}

	@Override
	public int isProvidingWeakPower(ForgeDirection opposite)
	{
		return 0;
	}

	@Override
	public boolean canConnectRedstone(EnumSet<ForgeDirection> of)
	{
		return false;
	}

	@Override
	public void onEntityCollision(Entity e)
	{

	}

	@Override
	public boolean activate(EntityPlayer player, Vec3 vecFromPool)
	{
		return false;
	}

	@Override
	public void onNeighborChanged()
	{

	}

	@Override
	public boolean isSolidOnSide(ForgeDirection side)
	{
		return false;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public SelectedPart selectPart(Vec3 v3)
	{
		return new SelectedPart();
	}

	@Override
	public boolean recolourBlock(ForgeDirection side, AEColor colour, EntityPlayer who)
	{
		return false;
	}

	@Override
	public boolean isLadder(EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random r)
	{

	}

	@Override
	public int getLightValue()
	{
		return 0;
	}

}
