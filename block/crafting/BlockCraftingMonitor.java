package appeng.block.crafting;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;

public class BlockCraftingMonitor extends AEBaseBlock
{

	public BlockCraftingMonitor() {
		super( BlockCraftingMonitor.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.Crafting ) );
	}

}
