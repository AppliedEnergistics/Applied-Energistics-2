package appeng.block.grindstone;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.tiles.ICrankable;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockCrank;
import appeng.core.features.AEFeature;
import appeng.tile.AEBaseTile;
import appeng.tile.grindstone.TileCrank;

public class BlockCrank extends AEBaseBlock
{

	public BlockCrank() {
		super( BlockCrank.class, Material.wood );
		setfeature( EnumSet.of( AEFeature.GrindStone ) );
		setTileEntiy( TileCrank.class );
		setLightOpacity( 0 );
		isFullSize = isOpaque = false;
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if ( p instanceof FakePlayer )
			return true;

		AEBaseTile tile = getTileEntity( w, x, y, z );
		if ( tile instanceof TileCrank )
			((TileCrank) tile).power();

		return true;
	}

	@Override
	public Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockCrank.class;
	}

	private boolean isCrankable(World w, int x, int y, int z, ForgeDirection offset)
	{
		TileEntity te = w.getTileEntity( x + offset.offsetX, y + offset.offsetY, z + offset.offsetZ );
		if ( te instanceof ICrankable )
		{
			return ((ICrankable) te).canCrankAttach( offset.getOpposite() );
		}
		return false;
	}

	private ForgeDirection findCrankable(World w, int x, int y, int z)
	{
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			if ( isCrankable( w, x, y, z, dir ) )
				return dir;
		return ForgeDirection.UNKNOWN;
	}

	@Override
	public boolean canPlaceBlockAt(World w, int x, int y, int z)
	{
		return findCrankable( w, x, y, z ) != ForgeDirection.UNKNOWN;
	}

	@Override
	public boolean isValidOrientation(World w, int x, int y, int z, ForgeDirection forward, ForgeDirection up)
	{
		TileEntity te = w.getTileEntity( x, y, z );
		return !(te instanceof TileCrank) || isCrankable( w, x, y, z, up.getOpposite() );
	}

	private void dropCrank(World w, int x, int y, int z)
	{
		w.func_147480_a( x, y, z, true ); // w.destroyBlock( x, y, z, true );
		w.markBlockForUpdate( x, y, z );
	}

	@Override
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase p, ItemStack is)
	{
		AEBaseTile tile = getTileEntity( w, x, y, z );
		if ( tile != null )
		{
			ForgeDirection mnt = findCrankable( w, x, y, z );
			ForgeDirection forward = ForgeDirection.UP;
			if ( mnt == ForgeDirection.UP || mnt == ForgeDirection.DOWN )
				forward = ForgeDirection.SOUTH;
			tile.setOrientation( forward, mnt.getOpposite() );
		}
		else
			dropCrank( w, x, y, z );
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block id)
	{
		AEBaseTile tile = getTileEntity( w, x, y, z );
		if ( tile != null )
		{
			if ( !isCrankable( w, x, y, z, tile.getUp().getOpposite() ) )
				dropCrank( w, x, y, z );
		}
		else
			dropCrank( w, x, y, z );
	}

}
