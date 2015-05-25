package appeng.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemSlab;

public class AEBaseItemBlockSlab extends ItemSlab {

	public AEBaseItemBlockSlab(Block block, AEBaseSlabBlock singleSlab, AEBaseSlabBlock doubleSlab, Boolean isDoubleSlab)
	{
		super( block, singleSlab, doubleSlab, isDoubleSlab );
	}
}
