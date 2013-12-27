package appeng.block.misc;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.tile.misc.TilePartitionerEditor;

public class BlockPartitionEditor extends AEBaseBlock
{

	public BlockPartitionEditor() {
		super( BlockPartitionEditor.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.StorageCells ) );
		setTileEntiy( TilePartitionerEditor.class );
	}

}
