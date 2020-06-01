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

package appeng.decorative.solid;


import appeng.client.render.effects.ChargedOreFX;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;


public class BlockChargedQuartzOre extends BlockQuartzOre
{
	public BlockChargedQuartzOre(Properties props) {
		super(props);
	}

	// FIXME: Loot tables
//	@Override
//	public Item getItemDropped( final BlockState state, final Random rand, final int fortune )
//	{
//		return Api.INSTANCE
//				.definitions()
//				.materials()
//				.certusQuartzCrystalCharged()
//				.maybeItem()
//				.orElseThrow( () -> new MissingDefinitionException( "Tried to access charged certus quartz crystal, even though they are disabled" ) );
//	}
//
//	@Override
//	public int damageDropped( final BlockState state )
//	{
//		return Api.INSTANCE
//				.definitions()
//				.materials()
//				.certusQuartzCrystalCharged()
//				.maybeStack( 1 )
//				.orElseThrow( () -> new MissingDefinitionException( "Tried to access charged certus quartz crystal, even though they are disabled" ) )
//				.getItemDamage();
//	}
//
//	@Override
//	public ItemStack getPickBlock( BlockState state, RayTraceResult target, World world, BlockPos pos, PlayerEntity player )
//	{
//		return Api.INSTANCE
//				.definitions()
//				.blocks()
//				.quartzOreCharged()
//				.maybeStack( 1 )
//				.orElseThrow( () -> new MissingDefinitionException( "Tried to access charged certus quartz ore, even though they are disabled" ) );
//	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public void animateTick( final BlockState state, final World w, final BlockPos pos, final Random r )
	{
		if( !AEConfig.instance().isEnableEffects() )
		{
			return;
		}

		double xOff = ( r.nextFloat() );
		double yOff = ( r.nextFloat() );
		double zOff = ( r.nextFloat() );

		switch( r.nextInt( 6 ) )
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

		if( AppEng.proxy.shouldAddParticles( r ) )
		{
			Minecraft.getInstance().particles.addParticle(ChargedOreFX.TYPE, pos.getX() + xOff, pos.getY() + yOff, pos.getZ() + zOff, 0.0f, 0.0f, 0.0f);
		}
	}
}
