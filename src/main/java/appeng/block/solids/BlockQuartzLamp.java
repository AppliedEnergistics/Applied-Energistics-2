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

package appeng.block.solids;


import java.util.EnumSet;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.client.render.effects.VibrantFX;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;


public class BlockQuartzLamp extends BlockQuartzGlass
{

	public BlockQuartzLamp()
	{
		this.setLightLevel( 1.0f );
		this.setBlockTextureName( "BlockQuartzGlass" );
		this.setFeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks, AEFeature.DecorativeLights ) );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void randomDisplayTick( World w, int x, int y, int z, Random r )
	{
		if( !AEConfig.instance.enableEffects )
		{
			return;
		}

		if( CommonHelper.proxy.shouldAddParticles( r ) )
		{
			double d0 = ( r.nextFloat() - 0.5F ) * 0.96D;
			double d1 = ( r.nextFloat() - 0.5F ) * 0.96D;
			double d2 = ( r.nextFloat() - 0.5F ) * 0.96D;

			VibrantFX fx = new VibrantFX( w, 0.5 + x + d0, 0.5 + y + d1, 0.5 + z + d2, 0.0D, 0.0D, 0.0D );

			Minecraft.getMinecraft().effectRenderer.addEffect( fx );
		}
	}
}
