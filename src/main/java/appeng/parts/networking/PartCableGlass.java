package appeng.parts.networking;

import net.minecraft.item.ItemStack;

public class PartCableGlass extends PartCable
{

	public PartCableGlass(Class c, ItemStack is) {
		super( c, is );
	}

	public PartCableGlass(ItemStack is) {
		this( PartCableGlass.class, is );
	}

}
