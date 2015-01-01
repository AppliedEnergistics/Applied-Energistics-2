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

package appeng.items.tools.powered;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.base.Optional;

import appeng.api.util.DimensionalCoord;
import appeng.block.misc.BlockTinyTNT;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.hooks.DispenserBlockTool;
import appeng.hooks.IBlockTool;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.InWorldToolOperationResult;
import appeng.util.Platform;


public class ToolEntropyManipulator extends AEBasePoweredItem implements IBlockTool
{
	private final Map<InWorldToolOperationIngredient, InWorldToolOperationResult> heatUp;
	private final Map<InWorldToolOperationIngredient, InWorldToolOperationResult> coolDown;

	public ToolEntropyManipulator()
	{
		super( ToolEntropyManipulator.class, Optional.<String>absent() );

		this.setFeature( EnumSet.of( AEFeature.EntropyManipulator, AEFeature.PoweredTools ) );

		this.heatUp = new HashMap<InWorldToolOperationIngredient, InWorldToolOperationResult>();
		this.coolDown = new HashMap<InWorldToolOperationIngredient, InWorldToolOperationResult>();

		this.maxStoredPower = AEConfig.instance.entropyManipulatorBattery;

		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.stone, 0 ), new InWorldToolOperationResult( new ItemStack( Blocks.cobblestone ) ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.stonebrick, 0 ), new InWorldToolOperationResult( new ItemStack( Blocks.stonebrick, 1, 2 ) ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.lava, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.obsidian ) ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.flowing_lava, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.obsidian ) ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.grass, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.dirt ) ) );

		List<ItemStack> snowBalls = new ArrayList<ItemStack>();
		snowBalls.add( new ItemStack( Items.snowball ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.flowing_water, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( null, snowBalls ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.water, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.ice ) ) );

		this.heatUp.put( new InWorldToolOperationIngredient( Blocks.ice, 0 ), new InWorldToolOperationResult( new ItemStack( Blocks.water ) ) );
		this.heatUp.put( new InWorldToolOperationIngredient( Blocks.flowing_water, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult() );
		this.heatUp.put( new InWorldToolOperationIngredient( Blocks.water, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult() );
		this.heatUp.put( new InWorldToolOperationIngredient( Blocks.snow, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.flowing_water ) ) );
	}

	@Override
	public void postInit()
	{
		super.postInit();
		BlockDispenser.dispenseBehaviorRegistry.putObject( this, new DispenserBlockTool() );
	}

	private static class InWorldToolOperationIngredient
	{
		private final Block blockID;
		private final int metadata;

		public InWorldToolOperationIngredient( Block blockID, int metadata )
		{
			this.blockID = blockID;
			this.metadata = metadata;
		}

		@Override
		public int hashCode()
		{
			return this.blockID.hashCode() ^ this.metadata;
		}

		@Override
		public boolean equals( Object obj )
		{
			if ( obj == null )
				return false;
			if ( this.getClass() != obj.getClass() )
				return false;
			InWorldToolOperationIngredient other = (InWorldToolOperationIngredient) obj;
			return this.blockID == other.blockID && this.metadata == other.metadata;
		}

		public int getMetadata()
		{
			return this.metadata;
		}
	}

	private void heat( Block blockID, int metadata, World w, int x, int y, int z )
	{
		InWorldToolOperationResult r = this.heatUp.get( new InWorldToolOperationIngredient( blockID, metadata ) );

		if ( r == null )
		{
			r = this.heatUp.get( new InWorldToolOperationIngredient( blockID, OreDictionary.WILDCARD_VALUE ) );
		}

		if ( r.BlockItem != null )
		{
			w.setBlock( x, y, z, Block.getBlockFromItem( r.BlockItem.getItem() ), r.BlockItem.getItemDamage(), 3 );
		}
		else
		{
			w.setBlock( x, y, z, Platform.air, 0, 3 );
		}

		if ( r.Drops != null )
		{
			Platform.spawnDrops( w, x, y, z, r.Drops );
		}
	}

	private boolean canHeat( Block blockID, int metadata )
	{
		InWorldToolOperationResult r = this.heatUp.get( new InWorldToolOperationIngredient( blockID, metadata ) );

		if ( r == null )
		{
			r = this.heatUp.get( new InWorldToolOperationIngredient( blockID, OreDictionary.WILDCARD_VALUE ) );
		}

		return r != null;
	}

	private void cool( Block blockID, int metadata, World w, int x, int y, int z )
	{
		InWorldToolOperationResult r = this.coolDown.get( new InWorldToolOperationIngredient( blockID, metadata ) );

		if ( r == null )
		{
			r = this.coolDown.get( new InWorldToolOperationIngredient( blockID, OreDictionary.WILDCARD_VALUE ) );
		}

		if ( r.BlockItem != null )
		{
			w.setBlock( x, y, z, Block.getBlockFromItem( r.BlockItem.getItem() ), r.BlockItem.getItemDamage(), 3 );
		}
		else
		{
			w.setBlock( x, y, z, Platform.air, 0, 3 );
		}

		if ( r.Drops != null )
		{
			Platform.spawnDrops( w, x, y, z, r.Drops );
		}
	}

	private boolean canCool( Block blockID, int metadata )
	{
		InWorldToolOperationResult r = this.coolDown.get( new InWorldToolOperationIngredient( blockID, metadata ) );

		if ( r == null )
		{
			r = this.coolDown.get( new InWorldToolOperationIngredient( blockID, OreDictionary.WILDCARD_VALUE ) );
		}

		return r != null;
	}

	@Override
	public boolean hitEntity( ItemStack item, EntityLivingBase target, EntityLivingBase hitter )
	{
		if ( this.getAECurrentPower( item ) > 1600 )
		{
			this.extractAEPower( item, 1600 );
			target.setFire( 8 );
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick( ItemStack item, World w, EntityPlayer p )
	{
		MovingObjectPosition target = this.getMovingObjectPositionFromPlayer( w, p, true );

		if ( target == null )
			return item;
		else
		{
			if ( target.typeOfHit == MovingObjectType.BLOCK )
			{
				int x = target.blockX;
				int y = target.blockY;
				int z = target.blockZ;

				if ( w.getBlock( x, y, z ).getMaterial() == Material.lava || w.getBlock( x, y, z ).getMaterial() == Material.water )
				{
					if ( Platform.hasPermissions( new DimensionalCoord( w, x, y, z ), p ) )
					{
						this.onItemUse( item, p, w, x, y, z, 0, 0.0F, 0.0F, 0.0F );
					}
				}
			}
		}

		return item;
	}

	@Override
	public boolean onItemUse( ItemStack item, EntityPlayer p, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ )
	{
		if ( this.getAECurrentPower( item ) > 1600 )
		{
			if ( !p.canPlayerEdit( x, y, z, side, item ) )
				return false;

			Block blockID = w.getBlock( x, y, z );
			int metadata = w.getBlockMetadata( x, y, z );

			if ( p.isSneaking() )
			{
				if ( this.canCool( blockID, metadata ) )
				{
					this.extractAEPower( item, 1600 );
					this.cool( blockID, metadata, w, x, y, z );
					return true;
				}
			}
			else
			{
				if ( blockID instanceof BlockTNT )
				{
					w.setBlock( x, y, z, Platform.air, 0, 3 );
					( (BlockTNT) blockID ).func_150114_a( w, x, y, z, 1, p );
					return true;
				}

				if ( blockID instanceof BlockTinyTNT )
				{
					w.setBlock( x, y, z, Platform.air, 0, 3 );
					( (BlockTinyTNT) blockID ).startFuse( w, x, y, z, p );
					return true;
				}

				if ( this.canHeat( blockID, metadata ) )
				{
					this.extractAEPower( item, 1600 );
					this.heat( blockID, metadata, w, x, y, z );
					return true;
				}

				ItemStack[] stack = Platform.getBlockDrops( w, x, y, z );
				List<ItemStack> out = new ArrayList<ItemStack>();
				boolean hasFurnaceable = false;
				boolean canFurnaceable = true;

				for ( ItemStack i : stack )
				{
					ItemStack result = FurnaceRecipes.smelting().getSmeltingResult( i );

					if ( result != null )
					{
						if ( result.getItem() instanceof ItemBlock )
						{
							if ( Block.getBlockFromItem( result.getItem() ) == blockID && result.getItem().getDamage( result ) == metadata )
							{
								canFurnaceable = false;
							}
						}
						hasFurnaceable = true;
						out.add( result );
					}
					else
					{
						canFurnaceable = false;
						out.add( i );
					}
				}

				if ( hasFurnaceable && canFurnaceable )
				{
					this.extractAEPower( item, 1600 );
					InWorldToolOperationResult or = InWorldToolOperationResult.getBlockOperationResult( out.toArray( new ItemStack[out.size()] ) );
					w.playSoundEffect( x + 0.5D, y + 0.5D, z + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F );

					if ( or.BlockItem == null )
					{
						w.setBlock( x, y, z, Platform.air, 0, 3 );
					}
					else
					{
						w.setBlock( x, y, z, Block.getBlockFromItem( or.BlockItem.getItem() ), or.BlockItem.getItemDamage(), 3 );
					}

					if ( or.Drops != null )
					{
						Platform.spawnDrops( w, x, y, z, or.Drops );
					}

					return true;
				}
				else
				{
					ForgeDirection dir = ForgeDirection.getOrientation( side );
					x += dir.offsetX;
					y += dir.offsetY;
					z += dir.offsetZ;

					if ( !p.canPlayerEdit( x, y, z, side, item ) )
						return false;

					if ( w.isAirBlock( x, y, z ) )
					{
						this.extractAEPower( item, 1600 );
						w.playSoundEffect( x + 0.5D, y + 0.5D, z + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F );
						w.setBlock( x, y, z, Blocks.fire );
					}

					return true;
				}
			}
		}

		return false;
	}
}
