package appeng.block.solids;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEDecorativeBlock;
import appeng.core.features.AEFeature;

public class BlockQuartz extends AEDecorativeBlock
{

	public BlockQuartz() {
		super( BlockQuartz.class, Material.rock );
		setfeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks ) );
	}

}
