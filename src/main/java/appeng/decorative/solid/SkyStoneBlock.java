/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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


import java.util.EnumSet;

import com.google.common.base.Optional;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.core.worlddata.WorldData;
import appeng.util.Platform;


public class SkyStoneBlock extends AEBaseBlock
{
	private static final float BLOCK_RESISTANCE = 150.0f;
	private static final double BREAK_SPEAK_SCALAR = 0.1;
	private static final double BREAK_SPEAK_THRESHOLD = 7.0;
	private final SkystoneType type;

	public SkyStoneBlock( final SkystoneType type )
	{
		super( Material.rock, Optional.of( type.name() ) );
		this.setHardness( 50 );
		this.hasSubtypes = true;
		this.blockResistance = BLOCK_RESISTANCE;
		if( type == SkystoneType.STONE )
		{
			this.setHarvestLevel( "pickaxe", 3 );
		}
		this.setFeature( EnumSet.of( AEFeature.Core ) );

		this.type = type;

		MinecraftForge.EVENT_BUS.register( this );
	}

	@SubscribeEvent
	public void breakFaster( final PlayerEvent.BreakSpeed event )
	{
		if( event.state.getBlock() == this && event.entityPlayer != null )
		{
			final ItemStack is = event.entityPlayer.inventory.getCurrentItem();
			int level = -1;

			if( is != null && is.getItem() != null )
			{
				level = is.getItem().getHarvestLevel( is, "pickaxe" );
			}

			if( type != SkystoneType.STONE || level >= 3 || event.originalSpeed > BREAK_SPEAK_THRESHOLD )
			{
				event.newSpeed /= BREAK_SPEAK_SCALAR;
			}
		}
	}

	@Override
	public void onBlockAdded( final World w, final BlockPos pos, final IBlockState state )
	{
		super.onBlockAdded( w, pos, state );
		if( Platform.isServer() )
		{
			WorldData.instance().compassData().service().updateArea( w, pos.getX(), pos.getY(), pos.getZ() );
		}
	}

	@Override
	public void breakBlock( final World w, final BlockPos pos, final IBlockState state )
	{
		super.breakBlock( w, pos, state );

		if( Platform.isServer() )
		{
			WorldData.instance().compassData().service().updateArea( w, pos.getX(), pos.getY(), pos.getZ() );
		}
	}

	@Override
	public String getUnlocalizedName( final ItemStack is )
	{
		switch( type )
		{
			case BLOCK:
				return this.getUnlocalizedName() + ".Block";
			case BRICK:
				return this.getUnlocalizedName() + ".Brick";
			case SMALL_BRICK:
				return this.getUnlocalizedName() + ".SmallBrick";
			default:
				return this.getUnlocalizedName();
		}
	}

	public enum SkystoneType
	{
		STONE, BLOCK, BRICK, SMALL_BRICK
	}
}
