package appeng.block.networking;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockWireless;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.helpers.AEGlassMaterial;
import appeng.helpers.ICustomCollision;
import appeng.tile.networking.TileWireless;
import appeng.util.Platform;

public class BlockWireless extends AEBaseBlock implements ICustomCollision
{

	public BlockWireless() {
		super( BlockWireless.class, AEGlassMaterial.instance );
		setFeature( EnumSet.of( AEFeature.Core, AEFeature.WirelessAccessTerminal ) );
		setTileEntity( TileWireless.class );
		setLightOpacity( 0 );
		isFullSize = false;
		isOpaque = false;
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockWireless.class;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(World w, int x, int y, int z, Entity e, boolean isVisual)
	{
		TileWireless tile = getTileEntity( w, x, y, z );
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
				minZ = minX = 3.0 / 16.0;
				maxZ = maxX = 13.0 / 16.0;
				maxY = 1.0;
				minY = 5.0 / 16.0;
				break;
			case EAST:
				minZ = minY = 3.0 / 16.0;
				maxZ = maxY = 13.0 / 16.0;
				maxX = 11.0 / 16.0;
				minX = 0.0;
				break;
			case NORTH:
				minY = minX = 3.0 / 16.0;
				maxY = maxX = 13.0 / 16.0;
				maxZ = 1.0;
				minZ = 5.0 / 16.0;
				break;
			case SOUTH:
				minY = minX = 3.0 / 16.0;
				maxY = maxX = 13.0 / 16.0;
				maxZ = 11.0 / 16.0;
				minZ = 0.0;
				break;
			case UP:
				minZ = minX = 3.0 / 16.0;
				maxZ = maxX = 13.0 / 16.0;
				maxY = 11.0 / 16.0;
				minY = 0.0;
				break;
			case WEST:
				minZ = minY = 3.0 / 16.0;
				maxZ = maxY = 13.0 / 16.0;
				maxX = 1.0;
				minX = 5.0 / 16.0;
				break;
			default:
				break;
			}

			return Arrays.asList( AxisAlignedBB.getBoundingBox( minX, minY, minZ, maxX, maxY, maxZ ) );
		}
		return Arrays.asList( AxisAlignedBB.getBoundingBox( 0.0, 0, 0.0, 1.0, 1.0, 1.0 ) );
	}

	@Override
	public void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e)
	{
		TileWireless tile = getTileEntity( w, x, y, z );
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
				minZ = minX = 3.0 / 16.0;
				maxZ = maxX = 13.0 / 16.0;
				maxY = 1.0;
				minY = 5.0 / 16.0;
				break;
			case EAST:
				minZ = minY = 3.0 / 16.0;
				maxZ = maxY = 13.0 / 16.0;
				maxX = 11.0 / 16.0;
				minX = 0.0;
				break;
			case NORTH:
				minY = minX = 3.0 / 16.0;
				maxY = maxX = 13.0 / 16.0;
				maxZ = 1.0;
				minZ = 5.0 / 16.0;
				break;
			case SOUTH:
				minY = minX = 3.0 / 16.0;
				maxY = maxX = 13.0 / 16.0;
				maxZ = 11.0 / 16.0;
				minZ = 0.0;
				break;
			case UP:
				minZ = minX = 3.0 / 16.0;
				maxZ = maxX = 13.0 / 16.0;
				maxY = 11.0 / 16.0;
				minY = 0.0;
				break;
			case WEST:
				minZ = minY = 3.0 / 16.0;
				maxZ = maxY = 13.0 / 16.0;
				maxX = 1.0;
				minX = 5.0 / 16.0;
				break;
			default:
				break;
			}

			out.add( AxisAlignedBB.getBoundingBox( minX, minY, minZ, maxX, maxY, maxZ ) );
		}
		else
			out.add( AxisAlignedBB.getBoundingBox( 0.0, 0.0, 0.0, 1.0, 1.0, 1.0 ) );
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if ( p.isSneaking() )
			return false;

		TileWireless tg = getTileEntity( w, x, y, z );
		if ( tg != null )
		{
			if ( Platform.isServer() )
				Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_WIRELESS );
			return true;
		}
		return false;
	}

}
