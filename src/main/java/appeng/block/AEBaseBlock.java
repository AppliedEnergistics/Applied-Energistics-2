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


import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.client.render.WorldRender;
import appeng.client.texture.FlippableIcon;
import appeng.client.texture.MissingIcon;
import appeng.core.features.*;
import appeng.helpers.AEGlassMaterial;
import appeng.helpers.ICustomCollision;
import appeng.tile.AEBaseTile;
import appeng.util.LookDirection;
import appeng.util.Platform;
import com.google.common.base.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


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
	private BlockRenderInfo renderInfo;

	protected AEBaseBlock( final Material mat )
	{
		this( mat, Optional.<String>absent() );
		this.setLightOpacity( 255 );
		this.setLightLevel( 0 );
		this.setHardness( 2.2F );
		this.setHarvestLevel( "pickaxe", 0 );
	}

	protected AEBaseBlock( final Material mat, final Optional<String> subName )
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

		final BaseBlockRender<? extends AEBaseBlock, ? extends AEBaseTile> renderer = this.getRenderer();
		this.renderInfo = new BlockRenderInfo( renderer );

		return this.renderInfo;
	}

	/**
	 * Factory method to create a new render instance.
	 *
	 * @return the newly created instance.
	 */
	@SideOnly( Side.CLIENT )
	protected BaseBlockRender<? extends AEBaseBlock, ? extends AEBaseTile> getRenderer()
	{
		return new BaseBlockRender<AEBaseBlock, AEBaseTile>();
	}

	IIcon unmappedGetIcon( final IBlockAccess w, final int x, final int y, final int z, final int s )
	{
		return super.getIcon( w, x, y, z, s );
	}

	protected void setFeature( final EnumSet<AEFeature> f )
	{
		final AEBlockFeatureHandler featureHandler = new AEBlockFeatureHandler( f, this, this.featureSubName );
		this.setHandler( featureHandler );
	}

	@Override
	public final IFeatureHandler handler()
	{
		return this.handler;
	}

	protected final void setHandler( final IFeatureHandler handler )
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
	public IIcon getIcon( final IBlockAccess w, final int x, final int y, final int z, final int s )
	{
		return this.getIcon( this.mapRotation( w, x, y, z, s ), w.getBlockMetadata( x, y, z ) );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public IIcon getIcon( final int direction, final int metadata )
	{
		return this.getRendererInstance().getTexture( ForgeDirection.getOrientation( direction ) );
	}

	protected ICustomCollision getCustomCollision( final World w, final int x, final int y, final int z )
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
	public void addCollisionBoxesToList( final World w, final int x, final int y, final int z, final AxisAlignedBB bb, final List out, final Entity e )
	{
		final ICustomCollision collisionHandler = this.getCustomCollision( w, x, y, z );

		if( collisionHandler != null && bb != null )
		{
			final List<AxisAlignedBB> tmp = new ArrayList<AxisAlignedBB>();
			collisionHandler.addCollidingBlockToList( w, x, y, z, bb, tmp, e );
			for( final AxisAlignedBB b : tmp )
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
	public final AxisAlignedBB getSelectedBoundingBoxFromPool( final World w, final int x, final int y, final int z )
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

				for( final AxisAlignedBB bb : bbs )
				{
					this.setBlockBounds( (float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ );

					final MovingObjectPosition r = super.collisionRayTrace( w, x, y, z, ld.getA(), ld.getB() );

					this.setBlockBounds( 0, 0, 0, 1, 1, 1 );

					if( r != null )
					{
						final double xLen = ( ld.getA().xCoord - r.hitVec.xCoord );
						final double yLen = ( ld.getA().yCoord - r.hitVec.yCoord );
						final double zLen = ( ld.getA().zCoord - r.hitVec.zCoord );

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

			for( final AxisAlignedBB bx : collisionHandler.getSelectedBoundingBoxesFromPool( w, x, y, z, null, false ) )
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
	public MovingObjectPosition collisionRayTrace( final World w, final int x, final int y, final int z, final Vec3 a, final Vec3 b )
	{
		final ICustomCollision collisionHandler = this.getCustomCollision( w, x, y, z );

		if( collisionHandler != null )
		{
			final Iterable<AxisAlignedBB> bbs = collisionHandler.getSelectedBoundingBoxesFromPool( w, x, y, z, null, true );
			MovingObjectPosition br = null;

			double lastDist = 0;

			for( final AxisAlignedBB bb : bbs )
			{
				this.setBlockBounds( (float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ );

				final MovingObjectPosition r = super.collisionRayTrace( w, x, y, z, a, b );

				this.setBlockBounds( 0, 0, 0, 1, 1, 1 );

				if( r != null )
				{
					final double xLen = ( a.xCoord - r.hitVec.xCoord );
					final double yLen = ( a.yCoord - r.hitVec.yCoord );
					final double zLen = ( a.zCoord - r.hitVec.zCoord );

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

		this.setBlockBounds( 0, 0, 0, 1, 1, 1 );
		return super.collisionRayTrace( w, x, y, z, a, b );
	}

	public boolean onActivated( final World w, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ )
	{
		return false;
	}

	@Override
	@SideOnly( Side.CLIENT )
	@SuppressWarnings( "unchecked" )
	public final void getSubBlocks( final Item item, final CreativeTabs tabs, final List itemStacks )
	{
		this.getCheckedSubBlocks( item, tabs, itemStacks );
	}

	@Override
	public boolean hasComparatorInputOverride()
	{
		return this.isInventory;
	}

	@Override
	public int getComparatorInputOverride( final World w, final int x, final int y, final int z, final int s )
	{
		return 0;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void registerBlockIcons( final IIconRegister iconRegistry )
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
	public final boolean isNormalCube( final IBlockAccess world, final int x, final int y, final int z )
	{
		return this.isFullSize;
	}

	public IOrientable getOrientable( final IBlockAccess w, final int x, final int y, final int z )
	{
		if( this instanceof IOrientableBlock )
		{
			return this.getOrientable( w, x, y, z );
		}
		return null;
	}

	@Override
	public final boolean rotateBlock( final World w, final int x, final int y, final int z, final ForgeDirection axis )
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

	protected void customRotateBlock( final IOrientable rotatable, final ForgeDirection axis )
	{

	}

	public boolean isValidOrientation( final World w, final int x, final int y, final int z, final ForgeDirection forward, final ForgeDirection up )
	{
		return true;
	}

	@Override
	public ForgeDirection[] getValidRotations( final World w, final int x, final int y, final int z )
	{
		return new ForgeDirection[0];
	}

	@SideOnly( Side.CLIENT )
	private FlippableIcon optionalIcon( final IIconRegister ir, final String name, IIcon substitute )
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

				final IResource res = Minecraft.getMinecraft().getResourceManager().getResource( resLoc );
				if( res != null )
				{
					return new FlippableIcon( ir.registerIcon( name ) );
				}
			}
			catch( final Throwable e )
			{
				return new FlippableIcon( substitute );
			}
		}

		return new FlippableIcon( ir.registerIcon( name ) );
	}

	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( final Item item, final CreativeTabs tabs, final List<ItemStack> itemStacks )
	{
		super.getSubBlocks( item, tabs, itemStacks );
	}

	private int mapRotation( final IBlockAccess w, final int x, final int y, final int z, final int s )
	{
		final IOrientable ori = this.getOrientable( w, x, y, z );

		if( ori != null && ori.canBeRotated() )
		{
			return this.mapRotation( ori, ForgeDirection.getOrientation( s ) ).ordinal();
		}

		return s;
	}

	public ForgeDirection mapRotation( final IOrientable ori, final ForgeDirection dir )
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

		final int west_x = forward.offsetY * up.offsetZ - forward.offsetZ * up.offsetY;
		final int west_y = forward.offsetZ * up.offsetX - forward.offsetX * up.offsetZ;
		final int west_z = forward.offsetX * up.offsetY - forward.offsetY * up.offsetX;

		ForgeDirection west = ForgeDirection.UNKNOWN;
		for( final ForgeDirection dx : ForgeDirection.VALID_DIRECTIONS )
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
	public void setRenderStateByMeta( final int itemDamage )
	{

	}

	public String getUnlocalizedName( final ItemStack is )
	{
		return this.getUnlocalizedName();
	}

	void addInformation( final ItemStack is, final EntityPlayer player, final List<String> lines, final boolean advancedItemTooltips )
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
