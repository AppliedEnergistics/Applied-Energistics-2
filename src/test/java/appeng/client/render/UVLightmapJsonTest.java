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

package appeng.client.render;


import java.util.Arrays;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import appeng.bootstrap.FeatureFactory;
import appeng.bootstrap.components.IBlockRegistrationComponent;
import appeng.bootstrap.components.IItemRegistrationComponent;
import appeng.bootstrap.components.ItemModelComponent;
import appeng.bootstrap.components.ItemVariantsComponent;
import appeng.core.Api;


@Mod( modid = "uvlightmapjsontest", name = "UVLightmapJsonTest", version = "0.0.0" )
public class UVLightmapJsonTest
{
	@EventHandler
	public void preInit( FMLPreInitializationEvent event )
	{
		final ResourceLocation uvlblockR = new ResourceLocation( "uvlightmapjsontest", "uvlblock" );
		final Block uvlblock = new Block( Material.IRON )
		{
			final AxisAlignedBB box = new AxisAlignedBB( 0.25, 0, 7 / 16d, 0.75, 1, 9 / 16d );

			@Override
			public boolean isFullBlock( IBlockState state )
			{
				return false;
			}

			@Override
			public boolean isOpaqueCube( IBlockState state )
			{
				return false;
			}

			@Override
			public AxisAlignedBB getBoundingBox( IBlockState state, IBlockAccess source, BlockPos pos )
			{
				return this.box;
			}

			@Override
			public BlockRenderLayer getBlockLayer()
			{
				return BlockRenderLayer.CUTOUT;
			}

		}.setLightLevel( 0.2f ).setCreativeTab( CreativeTabs.DECORATIONS ).setRegistryName( uvlblockR );
		
		final Item uvlblockItem = new ItemBlock( uvlblock ).setRegistryName( uvlblockR );
		
		FeatureFactory fact = Api.INSTANCE.definitions().getRegistry();
		fact.<IBlockRegistrationComponent>addBootstrapComponent( ( side, registry ) -> registry.register( uvlblock ) );
		fact.<IItemRegistrationComponent>addBootstrapComponent(	( side, registry ) -> registry.register( uvlblockItem ) );
		fact.addBootstrapComponent( new ItemVariantsComponent( uvlblockItem, Arrays.asList( uvlblockR ) ));
		fact.addBootstrapComponent( new ItemModelComponent( uvlblockItem, ImmutableMap.of( 0, new ModelResourceLocation( uvlblockR, "inventory" ) ) ) );
	}
}
