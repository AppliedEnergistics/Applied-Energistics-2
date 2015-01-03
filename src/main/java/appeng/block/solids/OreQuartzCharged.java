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

import java.util.Random;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.exceptions.MissingDefinition;
import appeng.client.render.effects.ChargedOreFX;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;

public class OreQuartzCharged extends OreQuartz
{

	public OreQuartzCharged() {
		super( OreQuartzCharged.class );
		this.setBoostBrightnessLow( 2 );
		this.setBoostBrightnessHigh( 5 );
	}

	@Nullable
	@Override
	public Item getItemDropped( int id, Random rand, int meta )
	{
		for ( Item charged : AEApi.instance().definitions().materials().certusQuartzCrystalCharged().maybeItem().asSet() )
		{
			return charged;
		}

		throw new MissingDefinition( "Tried to access charged certus quartz crystal, even though they are disabled" );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World w, int x, int y, int z, Random r)
	{
		if ( !AEConfig.instance.enableEffects )
			return;

		double xOff = (r.nextFloat());
		double yOff = (r.nextFloat());
		double zOff = (r.nextFloat());

		switch (r.nextInt( 6 ))
		{
		case 0:
			xOff = -0.01;
			break;
		case 1:
			yOff = -0.01;
			break;
		case 2:
			xOff = -0.01;
			break;
		case 3:
			zOff = -0.01;
			break;
		case 4:
			xOff = 1.01;
			break;
		case 5:
			yOff = 1.01;
			break;
		case 6:
			zOff = 1.01;
			break;
		}

		if ( CommonHelper.proxy.shouldAddParticles( r ) )
		{
			ChargedOreFX fx = new ChargedOreFX( w, x + xOff, y + yOff, z + zOff, 0.0f, 0.0f, 0.0f );
			Minecraft.getMinecraft().effectRenderer.addEffect( fx );
		}
	}

}
