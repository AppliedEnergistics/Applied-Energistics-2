package appeng.block.solids;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderNull;
import appeng.core.features.AEFeature;

public class BlockMatrixFrame extends AEBaseBlock
{

	public BlockMatrixFrame() {
		super( BlockMatrixFrame.class, Material.portal );
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

}
