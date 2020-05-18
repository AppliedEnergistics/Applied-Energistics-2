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

package appeng.block;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockStateContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.helpers.AEGlassMaterial;
import appeng.helpers.ICustomCollision;
import appeng.util.LookDirection;
import appeng.util.Platform;


public abstract class AEBaseBlock extends Block
{

	private boolean isOpaque = true;
	private boolean isFullSize = true;
	private boolean hasSubtypes = false;
	private boolean isInventory = false;

	protected AxisAlignedBB boundingBox = FULL_BLOCK_AABB;

	protected AEBaseBlock( final Material mat )
	{
		super( mat );

		if( mat == AEGlassMaterial.INSTANCE || mat == Material.GLASS )
		{
			this.setSoundType( SoundType.GLASS );
		}
		else if( mat == Material.ROCK )
		{
			this.setSoundType( SoundType.STONE );
		}
		else if( mat == Material.WOOD )
		{
			this.setSoundType( SoundType.WOOD );
		}
		else
		{
			this.setSoundType( SoundType.METAL );
		}

		this.setLightOpacity( 255 );
		this.setLightLevel( 0 );
		this.setHardness( 2.2F );
		this.setHarvestLevel( "pickaxe", 0 );

		// Workaround as vanilla sets it way too early.
		this.fullBlock = this.isFullSize();
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer( this, this.getAEStates() );
	}

	@Override
	public final boolean isNormalCube( BlockState state )
	{
		return this.isFullSize() && this.isOpaque();
	}

	@Override
	public AxisAlignedBB getBoundingBox( BlockState state, IBlockReader source, BlockPos pos )
	{
		return this.boundingBox;
	}

	@SuppressWarnings( "deprecation" )
	@Override
	public void addCollisionBoxToList( final BlockState state, final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, @Nullable final Entity e, boolean p_185477_7_ )
	{
		final ICustomCollision collisionHandler = this.getCustomCollision( w, pos );

		if( collisionHandler != null && bb != null )
		{
			final List<AxisAlignedBB> tmp = new ArrayList<>();
			collisionHandler.addCollidingBlockToList( w, pos, bb, tmp, e );
			for( final AxisAlignedBB b : tmp )
			{
				final AxisAlignedBB offset = b.offset( pos.getX(), pos.getY(), pos.getZ() );
				if( bb.intersects( offset ) )
				{
					out.add( offset );
				}
			}
		}
		else
		{
			super.addCollisionBoxToList( state, w, pos, bb, out, e, p_185477_7_ );
		}
	}

	@SuppressWarnings( "deprecation" )
	@Override
	@OnlyIn( Dist.CLIENT )
	public AxisAlignedBB getSelectedBoundingBox( BlockState state, final World w, final BlockPos pos )
	{
		final ICustomCollision collisionHandler = this.getCustomCollision( w, pos );

		if( collisionHandler != null )
		{
			if( Platform.isClient() )
			{
				final PlayerEntity player = Minecraft.getInstance().player;
				final LookDirection ld = Platform.getPlayerRay( player, Platform.getEyeOffset( player ) );

				final Iterable<AxisAlignedBB> bbs = collisionHandler.getSelectedBoundingBoxesFromPool( w, pos, Minecraft.getInstance().player, true );
				AxisAlignedBB br = null;

				double lastDist = 0;

				for( final AxisAlignedBB bb : bbs )
				{
					this.boundingBox = bb;

					final RayTraceResult r = super.collisionRayTrace( state, w, pos, ld.getA(), ld.getB() );

					this.boundingBox = FULL_BLOCK_AABB;

					if( r != null )
					{
						final double xLen = ( ld.getA().x - r.hitVec.x );
						final double yLen = ( ld.getA().y - r.hitVec.y );
						final double zLen = ( ld.getA().z - r.hitVec.z );

						final double thisDist = xLen * xLen + yLen * yLen + zLen * zLen;

						if( br == null || lastDist > thisDist )
						{
							lastDist = thisDist;
							br = bb;
						}
					}
				}

				if( br != null )
				{
					br = new AxisAlignedBB( br.minX + pos.getX(), br.minY + pos.getY(), br.minZ + pos.getZ(), br.maxX + pos.getX(), br.maxY + pos
							.getY(), br.maxZ + pos.getZ() );
					return br;
				}
			}

			AxisAlignedBB b = null; // new AxisAlignedBB( 16d, 16d, 16d, 0d, 0d, 0d );

			for( final AxisAlignedBB bx : collisionHandler.getSelectedBoundingBoxesFromPool( w, pos, null, false ) )
			{
				if( b == null )
				{
					b = bx;
					continue;
				}

				final double minX = Math.min( b.minX, bx.minX );
				final double minY = Math.min( b.minY, bx.minY );
				final double minZ = Math.min( b.minZ, bx.minZ );
				final double maxX = Math.max( b.maxX, bx.maxX );
				final double maxY = Math.max( b.maxY, bx.maxY );
				final double maxZ = Math.max( b.maxZ, bx.maxZ );

				b = new AxisAlignedBB( minX, minY, minZ, maxX, maxY, maxZ );
			}

			if( b == null )
			{
				b = new AxisAlignedBB( 16d, 16d, 16d, 0d, 0d, 0d );
			}
			else
			{
				b = new AxisAlignedBB( b.minX + pos.getX(), b.minY + pos.getY(), b.minZ + pos.getZ(), b.maxX + pos.getX(), b.maxY + pos.getY(), b.maxZ + pos
						.getZ() );
			}

			return b;
		}

		return super.getSelectedBoundingBox( state, w, pos );
	}

