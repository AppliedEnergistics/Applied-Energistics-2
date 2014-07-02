package appeng.block.spatial;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderNull;
import appeng.core.features.AEFeature;
import appeng.helpers.ICustomCollision;

public class BlockMatrixFrame extends AEBaseBlock implements ICustomCollision
{

	public BlockMatrixFrame() {
		super( BlockMatrixFrame.class, Material.anvil);
		setfeature( EnumSet.of( AEFeature.SpatialIO ) );
		setResistance( 6000000.0F );
		setBlockUnbreakable();
		setLightOpacity( 0 );
		isOpaque = false;
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderNull.class;
	}

	@Override
	public void getSubBlocks(Item id, CreativeTabs tab, List list)
	{

	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegistry)
	{

	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity)
	{
		return false;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxsFromPool(World w, int x, int y, int z, Entity e, boolean isVisual)
	{
		return Arrays.asList( new AxisAlignedBB[] {} );// AxisAlignedBB.getBoundingBox( 0.25, 0, 0.25, 0.75, 0.5, 0.75 )
														// } );
	}

	@Override
	public void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e)
	{
		out.add( AxisAlignedBB.getBoundingBox( 0.0, 0.0, 0.0, 1.0, 1.0, 1.0 ) );
	}

}
