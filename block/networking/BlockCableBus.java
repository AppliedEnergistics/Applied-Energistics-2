package appeng.block.networking;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.parts.SelectedPart;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RendererCableBus;
import appeng.client.texture.CableBusTextures;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.integration.abstraction.IFMP;
import appeng.parts.CableBusContainer;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;

public class BlockCableBus extends AEBaseBlock
{

	public BlockCableBus() {
		super( BlockCableBus.class, Material.glass );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setLightOpacity( 0 );
		isFullSize = isOpaque = false;
	}

	@Override
	public boolean isLadder(World world, int x, int y, int z, EntityLivingBase entity)
	{
		try
		{
			return cb( world, x, y, z ).isLadder( entity );
		}
		catch (Throwable t)
		{
		}
		return false;
	}

	@Override
	public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour)
	{
		try
		{
			return cb( world, x, y, z ).recolourBlock( side, colour );
		}
		catch (Throwable t)
		{
		}
		return false;
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random r)
	{
		CableBusContainer cb = cb( world, x, y, z );
		if ( cb != null )
			cb.randomDisplayTick( world, x, y, z, r );
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		CableBusContainer cb = cb( world, x, y, z );
		if ( cb != null )
			return cb.getLightValue();
		return 0;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		Vec3 v3 = target.hitVec.addVector( -x, -y, -z );
		SelectedPart sp = cb( world, x, y, z ).selectPart( v3 );
		if ( sp != null )
		{
			if ( sp.part != null )
				return sp.part.getItemStack( false );
			if ( sp.facade != null )
				return sp.facade.getItemStack();
		}
		return null;
	}

	@Override
	public boolean isBlockReplaceable(World world, int x, int y, int z)
	{
		return cb( world, x, y, z ).isEmpty();
	}

	@Override
	public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z)
	{
		if ( player.capabilities.isCreativeMode )
		{
			AEBaseTile tile = getTileEntity( world, x, y, z );
			if ( tile != null )
				tile.dropItems = false;
			// maybe ray trace?
		}
		return super.removeBlockByPlayer( world, player, x, y, z );
	}

	@Override
	public Icon getBlockTexture(IBlockAccess w, int x, int y, int z, int s)
	{
		return getIcon( s, 0 );
	}

	@Override
	public Icon getIcon(int direction, int metadata)
	{
		Icon i = super.getIcon( direction, metadata );
		if ( i != null )
			return i;
		return CableBusTextures.getMissing();
	}

	private CableBusContainer cb(IBlockAccess w, int x, int y, int z)
	{
		TileEntity te = w.getBlockTileEntity( x, y, z );
		if ( te instanceof TileCableBus )
			return ((TileCableBus) te).cb;

		if ( AppEng.instance.isIntegrationEnabled( "FMP" ) )
			return ((IFMP) AppEng.instance.getIntegration( "FMP" )).getCableContainer( te );

		return null;
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RendererCableBus.class;
	}

	@Override
	public void registerIcons(IconRegister iconRegistry)
	{

	}

	private int rs(int side)
	{
		int s = 0;
		switch (side)
		{
		case -1:
			s = 1;
			break;
		case 1:
			s = 2;
			break;
		case 2:
			s = 5;
			break;
		case 3:
			s = 3;
			break;
		case 4:
			s = 4;
			break;
		default:
		}
		return s;
	}

	@Override
	public boolean canProvidePower()
	{
		return true;
	}

	@Override
	public boolean isBlockSolidOnSide(World w, int x, int y, int z, ForgeDirection side)
	{
		return cb( w, x, y, z ).isSolidOnSide( side );
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, int meh)
	{
		cb( w, x, y, z ).onNeighborChanged();
		// kinda works
		/*
		 * if ( cb( w, x, y, z ).isEmpty() ) { w.setBlockToAir( x, y, z ); }
		 */
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		return cb( w, x, y, z ).activate( player, w.getWorldVec3Pool().getVecFromPool( hitX, hitY, hitZ ) );
	}

	@Override
	public void onEntityCollidedWithBlock(World w, int x, int y, int z, Entity e)
	{
		cb( w, x, y, z ).onEntityCollision( e );
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess w, int x, int y, int z, int side)
	{
		return cb( w, x, y, z ).canConnectRedstone( ForgeDirection.getOrientation( rs( side ) ) );
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess w, int x, int y, int z, int side)
	{
		return cb( w, x, y, z ).isProvidingWeakPower( ForgeDirection.getOrientation( side ) );
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess w, int x, int y, int z, int side)
	{
		return cb( w, x, y, z ).isProvidingStrongPower( ForgeDirection.getOrientation( side ) );
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{

	}

	public void setupTile()
	{
		setTileEntiy( Api.instance.partHelper.getCombinedInstance( TileCableBus.class.getName() ) );
	}

}
