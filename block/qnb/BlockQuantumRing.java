package appeng.block.qnb;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderQNB;
import appeng.core.features.AEFeature;
import appeng.tile.qnb.TileQuantumBridge;

public class BlockQuantumRing extends AEBaseBlock
{

	public BlockQuantumRing() {
		super( BlockQuantumRing.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.QuantumNetworkBridge ) );
		setTileEntiy( TileQuantumBridge.class );
		float shave = 2.0f / 16.0f;
		setBlockBounds( shave, shave, shave, 1.0f - shave, 1.0f - shave, 1.0f - shave );
		setLightOpacity( 1 );
		isFullSize = isOpaque = false;
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderQNB.class;
	}

}