	@Override
	public final boolean isOpaqueCube( BlockState state )
	{
		return this.isOpaque();
	}

	@SuppressWarnings( "deprecation" )
	@Override
	public RayTraceResult collisionRayTrace( final BlockState state, final World w, final BlockPos pos, final Vec3d a, final Vec3d b )
	{
		final ICustomCollision collisionHandler = this.getCustomCollision( w, pos );

		if( collisionHandler != null )
		{
			final Iterable<AxisAlignedBB> bbs = collisionHandler.getSelectedBoundingBoxesFromPool( w, pos, null, true );
			RayTraceResult br = null;

			double lastDist = 0;

			for( final AxisAlignedBB bb : bbs )
			{
				this.boundingBox = bb;

				final RayTraceResult r = super.collisionRayTrace( state, w, pos, a, b );

				this.boundingBox = FULL_BLOCK_AABB;

				if( r != null )
				{
					final double xLen = ( a.x - r.hitVec.x );
					final double yLen = ( a.y - r.hitVec.y );
					final double zLen = ( a.z - r.hitVec.z );

					final double thisDist = xLen * xLen + yLen * yLen + zLen * zLen;
					if( br == null || lastDist > thisDist )
					{
						lastDist = thisDist;
						br = r;
					}
				}
			}

			if( br != null )
			{
				return br;
			}

			return null;
		}

		this.boundingBox = FULL_BLOCK_AABB;
		return super.collisionRayTrace( state, w, pos, a, b );
	}

	@Override
	public boolean hasComparatorInputOverride( BlockState state )
	{
		return this.isInventory();
	}

	@Override
	public int getComparatorInputOverride( BlockState state, final World worldIn, final BlockPos pos )
	{
		return 0;
	}

	@Override
	public final boolean isNormalCube( BlockState state, final IBlockReader world, final BlockPos pos )
	{
		return this.isFullSize();
	}

	@Override
	public boolean rotateBlock( final World w, final BlockPos pos, final Direction axis )
	{
		final IOrientable rotatable = this.getOrientable( w, pos );

		if( rotatable != null && rotatable.canBeRotated() )
		{
			if( this.hasCustomRotation() )
			{
				this.customRotateBlock( rotatable, axis );
				return true;
			}
			else
			{
				Direction forward = rotatable.getForward();
				Direction up = rotatable.getUp();

				for( int rs = 0; rs < 4; rs++ )
				{
					forward = Platform.rotateAround( forward, axis );
					up = Platform.rotateAround( up, axis );

					if( this.isValidOrientation( w, pos, forward, up ) )
					{
						rotatable.setOrientation( forward, up );
						return true;
					}
				}
			}
		}

		return super.rotateBlock( w, pos, axis );
	}

	@Override
	public Direction[] getValidRotations( final World w, final BlockPos pos )
	{
		return new Direction[0];
	}

