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
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.client.UnlistedProperty;
import appeng.client.render.cablebus.CableBusBakedModel;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketClick;
import appeng.helpers.AEGlassMaterial;
import appeng.integration.abstraction.IAEFacade;
import appeng.parts.ICableBusContainer;
import appeng.parts.NullCableBusContainer;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.CableBusTESR;
import appeng.tile.networking.TileCableBus;
import appeng.tile.networking.TileCableBusTESR;
import appeng.util.Platform;


public class BlockCableBus extends AEBaseTileBlock implements IAEFacade
{

	public static final UnlistedProperty<CableBusRenderState> RENDER_STATE_PROPERTY = new UnlistedProperty<>( "cable_bus_render_state", CableBusRenderState.class );

	private static final ICableBusContainer NULL_CABLE_BUS = new NullCableBusContainer();

	private static Class<? extends AEBaseTile> noTesrTile;

	private static Class<? extends AEBaseTile> tesrTile;

	public BlockCableBus()
	{
		super( AEGlassMaterial.INSTANCE );
		this.setLightOpacity( 0 );
		this.setFullSize( false );
		this.setOpaque( false );

		// this will actually be overwritten later through setupTile and the
		// combined layers
		this.setTileEntity( TileCableBus.class );
	}

	@Override
	public boolean isFullCube( IBlockState state )
	{
		return false;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new ExtendedBlockState( this, new IProperty[0], new IUnlistedProperty[] { RENDER_STATE_PROPERTY } );
	}

	@Override
	public IBlockState getExtendedState( IBlockState state, IBlockAccess world, BlockPos pos )
	{
		CableBusRenderState renderState = this.cb( world, pos ).getRenderState();
		renderState.setWorld( world );
		renderState.setPos( pos );
		return ( (IExtendedBlockState) state ).withProperty( RENDER_STATE_PROPERTY, renderState );
	}

	@Override
	public void randomDisplayTick( final IBlockState state, final World worldIn, final BlockPos pos, final Random rand )
	{
		this.cb( worldIn, pos ).randomDisplayTick( worldIn, pos, rand );
	}

	@Override
	public void onNeighborChange( final IBlockAccess w, final BlockPos pos, final BlockPos neighbor )
	{
		this.cb( w, pos ).onNeighborChanged( w, pos, neighbor );
	}

	@Override
	public Item getItemDropped( final IBlockState state, final Random rand, final int fortune )
	{
		return null;
	}

	@Override
	public int getWeakPower( final IBlockState state, final IBlockAccess w, final BlockPos pos, final EnumFacing side )
	{
		return this.cb( w, pos ).isProvidingWeakPower( side.getOpposite() ); // TODO:
		// IS
		// OPPOSITE!?
	}

	@Override
	public boolean canProvidePower( final IBlockState state )
	{
		return true;
	}

	@Override
	public void onEntityCollidedWithBlock( final World w, final BlockPos pos, final IBlockState state, final Entity entityIn )
	{
		this.cb( w, pos ).onEntityCollision( entityIn );
	}

	@Override
	public int getStrongPower( final IBlockState state, final IBlockAccess w, final BlockPos pos, final EnumFacing side )
	{
		return this.cb( w, pos ).isProvidingStrongPower( side.getOpposite() ); // TODO:
		// IS
		// OPPOSITE!?
	}

	@Override
	public int getLightValue( final IBlockState state, final IBlockAccess world, final BlockPos pos )
	{
		if( state.getBlock() != this )
		{
			return state.getBlock().getLightValue( state, world, pos );
		}
		return this.cb( world, pos ).getLightValue();
	}

	@Override
	public boolean isLadder( final IBlockState state, final IBlockAccess world, final BlockPos pos, final EntityLivingBase entity )
	{
		return this.cb( world, pos ).isLadder( entity );
	}

	@Override
	public boolean isSideSolid( final IBlockState state, final IBlockAccess w, final BlockPos pos, final EnumFacing side )
	{
		return this.cb( w, pos ).isSolidOnSide( side );
	}

	@Override
	public boolean isReplaceable( final IBlockAccess w, final BlockPos pos )
	{
		return this.cb( w, pos ).isEmpty();
	}

	@Override
	public boolean removedByPlayer( final IBlockState state, final World world, final BlockPos pos, final EntityPlayer player, final boolean willHarvest )
	{
		if( player.capabilities.isCreativeMode )
		{
			final AEBaseTile tile = this.getTileEntity( world, pos );
			if( tile != null )
			{
				tile.disableDrops();
			}
			// maybe ray trace?
		}
		return super.removedByPlayer( state, world, pos, player, willHarvest );
	}

