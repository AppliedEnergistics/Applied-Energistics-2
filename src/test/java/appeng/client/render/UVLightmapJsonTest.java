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


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
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
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;


@Mod( modid = "UVLightmapJsonTest", name = "UVLightmapJsonTest", version = "0.0.0" )
public class UVLightmapJsonTest
{

	private static final ResourceLocation uvlblockR = new ResourceLocation( "UVLightmapJsonTest", "uvlblock" );

	public static Block uvlblock;
	public static Item uvlblockItem;

	@EventHandler
	public void preInit( FMLPreInitializationEvent event )
	{
		GameRegistry.register( uvlblock = new Block( Material.IRON )
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
				return box;
			}

			@Override
			public BlockRenderLayer getBlockLayer()
			{
				return BlockRenderLayer.CUTOUT;
			}

		}.setLightLevel( 0.2f ).setCreativeTab( CreativeTabs.DECORATIONS ).setRegistryName( uvlblockR ) );
		GameRegistry.register( uvlblockItem = new ItemBlock( uvlblock ).setRegistryName( uvlblockR ) );

		ModelBakery.registerItemVariants( uvlblockItem, uvlblockR );

	}

	@EventHandler
	public void init( FMLInitializationEvent event )
	{
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( uvlblockItem, 0, new ModelResourceLocation( uvlblockR, "inventory" ) );
	}

}
