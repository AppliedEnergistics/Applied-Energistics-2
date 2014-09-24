package appeng.block.solids;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEDecorativeBlock;
import appeng.core.features.AEFeature;

public class BlockQuartzChiseled extends AEDecorativeBlock
{

	public BlockQuartzChiseled() {
		super( BlockQuartzChiseled.class, Material.rock );
		setFeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks ) );
	}

}
