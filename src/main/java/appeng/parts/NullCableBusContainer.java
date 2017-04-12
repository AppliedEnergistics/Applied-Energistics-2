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


import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;
import java.util.Random;


public class NullCableBusContainer implements ICableBusContainer
{

	@Override
	public int isProvidingStrongPower( final ForgeDirection opposite )
	{
		return 0;
	}

	@Override
	public int isProvidingWeakPower( final ForgeDirection opposite )
	{
		return 0;
	}

	@Override
	public boolean canConnectRedstone( final EnumSet<ForgeDirection> of )
	{
		return false;
	}

	@Override
	public void onEntityCollision( final Entity e )
	{

	}

	@Override
	public boolean activate( final EntityPlayer player, final Vec3 vecFromPool )
	{
		return false;
	}

	@Override
	public void onNeighborChanged()
	{

	}

	@Override
	public boolean isSolidOnSide( final ForgeDirection side )
	{
		return false;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public SelectedPart selectPart( final Vec3 v3 )
	{
		return new SelectedPart();
	}

	@Override
	public boolean recolourBlock( final ForgeDirection side, final AEColor colour, final EntityPlayer who )
	{
		return false;
	}

	@Override
	public boolean isLadder( final EntityLivingBase entity )
	{
		return false;
	}

	@Override
	public void randomDisplayTick( final World world, final int x, final int y, final int z, final Random r )
	{

	}

	@Override
	public int getLightValue()
	{
		return 0;
	}
}
