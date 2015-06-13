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

package appeng.block.solids;


import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.core.worlddata.WorldData;
import appeng.helpers.LocationRotation;
import appeng.helpers.NullRotation;
import appeng.util.Platform;


public class BlockSkyStone extends AEBaseBlock implements IOrientableBlock
{
	private static final float BLOCK_RESISTANCE = 150.0f;
	private static final double BREAK_SPEAK_SCALAR = 0.1;
	private static final double BREAK_SPEAK_THRESHOLD = 7.0;

	@SideOnly( Side.CLIENT )
	private IIcon block;

	@SideOnly( Side.CLIENT )
	private IIcon brick;

	@SideOnly( Side.CLIENT )
	private IIcon smallBrick;

	public BlockSkyStone()
	{
		super( Material.rock );
		this.setHardness( 50 );
		this.hasSubtypes = true;
		this.blockResistance = BLOCK_RESISTANCE;
		this.setHarvestLevel( "pickaxe", 3, 0 );
		this.setFeature( EnumSet.of( AEFeature.Core ) );

		MinecraftForge.EVENT_BUS.register( this );
	}

	@SubscribeEvent
	public void breakFaster( PlayerEvent.BreakSpeed event )
	{
		if( event.block == this && event.entityPlayer != null )
		{
			ItemStack is = event.entityPlayer.inventory.getCurrentItem();
			int level = -1;

			if( is != null && is.getItem() != null )
			{
				level = is.getItem().getHarvestLevel( is, "pickaxe" );
			}

			if( event.metadata > 0 || level >= 3 || event.originalSpeed > BREAK_SPEAK_THRESHOLD )
			{
				event.newSpeed /= BREAK_SPEAK_SCALAR;
			}
		}
	}

	@Override
	public int damageDropped( int meta )
	{
		return meta;
	}

	@Override
	public ItemStack getPickBlock( MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player )
	{
		final ItemStack is = super.getPickBlock( target, world, x, y, z, player );
		is.setItemDamage( world.getBlockMetadata( x, y, z ) );

		return is;
	}

	@Override
	public void onBlockAdded( World w, int x, int y, int z )
	{
		super.onBlockAdded( w, x, y, z );
		if( Platform.isServer() )
		{
			WorldData.instance().compassData().service().updateArea( w, x, y, z );
		}
	}

	@Override
	public boolean usesMetadata()
	{
		return false;
	}

	@Override
	public IOrientable getOrientable( final IBlockAccess w, final int x, final int y, final int z )
	{
		if( w.getBlockMetadata( x, y, z ) == 0 )
		{
			return new LocationRotation( w, x, y, z );
		}

		return new NullRotation();
	}

	@Override
	public String getUnlocalizedName( ItemStack is )
	{
		if( is.getItemDamage() == 1 )
		{
			return this.getUnlocalizedName() + ".Block";
		}

		if( is.getItemDamage() == 2 )
		{
			return this.getUnlocalizedName() + ".Brick";
		}

		if( is.getItemDamage() == 3 )
		{
			return this.getUnlocalizedName() + ".SmallBrick";
		}

		return this.getUnlocalizedName();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void registerBlockIcons( IIconRegister ir )
	{
		super.registerBlockIcons( ir );
		this.block = ir.registerIcon( this.getTextureName() + ".Block" );
		this.brick = ir.registerIcon( this.getTextureName() + ".Brick" );
		this.smallBrick = ir.registerIcon( this.getTextureName() + ".SmallBrick" );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public IIcon getIcon( int direction, int metadata )
	{
		if( metadata == 1 )
		{
			return this.block;
		}
		if( metadata == 2 )
		{
			return this.brick;
		}
		if( metadata == 3 )
		{
			return this.smallBrick;
		}
		return super.getIcon( direction, metadata );
	}

	@Override
	public void setRenderStateByMeta( int metadata )
	{
		this.getRendererInstance().setTemporaryRenderIcon( this.getIcon( 0, metadata ) );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( Item item, CreativeTabs tabs, List<ItemStack> itemStacks )
	{
		super.getCheckedSubBlocks( item, tabs, itemStacks );

		itemStacks.add( new ItemStack( item, 1, 1 ) );
		itemStacks.add( new ItemStack( item, 1, 2 ) );
		itemStacks.add( new ItemStack( item, 1, 3 ) );
	}

	@Override
	public void breakBlock( World w, int x, int y, int z, Block b, int metadata )
	{
		super.breakBlock( w, x, y, z, b, metadata );
		if( Platform.isServer() )
		{
			WorldData.instance().compassData().service().updateArea( w, x, y, z );
		}
	}
}
