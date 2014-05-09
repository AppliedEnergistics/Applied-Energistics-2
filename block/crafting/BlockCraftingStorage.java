package appeng.block.crafting;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.tile.crafting.TileCraftingTile;

public class BlockCraftingStorage extends AEBaseBlock
{

	public BlockCraftingStorage() {
		super( BlockCraftingStorage.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.Crafting ) );
		setTileEntiy( TileCraftingTile.class );
	}

}
