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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;
import java.util.Random;


public interface ICableBusContainer
{

	int isProvidingStrongPower( ForgeDirection opposite );

	int isProvidingWeakPower( ForgeDirection opposite );

	boolean canConnectRedstone( EnumSet<ForgeDirection> of );

	void onEntityCollision( Entity e );

	boolean activate( EntityPlayer player, Vec3 vecFromPool );

	void onNeighborChanged();

	boolean isSolidOnSide( ForgeDirection side );

	boolean isEmpty();

	SelectedPart selectPart( Vec3 v3 );

	boolean recolourBlock( ForgeDirection side, AEColor colour, EntityPlayer who );

	boolean isLadder( EntityLivingBase entity );

	@SideOnly( Side.CLIENT )
	void randomDisplayTick( World world, int x, int y, int z, Random r );

	int getLightValue();
}
