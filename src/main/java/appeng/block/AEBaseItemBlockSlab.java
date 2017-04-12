package appeng.block;


import net.minecraft.block.Block;
import net.minecraft.item.ItemSlab;


public class AEBaseItemBlockSlab extends ItemSlab
{

	public AEBaseItemBlockSlab( final Block block, final AEBaseSlabBlock singleSlab, final AEBaseSlabBlock doubleSlab, final Boolean isDoubleSlab )
	{
		super( block, singleSlab, doubleSlab, isDoubleSlab );
	}
}
