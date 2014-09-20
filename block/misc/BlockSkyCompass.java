package appeng.block.misc;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockSkyCompass;
import appeng.core.features.AEFeature;
import appeng.helpers.ICustomCollision;
import appeng.tile.misc.TileSkyCompass;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockSkyCompass extends AEBaseBlock implements ICustomCollision
{

	public BlockSkyCompass() {
		super( BlockSkyCompass.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.MeteoriteCompass ) );
		setTileEntity( TileSkyCompass.class );
		isOpaque = isFullSize = false;
		lightOpacity = 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int direction, int metadata)
	{
		return Blocks.iron_block.getIcon( direction, metadata );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockSkyCompass.class;
	}

	private void dropTorch(World w, int x, int y, int z)
	{
		w.func_147480_a( x, y, z, true );
		// w.destroyBlock( x, y, z, true );
		w.markBlockForUpdate( x, y, z );
	}

	@Override
	public boolean canPlaceBlockAt(World w, int x, int y, int z)
	{
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			if ( canPlaceAt( w, x, y, z, dir ) )
				return true;
		return false;
	}

	private boolean canPlaceAt(World w, int x, int y, int z, ForgeDirection dir)
	{
		return w.isSideSolid( x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.getOpposite(), false );
	}

	@Override
	public boolean isValidOrientation(World w, int x, int y, int z, ForgeDirection forward, ForgeDirection up)
	{
		TileSkyCompass sc = getTileEntity( w, x, y, z );
		if ( sc != null )
			return false;
		return canPlaceAt( w, x, y, z, forward.getOpposite() );
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block id)
	{
		TileSkyCompass sc = getTileEntity( w, x, y, z );
		ForgeDirection up = sc.getForward();
		if ( !canPlaceAt( w, x, y, z, up.getOpposite() ) )
			dropTorch( w, x, y, z );
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxsFromPool(World w, int x, int y, int z, Entity e, boolean isVisual)
	{
		TileSkyCompass tile = getTileEntity( w, x, y, z );
		if ( tile != null )
		{
			ForgeDirection forward = tile.getForward();

			double minX = 0;
			double minY = 0;
			double minZ = 0;
			double maxX = 1;
			double maxY = 1;
			double maxZ = 1;

			switch (forward)
			{
			case DOWN:
				minZ = minX = 5.0 / 16.0;
				maxZ = maxX = 11.0 / 16.0;
				maxY = 1.0;
				minY = 14.0 / 16.0;
				break;
			case EAST:
				minZ = minY = 5.0 / 16.0;
				maxZ = maxY = 11.0 / 16.0;
				maxX = 2.0 / 16.0;
				minX = 0.0;
				break;
			case NORTH:
				minY = minX = 5.0 / 16.0;
				maxY = maxX = 11.0 / 16.0;
				maxZ = 1.0;
				minZ = 14.0 / 16.0;
				break;
			case SOUTH:
				minY = minX = 5.0 / 16.0;
				maxY = maxX = 11.0 / 16.0;
				maxZ = 2.0 / 16.0;
				minZ = 0.0;
				break;
			case UP:
				minZ = minX = 5.0 / 16.0;
				maxZ = maxX = 11.0 / 16.0;
				maxY = 2.0 / 16.0;
				minY = 0.0;
				break;
			case WEST:
				minZ = minY = 5.0 / 16.0;
				maxZ = maxY = 11.0 / 16.0;
				maxX = 1.0;
				minX = 14.0 / 16.0;
				break;
			default:
				break;
			}

			return Arrays.asList( new AxisAlignedBB[] { AxisAlignedBB.getBoundingBox( minX, minY, minZ, maxX, maxY, maxZ ) } );
		}
		return Arrays.asList( new AxisAlignedBB[] { AxisAlignedBB.getBoundingBox( 0.0, 0, 0.0, 1.0, 1.0, 1.0 ) } );
	}

	@Override
	public void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e)
	{

	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegistry)
	{
		// :P
	}
}
