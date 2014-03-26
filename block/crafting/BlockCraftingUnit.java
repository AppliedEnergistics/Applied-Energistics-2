package appeng.block.crafting;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;

public class BlockCraftingUnit extends AEBaseBlock
{

	public BlockCraftingUnit() {
		super( BlockCraftingUnit.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.Crafting ) );
	}

}
