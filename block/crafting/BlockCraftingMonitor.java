package appeng.block.crafting;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.tile.crafting.TileCraftingTile;

public class BlockCraftingMonitor extends AEBaseBlock
{

	public BlockCraftingMonitor() {
		super( BlockCraftingMonitor.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.Crafting ) );
		setTileEntiy( TileCraftingTile.class );
	}

}
