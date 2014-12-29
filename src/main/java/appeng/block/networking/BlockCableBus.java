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

package appeng.block.networking;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

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

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import powercrystals.minefactoryreloaded.api.rednet.connectivity.IRedNetConnection;
import powercrystals.minefactoryreloaded.api.rednet.connectivity.RedNetConnectionType;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.blocks.RendererCableBus;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.helpers.AEGlassMaterial;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IFMP;
import appeng.parts.ICableBusContainer;
import appeng.parts.NullCableBusContainer;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import appeng.tile.networking.TileCableBusTESR;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.Method;
import appeng.util.Platform;

@Interface(iface = "powercrystals.minefactoryreloaded.api.rednet.connectivity.IRedNetConnection", iname = "MFR")
public class BlockCableBus extends AEBaseBlock implements IRedNetConnection
{

	static private final ICableBusContainer nullCB = new NullCableBusContainer();
	static public Class<? extends TileEntity> noTesrTile;
	static public Class<? extends TileEntity> tesrTile;

	@Override
	public <T extends TileEntity> T getTileEntity(IBlockAccess w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity( x, y, z );

		if ( noTesrTile.isInstance( te ) )
			return (T) te;

		if ( tesrTile != null && tesrTile.isInstance( te ) )
			return (T) te;

		return null;
	}

	public BlockCableBus() {
		super( BlockCableBus.class, AEGlassMaterial.instance );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
		this.setLightOpacity( 0 );
		this.isFullSize = this.isOpaque = false;
	}

