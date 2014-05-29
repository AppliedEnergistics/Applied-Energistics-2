package appeng.block.networking;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.blocks.RendererCableBus;
import appeng.client.texture.ExtraTextures;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.integration.abstraction.IFMP;
import appeng.parts.ICableBusContainer;
import appeng.parts.NullCableBusContainer;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import appeng.tile.networking.TileCableBusTESR;
import appeng.util.Platform;

public class BlockCableBus extends AEBaseBlock
{

	static private ICableBusContainer nullCB = new NullCableBusContainer();
	static public Class<? extends TileEntity> noTesrTile;
	static public Class<? extends TileEntity> tesrTile;

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
		super( BlockCableBus.class, Material.glass );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setLightOpacity( 0 );
		isFullSize = isOpaque = false;
	}

	@Override
	public int getRenderBlockPass()
	{
		if ( AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass ) )
			return 1;
		return 0;
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
		ICableBusContainer cbc = cb( world, x, y, z );
		return cbc == null ? false : cbc.isLadder( entity );
	}

	@Override
	public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour)
	{
		return recolourBlock( world, x, y, z, side, colour, null );
	}

	public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour, EntityPlayer who)
	{
		ICableBusContainer cbc = cb( world, x, y, z );
		return cbc == null ? false : cbc.recolourBlock( side, colour, who );
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random r)
	{
		ICableBusContainer cbc = cb( world, x, y, z );
		if( cbc != null )
			cbc.randomDisplayTick( world, x, y, z, r );
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
		
		ICableBusContainer cbc = cb( world, x, y, z );		
		return cbc == null ? 0 : cbc.getLightValue();
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		Vec3 v3 = target.hitVec.addVector( -x, -y, -z );
		SelectedPart sp = cb( world, x, y, z ).selectPart( v3 );

		if ( sp.part != null )
			return sp.part.getItemStack( PartItemStack.Pick );
		else if ( sp.facade != null )
			return sp.facade.getItemStack();

		return null;
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, int x, int y, int z)
	{
		ICableBusContainer cbc = cb( world, x, y, z );
		return cbc == null ? false : cbc.isEmpty();
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
	{
		if ( player.capabilities.isCreativeMode )
		{
			AEBaseTile tile = getTileEntity( world, x, y, z );
			if ( tile != null )
				tile.dropItems = false;
			// maybe ray trace?
		}
		
		return super.removedByPlayer( world, player, x, y, z );
	}

	@Override
	public IIcon getIcon(IBlockAccess w, int x, int y, int z, int s)
	{
		return getIcon( s, 0 );
	}

	@Override
	public IIcon getIcon(int direction, int metadata)
	{
		IIcon i = super.getIcon( direction, metadata );
		if ( i != null )
			return i;

		return ExtraTextures.BlockQuartzGlassB.getIcon();
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
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		ICableBusContainer cbc = cb( world, x, y, z );
		return cbc == null ? false : cbc.isSolidOnSide( side );
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block meh)
	{
		ICableBusContainer cbc = cb( world, x, y, z );
		if ( cbc != null )
			cbc.onNeighborChanged();
	}

	@Override
	public Item getItemDropped(int i, Random r, int k)
	{
		return null;
	}

	@Override
	public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		ICableBusContainer cbc = cb( world, x, y, z );
		return cbc == null ? false : cbc.activate( player, world.getWorldVec3Pool().getVecFromPool( hitX, hitY, hitZ ) );
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity e)
	{
		ICableBusContainer cbc = cb( world, x, y, z );
		if ( cbc != null )
			cbc.onEntityCollision( e );
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side)
	{
		ICableBusContainer cbc = cb( world, x, y, z );
		
		if ( cbc != null )
		{
			switch (side)
			{
			case -1:
			case 4:
				return cb( world, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.UP, ForgeDirection.DOWN ) );
			case 0:
				return cb( world, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.NORTH ) );
			case 1:
				return cb( world, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.EAST ) );
			case 2:
				return cb( world, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.SOUTH ) );
			case 3:
				return cb( world, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.WEST ) );
			}
		}
		
		return false;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side)
	{
		ICableBusContainer cbc = cb( world, x, y, z );
		return cbc == null ? 0 : cbc.isProvidingWeakPower( ForgeDirection.getOrientation( side ).getOpposite() );
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side)
	{
		ICableBusContainer cbc = cb( world, x, y, z );
		return cbc == null ? 0 : cbc.isProvidingStrongPower( ForgeDirection.getOrientation( side ).getOpposite() );
	}

	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{

	}

	public void setupTile()
	{
		setTileEntiy( noTesrTile = Api.instance.partHelper.getCombinedInstance( TileCableBus.class.getName() ) );
		if ( Platform.isClient() )
		{
			tesrTile = Api.instance.partHelper.getCombinedInstance( TileCableBusTESR.class.getName() );
			CommonHelper.proxy.bindTileEntitySpecialRenderer( tesrTile, this );
		}
	}

	private ICableBusContainer cb(IBlockAccess w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity( x, y, z );
		ICableBusContainer out = null;

		if ( te instanceof TileCableBus )
			out = ((TileCableBus) te).cb;

		else if ( AppEng.instance.isIntegrationEnabled( "FMP" ) )
			out = ((IFMP) AppEng.instance.getIntegration( "FMP" )).getCableContainer( te );

		return out == null ? nullCB : out;
	}

	/**
	 * Immibis MB Support.
	 */
	boolean ImmibisMicroblocks_TransformableBlockMarker = true;

}
