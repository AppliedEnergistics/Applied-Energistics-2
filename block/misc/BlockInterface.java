package appeng.block.misc;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.tile.misc.TileInterface;

public class BlockInterface extends AEBaseBlock
{

	public BlockInterface() {
		super( BlockInterface.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setTileEntiy( TileInterface.class );
	}

}
