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

package appeng.block.networking;


import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.blocks.RendererCableBus;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.CommonHelper;
import appeng.core.features.AECableBusFeatureHandler;
import appeng.core.features.AEFeature;
import appeng.helpers.AEGlassMaterial;
import appeng.helpers.Reflected;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IFMP;
import appeng.parts.ICableBusContainer;
import appeng.parts.NullCableBusContainer;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import appeng.tile.networking.TileCableBusTESR;
import appeng.transformer.annotations.Integration.Interface;
import appeng.transformer.annotations.Integration.Method;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.api.rednet.connectivity.IRedNetConnection;
import powercrystals.minefactoryreloaded.api.rednet.connectivity.RedNetConnectionType;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;


@Interface( iface = "powercrystals.minefactoryreloaded.api.rednet.connectivity.IRedNetConnection", iname = IntegrationType.MFR )
public class BlockCableBus extends AEBaseTileBlock implements IRedNetConnection
{

	private static final ICableBusContainer NULL_CABLE_BUS = new NullCableBusContainer();
	private static Class<? extends TileEntity> noTesrTile;
	private static Class<? extends TileEntity> tesrTile;

	/**
	 * Immibis MB Support.
	 * <p>
	 * It will look for a field named ImmibisMicroblocks_TransformableBlockMarker or
	 * ImmibisMicroblocks_TransformableTileEntityMarker, modifiers, type, etc can be ignored.
	 */
	@Reflected
	private static final boolean ImmibisMicroblocks_TransformableBlockMarker = true;

	private int myColorMultiplier = 0xffffff;

	public BlockCableBus()
	{
		super( AEGlassMaterial.INSTANCE );
		this.setLightOpacity( 0 );
		this.isFullSize = this.isOpaque = false;

		// this will actually be overwritten later through setupTile and the combined layers
		this.setTileEntity( TileCableBus.class );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
	}

	@Override
	public void randomDisplayTick( final World world, final int x, final int y, final int z, final Random r )
	{
		this.cb( world, x, y, z ).randomDisplayTick( world, x, y, z, r );
	}

	@Override
	public void onNeighborBlockChange( final World w, final int x, final int y, final int z, final Block meh )
	{
		this.cb( w, x, y, z ).onNeighborChanged();
	}

	@Override
	public Item getItemDropped( final int i, final Random r, final int k )
	{
		return null;
	}

