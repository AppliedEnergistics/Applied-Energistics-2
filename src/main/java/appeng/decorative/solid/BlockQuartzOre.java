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


import appeng.block.AEBaseBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;


public class BlockQuartzOre extends AEBaseBlock
{
	public BlockQuartzOre(Properties props) {
		super(props);
	}

	// FIXME: Loot Tables
//	@Override
//	public int quantityDropped( BlockState state, int fortune, Random rand )
//	{
//		if( fortune > 0 && Item.getItemFromBlock( this ) != this.getItemDropped( null, rand, fortune ) )
//		{
//			int j = rand.nextInt( fortune + 2 ) - 1;
//
//			if( j < 0 )
//			{
//				j = 0;
//			}
//
//			return this.quantityDropped( rand ) * ( j + 1 );
//		}
//		else
//		{
//			return this.quantityDropped( rand );
//		}
//	}

//	@Override
//	public int quantityDropped( final Random rand )
//	{
//		return 1 + rand.nextInt( 2 );
//	}
//

	@Override
	public int getExpDrop(BlockState state, net.minecraft.world.IWorldReader reader, BlockPos pos, int fortune, int silktouch) {
		return silktouch == 0 ? MathHelper.nextInt(RANDOM, 2, 5) : 0;
	}

	// FIXME: loot tables
//	@Override
//	public Item getItemDropped( final BlockState state, final Random rand, final int fortune )
//	{
//		return AEApi.instance()
//				.definitions()
//				.materials()
//				.certusQuartzCrystal()
//				.maybeItem()
//				.orElseThrow( () -> new MissingDefinitionException( "Tried to access certus quartz crystal, even though they are disabled" ) );
//	}

	// FIXME: loot tables
//	@Override
//	public int damageDropped( final BlockState state )
//	{
//		return AEApi.instance()
//				.definitions()
//				.materials()
//				.certusQuartzCrystal()
//				.maybeStack( 1 )
//				.orElseThrow( () -> new MissingDefinitionException( "Tried to access certus quartz crystal, even though they are disabled" ) )
//				.getItemDamage();
//	}
}
