package appeng.block.mac;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.tile.mac.TilePatternProvider;

public class BlockPatternProvider extends AEBaseBlock
{

	public BlockPatternProvider() {
		super( BlockPatternProvider.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.MolecularAssembler ) );
		setTileEntiy( TilePatternProvider.class );
	}

}
