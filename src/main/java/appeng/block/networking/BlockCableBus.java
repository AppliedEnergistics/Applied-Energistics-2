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

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RendererCableBus;
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
import appeng.util.Platform;

// TODO: MFR INTEGRATION
//@Interface( iface = "powercrystals.minefactoryreloaded.api.rednet.connectivity.IRedNetConnection", iname = IntegrationType.MFR )
public class BlockCableBus extends AEBaseTileBlock // implements
													// IRedNetConnection
{

	private static final ICableBusContainer NULL_CABLE_BUS = new NullCableBusContainer();
	private static Class<? extends TileEntity> noTesrTile;
	private static Class<? extends TileEntity> tesrTile;

	/**
	 * Immibis MB Support.
	 *
	 * It will look for a field named
	 * ImmibisMicroblocks_TransformableBlockMarker or
	 * ImmibisMicroblocks_TransformableTileEntityMarker, modifiers, type, etc
	 * can be ignored.
	 */
	@Reflected
	private static final boolean ImmibisMicroblocks_TransformableBlockMarker = true;

	public BlockCableBus()
	{
		super( AEGlassMaterial.INSTANCE );
		setLightOpacity( 0 );
		setFullSize( setOpaque( false ) );

		// this will actually be overwritten later through setupTile and the
		// combined layers
		setTileEntity( TileCableBus.class );
		setFeature( EnumSet.of( AEFeature.Core ) );
	}

	@Override
	public void randomDisplayTick(
			final World worldIn,
			final BlockPos pos,
			final IBlockState state,
			final Random rand )
	{
		cb( worldIn, pos ).randomDisplayTick( worldIn, pos, rand );
	}

	@Override
	public void onNeighborChange(
			final IBlockAccess w,
			final BlockPos pos,
			final BlockPos neighbor )
	{
		cb( w, pos ).onNeighborChanged();
	}

	@Override
	public Item getItemDropped(
			final IBlockState state,
			final Random rand,
			final int fortune )
	{
		return null;
	}

	@Override
	public int getWeakPower(
			final IBlockAccess w,
			final BlockPos pos,
			final IBlockState state,
			final EnumFacing side )
	{
		return cb( w, pos ).isProvidingWeakPower( side.getOpposite() ); // TODO:
																		// IS
																		// OPPOSITE!?
	}

	@Override
	public boolean canProvidePower()
	{
		return true;
	}

	@Override
	public void onEntityCollidedWithBlock(
			final World w,
			final BlockPos pos,
			final IBlockState state,
			final Entity entityIn )
	{
		cb( w, pos ).onEntityCollision( entityIn );
	}

	@Override
	public int getStrongPower(
			final IBlockAccess w,
			final BlockPos pos,
			final IBlockState state,
			final EnumFacing side )
	{
		return cb( w, pos ).isProvidingStrongPower( side.getOpposite() ); // TODO:
																			// IS
																			// OPPOSITE!?
	}

	@Override
	public int getLightValue(
			final IBlockAccess world,
			final BlockPos pos )
	{
		final IBlockState block = world.getBlockState( pos );
		if ( block != null && block.getBlock() != this )
		{
			return block.getBlock().getLightValue( world, pos );
		}
		if ( block == null )
		{
			return 0;
		}
		return cb( world, pos ).getLightValue();
	}

	@Override
	public boolean isLadder(
			final IBlockAccess world,
			final BlockPos pos,
			final EntityLivingBase entity )
	{
		return cb( world, pos ).isLadder( entity );
	}

	@Override
	public boolean isSideSolid(
			final IBlockAccess w,
			final BlockPos pos,
			final EnumFacing side )
	{
		return cb( w, pos ).isSolidOnSide( side );
	}

	@Override
	public boolean isReplaceable(
			final World w,
			final BlockPos pos )
	{
		return cb( w, pos ).isEmpty();
	}

	@Override
	public boolean removedByPlayer(
			final World world,
			final BlockPos pos,
			final EntityPlayer player,
			final boolean willHarvest )
	{
		if ( player.capabilities.isCreativeMode )
		{
			final AEBaseTile tile = this.getTileEntity( world, pos );
			if ( tile != null )
			{
				tile.disableDrops();
			}
			// maybe ray trace?
		}
		return super.removedByPlayer( world, pos, player, willHarvest );
	}

	@Override
	public boolean canConnectRedstone(
			final IBlockAccess w,
			final BlockPos pos,
			EnumFacing side )
	{
		if ( side == null )
		{
			side = EnumFacing.UP;
		}

		return cb( w, pos ).canConnectRedstone( EnumSet.of( side ) );
	}

	@Override
	public boolean canRenderInLayer(
			final EnumWorldBlockLayer layer )
	{
		if ( AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass ) )
		{
			return layer == EnumWorldBlockLayer.CUTOUT || layer == EnumWorldBlockLayer.TRANSLUCENT;
		}

		return layer == EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	public ItemStack getPickBlock(
			final MovingObjectPosition target,
			final World world,
			final BlockPos pos )
	{
		final Vec3 v3 = target.hitVec.subtract( pos.getX(), pos.getY(), pos.getZ() );
		final SelectedPart sp = cb( world, pos ).selectPart( v3 );

		if ( sp.part != null )
		{
			return sp.part.getItemStack( PartItemStack.Pick );
		}
		else if ( sp.facade != null )
		{
			return sp.facade.getItemStack();
		}

		return null;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public boolean addHitEffects(
			final World world,
			final MovingObjectPosition target,
			final EffectRenderer effectRenderer )
	{
		final Object object = cb( world, target.getBlockPos() );
		if ( object instanceof IPartHost )
		{
			final IPartHost host = (IPartHost) object;

			// TODO HIT EFFECTS
			/*
			 * for( AEPartLocation side : AEPartLocation.values() ) { IPart p =
			 * host.getPart( side ); TextureAtlasSprite ico = this.getIcon( p );
			 * if( ico == null ) { continue; } byte b0 = (byte) (
			 * Platform.getRandomInt() % 2 == 0 ? 1 : 0 ); for( int i1 = 0; i1 <
			 * b0; ++i1 ) { for( int j1 = 0; j1 < b0; ++j1 ) { for( int k1 = 0;
			 * k1 < b0; ++k1 ) { double d0 = target.blockX + ( i1 + 0.5D ) / b0;
			 * double d1 = target.blockY + ( j1 + 0.5D ) / b0; double d2 =
			 * target.blockZ + ( k1 + 0.5D ) / b0; double dd0 =
			 * target.hitVec.xCoord; double dd1 = target.hitVec.yCoord; double
			 * dd2 = target.hitVec.zCoord; EntityDiggingFX fx = ( new
			 * EntityDiggingFX( world, dd0, dd1, dd2, d0 - target.blockX - 0.5D,
			 * d1 - target.blockY - 0.5D, d2 - target.blockZ - 0.5D, this, 0 )
			 * ).applyColourMultiplier( target.blockX, target.blockY,
			 * target.blockZ ); fx.setParticleIcon( ico );
			 * effectRenderer.addEffect( fx ); } } } }
			 */
		}

		return true;
	}

	@Override
	public boolean addDestroyEffects(
			final World world,
			final BlockPos pos,
			final EffectRenderer effectRenderer )
	{
		final Object object = cb( world, pos );
		if ( object instanceof IPartHost )
		{
			final IPartHost host = (IPartHost) object;

			// TODO DESTROY EFFECTS
			/*
			 * for( AEPartLocation side : AEPartLocation.values() ) { IPart p =
			 * host.getPart( side ); TextureAtlasSprite ico = this.getIcon( p );
			 * if( ico == null ) { continue; } byte b0 = 3; for( int i1 = 0; i1
			 * < b0; ++i1 ) { for( int j1 = 0; j1 < b0; ++j1 ) { for( int k1 =
			 * 0; k1 < b0; ++k1 ) { double d0 = x + ( i1 + 0.5D ) / b0; double
			 * d1 = y + ( j1 + 0.5D ) / b0; double d2 = z + ( k1 + 0.5D ) / b0;
			 * EntityDiggingFX fx = ( new EntityDiggingFX( world, d0, d1, d2, d0
			 * - x - 0.5D, d1 - y - 0.5D, d2 - z - 0.5D, this, meta )
			 * ).applyColourMultiplier( x, y, z ); fx.setParticleIcon( ico );
			 * effectRenderer.addEffect( fx ); } } } }
			 */
		}

		return true;
	}

	@Override
	public void onNeighborBlockChange(
			final World w,
			final BlockPos pos,
			final IBlockState state,
			final Block neighborBlock )
	{
		if ( Platform.isServer() )
		{
			cb( w, pos ).onNeighborChanged();
		}
	}

	private ICableBusContainer cb(
			final IBlockAccess w,
			final BlockPos pos )
	{
		final TileEntity te = w.getTileEntity( pos );
		ICableBusContainer out = null;

		if ( te instanceof TileCableBus )
		{
			out = ( (TileCableBus) te ).getCableBus();
		}
		else if ( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.FMP ) )
		{
			out = ( (IFMP) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.FMP ) ).getCableContainer( te );
		}

		return out == null ? NULL_CABLE_BUS : out;
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RendererCableBus.class;
	}

	@Override
	public boolean onActivated(
			final World w,
			final BlockPos pos,
			final EntityPlayer player,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		return cb( w, pos ).activate( player, new Vec3( hitX, hitY, hitZ ) );
	}

	@Override
	public boolean recolorBlock(
			final World world,
			final BlockPos pos,
			final EnumFacing side,
			final EnumDyeColor color )
	{
		return this.recolorBlock( world, pos, side, color, null );
	}

	public boolean recolorBlock(
			final World world,
			final BlockPos pos,
			final EnumFacing side,
			final EnumDyeColor color,
			final EntityPlayer who )
	{
		try
		{
			return cb( world, pos ).recolourBlock( side, AEColor.values()[color.ordinal()], who );
		}
		catch ( final Throwable ignored )
		{
		}
		return false;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks(
			final Item item,
			final CreativeTabs tabs,
			final List<ItemStack> itemStacks )
	{
		// do nothing
	}

	@Override
	public <T extends AEBaseTile> T getTileEntity(
			final IBlockAccess w,
			final BlockPos pos )
	{
		final TileEntity te = w.getTileEntity( pos );

		if ( noTesrTile.isInstance( te ) )
		{
			return (T) te;
		}

		if ( tesrTile != null && tesrTile.isInstance( te ) )
		{
			return (T) te;
		}

		return null;
	}

	@Override
	protected void setFeature(
			final EnumSet<AEFeature> f )
	{
		final AECableBusFeatureHandler featureHandler = new AECableBusFeatureHandler( f, this, getFeatureSubName() );
		setHandler( featureHandler );
	}

	public void setupTile()
	{
		noTesrTile = Api.INSTANCE.partHelper().getCombinedInstance( TileCableBus.class.getName() );
		setTileEntity( noTesrTile );
		GameRegistry.registerTileEntity( noTesrTile, "BlockCableBus" );
		if ( Platform.isClient() )
		{
			tesrTile = Api.INSTANCE.partHelper().getCombinedInstance( TileCableBusTESR.class.getName() );
			GameRegistry.registerTileEntity( tesrTile, "ClientOnly_TESR_CableBus" );
			CommonHelper.proxy.bindTileEntitySpecialRenderer( tesrTile, this );
		}
	}

	public static Class<? extends TileEntity> getTesrTile()
	{
		return tesrTile;
	}

	public static Class<? extends TileEntity> getNoTesrTile()
	{
		return noTesrTile;
	}

	// TODO MFR Integration
	// @Override
	// @Method( iname = IntegrationType.MFR )
	// public RedNetConnectionType getConnectionType( World world, int x, int y,
	// int z, ForgeDirection side )
	// {
	// return this.cb( world, x, y, z ).canConnectRedstone( EnumSet.allOf(
	// ForgeDirection.class ) ) ?
	// RedNetConnectionType.CableSingle : RedNetConnectionType.None;
	// }
	//
	// public void setRenderColor( int color )
	// {
	// this.myColorMultiplier = color;
	// }
}