	@OnlyIn( Dist.CLIENT )
	@Override
	public void addInformation( final ItemStack is, final World world, final List<String> lines, final ITooltipFlag advancedItemTooltips )
	{

	}

	public boolean onActivated( final World w, final BlockPos pos, final PlayerEntity player, final Hand hand, final @Nullable ItemStack heldItem, final Direction side, final float hitX, final float hitY, final float hitZ )
	{
		return false;
	}

	public final Direction mapRotation( final IOrientable ori, final Direction dir )
	{
		// case DOWN: return bottomIcon;
		// case UP: return blockIcon;
		// case NORTH: return northIcon;
		// case SOUTH: return southIcon;
		// case WEST: return sideIcon;
		// case EAST: return sideIcon;

		final Direction forward = ori.getForward();
		final Direction up = ori.getUp();

		if( forward == null || up == null )
		{
			return dir;
		}

		final int west_x = forward.getFrontOffsetY() * up.getFrontOffsetZ() - forward.getFrontOffsetZ() * up.getFrontOffsetY();
		final int west_y = forward.getFrontOffsetZ() * up.getFrontOffsetX() - forward.getFrontOffsetX() * up.getFrontOffsetZ();
		final int west_z = forward.getFrontOffsetX() * up.getFrontOffsetY() - forward.getFrontOffsetY() * up.getFrontOffsetX();

		Direction west = null;
		for( final Direction dx : Direction.VALUES )
		{
			if( dx.getFrontOffsetX() == west_x && dx.getFrontOffsetY() == west_y && dx.getFrontOffsetZ() == west_z )
			{
				west = dx;
			}
		}

		if( west == null )
		{
			return dir;
		}

		if( dir == forward )
		{
			return Direction.SOUTH;
		}
		if( dir == forward.getOpposite() )
		{
			return Direction.NORTH;
		}

		if( dir == up )
		{
			return Direction.UP;
		}
		if( dir == up.getOpposite() )
		{
			return Direction.DOWN;
		}

		if( dir == west )
		{
			return Direction.WEST;
		}
		if( dir == west.getOpposite() )
		{
			return Direction.EAST;
		}

		return null;
	}

	@Override
	public String toString()
	{
		String regName = this.getRegistryName() != null ? this.getRegistryName().getResourcePath() : "unregistered";
		return this.getClass().getSimpleName() + "[" + regName + "]";
	}

	protected String getTranslationKey( final ItemStack is )
	{
		return this.getTranslationKey();
	}

	protected boolean hasCustomRotation()
	{
		return false;
	}

	protected void customRotateBlock( final IOrientable rotatable, final Direction axis )
	{

	}

	protected IOrientable getOrientable( final IBlockReader w, final BlockPos pos )
	{
		if( this instanceof IOrientableBlock )
		{
			IOrientableBlock orientable = (IOrientableBlock) this;
			return orientable.getOrientable( w, pos );
		}
		return null;
	}

	protected boolean isValidOrientation( final World w, final BlockPos pos, final Direction forward, final Direction up )
	{
		return true;
	}

	protected ICustomCollision getCustomCollision( final World w, final BlockPos pos )
	{
		if( this instanceof ICustomCollision )
		{
			return (ICustomCollision) this;
		}
		return null;
	}

	protected IProperty[] getAEStates()
	{
		return new IProperty[0];
	}

	protected boolean isOpaque()
	{
		return this.isOpaque;
	}

	protected boolean setOpaque( final boolean isOpaque )
	{
		this.isOpaque = isOpaque;
		return isOpaque;
	}

	protected boolean hasSubtypes()
	{
		return this.hasSubtypes;
	}

	protected void setHasSubtypes( final boolean hasSubtypes )
	{
		this.hasSubtypes = hasSubtypes;
	}

	protected boolean isFullSize()
	{
		return this.isFullSize;
	}

	protected boolean setFullSize( final boolean isFullSize )
	{
		this.isFullSize = isFullSize;
		return isFullSize;
	}

	protected boolean isInventory()
	{
		return this.isInventory;
	}

	protected void setInventory( final boolean isInventory )
	{
		this.isInventory = isInventory;
	}

}
