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

package appeng.block.misc;


import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.EntityRegistry;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderTinyTNT;
import appeng.client.texture.FullIcon;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.entity.EntityIds;
import appeng.entity.EntityTinyTNTPrimed;
import appeng.helpers.ICustomCollision;
import appeng.hooks.DispenserBehaviorTinyTNT;

public class BlockTinyTNT extends AEBaseBlock implements ICustomCollision
{

	public BlockTinyTNT() {
		super( BlockTinyTNT.class, Material.tnt );
		setFeature( EnumSet.of( AEFeature.TinyTNT ) );
		setLightOpacity( 1 );
		setBlockBounds( 0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f );
		isFullSize = isOpaque = false;
		setStepSound( soundTypeGrass );
		setHardness( 0F );

		EntityRegistry.registerModEntity( EntityTinyTNTPrimed.class, "EntityTinyTNTPrimed", EntityIds.TINY_TNT, AppEng.instance, 16, 4, true );
	}

	@Override
	public void postInit()
	{
		super.postInit();
		BlockDispenser.dispenseBehaviorRegistry.putObject( Item.getItemFromBlock( this ), new DispenserBehaviorTinyTNT() );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderTinyTNT.class;
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegistry)
	{
		// no images required.
	}

	@Override
	public IIcon getIcon(int direction, int metadata)
	{
		return new FullIcon( Blocks.tnt.getIcon( direction, metadata ) );
	}

	@Override
	public void onEntityCollidedWithBlock(World w, int x, int y, int z, Entity entity)
	{
		if ( entity instanceof EntityArrow && !w.isRemote )
		{
			EntityArrow entityarrow = (EntityArrow) entity;

			if ( entityarrow.isBurning() )
			{
				this.startFuse( w, x, y, z, entityarrow.shootingEntity instanceof EntityLivingBase ? (EntityLivingBase) entityarrow.shootingEntity : null );
				w.setBlockToAir( x, y, z );
			}
		}
	}

	@Override
	public void onBlockAdded(World w, int x, int y, int z)
	{
		super.onBlockAdded( w, x, y, z );

		if ( w.isBlockIndirectlyGettingPowered( x, y, z ) )
		{
			this.startFuse( w, x, y, z, null );
			w.setBlockToAir( x, y, z );
		}
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block id)
	{
		if ( w.isBlockIndirectlyGettingPowered( x, y, z ) )
		{
			this.startFuse( w, x, y, z, null );
			w.setBlockToAir( x, y, z );
		}
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if ( player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Items.flint_and_steel )
		{
			this.startFuse( w, x, y, z, player );
			w.setBlockToAir( x, y, z );
			player.getCurrentEquippedItem().damageItem( 1, player );
			return true;
		}
		else
		{
			return super.onActivated( w, x, y, z, player, side, hitX, hitY, hitZ );
		}
	}

	@Override
	public void onBlockDestroyedByExplosion(World w, int x, int y, int z, Explosion exp)
	{
		if ( !w.isRemote )
		{
			EntityTinyTNTPrimed primedTinyTNTEntity = new EntityTinyTNTPrimed( w, x + 0.5F, y + 0.5F, z + 0.5F, exp.getExplosivePlacedBy() );
			primedTinyTNTEntity.fuse = w.rand.nextInt( primedTinyTNTEntity.fuse / 4 ) + primedTinyTNTEntity.fuse / 8;
			w.spawnEntityInWorld( primedTinyTNTEntity );
		}
	}

	public void startFuse(World w, int x, int y, int z, EntityLivingBase igniter)
	{
		if ( !w.isRemote )
		{
			EntityTinyTNTPrimed primedTinyTNTEntity = new EntityTinyTNTPrimed( w, x + 0.5F, y + 0.5F, z + 0.5F, igniter );
			w.spawnEntityInWorld( primedTinyTNTEntity );
			w.playSoundAtEntity( primedTinyTNTEntity, "game.tnt.primed", 1.0F, 1.0F );
		}
	}

	@Override
	public boolean canDropFromExplosion(Explosion exp)
	{
		return false;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(World w, int x, int y, int z, Entity e, boolean isVisual)
	{
		return Collections.singletonList( AxisAlignedBB.getBoundingBox( 0.25, 0, 0.25, 0.75, 0.5, 0.75 ) );
	}

	@Override
	public void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB bb, List<AxisAlignedBB> out, Entity e)
	{
		out.add( AxisAlignedBB.getBoundingBox( 0.25, 0, 0.25, 0.75, 0.5, 0.75 ) );
	}

}