	@Override
	public boolean canConnectRedstone( final IBlockState state, final IBlockAccess w, final BlockPos pos, EnumFacing side )
	{
		if( side == null )
		{
			side = EnumFacing.UP;
		}

		return this.cb( w, pos ).canConnectRedstone( EnumSet.of( side ) );
	}

	@Override
	public ItemStack getPickBlock( final IBlockState state, final RayTraceResult target, final World world, final BlockPos pos, final EntityPlayer player )
	{
		final Vec3d v3 = target.hitVec.subtract( pos.getX(), pos.getY(), pos.getZ() );
		final SelectedPart sp = this.cb( world, pos ).selectPart( v3 );

		if( sp.part != null )
		{
			return sp.part.getItemStack( PartItemStack.PICK );
		}
		else if( sp.facade != null )
		{
			return sp.facade.getItemStack();
		}

		return ItemStack.EMPTY;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public boolean addHitEffects( final IBlockState state, final World world, final RayTraceResult target, final ParticleManager effectRenderer )
	{

		// Half the particle rate. Since we're spawning concentrated on a specific spot,
		// our particle effect otherwise looks too strong
		if( Platform.getRandom().nextBoolean() )
		{
			return true;
		}

		ICableBusContainer cb = this.cb( world, target.getBlockPos() );

		// Our built-in model has the actual baked sprites we need
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState( this.getDefaultState() );

		// We cannot add the effect if we don't have the model
		if( !( model instanceof CableBusBakedModel ) )
		{
			return true;
		}

		CableBusBakedModel cableBusModel = (CableBusBakedModel) model;

		CableBusRenderState renderState = cb.getRenderState();

		// Spawn a particle for one of the particle textures
		TextureAtlasSprite texture = Platform.pickRandom( cableBusModel.getParticleTextures( renderState ) );
		if( texture != null )
		{
			double x = target.hitVec.x;
			double y = target.hitVec.y;
			double z = target.hitVec.z;

			Particle fx = new DestroyFX( world, x, y, z, 0.0D, 0.0D, 0.0D, state ).setBlockPos( target.getBlockPos() ).multipleParticleScaleBy( 0.8F );
			fx.setParticleTexture( texture );
			effectRenderer.addEffect( fx );
		}

		return true;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public boolean addDestroyEffects( final World world, final BlockPos pos, final ParticleManager effectRenderer )
	{
		ICableBusContainer cb = this.cb( world, pos );

		// Our built-in model has the actual baked sprites we need
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState( this.getDefaultState() );

		// We cannot add the effect if we dont have the model
		if( !( model instanceof CableBusBakedModel ) )
		{
			return true;
		}

		CableBusBakedModel cableBusModel = (CableBusBakedModel) model;

		CableBusRenderState renderState = cb.getRenderState();

		List<TextureAtlasSprite> textures = cableBusModel.getParticleTextures( renderState );

		if( !textures.isEmpty() )
		{
			// Shamelessly inspired by ParticleManager.addBlockDestroyEffects
			for( int j = 0; j < 4; ++j )
			{
				for( int k = 0; k < 4; ++k )
				{
					for( int l = 0; l < 4; ++l )
					{
						// Randomly select one of the textures if the cable bus has more than just one possibility here
						TextureAtlasSprite texture = Platform.pickRandom( textures );

						double d0 = (double) pos.getX() + ( (double) j + 0.5D ) / 4.0D;
						double d1 = (double) pos.getY() + ( (double) k + 0.5D ) / 4.0D;
						double d2 = (double) pos.getZ() + ( (double) l + 0.5D ) / 4.0D;
						ParticleDigging particle = new DestroyFX( world, d0, d1, d2, d0 - (double) pos.getX() - 0.5D, d1 - (double) pos.getY() - 0.5D, d2 - (double) pos.getZ() - 0.5D, this.getDefaultState() ).setBlockPos( pos );
						particle.setParticleTexture( texture );
						effectRenderer.addEffect( particle );
					}
				}
			}
		}

		return true;
	}

	@Override
	public void neighborChanged( IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos )
	{
		if( Platform.isServer() )
		{
			this.cb( world, pos ).onNeighborChanged( world, pos, fromPos );
		}
	}

	private ICableBusContainer cb( final IBlockAccess w, final BlockPos pos )
	{
		final TileEntity te = w.getTileEntity( pos );
		ICableBusContainer out = null;

		if( te instanceof TileCableBus )
		{
			out = ( (TileCableBus) te ).getCableBus();
		}

		return out == null ? NULL_CABLE_BUS : out;
	}

	@Nullable
	private IFacadeContainer fc( final IBlockAccess w, final BlockPos pos )
	{
		final TileEntity te = w.getTileEntity( pos );
		IFacadeContainer out = null;

		if( te instanceof TileCableBus )
		{
			out = ( (TileCableBus) te ).getCableBus().getFacadeContainer();
		}

		return out;
	}

	@Override
	public void onBlockClicked( World worldIn, BlockPos pos, EntityPlayer playerIn )
	{
		if( Platform.isClient() )
		{
			final RayTraceResult rtr = Minecraft.getMinecraft().objectMouseOver;
			if( rtr != null && rtr.typeOfHit == Type.BLOCK && pos.equals( rtr.getBlockPos() ) )
			{
				final Vec3d hitVec = rtr.hitVec.subtract( new Vec3d( pos ) );

				if( this.cb( worldIn, pos ).clicked( playerIn, EnumHand.MAIN_HAND, hitVec ) )
				{
					NetworkHandler.instance().sendToServer( new PacketClick( pos, rtr.sideHit, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z, EnumHand.MAIN_HAND, true ) );
				}
			}
		}
	}

	public void onBlockClickPacket( World worldIn, BlockPos pos, EntityPlayer playerIn, EnumHand hand, Vec3d hitVec )
	{
		this.cb( worldIn, pos ).clicked( playerIn, hand, hitVec );
	}

	@Override
	public boolean onActivated( final World w, final BlockPos pos, final EntityPlayer player, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		return this.cb( w, pos ).activate( player, hand, new Vec3d( hitX, hitY, hitZ ) );
	}

	@Override
	public boolean recolorBlock( final World world, final BlockPos pos, final EnumFacing side, final EnumDyeColor color )
	{
		return this.recolorBlock( world, pos, side, color, null );
	}

	public boolean recolorBlock( final World world, final BlockPos pos, final EnumFacing side, final EnumDyeColor color, final EntityPlayer who )
	{
		try
		{
			return this.cb( world, pos ).recolourBlock( side, AEColor.values()[color.ordinal()], who );
		}
		catch( final Throwable ignored )
		{
		}
		return false;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getSubBlocks( final CreativeTabs tabs, final NonNullList<ItemStack> itemStacks )
	{
		// do nothing
	}

	public void setupTile()
	{
		noTesrTile = Api.INSTANCE.partHelper().getCombinedInstance( TileCableBus.class );
		this.setTileEntity( noTesrTile );

		GameRegistry.registerTileEntity( noTesrTile, AppEng.MOD_ID.toLowerCase() + ":" + "BlockCableBus" );

		if( Platform.isClient() )
		{
			setupTesr();
		}
	}

	@SideOnly( Side.CLIENT )
	private static void setupTesr()
	{
		tesrTile = Api.INSTANCE.partHelper().getCombinedInstance( TileCableBusTESR.class );
		GameRegistry.registerTileEntity( tesrTile, AppEng.MOD_ID.toLowerCase() + ":" + "ClientOnly_TESR_CableBus" );
		ClientRegistry.bindTileEntitySpecialRenderer( BlockCableBus.getTesrTile(), new CableBusTESR() );
	}

	@Override
	public boolean canRenderInLayer( IBlockState state, BlockRenderLayer layer )
	{
		return true;
	}

	@Override
	public IBlockState getFacadeState( IBlockAccess world, BlockPos pos, EnumFacing side )
	{
		if( side != null )
		{
			IFacadeContainer container = fc( world, pos );
			if( container != null )
			{
				IFacadePart facade = container.getFacade( AEPartLocation.fromFacing( side ) );
				if( facade != null )
				{
					return facade.getBlockState();
				}
			}
		}
		return world.getBlockState( pos );
	}

	public static Class<? extends AEBaseTile> getNoTesrTile()
	{
		return noTesrTile;
	}

	public static Class<? extends AEBaseTile> getTesrTile()
	{
		return tesrTile;
	}

	// Helper to get access to the protected constructor
	@SideOnly( Side.CLIENT )
	private static class DestroyFX extends ParticleDigging
	{
		DestroyFX( World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, IBlockState state )
		{
			super( worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state );
		}
	}
}
