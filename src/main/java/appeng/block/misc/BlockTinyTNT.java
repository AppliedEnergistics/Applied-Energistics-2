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

package appeng.block.misc;


import appeng.block.AEBaseBlock;
import appeng.client.render.blocks.RenderTinyTNT;
import appeng.client.texture.FullIcon;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.entity.EntityIds;
import appeng.entity.EntityTinyTNTPrimed;
import appeng.helpers.ICustomCollision;
import appeng.hooks.DispenserBehaviorTinyTNT;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;


public class BlockTinyTNT extends AEBaseBlock implements ICustomCollision
{

	public BlockTinyTNT()
	{
		super( Material.tnt );
		this.setLightOpacity( 1 );
		this.setBlockBounds( 0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f );
		this.isFullSize = this.isOpaque = false;
		this.setStepSound( soundTypeGrass );
		this.setHardness( 0F );
		this.setFeature( EnumSet.of( AEFeature.TinyTNT ) );

		EntityRegistry.registerModEntity( EntityTinyTNTPrimed.class, "EntityTinyTNTPrimed", EntityIds.get( EntityTinyTNTPrimed.class ), AppEng.instance(), 16, 4, true );
	}

	@Override
	@SideOnly( Side.CLIENT )
	protected RenderTinyTNT getRenderer()
	{
		return new RenderTinyTNT();
	}

	@Override
	public void postInit()
	{
		super.postInit();
		BlockDispenser.dispenseBehaviorRegistry.putObject( Item.getItemFromBlock( this ), new DispenserBehaviorTinyTNT() );
	}

	@Override
	public IIcon getIcon( final int direction, final int metadata )
	{
		return new FullIcon( Blocks.tnt.getIcon( direction, metadata ) );
	}

	@Override
	public boolean onBlockActivated( World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ )
	{
		if( player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Items.flint_and_steel )
		{
			this.startFuse( w, x, y, z, player );
			w.setBlockToAir( x, y, z );
			player.getCurrentEquippedItem().damageItem( 1, player );

			return true;
		}

		return super.onBlockActivated( w, x, y, z, player, side, hitX, hitY, hitZ );
	}

	@Override
	public void registerBlockIcons( final IIconRegister iconRegistry )
	{
		// no images required.
	}

	@Override
	public void onBlockAdded( final World w, final int x, final int y, final int z )
	{
		super.onBlockAdded( w, x, y, z );

		if( w.isBlockIndirectlyGettingPowered( x, y, z ) )
		{
			this.startFuse( w, x, y, z, null );
			w.setBlockToAir( x, y, z );
		}
	}

	@Override
	public void onNeighborBlockChange( final World w, final int x, final int y, final int z, final Block id )
	{
		if( w.isBlockIndirectlyGettingPowered( x, y, z ) )
		{
			this.startFuse( w, x, y, z, null );
			w.setBlockToAir( x, y, z );
		}
	}

	@Override
	public void onBlockDestroyedByExplosion( final World w, final int x, final int y, final int z, final Explosion exp )
	{
		if( !w.isRemote )
		{
			final EntityTinyTNTPrimed primedTinyTNTEntity = new EntityTinyTNTPrimed( w, x + 0.5F, y + 0.5F, z + 0.5F, exp.getExplosivePlacedBy() );
			primedTinyTNTEntity.fuse = w.rand.nextInt( primedTinyTNTEntity.fuse / 4 ) + primedTinyTNTEntity.fuse / 8;
			w.spawnEntityInWorld( primedTinyTNTEntity );
		}
	}

	@Override
	public void onEntityCollidedWithBlock( final World w, final int x, final int y, final int z, final Entity entity )
	{
		if( entity instanceof EntityArrow && !w.isRemote )
		{
			final EntityArrow entityarrow = (EntityArrow) entity;

			if( entityarrow.isBurning() )
			{
				this.startFuse( w, x, y, z, entityarrow.shootingEntity instanceof EntityLivingBase ? (EntityLivingBase) entityarrow.shootingEntity : null );
				w.setBlockToAir( x, y, z );
			}
		}
	}

	@Override
	public boolean canDropFromExplosion( final Explosion exp )
	{
		return false;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final int x, final int y, final int z, final Entity e, final boolean isVisual )
	{
		return Collections.singletonList( AxisAlignedBB.getBoundingBox( 0.25, 0, 0.25, 0.75, 0.5, 0.75 ) );
	}

	@Override
	public void addCollidingBlockToList( final World w, final int x, final int y, final int z, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e )
	{
		out.add( AxisAlignedBB.getBoundingBox( 0.25, 0, 0.25, 0.75, 0.5, 0.75 ) );
	}

	public void startFuse( final World w, final int x, final int y, final int z, final EntityLivingBase igniter )
	{
		if( !w.isRemote )
		{
			final EntityTinyTNTPrimed primedTinyTNTEntity = new EntityTinyTNTPrimed( w, x + 0.5F, y + 0.5F, z + 0.5F, igniter );
			w.spawnEntityInWorld( primedTinyTNTEntity );
			w.playSoundAtEntity( primedTinyTNTEntity, "game.tnt.primed", 1.0F, 1.0F );
		}
	}
}
