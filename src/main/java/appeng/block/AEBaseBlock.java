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

package appeng.block;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.IResource;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.util.AEColor;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.networking.BlockCableBus;
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
import appeng.core.features.ItemStackSrc;
import appeng.helpers.AEGlassMaterial;
import appeng.helpers.ICustomCollision;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import appeng.tile.storage.TileSkyChest;
import appeng.util.LookDirection;
import appeng.util.Platform;
import appeng.util.SettingsFrom;


public class AEBaseBlock extends BlockContainer implements IAEFeature
{
	private final String featureFullName;
	private final Optional<String> featureSubName;
	@SideOnly( Side.CLIENT )
	public IIcon renderIcon;
	protected boolean isOpaque = true;
	protected boolean isFullSize = true;
	protected boolean hasSubtypes = false;
	protected boolean isInventory = false;
	@SideOnly( Side.CLIENT )
	BlockRenderInfo renderInfo;
	private IFeatureHandler handler;

	@Nullable
	private Class<? extends TileEntity> tileEntityType = null;

	protected AEBaseBlock( Material mat )
	{
		this( mat, Optional.<String>absent() );
		this.setLightOpacity( 255 );
		this.setLightLevel( 0 );
		this.setHardness( 2.2F );
		this.setTileProvider( false );
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

	// update Block value.
	private void setTileProvider( boolean b )
	{
		ReflectionHelper.setPrivateValue( Block.class, this, b, "isTileProvider" );
	}

	@Override
	public String toString()
	{
		return this.featureFullName;
	}

	public void registerNoIcons()
	{
		BlockRenderInfo info = this.getRendererInstance();
		FlippableIcon i = new FlippableIcon( new MissingIcon( this ) );
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
		catch ( InstantiationException e )
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

	protected void setTileEntity( Class<? extends TileEntity> c )
	{
		this.tileEntityType = c;

		AEBaseTile.registerTileItem( c, new ItemStackSrc( this, 0 ) );
		this.isInventory = IInventory.class.isAssignableFrom( c );
		this.setTileProvider( this.hasBlockTileEntity() );
	}

	public boolean hasBlockTileEntity()
	{
		return this.tileEntityType != null;
	}

	protected void setFeature( EnumSet<AEFeature> f )
	{
		this.handler = new AEBlockFeatureHandler( f, this, this.featureSubName );
	}

	@Override
	public final IFeatureHandler handler()
	{
		return this.handler;
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
		if( this.renderIcon != null )
		{
			return this.renderIcon;
		}

		return this.getRendererInstance().getTexture( ForgeDirection.getOrientation( direction ) );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	// NOTE: WAS FINAL, changed for Immibis
	public void addCollisionBoxesToList( World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e )
	{
		ICustomCollision collisionHandler = null;

		if( this instanceof ICustomCollision )
		{
			collisionHandler = (ICustomCollision) this;
		}
		else
		{
			AEBaseTile te = this.getTileEntity( w, x, y, z );
			if( te instanceof ICustomCollision )
			{
				collisionHandler = (ICustomCollision) te;
			}
		}

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
		ICustomCollision collisionHandler = null;
		AxisAlignedBB b = null;

		if( this instanceof ICustomCollision )
		{
			collisionHandler = (ICustomCollision) this;
		}
		else
		{
			AEBaseTile te = this.getTileEntity( w, x, y, z );
			if( te instanceof ICustomCollision )
			{
				collisionHandler = (ICustomCollision) te;
			}
		}

		if( collisionHandler != null )
		{
			if( Platform.isClient() )
			{
				EntityPlayer player = Minecraft.getMinecraft().thePlayer;
				LookDirection ld = Platform.getPlayerRay( player, Platform.getEyeOffset( player ) );

				Iterable<AxisAlignedBB> bbs = collisionHandler.getSelectedBoundingBoxesFromPool( w, x, y, z, Minecraft.getMinecraft().thePlayer, true );
				AxisAlignedBB br = null;

				double lastDist = 0;

				for( AxisAlignedBB bb : bbs )
				{
					this.setBlockBounds( (float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ );

					MovingObjectPosition r = super.collisionRayTrace( w, x, y, z, ld.a, ld.b );

					this.setBlockBounds( 0, 0, 0, 1, 1, 1 );

					if( r != null )
					{
						double xLen = ( ld.a.xCoord - r.hitVec.xCoord );
						double yLen = ( ld.a.yCoord - r.hitVec.yCoord );
						double zLen = ( ld.a.zCoord - r.hitVec.zCoord );

						double thisDist = xLen * xLen + yLen * yLen + zLen * zLen;
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

			for( AxisAlignedBB bx : collisionHandler.getSelectedBoundingBoxesFromPool( w, x, y, z, null, false ) )
			{
				if( b == null )
				{
					b = bx;
				}
				else
				{
					double minX = Math.min( b.minX, bx.minX );
					double minY = Math.min( b.minY, bx.minY );
					double minZ = Math.min( b.minZ, bx.minZ );
					double maxX = Math.max( b.maxX, bx.maxX );
					double maxY = Math.max( b.maxY, bx.maxY );
					double maxZ = Math.max( b.maxZ, bx.maxZ );
					b.setBounds( minX, minY, minZ, maxX, maxY, maxZ );
				}
			}

			b.setBounds( b.minX + x, b.minY + y, b.minZ + z, b.maxX + x, b.maxY + y, b.maxZ + z );
		}
		else
		{
			b = super.getSelectedBoundingBoxFromPool( w, x, y, z );
		}

		return b;
	}

	@Override
	public final boolean isOpaqueCube()
	{
		return this.isOpaque;
	}

	@Override
	public MovingObjectPosition collisionRayTrace( World w, int x, int y, int z, Vec3 a, Vec3 b )
	{
		ICustomCollision collisionHandler = null;

		if( this instanceof ICustomCollision )
		{
			collisionHandler = (ICustomCollision) this;
		}
		else
		{
			AEBaseTile te = this.getTileEntity( w, x, y, z );
			if( te instanceof ICustomCollision )
			{
				collisionHandler = (ICustomCollision) te;
			}
		}

		if( collisionHandler != null )
		{
			Iterable<AxisAlignedBB> bbs = collisionHandler.getSelectedBoundingBoxesFromPool( w, x, y, z, null, true );
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

	@Override
	public final boolean onBlockActivated( World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ )
	{
		if( player != null )
		{
			ItemStack is = player.inventory.getCurrentItem();
			if( is != null )
			{
				if( Platform.isWrench( player, is, x, y, z ) && player.isSneaking() )
				{
					Block id = w.getBlock( x, y, z );
					if( id != null )
					{
						AEBaseTile tile = this.getTileEntity( w, x, y, z );
						ItemStack[] drops = Platform.getBlockDrops( w, x, y, z );

						if( tile == null )
						{
							return false;
						}

						if( tile instanceof TileCableBus || tile instanceof TileSkyChest )
						{
							return false;
						}

						ItemStack op = new ItemStack( this );
						for( ItemStack ol : drops )
						{
							if( Platform.isSameItemType( ol, op ) )
							{
								NBTTagCompound tag = tile.downloadSettings( SettingsFrom.DISMANTLE_ITEM );
								if( tag != null )
								{
									ol.setTagCompound( tag );
								}
							}
						}

						if( id.removedByPlayer( w, player, x, y, z, false ) )
						{
							List<ItemStack> l = Lists.newArrayList( drops );
							Platform.spawnDrops( w, x, y, z, l );
							w.setBlockToAir( x, y, z );
						}
					}
					return false;
				}

				if( is.getItem() instanceof IMemoryCard && !( this instanceof BlockCableBus ) )
				{
					IMemoryCard memoryCard = (IMemoryCard) is.getItem();
					if( player.isSneaking() )
					{
						AEBaseTile t = this.getTileEntity( w, x, y, z );
						if( t != null )
						{
							String name = this.getUnlocalizedName();
							NBTTagCompound data = t.downloadSettings( SettingsFrom.MEMORY_CARD );
							if( data != null )
							{
								memoryCard.setMemoryCardContents( is, name, data );
								memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_SAVED );
								return true;
							}
						}
					}
					else
					{
						String name = memoryCard.getSettingsName( is );
						NBTTagCompound data = memoryCard.getData( is );
						if( this.getUnlocalizedName().equals( name ) )
						{
							AEBaseTile t = this.getTileEntity( w, x, y, z );
							t.uploadSettings( SettingsFrom.MEMORY_CARD, data );
							memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );
						}
						else
						{
							memoryCard.notifyUser( player, MemoryCardMessages.INVALID_MACHINE );
						}
						return false;
					}
				}
			}
		}

		return this.onActivated( w, x, y, z, player, side, hitX, hitY, hitZ );
	}

	public boolean onActivated( World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ )
	{
		return false;
	}

	@Override
	public void onBlockPlacedBy( World w, int x, int y, int z, EntityLivingBase player, ItemStack is )
	{
		if( is.hasDisplayName() )
		{
			TileEntity te = this.getTileEntity( w, x, y, z );
			if( te instanceof AEBaseTile )
			{
				( (AEBaseTile) w.getTileEntity( x, y, z ) ).setName( is.getDisplayName() );
			}
		}
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
		TileEntity te = this.getTileEntity( w, x, y, z );
		if( te instanceof IInventory )
		{
			return Container.calcRedstoneFromInventory( (IInventory) te );
		}
		return 0;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void registerBlockIcons( IIconRegister iconRegistry )
	{
		BlockRenderInfo info = this.getRendererInstance();
		FlippableIcon topIcon;
		FlippableIcon bottomIcon;
		FlippableIcon sideIcon;
		FlippableIcon eastIcon;
		FlippableIcon westIcon;
		FlippableIcon southIcon;
		FlippableIcon northIcon;

		this.blockIcon = topIcon = this.optionalIcon( iconRegistry, this.getTextureName(), null );
		bottomIcon = this.optionalIcon( iconRegistry, this.getTextureName() + "Bottom", topIcon );
		sideIcon = this.optionalIcon( iconRegistry, this.getTextureName() + "Side", topIcon );
		eastIcon = this.optionalIcon( iconRegistry, this.getTextureName() + "East", sideIcon );
		westIcon = this.optionalIcon( iconRegistry, this.getTextureName() + "West", sideIcon );
		southIcon = this.optionalIcon( iconRegistry, this.getTextureName() + "Front", sideIcon );
		northIcon = this.optionalIcon( iconRegistry, this.getTextureName() + "Back", sideIcon );

		info.updateIcons( bottomIcon, topIcon, northIcon, southIcon, eastIcon, westIcon );
	}

	@Override
	public final boolean isNormalCube( IBlockAccess world, int x, int y, int z )
	{
		return this.isFullSize;
	}

	@Override
	public final boolean rotateBlock( World w, int x, int y, int z, ForgeDirection axis )
	{
		IOrientable rotatable = null;

		if( this.hasBlockTileEntity() )
		{
			rotatable = (IOrientable) this.getTileEntity( w, x, y, z );
		}
		else if( this instanceof IOrientableBlock )
		{
			rotatable = ( (IOrientableBlock) this ).getOrientable( w, x, y, z );
		}

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
	public final ForgeDirection[] getValidRotations( World w, int x, int y, int z )
	{
		if( this.hasBlockTileEntity() )
		{
			AEBaseTile obj = this.getTileEntity( w, x, y, z );
			if( obj != null && obj.canBeRotated() )
			{
				return ForgeDirection.VALID_DIRECTIONS;
			}
		}

		return new ForgeDirection[0];
	}

	@Override
	public boolean recolourBlock( World world, int x, int y, int z, ForgeDirection side, int colour )
	{
		TileEntity te = this.getTileEntity( world, x, y, z );

		if( te instanceof IColorableTile )
		{
			IColorableTile ct = (IColorableTile) te;
			AEColor c = ct.getColor();
			AEColor newColor = AEColor.values()[colour];

			if( c != newColor )
			{
				ct.recolourBlock( side, newColor, null );
				return true;
			}
			return false;
		}

		return super.recolourBlock( world, x, y, z, side, colour );
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
		IOrientable ori = null;

		if( this.hasBlockTileEntity() )
		{
			ori = (IOrientable) this.getTileEntity( w, x, y, z );
		}
		else if( this instanceof IOrientableBlock )
		{
			ori = ( (IOrientableBlock) this ).getOrientable( w, x, y, z );
		}

		if( ori != null && ori.canBeRotated() )
		{
			return this.mapRotation( ori, ForgeDirection.getOrientation( s ) ).ordinal();
		}

		return s;
	}

	@Nullable
	public <T extends TileEntity> T getTileEntity( IBlockAccess w, int x, int y, int z )
	{
		if( !this.hasBlockTileEntity() )
		{
			return null;
		}

		TileEntity te = w.getTileEntity( x, y, z );
		if( this.tileEntityType.isInstance( te ) )
		{
			return (T) te;
		}

		return null;
	}

	public ForgeDirection mapRotation( IOrientable ori, ForgeDirection dir )
	{
		// case DOWN: return bottomIcon;
		// case UP: return blockIcon;
		// case NORTH: return northIcon;
		// case SOUTH: return southIcon;
		// case WEST: return sideIcon;
		// case EAST: return sideIcon;

		ForgeDirection forward = ori.getForward();
		ForgeDirection up = ori.getUp();
		ForgeDirection west = ForgeDirection.UNKNOWN;

		if( forward == null || up == null )
		{
			return dir;
		}

		int west_x = forward.offsetY * up.offsetZ - forward.offsetZ * up.offsetY;
		int west_y = forward.offsetZ * up.offsetX - forward.offsetX * up.offsetZ;
		int west_z = forward.offsetX * up.offsetY - forward.offsetY * up.offsetX;

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

	public Class<? extends TileEntity> getTileEntityClass()
	{
		return this.tileEntityType;
	}

	@SideOnly( Side.CLIENT )
	public void setRenderStateByMeta( int itemDamage )
	{

	}

	@Override
	public final TileEntity createNewTileEntity( World var1, int var2 )
	{
		if( this.hasBlockTileEntity() )
		{
			try
			{
				return this.tileEntityType.newInstance();
			}
			catch( InstantiationException e )
			{
				throw new IllegalStateException( "Failed to create a new instance of an illegal class " + this.tileEntityType , e );
			}
			catch( IllegalAccessException e )
			{
				throw new IllegalStateException( "Failed to create a new instance of " + this.tileEntityType + ", because lack of permissions", e );
			}
		}

		return null;
	}

	@Override
	public void breakBlock( World w, int x, int y, int z, Block a, int b )
	{
		AEBaseTile te = this.getTileEntity( w, x, y, z );
		if( te != null )
		{
			ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
			if( te.dropItems() )
			{
				te.getDrops( w, x, y, z, drops );
			}
			else
			{
				te.getNoDrops( w, x, y, z, drops );
			}

			// Cry ;_; ...
			Platform.spawnDrops( w, x, y, z, drops );
		}

		super.breakBlock( w, x, y, z, a, b );
		if( te != null )
		{
			w.setTileEntity( x, y, z, null );
		}
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