	@Override
	public int getRenderBlockPass()
	{
		if( AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass ) )
		{
			return 1;
		}
		return 0;
	}

	@Override
	public int colorMultiplier( final IBlockAccess world, final int x, final int y, final int z )
	{
		return this.myColorMultiplier;
	}

	@Override
	public int isProvidingWeakPower( final IBlockAccess w, final int x, final int y, final int z, final int side )
	{
		return this.cb( w, x, y, z ).isProvidingWeakPower( ForgeDirection.getOrientation( side ).getOpposite() );
	}

	@Override
	public boolean canProvidePower()
	{
		return true;
	}

	@Override
	public void onEntityCollidedWithBlock( final World w, final int x, final int y, final int z, final Entity e )
	{
		this.cb( w, x, y, z ).onEntityCollision( e );
	}

	@Override
	public int isProvidingStrongPower( final IBlockAccess w, final int x, final int y, final int z, final int side )
	{
		return this.cb( w, x, y, z ).isProvidingStrongPower( ForgeDirection.getOrientation( side ).getOpposite() );
	}

	@Override
	public int getLightValue( final IBlockAccess world, final int x, final int y, final int z )
	{
		final Block block = world.getBlock( x, y, z );
		if( block != null && block != this )
		{
			return block.getLightValue( world, x, y, z );
		}
		if( block == null )
		{
			return 0;
		}
		return this.cb( world, x, y, z ).getLightValue();
	}

	@Override
	public boolean isLadder( final IBlockAccess world, final int x, final int y, final int z, final EntityLivingBase entity )
	{
		return this.cb( world, x, y, z ).isLadder( entity );
	}

	@Override
	public boolean isSideSolid( final IBlockAccess w, final int x, final int y, final int z, final ForgeDirection side )
	{
		return this.cb( w, x, y, z ).isSolidOnSide( side );
	}

	@Override
	public boolean isReplaceable( final IBlockAccess world, final int x, final int y, final int z )
	{
		return this.cb( world, x, y, z ).isEmpty();
	}

	@SuppressWarnings( "deprecation" )
	@Override
	public boolean removedByPlayer( final World world, final EntityPlayer player, final int x, final int y, final int z )
	{
		if( player.capabilities.isCreativeMode )
		{
			final AEBaseTile tile = this.getTileEntity( world, x, y, z );
			if( tile != null )
			{
				tile.disableDrops();
			}
			// maybe ray trace?
		}
		return super.removedByPlayer( world, player, x, y, z );
	}

	@Override
	public boolean canConnectRedstone( final IBlockAccess w, final int x, final int y, final int z, final int side )
	{
		switch( side )
		{
			case -1:
			case 4:
				return this.cb( w, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.UP, ForgeDirection.DOWN ) );
			case 0:
				return this.cb( w, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.NORTH ) );
			case 1:
				return this.cb( w, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.EAST ) );
			case 2:
				return this.cb( w, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.SOUTH ) );
			case 3:
				return this.cb( w, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.WEST ) );
		}
		return false;
	}

	@Override
	public boolean canRenderInPass( final int pass )
	{
		BusRenderHelper.INSTANCE.setPass( pass );

		if( AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass ) )
		{
			return true;
		}

		return pass == 0;
	}

	@Override
	public ItemStack getPickBlock( final MovingObjectPosition target, final World world, final int x, final int y, final int z, final EntityPlayer player )
	{
		final Vec3 v3 = target.hitVec.addVector( -x, -y, -z );
		final SelectedPart sp = this.cb( world, x, y, z ).selectPart( v3 );

		if( sp.part != null )
		{
			return sp.part.getItemStack( PartItemStack.Pick );
		}
		else if( sp.facade != null )
		{
			return sp.facade.getItemStack();
		}

		return null;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public boolean addHitEffects( final World world, final MovingObjectPosition target, final EffectRenderer effectRenderer )
	{
		final Object object = this.cb( world, target.blockX, target.blockY, target.blockZ );
		if( object instanceof IPartHost )
		{
			final IPartHost host = (IPartHost) object;

			for( final ForgeDirection side : ForgeDirection.values() )
			{
				final IPart p = host.getPart( side );
				final IIcon ico = this.getIcon( p );

				if( ico == null )
				{
					continue;
				}

				final byte b0 = (byte) ( Platform.getRandomInt() % 2 == 0 ? 1 : 0 );

				for( int i1 = 0; i1 < b0; ++i1 )
				{
					for( int j1 = 0; j1 < b0; ++j1 )
					{
						for( int k1 = 0; k1 < b0; ++k1 )
						{
							final double d0 = target.blockX + ( i1 + 0.5D ) / b0;
							final double d1 = target.blockY + ( j1 + 0.5D ) / b0;
							final double d2 = target.blockZ + ( k1 + 0.5D ) / b0;

							final double dd0 = target.hitVec.xCoord;
							final double dd1 = target.hitVec.yCoord;
							final double dd2 = target.hitVec.zCoord;
							final EntityDiggingFX fx = ( new EntityDiggingFX( world, dd0, dd1, dd2, d0 - target.blockX - 0.5D, d1 - target.blockY - 0.5D, d2 - target.blockZ - 0.5D, this, 0 ) ).applyColourMultiplier( target.blockX, target.blockY, target.blockZ );

							fx.setParticleIcon( ico );

							effectRenderer.addEffect( fx );
						}
					}
				}
			}
		}

		return true;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public boolean addDestroyEffects( final World world, final int x, final int y, final int z, final int meta, final EffectRenderer effectRenderer )
	{
		final Object object = this.cb( world, x, y, z );
		if( object instanceof IPartHost )
		{
			final IPartHost host = (IPartHost) object;

			for( final ForgeDirection side : ForgeDirection.values() )
			{
				final IPart p = host.getPart( side );
				final IIcon ico = this.getIcon( p );

				if( ico == null )
				{
					continue;
				}

				final byte b0 = 3;

				for( int i1 = 0; i1 < b0; ++i1 )
				{
					for( int j1 = 0; j1 < b0; ++j1 )
					{
						for( int k1 = 0; k1 < b0; ++k1 )
						{
							final double d0 = x + ( i1 + 0.5D ) / b0;
							final double d1 = y + ( j1 + 0.5D ) / b0;
							final double d2 = z + ( k1 + 0.5D ) / b0;
							final EntityDiggingFX fx = ( new EntityDiggingFX( world, d0, d1, d2, d0 - x - 0.5D, d1 - y - 0.5D, d2 - z - 0.5D, this, meta ) ).applyColourMultiplier( x, y, z );

							fx.setParticleIcon( ico );

							effectRenderer.addEffect( fx );
						}
					}
				}
			}
		}

		return true;
	}

	@Override
	public void onNeighborChange( final IBlockAccess w, final int x, final int y, final int z, final int tileX, final int tileY, final int tileZ )
	{
		if( Platform.isServer() )
		{
			this.cb( w, x, y, z ).onNeighborChanged();
		}
	}

	private IIcon getIcon( final IPart p )
	{
		if( p == null )
		{
			return null;
		}

		try
		{
			final IIcon ico = p.getBreakingTexture();
			if( ico != null )
			{
				return ico;
			}
		}
		catch( final Throwable t )
		{
			// nothing.
		}

		final ItemStack is = p.getItemStack( PartItemStack.Network );
		if( is == null || is.getItem() == null )
		{
			return null;
		}

		return is.getItem().getIcon( is, 0 );
	}

	private ICableBusContainer cb( final IBlockAccess w, final int x, final int y, final int z )
	{
		final TileEntity te = w.getTileEntity( x, y, z );
		ICableBusContainer out = null;

		if( te instanceof TileCableBus )
		{
			out = ( (TileCableBus) te ).getCableBus();
		}
		else if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.FMP ) )
		{
			out = ( (IFMP) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.FMP ) ).getCableContainer( te );
		}

		return out == null ? NULL_CABLE_BUS : out;
	}

	@Override
	@SideOnly( Side.CLIENT )
	protected RendererCableBus getRenderer()
	{
		return new RendererCableBus();
	}

	@Override
	public IIcon getIcon( final IBlockAccess w, final int x, final int y, final int z, final int s )
	{
		return this.getIcon( s, 0 );
	}

	@Override
	public IIcon getIcon( final int direction, final int metadata )
	{
		final IIcon i = super.getIcon( direction, metadata );
		if( i != null )
		{
			return i;
		}

		return ExtraBlockTextures.BlockQuartzGlassB.getIcon();
	}

	@Override
	public boolean onActivated( final World w, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ )
	{
		return this.cb( w, x, y, z ).activate( player, Vec3.createVectorHelper( hitX, hitY, hitZ ) );
	}

	@Override
	public void registerBlockIcons( final IIconRegister iconRegistry )
	{

	}

	@Override
	public boolean recolourBlock( final World world, final int x, final int y, final int z, final ForgeDirection side, final int colour )
	{
		return this.recolourBlock( world, x, y, z, side, colour, null );
	}

	public boolean recolourBlock( final World world, final int x, final int y, final int z, final ForgeDirection side, final int colour, final EntityPlayer who )
	{
		try
		{
			return this.cb( world, x, y, z ).recolourBlock( side, AEColor.values()[colour], who );
		}
		catch( final Throwable ignored )
		{
		}
		return false;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( final Item item, final CreativeTabs tabs, final List<ItemStack> itemStacks )
	{
		// do nothing
	}

	@Override
	public <T extends AEBaseTile> T getTileEntity( final IBlockAccess w, final int x, final int y, final int z )
	{
		final TileEntity te = w.getTileEntity( x, y, z );

		if( noTesrTile.isInstance( te ) )
		{
			return (T) te;
		}

		if( tesrTile != null && tesrTile.isInstance( te ) )
		{
			return (T) te;
		}

		return null;
	}

	@Override
	protected void setFeature( final EnumSet<AEFeature> f )
	{
		final AECableBusFeatureHandler featureHandler = new AECableBusFeatureHandler( f, this, this.featureSubName );
		this.setHandler( featureHandler );
	}

	public void setupTile()
	{
		noTesrTile = Api.INSTANCE.partHelper().getCombinedInstance( TileCableBus.class.getName() );
		this.setTileEntity( noTesrTile );
		GameRegistry.registerTileEntity( noTesrTile, "BlockCableBus" );
		if( Platform.isClient() )
		{
			tesrTile = Api.INSTANCE.partHelper().getCombinedInstance( TileCableBusTESR.class.getName() );
			GameRegistry.registerTileEntity( tesrTile, "ClientOnly_TESR_CableBus" );
			CommonHelper.proxy.bindTileEntitySpecialRenderer( tesrTile, this );
		}
	}

	@Override
	@Method( iname = IntegrationType.MFR )
	public RedNetConnectionType getConnectionType( final World world, final int x, final int y, final int z, final ForgeDirection side )
	{
		return this.cb( world, x, y, z ).canConnectRedstone( EnumSet.allOf( ForgeDirection.class ) ) ? RedNetConnectionType.CableSingle : RedNetConnectionType.None;
	}

	public void setRenderColor( final int color )
	{
		this.myColorMultiplier = color;
	}

	public static Class<? extends TileEntity> getTesrTile()
	{
		return BlockCableBus.tesrTile;
	}

	public static Class<? extends TileEntity> getNoTesrTile()
	{
		return BlockCableBus.noTesrTile;
	}
}
