package appeng.block.networking;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
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
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RendererCableBus;
import appeng.client.texture.CableBusTextures;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.integration.abstraction.IFMP;
import appeng.parts.ICableBusContainer;
import appeng.parts.NullCableBusContainer;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import appeng.util.Platform;

public class BlockCableBus extends AEBaseBlock
{

	static private ICableBusContainer nullCB = new NullCableBusContainer();

	public BlockCableBus() {
		super( BlockCableBus.class, Material.glass );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setLightOpacity( 0 );
		isFullSize = isOpaque = false;
	}

	@Override
	public boolean isLadder(World world, int x, int y, int z, EntityLivingBase entity)
	{
		return cb( world, x, y, z ).isLadder( entity );
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
		cb( world, x, y, z ).randomDisplayTick( world, x, y, z, r );
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		Block block = blocksList[world.getBlockId( x, y, z )];
		if ( block != null && block != this )
		{
			return block.getLightValue( world, x, y, z );
		}
		if ( block == null )
			return 0;
		return cb( world, x, y, z ).getLightValue();
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		Vec3 v3 = target.hitVec.addVector( -x, -y, -z );
		SelectedPart sp = cb( world, x, y, z ).selectPart( v3 );

		if ( sp.part != null )
			return sp.part.getItemStack( PartItemStack.Break );
		else if ( sp.facade != null )
			return sp.facade.getItemStack();

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

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RendererCableBus.class;
	}

	@Override
	public void registerIcons(IconRegister iconRegistry)
	{

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
	}

	@Override
	public int idDropped(int i, Random r, int k)
	{
		return 0;
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
		switch (side)
		{
		case -1:
		case 4:
			return cb( w, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.UP, ForgeDirection.DOWN ) );
		case 0:
			return cb( w, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.NORTH ) );
		case 1:
			return cb( w, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.EAST ) );
		case 2:
			return cb( w, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.SOUTH ) );
		case 3:
			return cb( w, x, y, z ).canConnectRedstone( EnumSet.of( ForgeDirection.WEST ) );
		}
		return false;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess w, int x, int y, int z, int side)
	{
		return cb( w, x, y, z ).isProvidingWeakPower( ForgeDirection.getOrientation( side ).getOpposite() );
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess w, int x, int y, int z, int side)
	{
		return cb( w, x, y, z ).isProvidingStrongPower( ForgeDirection.getOrientation( side ).getOpposite() );
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{

	}

	public void setupTile()
	{
		setTileEntiy( Api.instance.partHelper.getCombinedInstance( TileCableBus.class.getName() ) );
		if ( Platform.isClient() )
			CommonHelper.proxy.bindTileEntitySpecialRenderer( getTileEntityClass(), this );
	}

	private ICableBusContainer cb(IBlockAccess w, int x, int y, int z)
	{
		TileEntity te = w.getBlockTileEntity( x, y, z );
		if ( te instanceof TileCableBus )
			return ((TileCableBus) te).cb;

		if ( AppEng.instance.isIntegrationEnabled( "FMP" ) )
			return ((IFMP) AppEng.instance.getIntegration( "FMP" )).getCableContainer( te );

		return nullCB;
	}

}
