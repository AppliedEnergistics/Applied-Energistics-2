package appeng.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemSlab;

public class AEBaseItemBlockSlab extends ItemSlab {

	public AEBaseItemBlockSlab(Block b, AEBaseSlabBlock sb, AEBaseSlabBlock db, Boolean id) {
		super( b, sb, db, id );
		System.out.println(b);
		System.out.println(sb);
		System.out.println(db);
		System.out.println(id);
	}
}
