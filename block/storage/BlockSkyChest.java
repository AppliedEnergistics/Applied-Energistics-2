package appeng.block.storage;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockSkyChest;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.helpers.ICustomCollision;
import appeng.tile.storage.TileSkyChest;
import appeng.util.Platform;

public class BlockSkyChest extends AEBaseBlock implements ICustomCollision
{

	public BlockSkyChest() {
		super( BlockSkyChest.class, Material.rock );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setTileEntiy( TileSkyChest.class );
		isOpaque = isFullSize = false;
		lightOpacity = 0;
		setHardness( 50 );
		blockResistance = 150.0f;
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if ( Platform.isServer() )
			Platform.openGUI( player, getTileEntity( w, x, y, z ), ForgeDirection.getOrientation( side ), GuiBridge.GUI_SKYCHEST );

		return true;
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockSkyChest.class;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxsFromPool(World w, int x, int y, int z, Entity e, boolean isVisual)
	{
		return Arrays.asList( new AxisAlignedBB[] { AxisAlignedBB.getBoundingBox( 0.05, 0.05, 0.05, 0.95, 0.95, 0.95 ) } );
	}

	@Override
	public void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e)
	{
		out.add( AxisAlignedBB.getAABBPool().getAABB( 0.05, 0.05, 0.05, 0.95, 0.95, 0.95 ) );
	}

	@Override
	public IIcon getIcon(int direction, int metadata)
	{
		return AEApi.instance().blocks().blockSkyRock.block().getIcon( direction, metadata );
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegistry)
	{
	}
}
