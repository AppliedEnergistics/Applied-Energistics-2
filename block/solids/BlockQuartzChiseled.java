package appeng.block.solids;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;

public class BlockQuartzChiseled extends AEBaseBlock
{

	public BlockQuartzChiseled() {
		super( BlockQuartzChiseled.class, Material.rock );
		setfeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks ) );
	}

}
