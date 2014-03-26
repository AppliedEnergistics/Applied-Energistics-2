package appeng.block.crafting;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;

public class BlockMolecularAssembler extends AEBaseBlock
{

	public BlockMolecularAssembler() {
		super( BlockMolecularAssembler.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.Crafting ) );
	}

}
