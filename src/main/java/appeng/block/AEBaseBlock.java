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

import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.IResource;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.client.render.WorldRender;
import appeng.client.texture.FlippableIcon;
import appeng.client.texture.MissingIcon;
import appeng.core.features.AEBlockFeatureHandler;
import appeng.core.features.AEFeature;
import appeng.core.features.FeatureNameExtractor;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.helpers.AEGlassMaterial;
import appeng.helpers.ICustomCollision;
import appeng.util.LookDirection;
import appeng.util.Platform;


public abstract class AEBaseBlock extends Block implements IAEFeature
{
	private final String featureFullName;
	protected final Optional<String> featureSubName;
	protected boolean isOpaque = true;
	protected boolean isFullSize = true;
	protected boolean hasSubtypes = false;
	protected boolean isInventory = false;
	private IFeatureHandler handler;
	@SideOnly( Side.CLIENT )
	BlockRenderInfo renderInfo;

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

	public void registerNoIcons()
	{
		final BlockRenderInfo info = this.getRendererInstance();
		final FlippableIcon i = new FlippableIcon( new MissingIcon( this ) );
		info.updateIcons( i, i, i, i, i, i );
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
			final BaseBlockRender renderer = this.getRenderer().newInstance();
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

	@SideOnly( Side.CLIENT )
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return BaseBlockRender.class;
	}