	@Override
	public int getRenderBlockPass()
	{
		if ( AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass ) )
			return 1;
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer)
	{
		Object object = this.cb( world, target.blockX, target.blockY, target.blockZ );
		if ( object instanceof IPartHost )
		{
			IPartHost host = (IPartHost) object;

			for (ForgeDirection side : ForgeDirection.values())
			{
				IPart p = host.getPart( side );
				IIcon ico = this.getIcon( p );

				if ( ico == null )
					continue;

				byte b0 = (byte) (Platform.getRandomInt() % 2 == 0 ? 1 : 0);

				for (int i1 = 0; i1 < b0; ++i1)
				{
					for (int j1 = 0; j1 < b0; ++j1)
					{
						for (int k1 = 0; k1 < b0; ++k1)
						{
							double d0 = target.blockX + (i1 + 0.5D) / b0;
							double d1 = target.blockY + (j1 + 0.5D) / b0;
							double d2 = target.blockZ + (k1 + 0.5D) / b0;

							double dd0 = target.hitVec.xCoord;
							double dd1 = target.hitVec.yCoord;
							double dd2 = target.hitVec.zCoord;
							EntityDiggingFX fx = (new EntityDiggingFX( world, dd0, dd1, dd2, d0 - target.blockX - 0.5D, d1 - target.blockY
									- 0.5D, d2 - target.blockZ - 0.5D, this, 0 )).applyColourMultiplier( target.blockX, target.blockY, target.blockZ );

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
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer)
	{
		Object object = this.cb( world, x, y, z );
		if ( object instanceof IPartHost )
		{
			IPartHost host = (IPartHost) object;

			for (ForgeDirection side : ForgeDirection.values())
			{
				IPart p = host.getPart( side );
				IIcon ico = this.getIcon( p );

				if ( ico == null )
					continue;

				byte b0 = 3;

				for (int i1 = 0; i1 < b0; ++i1)
				{
					for (int j1 = 0; j1 < b0; ++j1)
					{
						for (int k1 = 0; k1 < b0; ++k1)
						{
							double d0 = x + (i1 + 0.5D) / b0;
							double d1 = y + (j1 + 0.5D) / b0;
							double d2 = z + (k1 + 0.5D) / b0;
							EntityDiggingFX fx = (new EntityDiggingFX( world, d0, d1, d2, d0 - x - 0.5D, d1 - y - 0.5D, d2 - z
									- 0.5D, this, meta )).applyColourMultiplier( x, y, z );

							fx.setParticleIcon( ico );

							effectRenderer.addEffect( fx );
						}
					}
				}
			}
		}

		return true;
	}

	private IIcon getIcon(IPart p)
	{
		if ( p == null )
			return null;

		try
		{
			IIcon ico = p.getBreakingTexture();
			if ( ico != null )
				return ico;
		}
		catch (Throwable t)
		{
			// nothing.
		}

		ItemStack is = p.getItemStack( PartItemStack.Network );
		if ( is == null || is.getItem() == null )
			return null;

		return is.getItem().getIcon( is, 0 );
	}

	@Override
	public boolean canRenderInPass(int pass)
	{
		BusRenderHelper.instance.setPass( pass );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass ) )
			return true;

		return pass == 0;
	}

	@Override
	public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity)
	{
		return this.cb( world, x, y, z ).isLadder( entity );
	}

	@Override
	public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour)
	{
		return this.recolourBlock( world, x, y, z, side, colour, null );
	}

	public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour, EntityPlayer who)
	{
		try
		{
			return this.cb( world, x, y, z ).recolourBlock( side, AEColor.values()[colour], who );
		}
		catch (Throwable ignored)
		{
		}
		return false;
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random r)
	{
		this.cb( world, x, y, z ).randomDisplayTick( world, x, y, z, r );
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		Block block = world.getBlock( x, y, z );
		if ( block != null && block != this )
		{
			return block.getLightValue( world, x, y, z );
		}
		if ( block == null )
			return 0;
		return this.cb( world, x, y, z ).getLightValue();
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		Vec3 v3 = target.hitVec.addVector( -x, -y, -z );
		SelectedPart sp = this.cb( world, x, y, z ).selectPart( v3 );

		if ( sp.part != null )
			return sp.part.getItemStack( PartItemStack.Pick );
		else if ( sp.facade != null )
			return sp.facade.getItemStack();

		return null;
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, int x, int y, int z)
	{
		return this.cb( world, x, y, z ).isEmpty();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
	{
		if ( player.capabilities.isCreativeMode )
		{
			AEBaseTile tile = this.getTileEntity( world, x, y, z );
			if ( tile != null )
				tile.disableDrops();
			// maybe ray trace?
		}
		return super.removedByPlayer( world, player, x, y, z );
	}

	@Override
	public IIcon getIcon(IBlockAccess w, int x, int y, int z, int s)
	{
		return this.getIcon( s, 0 );
	}

	@Override
	public IIcon getIcon(int direction, int metadata)
	{
		IIcon i = super.getIcon( direction, metadata );
		if ( i != null )
			return i;

		return ExtraBlockTextures.BlockQuartzGlassB.getIcon();
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RendererCableBus.class;
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegistry)
	{

	}

	@Override
	public boolean canProvidePower()
	{
		return true;
	}

	@Override
	public boolean isSideSolid(IBlockAccess w, int x, int y, int z, ForgeDirection side)
	{
		return this.cb( w, x, y, z ).isSolidOnSide( side );
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block meh)
	{
		this.cb( w, x, y, z ).onNeighborChanged();
	}

	@Override
	public void onNeighborChange(IBlockAccess w, int x, int y, int z, int tileX, int tileY, int tileZ)
	{
		if ( Platform.isServer() )
			this.cb( w, x, y, z ).onNeighborChanged();
	}

	@Override
	public Item getItemDropped(int i, Random r, int k)
	{
		return null;
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		return this.cb( w, x, y, z ).activate( player, Vec3.createVectorHelper( hitX, hitY, hitZ ) );
	}

	@Override
	public void onEntityCollidedWithBlock(World w, int x, int y, int z, Entity e)
	{
		this.cb( w, x, y, z ).onEntityCollision( e );
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess w, int x, int y, int z, int side)
	{
		switch (side)
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
	public int isProvidingWeakPower(IBlockAccess w, int x, int y, int z, int side)
	{
		return this.cb( w, x, y, z ).isProvidingWeakPower( ForgeDirection.getOrientation( side ).getOpposite() );
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess w, int x, int y, int z, int side)
	{
		return this.cb( w, x, y, z ).isProvidingStrongPower( ForgeDirection.getOrientation( side ).getOpposite() );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getCheckedSubBlocks(Item item, CreativeTabs tabs, List<ItemStack> itemStacks)
	{
		// do nothing
	}

	public void setupTile()
	{
		this.setTileEntity( noTesrTile = Api.instance.partHelper.getCombinedInstance( TileCableBus.class.getName() ) );
		if ( Platform.isClient() )
		{
			tesrTile = Api.instance.partHelper.getCombinedInstance( TileCableBusTESR.class.getName() );
			GameRegistry.registerTileEntity( tesrTile, "ClientOnly_TESR_CableBus" );
			CommonHelper.proxy.bindTileEntitySpecialRenderer( tesrTile, this );
		}
	}

	private ICableBusContainer cb(IBlockAccess w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity( x, y, z );
		ICableBusContainer out = null;

		if ( te instanceof TileCableBus )
			out = ((TileCableBus) te).cb;

		else if ( AppEng.instance.isIntegrationEnabled( IntegrationType.FMP ) )
			out = ((IFMP) AppEng.instance.getIntegration( IntegrationType.FMP )).getCableContainer( te );

		return out == null ? nullCB : out;
	}

	/**
	 * Immibis MB Support.
	 */
	boolean ImmibisMicroblocks_TransformableBlockMarker = true;

	@Override
	@Method(iname = "MFR")
	public RedNetConnectionType getConnectionType(World world, int x, int y, int z, ForgeDirection side)
	{
		return this.cb( world, x, y, z ).canConnectRedstone( EnumSet.allOf( ForgeDirection.class ) ) ? RedNetConnectionType.CableSingle : RedNetConnectionType.None;
	}

	int myColorMultiplier = 0xffffff;

	public void setRenderColor(int color)
	{
		this.myColorMultiplier = color;
	}

	@Override
	public int colorMultiplier(IBlockAccess p_149720_1_, int p_149720_2_, int p_149720_3_, int p_149720_4_)
	{
		return this.myColorMultiplier;
	}

}
