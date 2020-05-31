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


import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.helpers.ICustomCollision;


public abstract class AEBaseBlock extends Block
{

	private boolean isOpaque = true;
	private boolean isFullSize = true;
	private boolean isInventory = false;

	protected VoxelShape boundingBox = VoxelShapes.fullCube();

	protected AEBaseBlock( final Block.Properties props )
	{
		super( props );

		// FIXME: Move to block registration
		// FIXME if( mat == AEGlassMaterial.INSTANCE || mat == Material.GLASS )
		// FIXME {
		// FIXME 	this.setSoundType( SoundType.GLASS );
		// FIXME }
		// FIXME else if( mat == Material.ROCK )
		// FIXME {
		// FIXME 	this.setSoundType( SoundType.STONE );
		// FIXME }
		// FIXME else if( mat == Material.WOOD )
		// FIXME {
		// FIXME 	this.setSoundType( SoundType.WOOD );
		// FIXME }
		// FIXME else
		// FIXME {
		// FIXME 	this.setSoundType( SoundType.METAL );
		// FIXME }

		// FIXME this.setLightOpacity( 255 );
		// FIXME this.setLightLevel( 0 );
		// FIXME this.setHardness( 2.2F );
		// FIXME this.setHarvestLevel( "pickaxe", 0 );
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(getAEStates());
	}

	@Override
	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return this.isFullSize() && this.isOpaque();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return this.boundingBox;
	}

// FIXME	@SuppressWarnings( "deprecation" )
// FIXME	@Override
// FIXME	public void addCollisionBoxToList(final BlockState state, final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, @Nullable final Entity e, boolean p_185477_7_ )
// FIXME	{
// FIXME		final ICustomCollision collisionHandler = this.getCustomCollision( w, pos );
// FIXME
// FIXME		if( collisionHandler != null && bb != null )
// FIXME		{
// FIXME			final List<AxisAlignedBB> tmp = new ArrayList<>();
// FIXME			collisionHandler.addCollidingBlockToList( w, pos, bb, tmp, e );
// FIXME			for( final AxisAlignedBB b : tmp )
// FIXME			{
// FIXME				final AxisAlignedBB offset = b.offset( pos.getX(), pos.getY(), pos.getZ() );
// FIXME				if( bb.intersects( offset ) )
// FIXME				{
// FIXME					out.add( offset );
// FIXME				}
// FIXME			}
// FIXME		}
// FIXME		else
// FIXME		{
// FIXME			super.addCollisionBoxToList( state, w, pos, bb, out, e, p_185477_7_ );
// FIXME		}
// FIXME	}
// FIXME

// FIXME	@SuppressWarnings( "deprecation" )
// FIXME	@Override
// FIXME	@OnlyIn( Dist.CLIENT )
// FIXME	public VoxelShape getRaytraceShape(BlockState state, IBlockReader w, BlockPos pos)
// FIXME	{
// FIXME		final ICustomCollision collisionHandler = this.getCustomCollision( w, pos );
// FIXME
// FIXME		if( collisionHandler != null )
// FIXME		{
// FIXME			if( Platform.isClient() )
// FIXME			{
// FIXME				final PlayerEntity player = Minecraft.getInstance().player;
// FIXME				final LookDirection ld = Platform.getPlayerRay( player, Platform.getEyeOffset( player ) );
// FIXME
// FIXME				final Iterable<VoxelShape> bbs = collisionHandler.getSelectedBoundingBoxesFromPool( w, pos, Minecraft.getInstance().player, true );
// FIXME				VoxelShape br = null;
// FIXME
// FIXME				double lastDist = 0;
// FIXME
// FIXME				for( final VoxelShape bb : bbs )
// FIXME				{
// FIXME					final RayTraceResult r = bb.rayTrace(ld.getA(), ld.getB(), pos);
// FIXME
// FIXME					if( r != null )
// FIXME					{
// FIXME						final double xLen = ( ld.getA().x - r.getHitVec().x );
// FIXME						final double yLen = ( ld.getA().y - r.getHitVec().y );
// FIXME						final double zLen = ( ld.getA().z - r.getHitVec().z );
// FIXME
// FIXME						final double thisDist = xLen * xLen + yLen * yLen + zLen * zLen;
// FIXME
// FIXME						if( br == null || lastDist > thisDist )
// FIXME						{
// FIXME							lastDist = thisDist;
// FIXME							br = bb;
// FIXME						}
// FIXME					}
// FIXME				}
// FIXME
// FIXME				if( br != null )
// FIXME				{
// FIXME					return br;
// FIXME				}
// FIXME			}
// FIXME
// FIXME			VoxelShape b = null; // new AxisAlignedBB( 16d, 16d, 16d, 0d, 0d, 0d );
// FIXME
// FIXME			for( final VoxelShape bx : collisionHandler.getSelectedBoundingBoxesFromPool( w, pos, null, false ) )
// FIXME			{
// FIXME				if( b == null )
// FIXME				{
// FIXME					b = bx;
// FIXME					continue;
// FIXME				}
// FIXME
// FIXME				final double minX = Math.min( b.minX, bx.minX );
// FIXME				final double minY = Math.min( b.minY, bx.minY );
// FIXME				final double minZ = Math.min( b.minZ, bx.minZ );
// FIXME				final double maxX = Math.max( b.maxX, bx.maxX );
// FIXME				final double maxY = Math.max( b.maxY, bx.maxY );
// FIXME				final double maxZ = Math.max( b.maxZ, bx.maxZ );
// FIXME
// FIXME				b = new AxisAlignedBB( minX, minY, minZ, maxX, maxY, maxZ );
// FIXME			}
// FIXME
// FIXME			if( b == null )
// FIXME			{
// FIXME				b = new AxisAlignedBB( 16d, 16d, 16d, 0d, 0d, 0d );
// FIXME			}
// FIXME			else
// FIXME			{
// FIXME				b = new AxisAlignedBB( b.minX + pos.getX(), b.minY + pos.getY(), b.minZ + pos.getZ(), b.maxX + pos.getX(), b.maxY + pos.getY(), b.maxZ + pos
// FIXME						.getZ() );
// FIXME			}
// FIXME
// FIXME			return b;
// FIXME		}
// FIXME
// FIXME		return super.getSelectedBoundingBox( state, w, pos );
// FIXME	}

	// FIXME: Move to state
