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

import appeng.helpers.AEGlassMaterial;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import net.minecraftforge.common.ToolType;


public abstract class AEBaseBlock extends Block
{

	private boolean isOpaque = true;
	private boolean isFullSize = true;
	private boolean isInventory = false;

	protected VoxelShape boundingBox = VoxelShapes.fullCube();

	protected AEBaseBlock( final Block.Properties props )
	{
		super( props );
	}

	/**
	 * Utility function to create block properties with some sensible defaults for AE blocks.
	 */
	public static Block.Properties defaultProps(Material material) {
		return defaultProps(material, material.getColor());
	}

	/**
	 * Utility function to create block properties with some sensible defaults for AE blocks.
	 */
	public static Block.Properties defaultProps(Material material, MaterialColor color) {
		return Block.Properties.create(material, color)
				// These values previousls were encoded in AEBaseBlock
				.hardnessAndResistance(2.2f, 11.f)
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(0)
				.sound(getDefaultSoundByMaterial(material));
	}

	private static SoundType getDefaultSoundByMaterial(Material mat) {
		if( mat == AEGlassMaterial.INSTANCE || mat == Material.GLASS )
		{
			return SoundType.GLASS;
		}
		else if( mat == Material.ROCK )
		{
			return SoundType.STONE;
		}
		else if( mat == Material.WOOD )
		{
			return SoundType.WOOD;
		}
		else
		{
			return SoundType.METAL;
		}
	}

	@Override
	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return this.isFullSize() && this.isOpaque();
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


//	@Override
//	public VoxelShape getShape(BlockState state, IBlockReader w, BlockPos pos, ISelectionContext context) {
//		final ICustomCollision collisionHandler = this.getCustomCollision( w, pos );
//
//		if( context instanceof EntitySelectionContext && collisionHandler != null )
//		{
//			EntitySelectionContext entitySelectionContext = (EntitySelectionContext) context;
//
//			if( Platform.isClient() )
//			{
//				final PlayerEntity player = Minecraft.getInstance().player;
//				if (player != null && player == entitySelectionContext.getEntity()) {
//					final LookDirection ld = Platform.getPlayerRay(player);
//
//					final Iterable<VoxelShape> bbs = collisionHandler.getSelectedBoundingBoxesFromPool(w, pos, Minecraft.getInstance().player, true);
//					VoxelShape br = null;
//
//					double lastDist = 0;
//
//					for (final VoxelShape bb : bbs) {
//						final RayTraceResult r = bb.rayTrace(ld.getA(), ld.getB(), pos);
//
//						if (r != null) {
//							final double xLen = (ld.getA().x - r.getHitVec().x);
//							final double yLen = (ld.getA().y - r.getHitVec().y);
//							final double zLen = (ld.getA().z - r.getHitVec().z);
//
//							final double thisDist = xLen * xLen + yLen * yLen + zLen * zLen;
//
//							if (br == null || lastDist > thisDist) {
//								lastDist = thisDist;
//								br = bb;
//							}
//						}
//					}
//
//					if (br != null) {
//						return br;
//					}
//				}
//			}
//
//			// FIXME: This seems fishy because it is NOT solely related to selection
////			VoxelShape b = VoxelShapes.empty();
////
////			for( final VoxelShape bx : collisionHandler.getSelectedBoundingBoxesFromPool( w, pos, null, false ) )
////			{
////				if( b.isEmpty() )
////				{
////					b = bx;
////					continue;
////				}
////
////				b = VoxelShapes.or(b, bx);
////			}
////
////			return b;
//		}
//
//		return super.getShape( state, w, pos, context );
//	}

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
//FIXME					final double xLen = ( a.x - r.getHitVec().x );
//FIXME					final double yLen = ( a.y - r.getHitVec().y );
//FIXME					final double zLen = ( a.z - r.getHitVec().z );
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

	/**
	 * Rotates around the given Axis (usually the current up axis).
	 */
	public boolean rotateAroundFaceAxis(IWorld w, BlockPos pos, Direction face) {
		final IOrientable rotatable = this.getOrientable( w, pos );

		if( rotatable != null && rotatable.canBeRotated() )
		{
			if( this.hasCustomRotation() )
			{
				this.customRotateBlock( rotatable, face );
				return true;
			}
			else
			{
				Direction forward = rotatable.getForward();
				Direction up = rotatable.getUp();

				for( int rs = 0; rs < 4; rs++ )
				{
					forward = Platform.rotateAround( forward, face );
					up = Platform.rotateAround( up, face );

					if( this.isValidOrientation( w, pos, forward, up ) )
					{
						rotatable.setOrientation( forward, up );
						return true;
					}
				}
			}
		}

		return false;
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
