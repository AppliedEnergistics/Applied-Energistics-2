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
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.client.texture.BaseIcon;
import appeng.client.texture.FlippableIcon;
import appeng.client.texture.IAESprite;
import appeng.core.AppEng;
import appeng.core.features.AEBlockFeatureHandler;
import appeng.core.features.AEFeature;
import appeng.core.features.ActivityState;
import appeng.core.features.FeatureNameExtractor;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.helpers.AEGlassMaterial;
import appeng.helpers.ICustomCollision;
import appeng.util.LookDirection;
import appeng.util.Platform;

import com.google.common.base.Optional;


public abstract class AEBaseBlock extends Block implements IAEFeature
{
    public static final PropertyEnum AXIS_ORIENTATION = PropertyEnum.create("axis", EnumFacing.Axis.class);
	
	private final String featureFullName;
	protected final Optional<String> featureSubName;
	protected boolean isOpaque = true;
	protected boolean isFullSize = true;
	protected boolean hasSubtypes = false;
	protected boolean isInventory = false;
	private IFeatureHandler handler;
	@SideOnly( Side.CLIENT )
	BlockRenderInfo renderInfo;
	
	@Override
	public boolean isVisuallyOpaque()
	{
		return isOpaque && isFullSize;
	}
	
	protected AEBaseBlock( Material mat )
	{
		this( mat, Optional.<String>absent() );
		this.setLightOpacity( 255 );
		this.setLightLevel( 0 );
		this.setHardness( 2.2F );
		this.setHarvestLevel( "pickaxe", 0 );
	}

	protected AEBaseBlock( Material mat, Optional<String> subName )
	{
		super( mat );

		if( mat == AEGlassMaterial.INSTANCE || mat == Material.glass )
		{
			this.setStepSound( Block.soundTypeGlass );
		}
		else if( mat == Material.rock )
		{
			this.setStepSound( Block.soundTypeStone );
		}
		else if( mat == Material.wood )
		{
			this.setStepSound( Block.soundTypeWood );
		}
		else
		{
			this.setStepSound( Block.soundTypeMetal );
		}

		this.featureFullName = new FeatureNameExtractor( this.getClass(), subName ).get();
		this.featureSubName = subName;
	}

	@Override
	public String toString()
	{
		return this.featureFullName;
	}

	public static final UnlistedBlockPos AE_BLOCK_POS = new UnlistedBlockPos();
	public static final UnlistedBlockAccess AE_BLOCK_ACCESS = new UnlistedBlockAccess();
	
	@Override
	final protected BlockState createBlockState()
	{
		return new ExtendedBlockState( this, getAEStates(), new IUnlistedProperty[] { AE_BLOCK_POS, AE_BLOCK_ACCESS} );
	}

	@Override
	final public IBlockState getExtendedState(
			IBlockState state,
			IBlockAccess world,
			BlockPos pos )
	{
		return ((IExtendedBlockState)super.getExtendedState( state, world, pos ) ).withProperty( AE_BLOCK_POS, pos  ).withProperty( AE_BLOCK_ACCESS, world );
	}
	
	protected IProperty[] getAEStates()
	{
		return new IProperty[0];
	}
	
	@SideOnly( Side.CLIENT )
	public BlockRenderInfo getRendererInstance()
	{
		if( this.renderInfo != null )
		{
			return this.renderInfo;
		}

		try
		{
			Class<? extends BaseBlockRender> re = this.getRenderer();
			if ( re == null ) return null; // use 1.8 models.
			final BaseBlockRender renderer = re.newInstance();
			this.renderInfo = new BlockRenderInfo( renderer );

			return this.renderInfo;
		}
		catch( InstantiationException e )
		{
			throw new IllegalStateException( "Failed to create a new instance of an illegal class " + this.getRenderer(), e );
		}
		catch( IllegalAccessException e )
		{
			throw new IllegalStateException( "Failed to create a new instance of " + this.getRenderer() + " because of permissions.", e );
		}
	}
	
	@Override
	public int colorMultiplier(
			IBlockAccess worldIn,
			BlockPos pos,
			int colorTint )
	{
		return colorTint;
	}