// FIXME	@Override
// FIXME	public final boolean isOpaqueCube( BlockState state )
//	{
//		return this.isOpaque();
//	}

//FIXME	@SuppressWarnings( "deprecation" )
//FIXME	@Override
//FIXME	public RayTraceResult collisionRayTrace(final BlockState state, final World w, final BlockPos pos, final Vec3d a, final Vec3d b )
//FIXME	{
//FIXME		final ICustomCollision collisionHandler = this.getCustomCollision( w, pos );
//FIXME
//FIXME		if( collisionHandler != null )
//FIXME		{
//FIXME			final Iterable<VoxelShape> bbs = collisionHandler.getSelectedBoundingBoxesFromPool( w, pos, null, true );
//FIXME			RayTraceResult br = null;
//FIXME
//FIXME			double lastDist = 0;
//FIXME
//FIXME			for( final VoxelShape bb : bbs )
//FIXME			{
//FIXME				final RayTraceResult r = bb.rayTrace( state, w, pos, a, b );
//FIXME
//FIXME				if( r != null )
//FIXME				{
//FIXME					final double xLen = ( a.x - r.hitVec.x );
//FIXME					final double yLen = ( a.y - r.hitVec.y );
//FIXME					final double zLen = ( a.z - r.hitVec.z );
//FIXME
//FIXME					final double thisDist = xLen * xLen + yLen * yLen + zLen * zLen;
//FIXME					if( br == null || lastDist > thisDist )
//FIXME					{
//FIXME						lastDist = thisDist;
//FIXME						br = r;
//FIXME					}
//FIXME				}
//FIXME			}
//FIXME
//FIXME			if( br != null )
//FIXME			{
//FIXME				return br;
//FIXME			}
//FIXME
//FIXME			return null;
//FIXME		}
//FIXME
//FIXME		this.boundingBox = FULL_BLOCK_AABB;
//FIXME		return super.collisionRayTrace( state, w, pos, a, b );
//FIXME	}
//FIXME
	@Override
	public boolean hasComparatorInputOverride( BlockState state )
	{
		return this.isInventory();
	}

	@Override
	public int getComparatorInputOverride(BlockState state, final World worldIn, final BlockPos pos )
	{
		return 0;
	}

	@Override
	public BlockState rotate(BlockState state, IWorld w, BlockPos pos, Rotation direction) {
		final IOrientable rotatable = this.getOrientable( w, pos );

		if( rotatable != null && rotatable.canBeRotated() )
		{
			if( this.hasCustomRotation() )
			{
				// FIXME this.customRotateBlock( rotatable, axis );
				// FIXME return true;
				throw new IllegalStateException();
			}
			else
			{
				Direction forward = rotatable.getForward();
				Direction up = rotatable.getUp();

				for( int rs = 0; rs < 4; rs++ )
				{
					// FIXME forward = Platform.rotateAround( forward, axis );
					// FIXME up = Platform.rotateAround( up, axis );

					if( this.isValidOrientation( w, pos, forward, up ) )
					{
						rotatable.setOrientation( forward, up );
						// FIXME
						throw new IllegalStateException();
					}
				}
			}
		}

		return state;
	}

	@Override
	public Direction[] getValidRotations(BlockState state, IBlockReader world, BlockPos pos)
	{
		return new Direction[0];
	}

	public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand, final @Nullable ItemStack heldItem, final BlockRayTraceResult hit)
	{
		return ActionResultType.PASS;
	}

	public final Direction mapRotation(final IOrientable ori, final Direction dir )
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

		final int west_x = forward.getYOffset() * up.getZOffset() - forward.getZOffset() * up.getYOffset();
		final int west_y = forward.getZOffset() * up.getXOffset() - forward.getXOffset() * up.getZOffset();
		final int west_z = forward.getXOffset() * up.getYOffset() - forward.getYOffset() * up.getXOffset();

		Direction west = null;
		for( final Direction dx : Direction.values() )
		{
			if( dx.getXOffset() == west_x && dx.getYOffset() == west_y && dx.getZOffset() == west_z )
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
		String regName = this.getRegistryName() != null ? this.getRegistryName().getPath() : "unregistered";
		return this.getClass().getSimpleName() + "[" + regName + "]";
	}

	protected String getUnlocalizedName( final ItemStack is )
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

	protected boolean isValidOrientation(final IWorld w, final BlockPos pos, final Direction forward, final Direction up )
	{
		return true;
	}

	protected ICustomCollision getCustomCollision( final IBlockReader w, final BlockPos pos )
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
