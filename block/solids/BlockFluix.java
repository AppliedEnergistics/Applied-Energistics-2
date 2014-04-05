package appeng.block.solids;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEDecorativeBlock;
import appeng.core.features.AEFeature;

public class BlockFluix extends AEDecorativeBlock
{

	public BlockFluix() {
		super( BlockFluix.class, Material.rock );
		setfeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks ) );
	}

}
