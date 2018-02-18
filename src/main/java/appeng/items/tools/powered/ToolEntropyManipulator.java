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


import appeng.api.util.DimensionalCoord;
import appeng.block.misc.BlockTinyTNT;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.hooks.DispenserBlockTool;
import appeng.hooks.IBlockTool;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.InWorldToolOperationResult;
import appeng.util.Platform;
import com.google.common.base.Optional;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;


public class ToolEntropyManipulator extends AEBasePoweredItem implements IBlockTool
{
	private final Map<InWorldToolOperationIngredient, InWorldToolOperationResult> heatUp;
	private final Map<InWorldToolOperationIngredient, InWorldToolOperationResult> coolDown;

	public ToolEntropyManipulator()
	{
		super( AEConfig.instance.entropyManipulatorBattery, Optional.<String>absent() );

		this.setFeature( EnumSet.of( AEFeature.EntropyManipulator, AEFeature.PoweredTools ) );

		this.heatUp = new HashMap<InWorldToolOperationIngredient, InWorldToolOperationResult>();
		this.coolDown = new HashMap<InWorldToolOperationIngredient, InWorldToolOperationResult>();

		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.stone, 0 ), new InWorldToolOperationResult( new ItemStack( Blocks.cobblestone ) ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.stonebrick, 0 ), new InWorldToolOperationResult( new ItemStack( Blocks.stonebrick, 1, 2 ) ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.lava, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.obsidian ) ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.flowing_lava, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.obsidian ) ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.grass, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.dirt ) ) );

		final List<ItemStack> snowBalls = new ArrayList<ItemStack>();
		snowBalls.add( new ItemStack( Items.snowball ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.flowing_water, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( null, snowBalls ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.water, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.ice ) ) );

		this.heatUp.put( new InWorldToolOperationIngredient( Blocks.ice, 0 ), new InWorldToolOperationResult( new ItemStack( Blocks.water ) ) );
		this.heatUp.put( new InWorldToolOperationIngredient( Blocks.flowing_water, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult() );
		this.heatUp.put( new InWorldToolOperationIngredient( Blocks.water, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult() );
		this.heatUp.put( new InWorldToolOperationIngredient( Blocks.snow, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.flowing_water ) ) );
	}

	private static final boolean breakBlockWithCheck( final World w, final EntityPlayer p, final int x, final int y, final int z )
	{
		BlockEvent.BreakEvent event = new BlockEvent.BreakEvent( x, y, z, w, w.getBlock( x, y, z ), w.getBlockMetadata( x, y, z ), p );
		MinecraftForge.EVENT_BUS.post( event );
		return !event.isCanceled() && w.setBlockToAir( x, y, z );
	}

	@Override
	public void postInit()
	{
		super.postInit();
		BlockDispenser.dispenseBehaviorRegistry.putObject( this, new DispenserBlockTool() );
	}

	private boolean heat( final Block blockID, final EntityPlayer p, final int metadata, final World w, final int x, final int y, final int z )
	{
		if( !breakBlockWithCheck( w, p, x, y, z ) )
			return false;

		InWorldToolOperationResult r = this.heatUp.get( new InWorldToolOperationIngredient( blockID, metadata ) );


		if( r == null )
		{
			r = this.heatUp.get( new InWorldToolOperationIngredient( blockID, OreDictionary.WILDCARD_VALUE ) );
		}

		if( r.getBlockItem() != null )
		{
			w.setBlock( x, y, z, Block.getBlockFromItem( r.getBlockItem().getItem() ), r.getBlockItem().getItemDamage(), 3 );
		}

		if( r.getDrops() != null )
		{
			Platform.spawnDrops( w, x, y, z, r.getDrops() );
		}

		return true;
	}

	private boolean canHeat( final Block blockID, final int metadata )
	{
		InWorldToolOperationResult r = this.heatUp.get( new InWorldToolOperationIngredient( blockID, metadata ) );

		if( r == null )
		{
			r = this.heatUp.get( new InWorldToolOperationIngredient( blockID, OreDictionary.WILDCARD_VALUE ) );
		}

		return r != null;
	}

	private boolean cool( final Block blockID, final EntityPlayer p, final int metadata, final World w, final int x, final int y, final int z )
	{
		if( !breakBlockWithCheck( w, p, x, y, z ) )
			return false;

		InWorldToolOperationResult r = this.coolDown.get( new InWorldToolOperationIngredient( blockID, metadata ) );

		if( r == null )
		{
			r = this.coolDown.get( new InWorldToolOperationIngredient( blockID, OreDictionary.WILDCARD_VALUE ) );
		}

		if( r.getBlockItem() != null )
		{
			w.setBlock( x, y, z, Block.getBlockFromItem( r.getBlockItem().getItem() ), r.getBlockItem().getItemDamage(), 3 );
		}

		if( r.getDrops() != null )
		{
			Platform.spawnDrops( w, x, y, z, r.getDrops() );
		}

		return true;
	}

	private boolean canCool( final Block blockID, final int metadata )
	{
		InWorldToolOperationResult r = this.coolDown.get( new InWorldToolOperationIngredient( blockID, metadata ) );

		if( r == null )
		{
			r = this.coolDown.get( new InWorldToolOperationIngredient( blockID, OreDictionary.WILDCARD_VALUE ) );
		}

		return r != null;
	}

	@Override
	public boolean hitEntity( final ItemStack item, final EntityLivingBase target, final EntityLivingBase hitter )
	{
		if( this.getAECurrentPower( item ) > 1600 )
		{
			this.extractAEPower( item, 1600 );
			target.setFire( 8 );
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick( final ItemStack item, final World w, final EntityPlayer p )
	{
		final MovingObjectPosition target = this.getMovingObjectPositionFromPlayer( w, p, true );

		if( target == null )
		{
			return item;
		}
		else
		{
			if( target.typeOfHit == MovingObjectType.BLOCK )
			{
				final int x = target.blockX;
				final int y = target.blockY;
				final int z = target.blockZ;

				if( w.getBlock( x, y, z ).getMaterial() == Material.lava || w.getBlock( x, y, z ).getMaterial() == Material.water )
				{
					if( Platform.hasPermissions( new DimensionalCoord( w, x, y, z ), p ) )
					{
						this.onItemUse( item, p, w, x, y, z, 0, 0.0F, 0.0F, 0.0F );
					}
				}
			}
		}

		return item;
	}

	@Override
	public boolean onItemUse( final ItemStack item, final EntityPlayer p, final World w, int x, int y, int z, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( this.getAECurrentPower( item ) > 1600 )
		{
			if( !p.canPlayerEdit( x, y, z, side, item ) )
			{
				return false;
			}

			final Block blockID = w.getBlock( x, y, z );
			final int metadata = w.getBlockMetadata( x, y, z );

			if( blockID == null || ForgeEventFactory.onPlayerInteract( p,
					blockID.isAir( w, x, y, z ) ? PlayerInteractEvent.Action.RIGHT_CLICK_AIR : PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,
					x, y, z, side, w ).isCanceled() )
				return false;

			if( p.isSneaking() )
			{
				if( this.canCool( blockID, metadata ) )
				{
					if( this.cool( blockID, p, metadata, w, x, y, z ) )
					{
						this.extractAEPower( item, 1600 );
						return true;
					}
					return false;
				}
			}
			else
			{
				if( blockID instanceof BlockTNT )
				{
					if( !breakBlockWithCheck( w, p, x, y, z ) )
						return false;
					( (BlockTNT) blockID ).func_150114_a( w, x, y, z, 1, p );
					return true;
				}

				if( blockID instanceof BlockTinyTNT )
				{
					if( !breakBlockWithCheck( w, p, x, y, z ) )
						return false;
					( (BlockTinyTNT) blockID ).startFuse( w, x, y, z, p );
					return true;
				}

				if( this.canHeat( blockID, metadata ) )
				{
					if( this.heat( blockID, p, metadata, w, x, y, z ) )
					{
						this.extractAEPower( item, 1600 );
						return true;
					}
					return false;
				}

				final ItemStack[] stack = Platform.getBlockDrops( w, x, y, z );
				final List<ItemStack> out = new ArrayList<ItemStack>();
				boolean hasFurnaceable = false;
				boolean canFurnaceable = true;

				for( final ItemStack i : stack )
				{
					final ItemStack result = FurnaceRecipes.smelting().getSmeltingResult( i );

					if( result != null )
					{
						if( result.getItem() instanceof ItemBlock )
						{
							if( Block.getBlockFromItem( result.getItem() ) == blockID && result.getItem().getDamage( result ) == metadata )
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

				if( hasFurnaceable && canFurnaceable )
				{
					if( !breakBlockWithCheck( w, p, x, y, z ) )
						return false;

					this.extractAEPower( item, 1600 );
					final InWorldToolOperationResult or = InWorldToolOperationResult.getBlockOperationResult( out.toArray( new ItemStack[out.size()] ) );
					w.playSoundEffect( x + 0.5D, y + 0.5D, z + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F );

					if( or.getBlockItem() != null )
					{
						w.setBlock( x, y, z, Block.getBlockFromItem( or.getBlockItem().getItem() ), or.getBlockItem().getItemDamage(), 3 );
					}

					if( or.getDrops() != null )
					{
						Platform.spawnDrops( w, x, y, z, or.getDrops() );
					}

					return true;
				}
				else
				{
					final ForgeDirection dir = ForgeDirection.getOrientation( side );
					x += dir.offsetX;
					y += dir.offsetY;
					z += dir.offsetZ;

					if( !p.canPlayerEdit( x, y, z, side, item ) )
					{
						return false;
					}

					if( w.isAirBlock( x, y, z ) )
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

	private static class InWorldToolOperationIngredient
	{
		private final Block blockID;
		private final int metadata;

		public InWorldToolOperationIngredient( final Block blockID, final int metadata )
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
		public boolean equals( final Object obj )
		{
			if( obj == null )
			{
				return false;
			}
			if( this.getClass() != obj.getClass() )
			{
				return false;
			}
			final InWorldToolOperationIngredient other = (InWorldToolOperationIngredient) obj;
			return this.blockID == other.blockID && this.metadata == other.metadata;
		}
	}

}