	public IIcon unmappedGetIcon( IBlockAccess w, int x, int y, int z, int s )
	{
		return super.getIcon( w, x, y, z, s );
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
	public boolean renderAsNormalBlock()
	{
		return this.isFullSize && this.isOpaque;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public int getRenderType()
	{
		return WorldRender.INSTANCE.getRenderId();
	}

	@Override
	public IIcon getIcon( IBlockAccess w, int x, int y, int z, int s )
	{
		return this.getIcon( this.mapRotation( w, x, y, z, s ), w.getBlockMetadata( x, y, z ) );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public IIcon getIcon( int direction, int metadata )
	{
		return this.getRendererInstance().getTexture( ForgeDirection.getOrientation( direction ) );
	}

	protected ICustomCollision getCustomCollision( World w, int x, int y, int z )
	{
		if( this instanceof ICustomCollision )
		{
			return (ICustomCollision) this;
		}
		return null;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	// NOTE: WAS FINAL, changed for Immibis
	public void addCollisionBoxesToList( World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e )
	{
		final ICustomCollision collisionHandler = this.getCustomCollision( w, x, y, z );

		if( collisionHandler != null && bb != null )
		{
			List<AxisAlignedBB> tmp = new ArrayList<AxisAlignedBB>();
			collisionHandler.addCollidingBlockToList( w, x, y, z, bb, tmp, e );
			for( AxisAlignedBB b : tmp )
			{
				b.minX += x;
				b.minY += y;
				b.minZ += z;
				b.maxX += x;
				b.maxY += y;
				b.maxZ += z;
				if( bb.intersectsWith( b ) )
				{
					out.add( b );
				}
			}
		}
		else
		{
			super.addCollisionBoxesToList( w, x, y, z, bb, out, e );
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public final AxisAlignedBB getSelectedBoundingBoxFromPool( World w, int x, int y, int z )
	{
		final ICustomCollision collisionHandler = this.getCustomCollision( w, x, y, z );

		if( collisionHandler != null )
		{
			if( Platform.isClient() )
			{
				final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
				final LookDirection ld = Platform.getPlayerRay( player, Platform.getEyeOffset( player ) );

				final Iterable<AxisAlignedBB> bbs = collisionHandler.getSelectedBoundingBoxesFromPool( w, x, y, z, Minecraft.getMinecraft().thePlayer, true );
				AxisAlignedBB br = null;

				double lastDist = 0;

				for( AxisAlignedBB bb : bbs )
				{
					this.setBlockBounds( (float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ );

					MovingObjectPosition r = super.collisionRayTrace( w, x, y, z, ld.a, ld.b );

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
					br.setBounds( br.minX + x, br.minY + y, br.minZ + z, br.maxX + x, br.maxY + y, br.maxZ + z );
					return br;
				}
			}

			final AxisAlignedBB b = AxisAlignedBB.getBoundingBox( 16d, 16d, 16d, 0d, 0d, 0d );

			for( AxisAlignedBB bx : collisionHandler.getSelectedBoundingBoxesFromPool( w, x, y, z, null, false ) )
			{
				final double minX = Math.min( b.minX, bx.minX );
				final double minY = Math.min( b.minY, bx.minY );
				final double minZ = Math.min( b.minZ, bx.minZ );
				final double maxX = Math.max( b.maxX, bx.maxX );
				final double maxY = Math.max( b.maxY, bx.maxY );
				final double maxZ = Math.max( b.maxZ, bx.maxZ );

				b.setBounds( minX, minY, minZ, maxX, maxY, maxZ );
			}

			b.setBounds( b.minX + x, b.minY + y, b.minZ + z, b.maxX + x, b.maxY + y, b.maxZ + z );

			return b;
		}

		return super.getSelectedBoundingBoxFromPool( w, x, y, z );
	}

	@Override
	public final boolean isOpaqueCube()
	{
		return this.isOpaque;
	}

	@Override
	public MovingObjectPosition collisionRayTrace( World w, int x, int y, int z, Vec3 a, Vec3 b )
	{
		final ICustomCollision collisionHandler = this.getCustomCollision( w, x, y, z );

		if( collisionHandler != null )
		{
			final Iterable<AxisAlignedBB> bbs = collisionHandler.getSelectedBoundingBoxesFromPool( w, x, y, z, null, true );
			MovingObjectPosition br = null;

			double lastDist = 0;

			for( AxisAlignedBB bb : bbs )
			{
				this.setBlockBounds( (float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ );

				MovingObjectPosition r = super.collisionRayTrace( w, x, y, z, a, b );

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
		return super.collisionRayTrace( w, x, y, z, a, b );
	}

	public boolean onActivated( World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ )
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
	public int getComparatorInputOverride( World w, int x, int y, int z, int s )
	{
		return 0;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void registerBlockIcons( IIconRegister iconRegistry )
	{
		final BlockRenderInfo info = this.getRendererInstance();
		final FlippableIcon topIcon = this.optionalIcon( iconRegistry, this.getTextureName(), null );
		final FlippableIcon bottomIcon = this.optionalIcon( iconRegistry, this.getTextureName() + "Bottom", topIcon );
		final FlippableIcon sideIcon = this.optionalIcon( iconRegistry, this.getTextureName() + "Side", topIcon );
		final FlippableIcon eastIcon = this.optionalIcon( iconRegistry, this.getTextureName() + "East", sideIcon );
		final FlippableIcon westIcon = this.optionalIcon( iconRegistry, this.getTextureName() + "West", sideIcon );
		final FlippableIcon southIcon = this.optionalIcon( iconRegistry, this.getTextureName() + "Front", sideIcon );
		final FlippableIcon northIcon = this.optionalIcon( iconRegistry, this.getTextureName() + "Back", sideIcon );

		this.blockIcon = topIcon;

		info.updateIcons( bottomIcon, topIcon, northIcon, southIcon, eastIcon, westIcon );
	}

	@Override
	public final boolean isNormalCube( IBlockAccess world, int x, int y, int z )
	{
		return this.isFullSize;
	}

	public IOrientable getOrientable( IBlockAccess w, int x, int y, int z )
	{
		if( this instanceof IOrientableBlock )
		{
			return ( (IOrientableBlock) this ).getOrientable( w, x, y, z );
		}
		return null;
	}

	@Override
	public final boolean rotateBlock( World w, int x, int y, int z, ForgeDirection axis )
	{
		final IOrientable rotatable = this.getOrientable( w, x, y, z );

		if( rotatable != null && rotatable.canBeRotated() )
		{
			if( this.hasCustomRotation() )
			{
				this.customRotateBlock( rotatable, axis );
				return true;
			}
			else
			{
				ForgeDirection forward = rotatable.getForward();
				ForgeDirection up = rotatable.getUp();

				for( int rs = 0; rs < 4; rs++ )
				{
					forward = Platform.rotateAround( forward, axis );
					up = Platform.rotateAround( up, axis );

					if( this.isValidOrientation( w, x, y, z, forward, up ) )
					{
						rotatable.setOrientation( forward, up );
						return true;
					}
				}
			}
		}

		return super.rotateBlock( w, x, y, z, axis );
	}

	protected boolean hasCustomRotation()
	{
		return false;
	}

	protected void customRotateBlock( IOrientable rotatable, ForgeDirection axis )
	{

	}

	public boolean isValidOrientation( World w, int x, int y, int z, ForgeDirection forward, ForgeDirection up )
	{
		return true;
	}

	@Override
	public ForgeDirection[] getValidRotations( World w, int x, int y, int z )
	{
		return new ForgeDirection[0];
	}

	@SideOnly( Side.CLIENT )
	private FlippableIcon optionalIcon( IIconRegister ir, String name, IIcon substitute )
	{
		// if the input is an flippable IIcon find the original.
		while( substitute instanceof FlippableIcon )
		{
			substitute = ( (FlippableIcon) substitute ).getOriginal();
		}

		if( substitute != null )
		{
			try
			{
				ResourceLocation resLoc = new ResourceLocation( name );
				resLoc = new ResourceLocation( resLoc.getResourceDomain(), String.format( "%s/%s%s", "textures/blocks", resLoc.getResourcePath(), ".png" ) );

				IResource res = Minecraft.getMinecraft().getResourceManager().getResource( resLoc );
				if( res != null )
				{
					return new FlippableIcon( ir.registerIcon( name ) );
				}
			}
			catch( Throwable e )
			{
				return new FlippableIcon( substitute );
			}
		}

		return new FlippableIcon( ir.registerIcon( name ) );
	}

	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( Item item, CreativeTabs tabs, List<ItemStack> itemStacks )
	{
		super.getSubBlocks( item, tabs, itemStacks );
	}

	int mapRotation( IBlockAccess w, int x, int y, int z, int s )
	{
		final IOrientable ori = this.getOrientable( w, x, y, z );

		if( ori != null && ori.canBeRotated() )
		{
			return this.mapRotation( ori, ForgeDirection.getOrientation( s ) ).ordinal();
		}

		return s;
	}

	public ForgeDirection mapRotation( IOrientable ori, ForgeDirection dir )
	{
		// case DOWN: return bottomIcon;
		// case UP: return blockIcon;
		// case NORTH: return northIcon;
		// case SOUTH: return southIcon;
		// case WEST: return sideIcon;
		// case EAST: return sideIcon;

		final ForgeDirection forward = ori.getForward();
		final ForgeDirection up = ori.getUp();

		if( forward == null || up == null )
		{
			return dir;
		}

		int west_x = forward.offsetY * up.offsetZ - forward.offsetZ * up.offsetY;
		int west_y = forward.offsetZ * up.offsetX - forward.offsetX * up.offsetZ;
		int west_z = forward.offsetX * up.offsetY - forward.offsetY * up.offsetX;

		ForgeDirection west = ForgeDirection.UNKNOWN;
		for( ForgeDirection dx : ForgeDirection.VALID_DIRECTIONS )
		{
			if( dx.offsetX == west_x && dx.offsetY == west_y && dx.offsetZ == west_z )
			{
				west = dx;
			}
		}

		if( dir == forward )
		{
			return ForgeDirection.SOUTH;
		}
		if( dir == forward.getOpposite() )
		{
			return ForgeDirection.NORTH;
		}

		if( dir == up )
		{
			return ForgeDirection.UP;
		}
		if( dir == up.getOpposite() )
		{
			return ForgeDirection.DOWN;
		}

		if( dir == west )
		{
			return ForgeDirection.WEST;
		}
		if( dir == west.getOpposite() )
		{
			return ForgeDirection.EAST;
		}

		return ForgeDirection.UNKNOWN;
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

}