	@SideOnly( Side.CLIENT )
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return BaseBlockRender.class;
	}

	protected void setFeature( EnumSet<AEFeature> f )
	{
		final AEBlockFeatureHandler featureHandler = new AEBlockFeatureHandler( f, this, this.featureSubName );
		this.setHandler( featureHandler );
	}

	@Override
	public final IFeatureHandler handler()
	{
		return this.handler;
	}

	protected final void setHandler( IFeatureHandler handler )
	{
		this.handler = handler;
	}

	@Override
	public void postInit()
	{
		// override!
	}

	public boolean isOpaque()
	{
		return this.isOpaque;
	}
	
	@Override
	public boolean isNormalCube()
	{
		return this.isFullSize && this.isOpaque;
	}
	
	protected ICustomCollision getCustomCollision( World w, BlockPos pos  )
	{
		if( this instanceof ICustomCollision )
		{
			return (ICustomCollision) this;
		}
		return null;
	}

	@SideOnly( Side.CLIENT )
	public IAESprite getIcon( IBlockAccess w, BlockPos pos, EnumFacing side )
	{
		IBlockState state =w.getBlockState( pos );
		IOrientable ori = getOrientable( w, pos );
		
		if ( ori == null )
			return getIcon( side,state );
		
		return this.getIcon( this.mapRotation( ori, side ), state );
	}

	@SideOnly( Side.CLIENT )
	public IAESprite getIcon( EnumFacing side, IBlockState state )
	{
		return this.getRendererInstance().getTexture( AEPartLocation.fromFacing( side ) );
	}

	@Override
	public void addCollisionBoxesToList(
			World w,
			BlockPos pos,
			IBlockState state,
			AxisAlignedBB bb,
			List out,
			Entity e )
	{
		final ICustomCollision collisionHandler = this.getCustomCollision( w, pos );

		if( collisionHandler != null && bb != null )
		{
			List<AxisAlignedBB> tmp = new ArrayList<AxisAlignedBB>();
			collisionHandler.addCollidingBlockToList( w, pos, bb, tmp, e );
			for( AxisAlignedBB b : tmp )
			{
				AxisAlignedBB offset = b.offset( pos.getX(), pos.getY(), pos.getZ() );
				if( bb.intersectsWith( offset ) )
				{
					out.add( offset );
				}
			}
		}
		else
		{
			super.addCollisionBoxesToList( w, pos, state, bb, out, e );
		}
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public AxisAlignedBB getSelectedBoundingBox(
			World w,
			BlockPos pos )
	{
		final ICustomCollision collisionHandler = this.getCustomCollision( w, pos );

		if( collisionHandler != null )
		{
			if( Platform.isClient() )
			{
				final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
				final LookDirection ld = Platform.getPlayerRay( player, Platform.getEyeOffset( player ) );

				final Iterable<AxisAlignedBB> bbs = collisionHandler.getSelectedBoundingBoxesFromPool( w, pos, Minecraft.getMinecraft().thePlayer, true );
				AxisAlignedBB br = null;

				double lastDist = 0;

				for( AxisAlignedBB bb : bbs )
				{
					this.setBlockBounds( (float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ );

					MovingObjectPosition r = super.collisionRayTrace( w, pos, ld.a, ld.b );

					this.setBlockBounds( 0, 0, 0, 1, 1, 1 );

					if( r != null )
					{
						final double xLen = ( ld.a.xCoord - r.hitVec.xCoord );
						final double yLen = ( ld.a.yCoord - r.hitVec.yCoord );
						final double zLen = ( ld.a.zCoord - r.hitVec.zCoord );

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
					br = AxisAlignedBB.fromBounds( br.minX + pos.getX(), br.minY + pos.getY(), br.minZ + pos.getZ(), br.maxX + pos.getX(), br.maxY + pos.getY(), br.maxZ + pos.getZ() );
					return br;
				}
			}

			AxisAlignedBB b = null; // new AxisAlignedBB( 16d, 16d, 16d, 0d, 0d, 0d );

			for( AxisAlignedBB bx : collisionHandler.getSelectedBoundingBoxesFromPool( w, pos, null, false ) )
			{
				if ( b == null )
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

				b = AxisAlignedBB.fromBounds( minX, minY, minZ, maxX, maxY, maxZ );
			}

			if ( b == null )
				b = new AxisAlignedBB( 16d, 16d, 16d, 0d, 0d, 0d );
			else
				b = AxisAlignedBB.fromBounds( b.minX + pos.getX(), b.minY + pos.getY(), b.minZ + pos.getZ(), b.maxX+ pos.getX(), b.maxY + pos.getY(), b.maxZ + pos.getZ() );

			return b;
		}

		return super.getSelectedBoundingBox( w, pos );
	}

	@Override
	public final boolean isOpaqueCube()
	{
		return this.isOpaque;
	}

	@Override
	public MovingObjectPosition collisionRayTrace(
			World w,
			BlockPos pos,
			Vec3 a,
			Vec3 b )
	{
		final ICustomCollision collisionHandler = this.getCustomCollision( w, pos );

		if( collisionHandler != null )
		{
			final Iterable<AxisAlignedBB> bbs = collisionHandler.getSelectedBoundingBoxesFromPool( w, pos, null, true );
			MovingObjectPosition br = null;

			double lastDist = 0;

			for( AxisAlignedBB bb : bbs )
			{
				this.setBlockBounds( (float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ );

				MovingObjectPosition r = super.collisionRayTrace( w, pos, a, b );

				this.setBlockBounds( 0, 0, 0, 1, 1, 1 );

				if( r != null )
				{
					double xLen = ( a.xCoord - r.hitVec.xCoord );
					double yLen = ( a.yCoord - r.hitVec.yCoord );
					double zLen = ( a.zCoord - r.hitVec.zCoord );

					double thisDist = xLen * xLen + yLen * yLen + zLen * zLen;
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

		this.setBlockBounds( 0, 0, 0, 1, 1, 1 );
		return super.collisionRayTrace( w, pos, a, b );
	}

	public boolean onActivated( World w, BlockPos pos, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ )
	{
		return false;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	@SuppressWarnings( "unchecked" )
	public final void getSubBlocks( Item item, CreativeTabs tabs, List itemStacks )
	{
		this.getCheckedSubBlocks( item, tabs, itemStacks );
	}

	@Override
	public boolean hasComparatorInputOverride()
	{
		return this.isInventory;
	}
	
	@Override
	public int getComparatorInputOverride(
			World worldIn,
			BlockPos pos )
	{
		return 0;
	}
	
	@Override
	public boolean isNormalCube(
			IBlockAccess world,
			BlockPos pos )
	{
		return this.isFullSize;
	}

	public IOrientable getOrientable( IBlockAccess w, BlockPos pos )
	{
		if( this instanceof IOrientableBlock )
		{
			return ( (IOrientableBlock) this ).getOrientable( w, pos );
		}
		return null;
	}

	@Override
	public boolean rotateBlock(
			World w,
			BlockPos pos,
			EnumFacing axis )
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
				EnumFacing forward = rotatable.getForward();
				EnumFacing up = rotatable.getUp();

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

	protected boolean hasCustomRotation()
	{
		return false;
	}

	protected void customRotateBlock( IOrientable rotatable, EnumFacing axis )
	{

	}

	public boolean isValidOrientation( World w, BlockPos pos, EnumFacing forward, EnumFacing up )
	{
		return true;
	}

	@Override
	public EnumFacing[] getValidRotations( World w, BlockPos pos )
	{
		return new EnumFacing[0];
	}

	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( Item item, CreativeTabs tabs, List<ItemStack> itemStacks )
	{
		super.getSubBlocks( item, tabs, itemStacks );
	}

	@SideOnly( Side.CLIENT )
	public void setRenderStateByMeta( int itemDamage )
	{

	}

	public String getUnlocalizedName( ItemStack is )
	{
		return this.getUnlocalizedName();
	}

	public void addInformation( ItemStack is, EntityPlayer player, List<String> lines, boolean advancedItemTooltips )
	{

	}

	public Class<? extends AEBaseItemBlock> getItemBlockClass()
	{
		return AEBaseItemBlock.class;
	}

	public boolean hasSubtypes()
	{
		return this.hasSubtypes;
	}

	public EnumFacing mapRotation(
			IOrientable  ori,
			EnumFacing dir )
	{
		// case DOWN: return bottomIcon;
		// case UP: return blockIcon;
		// case NORTH: return northIcon;
		// case SOUTH: return southIcon;
		// case WEST: return sideIcon;
		// case EAST: return sideIcon;

		final EnumFacing forward = ori.getForward();
		final EnumFacing up = ori.getUp();

		if( forward == null || up == null )
		{
			return dir;
		}

		int west_x = forward.getFrontOffsetY() * up.getFrontOffsetZ() - forward.getFrontOffsetZ() * up.getFrontOffsetY();
		int west_y = forward.getFrontOffsetZ() * up.getFrontOffsetX() - forward.getFrontOffsetX() * up.getFrontOffsetZ();
		int west_z = forward.getFrontOffsetX() * up.getFrontOffsetY() - forward.getFrontOffsetY() * up.getFrontOffsetX();

		EnumFacing west = null;
		for( EnumFacing dx : EnumFacing.VALUES )
		{
			if( dx.getFrontOffsetX() == west_x && dx.getFrontOffsetY() == west_y && dx.getFrontOffsetZ() == west_z )
			{
				west = dx;
			}
		}
		
		if ( west == null )
			return dir;

		if( dir == forward )
		{
			return EnumFacing.SOUTH;
		}
		if( dir == forward.getOpposite() )
		{
			return EnumFacing.NORTH;
		}

		if( dir == up )
		{
			return EnumFacing.UP;
		}
		if( dir == up.getOpposite() )
		{
			return EnumFacing.DOWN;
		}

		if( dir == west )
		{
			return EnumFacing.WEST;
		}
		if( dir == west.getOpposite() )
		{
			return EnumFacing.EAST;
		}

		return null;
	}

	@SideOnly( Side.CLIENT )
	public void registerBlockIcons(
			TextureMap clientHelper,
			String name )
	{
		final BlockRenderInfo info = this.getRendererInstance();
		final FlippableIcon topIcon;

		final FlippableIcon blockIcon = topIcon = this.optionalIcon( clientHelper, this.getTextureName(), null );
		final FlippableIcon bottomIcon = this.optionalIcon( clientHelper, this.getTextureName() + "Bottom", topIcon );
		final FlippableIcon sideIcon = this.optionalIcon( clientHelper, this.getTextureName() + "Side", topIcon );
		final FlippableIcon eastIcon = this.optionalIcon( clientHelper, this.getTextureName() + "East", sideIcon );
		final FlippableIcon westIcon = this.optionalIcon( clientHelper, this.getTextureName() + "West", sideIcon );
		final FlippableIcon southIcon = this.optionalIcon( clientHelper, this.getTextureName() + "Front", sideIcon );
		final FlippableIcon northIcon = this.optionalIcon( clientHelper, this.getTextureName() + "Back", sideIcon );

		info.updateIcons( bottomIcon, topIcon, northIcon, southIcon, eastIcon, westIcon );
	}

	@SideOnly( Side.CLIENT )
	private FlippableIcon optionalIcon( TextureMap ir, String name, IAESprite substitute )
	{
		// if the input is an flippable IAESprite find the original.
		while( substitute instanceof FlippableIcon )
		{
			substitute = ( (FlippableIcon) substitute ).getOriginal();
		}

		if( substitute != null )
		{
			try
			{
				ResourceLocation resLoc = new ResourceLocation( AppEng.MOD_ID, String.format( "%s/%s%s", "textures/blocks", name, ".png" ) );

				IResource res = Minecraft.getMinecraft().getResourceManager().getResource( resLoc );
				if( res != null )
				{
					return new FlippableIcon( new BaseIcon( ir.registerSprite( new ResourceLocation(AppEng.MOD_ID, "blocks/" + name ) ) ) );
				}
			}
			catch( Throwable e )
			{
				return new FlippableIcon( substitute );
			}
		}

		ResourceLocation resLoc = new ResourceLocation(AppEng.MOD_ID, "blocks/" + name );
		return new FlippableIcon(new BaseIcon( ir.registerSprite( resLoc ) ) );
	}
	
	String textureName;
	public void setBlockTextureName( String texture )
	{
		textureName = texture;
	}
	
	private String getTextureName()
	{
		return textureName;
	}

}
